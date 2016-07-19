package com.example.ol.popinfo;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.ol.popinfo.Singers.Singer;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
    implements Interfaces.SingerDetailViewProcessor {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private CoordinatorLayout mCoordinatorLayout;
  private Toolbar mToolbar;
  private CollapsingToolbarLayout mCollapsingToolbar;

  private GlobalStorage mGlobalStorage;
  private MainFragment mListFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(mToolbar);
    mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    if (null != mCollapsingToolbar) {
      mCollapsingToolbar.setTitle(getResources().getString(R.string.main_activity_title));
      mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    }

    ImageView im = (ImageView) findViewById(R.id.toolbarImage);
    if (null != im) {
      Picasso.with(this)
        .load(R.drawable.bar_background)
        .fit()
        .into(im);
    } else {
      /// ordinary (=small) toolbar background
      mToolbar.setBackgroundResource(R.drawable.bar_background);
    }

    mGlobalStorage = (GlobalStorage) getApplicationContext();

    mListFragment = (MainFragment)
        getSupportFragmentManager().findFragmentById(R.id.fragment_main);
    if (null == mListFragment) {
      Log.e(LOG_TAG, "ERROR while getting MainFragment from layout - finish activity");
      finish();
    }

    mListFragment.setCoordinatorLayout(mCoordinatorLayout);

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
      mListFragment.showFoundSingers(searchQuery);
    }
  }

  @Override
  public void singerDetailView(Singer singerDetailed) {
    View fragmentContainer = findViewById(R.id.fragment_detail_container);
    if (fragmentContainer != null) {
      DetailsFragment detailsFragment = new DetailsFragment();
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      detailsFragment.setDetailedSinger(singerDetailed);
      ft.replace(R.id.fragment_detail_container, detailsFragment);
      ft.commit();
    } else {
      mGlobalStorage.setDetailedSinger(singerDetailed);
      Intent intent = new Intent(this, DetailsActivity.class);
      startActivity(intent);
    }
  }
}
