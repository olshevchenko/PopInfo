package com.example.ol.popinfo;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.example.ol.popinfo.Images.ImagesHelper;
import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;
import com.example.ol.popinfo.http.YandexClient;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ol on 27.06.16
 */
public class MainFragment extends Fragment implements
    Interfaces.OnSingerItemClickListener,
    Interfaces.OnSingerListUpdateProcessor,
    ActionMode.Callback {

  /// for logging
  private static final String LOG_TAG = MainFragment.class.getName();

  private MainActivity mActivity;
  private Context mContext = null;

  private static Interfaces.OnSingerDetailViewProcessor mDetailViewProcessor = null;
  private static Interfaces.OnSingerListRequestProcessor mListRequestProcessor = null;

  private Handler mHandler = null;
  private ListLogic mSingerListLogic; /// engine for singer list processing
  private ImagesHelper mImagesHelper; /// holder for images (cache + async decoder)

  /// Yandex client instance & pure interface reference on it
  private YandexClient mHttpClient = null;

  private CoordinatorLayout mCoordinatorLayout = null;
  private CollapsingToolbarLayout mCollapsingToolbar = null;
  private Toolbar mToolbar = null;
  private ActionMode mCABMode;

  private RecyclerView mRecyclerView;
  private RecyclerAdapter mAdapter;
  private Picasso mPicasso;

  private MyAnimationListener mAnimationListener; /// singer list item animation for onClick()

  /// storage for singer list item onClick() parameters
  private int mClickedPosition = -1; /// position
  private boolean mNeedToPerformClick = false;


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    mActivity = (MainActivity) context;

    if (null == mContext) {
      mContext = context;
      mPicasso = Picasso.with(context);
    }

/// ToDo Check AFTER cfg changing (the parent activity h.b. recreated for EXISTED fragment)
    try {
      mDetailViewProcessor = (Interfaces.OnSingerDetailViewProcessor) mActivity;
    }
    catch (ClassCastException ex) {
      Log.e(LOG_TAG, "Parent activity " + mActivity.toString() +
          "MUST implement OnSingerDetailViewProcessor interface => finishing..");
      throw new ClassCastException(mActivity.toString() +
          "MUST implement OnSingerDetailViewProcessor");
    }

    mCoordinatorLayout = mActivity.getCoordinatorLayout();
    mToolbar = mActivity.getToolbar();

    if (null != mHttpClient)
      /// after cfg changes, here we reattach existed MainFragment instance to new MainActivity one
      mHttpClient.setFragmentManager(mActivity.getSupportFragmentManager());
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    setRetainInstance(true);

    mHandler = new Handler();

    SingerHelper.Favors.loadFavoriteSingers(mContext);

    mImagesHelper = ImagesHelper.getInstance();

    mAdapter = new RecyclerAdapter(mContext, mImagesHelper,
        SingerHelper.Lists.getsCommonList(), this);

    mAnimationListener = new MyAnimationListener();

    /// init core logic
    mSingerListLogic = new ListLogic(mContext, mAdapter);

    /// init & tune Yandex HTTP instance - IF NOT EXISTS ALREADY
    if (null == mHttpClient) {
      mHttpClient = new YandexClient(this, mActivity.getSupportFragmentManager());
      mListRequestProcessor = mHttpClient;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_main, container, false);
    mNeedToPerformClick = true; /// after cfg change - we need to show later any details (if double-panel mode)

    setMenuVisibility(true);
    if (null == mCoordinatorLayout)
      /// not found in activity => find in the fragment
    mCoordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);

    if (null == mToolbar) {
      /// not found in activity => find in the fragment
      mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
      if (null != mToolbar)
        mActivity.setSupportActionBar(mToolbar);
    }
//    mActivity.getSupportActionBar().invalidateOptionsMenu();

    ImageView im = (ImageView) v.findViewById(R.id.toolbarImage);
    if (null != im) {
      mPicasso.load(R.drawable.bar_background)
          .fit()
          .into(im);
    }
    else {
      if ( (mActivity.getToolbar() == null) && (null != mToolbar) )
        /// ordinary (=small) toolbar background IS REALLY defined here, NOT in activity
        mToolbar.setBackgroundResource(R.drawable.bar_background);
    }

    mCollapsingToolbar = (CollapsingToolbarLayout) v.findViewById(R.id.collapsingToolbar);
    if (null != mCollapsingToolbar) {
      mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
      mCollapsingToolbar.setTitle(getResources().getString(R.string.main_fragment_title));
    }

    /// init & tune recycler
    mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setAdapter(mAdapter);

    if (ScreenConfiguration.getScreenConfigurationState() !=
        Constants.ScreenConfigurationState.TABLET_LANDSCAPE) {
      mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }
    else {
      /// dual-panel mode => click 1'st singer list item
//      mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
      mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext) {
        @Override
        public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
          super.onLayoutChildren(recycler, state);
          int firstVisibleItemPosition = findFirstVisibleItemPosition();
          if (firstVisibleItemPosition < 0)
            return;
          if (! mNeedToPerformClick)
            return;

          /// it's time => try to execute (delayed) handly item click
          mHandler.postDelayed(
              new Runnable() {
                @Override
                public void run() {
                  if (mNeedToPerformClick) {
                    mAdapter.clickItem(mClickedPosition);
                  }
                }
              },500);
        }
      });
    }

    return v;
  }

  @Override
  public void onResume() {
    super.onResume();
    View view = getView();
    if ((null == view) ||
        (null == mImagesHelper))
      return;

    if (SingerHelper.Lists.getsCommonList().size() == 0) {
      /// load singers automatically only 1'st time
      mListRequestProcessor.listRequest(mActivity);
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mDetailViewProcessor = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    SingerHelper.Favors.saveFavoriteSingers(); ///store favorite singers locally
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
    MenuItem itemNone = menu.findItem(R.id.action_sort_by_none);
    MenuItem itemByName = menu.findItem(R.id.action_sort_by_name);
    MenuItem itemByGenres = menu.findItem(R.id.action_sort_by_genres);
    switch (SingerHelper.Sorting.getsSortingState()) {
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
        if (null != mListRequestProcessor)
          mListRequestProcessor.listRequest(mActivity);
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


  class MyAnimationListener implements Animation.AnimationListener {
    private int mPosition = -1;
    private View mSingerView = null;
    private boolean mIsNewClick = true; /// do we animate new click or rollback old one?

    @Override
    public void onAnimationStart(Animation animation) {
      mSingerView.setEnabled(false);
      if (mIsNewClick)
        /// pass click singer event through up for detail show
        mDetailViewProcessor.onDetailView(mPosition);
      else
        ; /// no logic here
    }

    @Override
    public void onAnimationEnd(Animation animation) {
      mSingerView.setEnabled(true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
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

  @Override
  public void onClick(int position) {

    mNeedToPerformClick = false; /// got USER (or emulation) click - no more need to make performance : ))

/// ToDo animate click on the card
    ;
/*
    ///animate forward now!
    mAnimationListener.setParams(mClickedPosition, mClickedView, true);
    Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.singer_item_trans);
    anim.setAnimationListener(mAnimationListener);
    view.startAnimation(anim);
*/
    mAdapter.toggleClick(mClickedPosition, position); /// process old item's UNclick, new one's click
    mClickedPosition = position; /// remember new position for further calls
    mDetailViewProcessor.onDetailView(position); /// pass click singer event through up for detail show
  }

  @Override
  public void onLongClick(int position) {
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
  public void listUpdate(List<Singer> newList) {
    mSingerListLogic.listUpdate(newList);
    mClickedPosition = 0; /// clear stored position for NEW data
    mNeedToPerformClick = true; /// after data refresh - we need to show any details (if double-panel mode)
  }

  /**
   * creates new list from search results and shows them up
   * @param searchQuery - name (mask) of artists to search from main list
   */
  public void showFoundSingers(String searchQuery) {
    SingerHelper.Searching.makeSearchByName(searchQuery);
    mAdapter.resetList(SingerHelper.Searching.getSearchedList());
  }

  /**
   * restores original BIG singerList
   */
  private void clearFoundSingers() {
    SingerHelper.Searching.clearSearch();
    mAdapter.resetList(SingerHelper.Lists.getsCommonList());
  }

  public void changeRating(Singer singer, int newRating) {
    SingerHelper.Favors.updateFavorite(singer, newRating);

    if (mClickedPosition >= 0) /// use position hold from onClick() processing
      mAdapter.notifyItemChanged(mClickedPosition);
  }

  public int getClickedPosition() {
    return mClickedPosition;
  }
}
