package com.example.ol.popinfo;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.ol.popinfo.Images.ImagesHelper;
import com.example.ol.popinfo.Singers.SingerHelper;
import com.example.ol.popinfo.http.YandexClient;

/**
 * Created by ol on 27.06.16
 */
public class MainFragment extends Fragment
  implements Interfaces.OnSingerItemClickListener,
    ActionMode.Callback {

  private MainActivity mActivity;
  private Context mContext = null;

  private static Interfaces.SingerDetailViewProcessor mSingerDetailViewProcessor = null;
  private static Interfaces.SingersRequestInfoProcessor mSingersRequestInfoProcessor = null;

  private SingerHelper mSingerHelper = null; /// holder for singer's info
  private ListLogic mSingerListLogic; /// engine for singer list processing
  private ImagesHelper mImagesHelper; /// holder for images (cache + async decoder)

  /// Yandex client instance & pure interface reference on it
  private YandexClient mHttpClient = null;

  private CoordinatorLayout mCoordinatorLayout;
  private RecyclerView mRecyclerView;
  private RecyclerAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private ActionMode mCABMode;

  private int mClickedPosition = -1; /// click item storage

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    mActivity = (MainActivity) activity;

    if (null == mContext)
      /// will always use application's rather than activity's one
      mContext = mActivity.getApplicationContext();

    //ToDo ! Check AFTER cfg changing (the parent activity h.b. recreated for EXISTED fragment) !
    mSingerDetailViewProcessor = mActivity;

    if (null != mHttpClient)
      /// after cfg changes, here we reattach existed MainFragment instance to new MainActivity one
      mHttpClient.setFragmentManager(mActivity.getSupportFragmentManager());
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);

    mSingerHelper = SingerHelper.getInstance(mContext);

    mImagesHelper = ImagesHelper.getInstance();

    mAdapter = new RecyclerAdapter(mContext, mImagesHelper, mSingerHelper.getCommonList(), this);

    /// init core logic
    mSingerListLogic = new ListLogic(mContext, mSingerHelper, mAdapter);

    /// init & tune Yandex HTTP instance - IF NOT EXISTS ALREADY
    if (null == mHttpClient) {
      mHttpClient = new YandexClient(mSingerListLogic, mActivity.getSupportFragmentManager());
      mSingersRequestInfoProcessor = mHttpClient;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_main, container, false);

    /// init & tune recycler
    mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setAdapter(mAdapter);

    mLayoutManager = new LinearLayoutManager(mContext);
    mRecyclerView.setLayoutManager(mLayoutManager);

    return v;
  }

  @Override
  public void onResume() {
    super.onResume();
    View view = getView();
    if ((null == view) ||
        (null == mImagesHelper))
      return;

    if (0 == mSingerHelper.getCommonList().size())
      /// load singers automatically only 1'st time
      mSingersRequestInfoProcessor.singersRequestInfo(mActivity);

    if (mClickedPosition >= 0)
      /// we are possible back from singer details view => refresh this singer item
      mAdapter.notifyItemChanged(mClickedPosition);
  }

  @Override
  public void onPause() {
    super.onPause();
    mSingerHelper.saveFavoriteSingers(); ///store favorite singers locally
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_main, menu);

    /// search configuration
    SearchManager searchManager = (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);
//    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
    searchView.setSearchableInfo(searchManager.getSearchableInfo(mActivity.getComponentName()));
    searchView.setIconifiedByDefault(true); /// collapsed by default

    /// hide & show all menu according to search collapsing state
    final MenuItem itemSearch = menu.findItem(R.id.action_search);
    MenuItemCompat.setOnActionExpandListener(itemSearch, new MenuItemCompat.OnActionExpandListener() {
      @Override
      public boolean onMenuItemActionExpand(final MenuItem item) {
        setOptionsItemsVisibility(menu, itemSearch, false); /// hide another items
        return true;
      }
      @Override
      public boolean onMenuItemActionCollapse(final MenuItem item) {
        setOptionsItemsVisibility(menu, itemSearch, true); /// show all menu items back
        clearFoundSingers();
        return true;
      }
    });

    /// update corresponding 'sorting' item state
    if (null != mSingerHelper) {
      MenuItem itemNone = menu.findItem(R.id.action_sort_by_none);
      MenuItem itemByName = menu.findItem(R.id.action_sort_by_name);
      MenuItem itemByGenres = menu.findItem(R.id.action_sort_by_genres);
      switch (mSingerHelper.getSortingState()) {
        case BY_NAME:
          if (null != itemByName)
            itemByName.setChecked(true);
          break;
        case BY_GENRES:
          if (null != itemByGenres)
            itemByGenres.setChecked(true);
          break;
        case NOT:
        default:
          if (null != itemNone)
            itemNone.setChecked(true);
          break;
      }
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  private void setOptionsItemsVisibility(final Menu menu, final MenuItem excepted,
                                         final boolean visible) {
    for (int i = 0; i < menu.size(); ++i) {
      MenuItem item = menu.getItem(i);
      if (item != excepted)
        item.setVisible(visible);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.action_reload: //reload singers info from the server
        if (null != mSingersRequestInfoProcessor)
          mSingersRequestInfoProcessor.singersRequestInfo(mActivity);
        return true;

      case R.id.action_sort_by_none:
      case R.id.action_sort_by_name:
      case R.id.action_sort_by_genres:
        if (item.isChecked())
          ; /// nothing to do
        else
          mSingerListLogic.sortSingers(item);
        return true;

      case R.id.action_search:
      case R.id.action_about:
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  /**
   * sets outer layout view for the fragment
   */
  public void setCoordinatorLayout(CoordinatorLayout coordinatorLayout) {
    mCoordinatorLayout = coordinatorLayout;
  }

  /**
   * creates new list from search results and shows them up
   * @param searchQuery - name (mask) of artists to search from main list
   */
  public void showFoundSingers(String searchQuery) {
    mAdapter.resetList(mSingerHelper.makeSearchByName(searchQuery));
  }

  /**
   * restores original BIG singerList
   */
  private void clearFoundSingers() {
    mSingerHelper.clearSearch();
    mAdapter.resetList(mSingerHelper.getCommonList());
  }


  @Override
  public void onClick(int position, View view) {
    mClickedPosition = position;
    /// pass click singer event through up for detail show
    mSingerDetailViewProcessor.singerDetailView(mAdapter.getItem(position));
  }

  @Override
  public void onLongClick(int position, View view) {
    if (null == mSingerHelper)
      return;

    /// toggle item's selection
    mAdapter.toggleSelection(position);

    if (null == mCABMode) { /// Start the CAB through the ActionMode.Callback
      mCABMode = getActivity().startActionMode(MainFragment.this);
    }
    /// eval & show selection counter as a CAB title
    int selectedNum = mAdapter.getSelectedItemCount();
    if (0 == selectedNum)
      mCABMode.finish(); /// just nothing to do..
    else {
      String title = getString(R.string.count_cab_selected, mAdapter.getSelectedItemCount());
      mCABMode.setTitle(title);
      }
  }

  @Override
  public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
    actionMode.getMenuInflater().inflate(R.menu.menu_cab_singers, menu);
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode actionMode) {
    mCABMode = null;
    mAdapter.clearSelections();
  }

  @Override
  public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
    switch (menuItem.getItemId()) {
      case R.id.menu_delete:
        mSingerListLogic.deleteSelectedSingers(mCABMode, mCoordinatorLayout);
        return true;
      default:
        return false;
    }
  }
}
