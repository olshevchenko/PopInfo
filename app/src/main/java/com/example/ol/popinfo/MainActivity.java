package com.example.ol.popinfo;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingersHelper;
import com.example.ol.popinfo.http.YandexClient;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements Logic.SingersUpdateProcessor,
    Logic.OnSingerItemClickListener,
    Logic.OnSingerDetailEventListener {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private SingersHelper mSingersHelper = null; /// holder for singer's info

  private ImagesHelper mImagesHelper = ImagesHelper.getInstance(); /// holder for images

  /// Yandex client instance & pure interface reference on it
  private YandexClient mHttpClient = null;
  private Logic.SingersRequestInfoProcessor mSingersRequestInfoProcessor = null;


  private Toolbar mToolbar;
  private CollapsingToolbarLayout mCollapsingToolbar;
  private ImageView im;
  private RecyclerView mRecyclerView;
  private RecyclerAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
            mSingersHelper.SortById();
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
            mSingersHelper.SortByName();
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
            mSingersHelper.SortByGenres();
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
    mSingersHelper.Sort(); /// resort the list just assembled accordingly
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
      Singer singer = mSingersHelper.getSinger(position);
      Log.d(LOG_TAG, "onLongClick(pos:" + position + ", Singer:" + singer + ")");
      mAdapter.toggleSelection(position);
      return true;
    }
    return false;
  }
}
