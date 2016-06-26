package com.example.ol.popinfo;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.ol.popinfo.Images.ImagesHelper;
import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;
import com.example.ol.popinfo.http.YandexClient;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements Interfaces.OnSingerItemClickListener,
    Interfaces.OnSingerDetailEventListener,
    ActionMode.Callback {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private ListLogic mSingerListLogic; /// engine for singer list processing
  private SingerHelper mSingerHelper = null; /// holder for singer's info
  private ImagesHelper mImagesHelper = ImagesHelper.getInstance(); /// holder for images

  /// Yandex client instance & pure interface reference on it
  private YandexClient mHttpClient = null;
  private Interfaces.SingersRequestInfoProcessor mSingersRequestInfoProcessor = null;

  private CoordinatorLayout mCoordinatorLayout;
  private Toolbar mToolbar;
  private CollapsingToolbarLayout mCollapsingToolbar;
  private RecyclerView mRecyclerView;
  private RecyclerAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private ActionMode mCABMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);
    mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    mCollapsingToolbar.setTitle(getResources().getString(R.string.main_activity_title));
    ImageView im = (ImageView) findViewById(R.id.toolbarImage);
    Picasso.with(this).load(R.drawable.bar_background).fit().into(im);

    mSingerHelper = new SingerHelper(getApplicationContext());

    /// init & tune recycler
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mRecyclerView.setHasFixedSize(true);

    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new RecyclerAdapter(this, mImagesHelper, mSingerHelper.getCommonList(), this);
    mRecyclerView.setAdapter(mAdapter);

    /// init core logic
    mSingerListLogic = new ListLogic(getApplicationContext(), mSingerHelper, mAdapter);

    /// init & tune Yandex HTTP instance - IF NOT EXISTS ALREADY
    if (null == mHttpClient) {
      mHttpClient = new YandexClient(this, mSingerListLogic, getSupportFragmentManager());
    }
    mSingersRequestInfoProcessor = mHttpClient;
  }

  /**
   * checks for search query intent and processes it
   * @param intent
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String searchQuery = intent.getStringExtra(SearchManager.QUERY);
      showFoundSingers(searchQuery);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    mSingersRequestInfoProcessor.singersRequestInfo();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSingerHelper.saveFavoriteSingers(); ///store favorite singers locally
/*
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        mSingerHelper.saveFavoriteSingers(); ///store favorite singers locally
      }
    });
    thread.start();
*/

  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);

    /// search configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//    SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
    return true;
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
          mSingersRequestInfoProcessor.singersRequestInfo();
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
   * creates new list from search results and shows them up
   * @param searchQuery - name (mask) of artists to search from main list
   */
  private void showFoundSingers(String searchQuery) {
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
  public void onRatingBarChange(Singer singer, int value) {
    mSingerHelper.changeSingerRating(singer, value);
  }

  @Override
  public void onClick(int position, View view) {
    if (null != mSingerHelper) {
//ToDo remake to workingList item!
//      Singer singer = mSingerHelper.getSinger(position);
      Log.d(LOG_TAG, "onClick(pos:" + position + ")");
    }
  }

  @Override
  public boolean onLongClick(int position, View view) {
    if (null != mSingerHelper) {
      /// toggle item's selection
      mAdapter.toggleSelection(position);

      if (null == mCABMode) { /// Start the CAB through the ActionMode.Callback
        mCABMode = startActionMode(MainActivity.this);
      }
      /// eval & show selection counter as a CAB title
      int selectedNum = mAdapter.getSelectedItemCount();
      if (0 == selectedNum)
        mCABMode.finish(); /// just nothing to do..
      else {
        String title = getString(R.string.count_cab_selected, mAdapter.getSelectedItemCount());
        mCABMode.setTitle(title);
      }
      return true;
    }
    return false;
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
  public void onDestroyActionMode(ActionMode actionMode) {
    mCABMode = null;
    mAdapter.clearSelections();
  }

}
