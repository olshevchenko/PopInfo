package com.example.ol.popinfo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;
import com.squareup.picasso.Picasso;

/**
 * Created by ol on 27.06.16
 */
public class DetailsFragment extends Fragment {
  private Context mContext = null;
  private SingerHelper mSingerHelper = null; /// holder for singer's info
  private static Singer mDetailedSinger = null;

  private static String sAlbumsStr = null;
  private static String sTracksStr = null;

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

  @Override
  public void onStart() {
    super.onStart();
    View view = getView();
    if ((null == view) ||
        (null == mDetailedSinger))
      return;

    ImageView singerCoverBig;
    TextView singerName;
    TextView singerLink;
    TextView singerGenres;
    TextView singerAlbums;
    TextView singerTracks;
    ImageView singerFavorite;
    TextView singerDescription;

    Singer.Header header = mDetailedSinger.getHeader();
    Singer.Details details = mDetailedSinger.getDetails();
    Singer.Data data = header.getData();

    singerCoverBig = (ImageView) view.findViewById(R.id.ivDetailCoverBig);
    Picasso.with(mContext)
        .load(details.getCoverBig())
        .placeholder(R.drawable.ic_photo_white_48dp)
        .error(R.drawable.ic_broken_image_white_48dp)
        .fit()
        .into(singerCoverBig);

    singerName = (TextView) view.findViewById(R.id.tvDetailName);
    singerName.setText(data.getName());

    singerLink = (TextView) view.findViewById(R.id.tvDetailLink);
    singerLink.setText(details.getLink());

    singerGenres = (TextView) view.findViewById(R.id.tvDetailGenres);
    singerGenres.setText(data.getGenres());

    singerAlbums = (TextView) view.findViewById(R.id.tvDetailAlbums);
    singerAlbums.setText(sAlbumsStr + Integer.toString(data.getAlbums()));

    singerTracks = (TextView) view.findViewById(R.id.tvDetailTracks);
    singerTracks.setText(sTracksStr + Integer.toString(data.getTracks()));

    singerFavorite = (ImageView) view.findViewById(R.id.ivFavorite);
    singerFavorite.setVisibility(data.getRating() > 0? View.VISIBLE : View.GONE);

    singerDescription = (TextView) view.findViewById(R.id.tvDetailDescription);
    singerDescription.setText(details.getDescription());
  }

  public static void setDetailedSinger(Singer detailedSinger) {
    mDetailedSinger = detailedSinger;
  }
}
