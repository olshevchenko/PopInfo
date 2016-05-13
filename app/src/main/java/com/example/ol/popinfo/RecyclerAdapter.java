package com.example.ol.popinfo;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.popinfo.Singers.Singer;

import java.util.List;

/**
 * Created by ol on 12.04.16.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.SingerViewHolder> {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private final Context mContext;
  private ImagesHelper mImagesHelper;
  private List<Singer> mSingers;
  private String mAlbumsStr;
  private String mTracksStr;

  public static class SingerViewHolder extends RecyclerView.ViewHolder {
    CardView cv;
    ImageView singerCoverSmall;
    TextView singerName;
    TextView singerGenres;
    TextView singerAlbumsTracks;
    SingerViewHolder(View itemView) {
      super(itemView);
      cv = (CardView)itemView.findViewById(R.id.cardView);
      singerCoverSmall = (ImageView)itemView.findViewById(R.id.ivCoverSmall);
      singerName = (TextView)itemView.findViewById(R.id.tvSingerName);
      singerGenres = (TextView)itemView.findViewById(R.id.tvSingerGenres);
      singerAlbumsTracks = (TextView)itemView.findViewById(R.id.tvSingerAlbumsTracks);
    }
  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public RecyclerAdapter(Context context, ImagesHelper imagesHelper, List<Singer> singers) {

    mContext = context;
    mImagesHelper = imagesHelper;
    mSingers = singers;

    /// preliminary get "albums" & "tracks" string resources for further multiple use
    mAlbumsStr = mContext.getResources().getString(R.string.tvSingersAlbums);
    mTracksStr = mContext.getResources().getString(R.string.tvSingersTracks);
  }

  // Create new views (invoked by the layout manager)
  @Override
  public RecyclerAdapter.SingerViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
    // create a new view & its holder
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,
        parent, false);
    // set the view's size, margins, paddings and layout parameters
    return new SingerViewHolder(v);
  }

  // Replace the contents of a view (invoked by the layout manager)
  @Override
  public void onBindViewHolder(SingerViewHolder holder, int position) {
    Singer.Header header = mSingers.get(position).getHeader();
    Singer.Data data = header.getData();
    holder.singerName.setText(data.getName());
    holder.singerGenres.setText(data.getGenres());
    /**
     * split the whole string from 2 numbers + 2 localized strings
     */
    StringBuilder albumsTracks = new StringBuilder(Constants.Singers.ALBUMS_TRACKS_STR_LEN);
    albumsTracks.append(mAlbumsStr);
    albumsTracks.append(data.getAlbums());
    albumsTracks.append(mTracksStr);
    albumsTracks.append(data.getTracks());
    holder.singerAlbumsTracks.setText(albumsTracks.toString());

    /// ToDo - replace to the using cash
    mImagesHelper.setImageBitmap(header.getCoverSmall(), holder.singerCoverSmall);
//    holder.singerCoverSmall.setImageResource(R.drawable.ic_settings_white_36dp);
/*
    Drawable d;
    try {
      InputStream is = (InputStream) new URL(header.getCoverSmall()).getContent();
      d = Drawable.createFromStream(is, null);
      holder.singerCoverSmall.setImageDrawable(d);

      URLConnection urlCon = new URL(header.getCoverSmall()).openConnection();
      d = Drawable.createFromStream(urlCon.getInputStream(), "src name");
      holder.singerCoverSmall.setImageDrawable(d);
    } catch (Exception ex) {
      Log.e(LOG_TAG, "got error while getting image: " + ex);
    }
*/
  }
/*
  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }
*/
  @Override
  public int getItemCount() {
  return mSingers.size();
  }
}
