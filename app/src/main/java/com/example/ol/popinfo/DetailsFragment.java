package com.example.ol.popinfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by ol on 27.06.16
 */
public class DetailsFragment extends Fragment {
  /// for logging
  private static final String LOG_TAG = DetailsFragment.class.getName();

  private MainActivity mActivity = null;
  private Interfaces.OnSingerRatingChangeListener mRatingChangeListener = null;
  private Context mContext = null;
  private View mRootView = null;
  private int mSingerIdx = -1; /// (adapter) index of the element in the holder

  private static String sAlbumsStr = null;
  private static String sTracksStr = null;

  private Target mTarget; /// preventing early termination by GC

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    mActivity = (MainActivity) context;

    if (null == mContext)
      mContext = context;

    try {
      mRatingChangeListener = (Interfaces.OnSingerRatingChangeListener) mActivity;
    }
    catch (ClassCastException ex) {
      Log.e(LOG_TAG, "Parent activity " + mActivity.toString() +
          "MUST implement OnSingerRatingChangeListener interface => finishing..");
      throw new ClassCastException(mActivity.toString() +
          "MUST implement OnSingerRatingChangeListener");
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    if (null == sAlbumsStr)
      sAlbumsStr = mContext.getResources().getString(R.string.tvSingersAlbums);
    if (null == sTracksStr)
      sTracksStr = mContext.getResources().getString(R.string.tvSingersTracks);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_details, container, false);

    if (-1 != mSingerIdx)
      showDetails(); /// it's time to fill view

    return mRootView;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mRatingChangeListener = null;
  }

  /**
   * full view inflation using both layout and singer record already set
   */
  @SuppressWarnings("deprecation")
  public void showDetails() {
    if ((null == mRootView) ||
        (-1 == mSingerIdx))
      return; /// wait for fragment creation + position setting

    final Singer singer = SingerHelper.getSingerForAdapterPosition(mSingerIdx);
    if (null == singer)
      return;

    Singer.Header header = singer.getHeader();
    Singer.Details details = singer.getDetails();
    final Singer.Data data = header.getData();

    Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbarDetails);

    if (ScreenConfiguration.getScreenConfigurationState() ==
        Constants.ScreenConfigurationState.TABLET_LANDSCAPE)
      ; /// dual-panel mode => no need to navigate UP
    else
    {
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mActivity.getSupportFragmentManager().popBackStack();
        }
      });
    }

    final CollapsingToolbarLayout collapsingToolbar =
        (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsingToolbarDetails);
    if (null != collapsingToolbar) {
      collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
      collapsingToolbar.setTitle(data.getName());
    }

    final TextView singerName = (TextView) mRootView.findViewById(R.id.tvDetailName);
    singerName.setText(data.getName());

    /// load CoverBig picture and get its palette
    final ImageView singerCoverBig = (ImageView) mRootView.findViewById(R.id.ivDetailCoverBig);
    final CardView cardViewCover = (CardView) mRootView.findViewById(R.id.cardViewDetailsCoverBig);
    final CardView cardViewContent = (CardView) mRootView.findViewById(R.id.cardViewDetailsContent);

    int targetCoverWidth = mContext.getResources()
        .getDimensionPixelSize(R.dimen.singer_cover_big_size) - 2*cardViewCover.getPaddingStart();
    int targetCoverHeight = mContext.getResources()
        .getDimensionPixelSize(R.dimen.singer_cover_big_size) - 2*cardViewCover.getPaddingTop();

    /// init custom target view for picasso load
    mTarget = new Target() {
      @Override
      public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
        singerCoverBig.setImageBitmap(bitmap);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
          @Override
          public void onGenerated(Palette palette) {
            Palette.Swatch textSwatch = palette.getLightMutedSwatch();
            if (null == textSwatch)
              return;
            int rgbColor = textSwatch.getRgb();
            Log.i(LOG_TAG, "Got palette for image, its RgbColor = " + rgbColor);
            collapsingToolbar.setContentScrimColor(rgbColor);
            cardViewCover.setCardBackgroundColor(rgbColor);
            cardViewContent.setCardBackgroundColor(rgbColor);
            singerName.setTextColor(textSwatch.getTitleTextColor());
          }
        });
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {
        Log.w(LOG_TAG, "FAILED to load bitmap for singer " + singer);
      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.d(LOG_TAG, "PrepareLoad bitmap for singer " + singer);
      }
    };

    Picasso.with(mContext)
        .load(details.getCoverBig())
        .placeholder(R.drawable.ic_photo_white_48dp)
        .error(R.drawable.ic_broken_image_white_48dp)
//        .resizeDimen(R.dimen.singer_cover_big_size, R.dimen.singer_cover_big_size)
        .resize(targetCoverWidth, targetCoverHeight)
        .centerInside()
        .into(mTarget);
/*
    int rgbColor = mContext.getResources().getColor(R.color.colorPrimary);
    collapsingToolbar.setContentScrimColor(rgbColor);
    cardViewCover.setCardBackgroundColor(rgbColor);
    cardViewContent.setCardBackgroundColor(rgbColor);

    Picasso.with(mContext)
        .load(details.getCoverBig())
        .resizeDimen(R.dimen.singer_cover_big_size, R.dimen.singer_cover_big_size)
        .centerInside()
        .into(singerCoverBig);
*/
    TextView singerLink = (TextView) mRootView.findViewById(R.id.tvDetailLink);
    singerLink.setText(details.getLink());

    TextView singerGenres = (TextView) mRootView.findViewById(R.id.tvDetailGenres);
    singerGenres.setText(data.getGenres());

    TextView singerAlbums = (TextView) mRootView.findViewById(R.id.tvDetailAlbums);
    singerAlbums.setText(sAlbumsStr + Integer.toString(data.getAlbums()));

    TextView singerTracks = (TextView) mRootView.findViewById(R.id.tvDetailTracks);
    singerTracks.setText(sTracksStr + Integer.toString(data.getTracks()));

    TextView singerDescription = (TextView) mRootView.findViewById(R.id.tvDetailDescription);
    singerDescription.setText(details.getDescription());

    final FloatingActionButton fab = (FloatingActionButton) mRootView.findViewById(R.id.fabFavorite);
    fab.setImageDrawable(getFavoriteFABDrawable(data.getRating()));

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int newRating = data.getRating()^1; /// value inversion
        fab.setImageDrawable(getFavoriteFABDrawable(newRating));
        data.setRating(newRating);
        if (null != mRatingChangeListener)
          mRatingChangeListener.onRatingChange(singer, newRating);
      }
    });
  }

  int getPosition() {
    return mSingerIdx;
  }

  public void setPosition(int position) {
    mSingerIdx = position;
  }

  /**
   * gets drawable for "like-dislike" FAB depending on current rating value
   */
  private Drawable getFavoriteFABDrawable(int rating) {
    return 0 == rating?
        ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_white_48dp) : /// can like
//        ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_white_48dp); /// can like
//        ContextCompat.getDrawable(getContext(), R.drawable.ic_unfavorite_white_48dp); /// dislike
//        ContextCompat.getDrawable(getContext(), R.drawable.ic_unfavorite2_white_48dp); /// dislike
        ContextCompat.getDrawable(getContext(), R.drawable.ic_unfavorite3_white_48dp); /// dislike
  }

}
