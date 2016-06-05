package com.example.ol.popinfo;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingersHelper;
import com.example.ol.popinfo.http.YandexClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements Logic.SingersUpdateProcessor,
    Logic.OnSingerItemClickListener,
    Logic.OnSingerDetailEventListener,
    ActionMode.Callback {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private SingersHelper mSingersHelper = null; /// holder for singer's info

  private ImagesHelper mImagesHelper = ImagesHelper.getInstance(); /// holder for images

  /// Yandex client instance & pure interface reference on it
  private YandexClient mHttpClient = null;
  private Logic.SingersRequestInfoProcessor mSingersRequestInfoProcessor = null;


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
    mCollapsingToolbar.setTitle("PopInfo");
    ImageView im = (ImageView) findViewById(R.id.toolbarImage);
    Picasso.with(this).load(R.drawable.bar_background).fit().into(im);

    /// init & tune Yandex HTTP instance - IF NOT EXISTS ALREADY
    if (null == mHttpClient) {
      mHttpClient = new YandexClient(this, getSupportFragmentManager());
    }
    mSingersRequestInfoProcessor = mHttpClient;

    mSingersHelper = new SingersHelper(getApplicationContext());

    /// init & tune recycler
    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mRecyclerView.setHasFixedSize(true);

    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new RecyclerAdapter(this, mImagesHelper, mSingersHelper.getSingerList(), this);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onStart() {
    super.onStart();
    mSingersRequestInfoProcessor.singersRequestInfo();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mSingersHelper.saveFavoriteSingers(); ///store favorite singers locally
/*
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        mSingersHelper.saveFavoriteSingers(); ///store favorite singers locally
      }
    });
    thread.start();
*/

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);

    ///update corresponding 'sorting' item state
    if (null != mSingersHelper) {
      MenuItem itemNone = menu.findItem(R.id.action_sort_by_none);
      MenuItem itemByName = menu.findItem(R.id.action_sort_by_name);
      MenuItem itemByGenres = menu.findItem(R.id.action_sort_by_genres);
      switch (mSingersHelper.getSortingState()) {
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case R.id.action_reload: //reload singers info from the server
        if (null != mSingersRequestInfoProcessor)
          mSingersRequestInfoProcessor.singersRequestInfo();
        return true;

      case R.id.action_sort_by_none:
        if (item.isChecked())
          ; /// nothing to do
        else {
          if (null != mSingersHelper) {
            item.setChecked(true);
            mSingersHelper.setSortingState(Constants.SortingState.NOT);
            mSingersHelper.sortById();
            mAdapter.notifyDataSetChanged();
          }
        }
        return true;

      case R.id.action_sort_by_name:
        if (item.isChecked())
          ; /// nothing to do
        else {
          if (null != mSingersHelper) {
            item.setChecked(true);
            mSingersHelper.setSortingState(Constants.SortingState.BY_NAME);
            mSingersHelper.sortByName();
            mAdapter.notifyDataSetChanged();
          }
        }
        return true;

      case R.id.action_sort_by_genres:
        if (item.isChecked())
          ; /// nothing to do
        else {
          if (null != mSingersHelper) {
            item.setChecked(true);
            mSingersHelper.setSortingState(Constants.SortingState.BY_GENRES);
            mSingersHelper.sortByGenres();
            mAdapter.notifyDataSetChanged();
          }
        }
        return true;

      case R.id.action_search:
      case R.id.action_about:
        return true;

      default:
        return super.onOptionsItemSelected(item);

    }
  }

  @Override
  public void singersUpdate(List<Singer> newList) {
    mSingersHelper.setCommonList(newList); /// store the records newly loaded from the server side
    mSingersHelper.mergeLists(); /// merge with local favorite ones
    mSingersHelper.sort(); /// resort the list just assembled accordingly
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onRatingBarChange(Singer singer, int value) {
    mSingersHelper.changeRating(singer, value);
  }

  @Override
  public void onClick(int position, View view) {
    if (null != mSingersHelper) {
      Singer singer = mSingersHelper.getSinger(position);
      Log.d(LOG_TAG, "onClick(pos:" + position + ", Singer:" + singer + ")");
    }
  }

  @Override
  public boolean onLongClick(int position, View view) {
    if (null != mSingersHelper) {
      /// toggle item's selection
      Singer singer = mSingersHelper.getSinger(position);
      Log.d(LOG_TAG, "onLongClick(pos:" + position + ", Singer:" + singer + ")");
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
        deleteSelectedSingers();
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

  /**
   * deletes selected part of singers list (both in singers data storage and adapter
   * allows to undo deletion by using snackbar w.action
   */
  private void deleteSelectedSingers() {
    final List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
    if (null == selectedItemPositions)
      return;
    final int selectedNum = selectedItemPositions.size();
    if (0 == selectedNum)
      return;

    /// reversely sort positions to prevent chaos during undeletion
    Collections.sort(selectedItemPositions);
    Collections.reverse(selectedItemPositions);

    /// tmp. stored items for delayed (UNDO) removing
    final List<Singer> singers2Remove = new ArrayList<>(selectedNum);

    /// execute predeletion
    int currPos;
    for (int i = 0; i < selectedNum; i++) { /// shift remainder up
      currPos = selectedItemPositions.get(i);
      singers2Remove.add(mSingersHelper.getSinger(currPos)); /// store the UNDO item (reversely)
      mSingersHelper.removeSinger(currPos);
      mAdapter.removeItem(currPos); /// update view
    }

    /// show UNDO case
    Snackbar snackbar = Snackbar
        .make(mCoordinatorLayout, getString(R.string.sb_count_deleted, selectedNum), Snackbar.LENGTH_LONG)
        .setCallback(new Snackbar.Callback() {
          @Override
          public void onDismissed(Snackbar snackbar, int event) {
            if (DISMISS_EVENT_ACTION == event)
              ;
            else {
              singers2Remove.clear();
              mCABMode.finish();
            }
          }
        })
        .setAction(getString(R.string.sb_undo), new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            int currPos;
            for (int i = selectedNum-1; i >= 0; i--) { /// restore items from the list head
              currPos = selectedItemPositions.get(i);
              mSingersHelper.addSinger(singers2Remove.get(i), currPos); /// add back to the old pos
              mAdapter.addItem(currPos); /// update view
            }
            singers2Remove.clear();
          }
        });

    // Changing message text color
    snackbar.setActionTextColor(Color.RED);
    snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    snackbar.setDuration(Snackbar.LENGTH_SHORT);
    snackbar.show();
  }
}
