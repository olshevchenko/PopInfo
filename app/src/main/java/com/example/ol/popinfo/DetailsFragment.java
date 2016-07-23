package com.example.ol.popinfo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

  private Context mContext = null;
  private SingerHelper mSingerHelper = null; /// holder for singer's info
  private static Singer mDetailedSinger = null;

  private static String sAlbumsStr = null;
  private static String sTracksStr = null;

  private Target mTarget; /// preventing early termination by GC

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (null == mContext)
      /// will always use application's rather than activity's one
      mContext = activity.getApplicationContext();
  }

  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mSingerHelper = SingerHelper.getInstance(mContext);
    if (null == sAlbumsStr)
      sAlbumsStr = mContext.getResources().getString(R.string.tvSingersAlbums);
    if (null == sTracksStr)
      sTracksStr = mContext.getResources().getString(R.string.tvSingersTracks);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_details, container, false);
    return v;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onStart() {
    super.onStart();
    View view = getView();
    if ((null == view) ||
        (null == mDetailedSinger))
      return;

    Singer.Header header = mDetailedSinger.getHeader();
    Singer.Details details = mDetailedSinger.getDetails();
    final Singer.Data data = header.getData();

    final TextView singerName = (TextView) view.findViewById(R.id.tvDetailName);
    singerName.setText(data.getName());

    TextView singerLink = (TextView) view.findViewById(R.id.tvDetailLink);
    singerLink.setText(details.getLink());

    TextView singerGenres = (TextView) view.findViewById(R.id.tvDetailGenres);
    singerGenres.setText(data.getGenres());

    TextView singerAlbums = (TextView) view.findViewById(R.id.tvDetailAlbums);
    singerAlbums.setText(sAlbumsStr + Integer.toString(data.getAlbums()));

    TextView singerTracks = (TextView) view.findViewById(R.id.tvDetailTracks);
    singerTracks.setText(sTracksStr + Integer.toString(data.getTracks()));

    final ImageView singerFavorite = (ImageView) view.findViewById(R.id.ivFavorite);
    singerFavorite.setVisibility(data.getRating() == 0? View.GONE : View.VISIBLE);

    TextView singerDescription = (TextView) view.findViewById(R.id.tvDetailDescription);
    singerDescription.setText(details.getDescription());

    final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabFavorite);
    fab.setImageDrawable(getFavoriteFABDrawable(data.getRating()));

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int newRating = data.getRating()^1; /// value inversion
        mSingerHelper.changeSingerRating(mDetailedSinger, newRating);
        fab.setImageDrawable(getFavoriteFABDrawable(newRating));
      }
    });

    /// load CoverBig picture and get its palette
    final ImageView singerCoverBig = (ImageView) view.findViewById(R.id.ivDetailCoverBig);

    final View background = getView();

    mTarget = new Target() {
      @Override
      public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        singerCoverBig.setImageBitmap(bitmap);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
          @Override
          public void onGenerated(Palette palette) {
            Palette.Swatch textSwatch = palette.getLightMutedSwatch();
            if (null == textSwatch)
              return;
            background.setBackgroundColor(textSwatch.getRgb());
            singerName.setTextColor(textSwatch.getTitleTextColor());
          }
        });
      }

      @Override
      public void onBitmapFailed(Drawable errorDrawable) {
        Log.w(LOG_TAG, "FAILED to load bitmap for singer " + mDetailedSinger);
      }

      @Override
      public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.d(LOG_TAG, "PrepareLoad bitmap for singer " + mDetailedSinger);
      }
    };

    Picasso.with(mContext)
        .load(details.getCoverBig())
        .placeholder(R.drawable.ic_photo_white_48dp)
        .error(R.drawable.ic_broken_image_white_48dp)
        .resize(Constants.Singers.COVER_BIG_IMG_SIZE, Constants.Singers.COVER_BIG_IMG_SIZE)
        .centerInside()
        .into(mTarget);
  }

  public static void setDetailedSinger(Singer detailedSinger) {
    mDetailedSinger = detailedSinger;
  }

  /**
   * gets drawable for "like-dislike" FAB depending on current rating value
   */
  private Drawable getFavoriteFABDrawable(int rating) {
    return 0 == rating?
        ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_white_48dp) : /// can like
        ContextCompat.getDrawable(getContext(), R.drawable.ic_favorite_border_black_48dp); /// dislike
  }
}
