package com.example.ol.popinfo;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ol.popinfo.Singers.Singer;

public class MainActivity extends AppCompatActivity implements
    Interfaces.OnSingerDetailViewProcessor,
    Interfaces.OnSingerRatingChangeListener {

  /// for logging
  private static final String LOG_TAG = MainActivity.class.getName();

  private CoordinatorLayout mCoordinatorLayout = null;
  private Toolbar mToolbar = null;

  private MainFragment mMainFragment = null;
  private DetailsFragment mDetailsFragment = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /// will be called at start and for every orientation changes
    ScreenConfiguration.setScreenConfigurationState(getResources().getConfiguration());

    mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    if (null != mToolbar) {
      setSupportActionBar(mToolbar);
      /// in dual-panel mode we have ordinary (=small) toolbar => set its background & title
      mToolbar.setBackgroundResource(R.drawable.bar_background);
      mToolbar.setTitle(R.string.app_name);
    }

    startFragments();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   * prepares, creates and start fragment panels for all configuration
   */
  private void startFragments() {
    Fragment existedFragment = getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (null == existedFragment) {
      /// no one is there - it's 1'st start => start MainFragment and exit
      mMainFragment = new MainFragment();
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.fragment_container, mMainFragment)
          .commit();
      return;
    }

    /// there IS some fragment(s) in transactions - it's cfg changing
    if (ScreenConfiguration.getScreenConfigurationState() ==
        Constants.ScreenConfigurationState.TABLET_LANDSCAPE) {
      /// cfg change (single portrait) > (dual-panel) mode => remove DetailsFragment if existed
      try {
        mDetailsFragment = (DetailsFragment) existedFragment;
      }
      catch (ClassCastException ex) {
        mDetailsFragment = null;
      }
      if (null != mDetailsFragment) {
        /// previous view WAS portrait with Details - rollback it to MainFragment..
//        getFragmentManager().executePendingTransactions();
        getSupportFragmentManager()
            .beginTransaction()
            .remove(mDetailsFragment)
            .commit();
      }
      /// here is MainFragment on the top - it's ok
      return; /// details for second panel will be shown later by onDetailView() call
    }

    /// cfg change (dual-panel) > (single-portrait) mode => everything is ok
    /// we already have MainFragment, just use it with new view
    /// (leave DetailsFragment for double-panel as is for further use)
  }

  /**
   * checks for search query intent and processes it
   * @param intent
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String searchQuery = intent.getStringExtra(SearchManager.QUERY);
      mMainFragment.showFoundSingers(searchQuery);
    }
  }

  @Override
  public void onDetailView(int position) {
    if (ScreenConfiguration.getScreenConfigurationState() ==
        Constants.ScreenConfigurationState.TABLET_LANDSCAPE) {
      /// dual-panel mode => find existed Details fragment or create new if it's absent
      mDetailsFragment = (DetailsFragment) getSupportFragmentManager()
          .findFragmentById(R.id.fragment_detail_container);
      if (null == mDetailsFragment) {
        mDetailsFragment = new DetailsFragment();
        mDetailsFragment.setPosition(position);
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.fragment_detail_container, mDetailsFragment)
            .commit();
      }
      else {
        /// use existed Details fragment - just redraw it with NEW details info
        if (mDetailsFragment.getPosition() != position) {
          mDetailsFragment.setPosition(position);
          mDetailsFragment.showDetails();
        }
      }
      return;
    } // if TABLET_LANDSCAPE

    /// single-panel mode => replace existed Main fragment with Details one
    mDetailsFragment = new DetailsFragment();
    mDetailsFragment.setPosition(position);
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.fragment_container, mDetailsFragment)
        .addToBackStack(null) /// back to Main fragment
        .commit();
  }

  @Override
  public void onRatingChange(Singer singer, int newRating) {
    if (null != mMainFragment)
      mMainFragment.changeRating(singer, newRating);
  }

  public CoordinatorLayout getCoordinatorLayout() {
    return mCoordinatorLayout;
  }

  public Toolbar getToolbar() {
    return mToolbar;
  }
}
