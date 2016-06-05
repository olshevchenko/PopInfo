package com.example.ol.popinfo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ol.popinfo.Singers.Singer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ol on 12.04.16.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.SingerViewHolder> {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private final Context mContext;
  private static ImagesHelper mImagesHelper;
  private static Logic.OnSingerItemClickListener mClickListener;

  private List<Singer> mSingers; /// current list
  private static SparseBooleanArray mSelectedItems = new SparseBooleanArray();

  private static String sAlbumsStr;
  private static String sTracksStr;
  private static int sCardDefColor;
  private static int sCardSelectColor;

  static class SingerViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener, View.OnLongClickListener {
    CardView mCV;
    ImageView mSingerCoverSmall;
    TextView mSingerName;
    TextView mSingerGenres;
    TextView mSingerAlbumsTracks;
    RatingBar mSingerRating;
    ImageView mSelector;

    SingerViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
      mCV = (CardView)itemView.findViewById(R.id.cardView);
      mSingerCoverSmall = (ImageView)itemView.findViewById(R.id.ivCoverSmall);
      mSingerName = (TextView)itemView.findViewById(R.id.tvSingerName);
      mSingerGenres = (TextView)itemView.findViewById(R.id.tvSingerGenres);
      mSingerAlbumsTracks = (TextView)itemView.findViewById(R.id.tvSingerAlbumsTracks);
      mSingerRating = (RatingBar)itemView.findViewById(R.id.rbSingerRating);
      mSelector = (ImageView)itemView.findViewById(R.id.ivSelector);
    }

    /**
     * binds singer item to the holder
     * adds 2 listener (click for moving to singer's detail view, long click for items selection)
     * @param singerItem
     * @param isSelected whether the item was selected already or not yet
     */
    public void bind(final Singer singerItem, boolean isSelected) {
      /// 1'st, highlight the item accordingly
      mCV.setCardBackgroundColor(isSelected? sCardSelectColor : sCardDefColor);
//      mCV.setMaxCardElevation(isSelected? 0.0f : 6.0f);
      mSelector.setVisibility(isSelected? View.VISIBLE : View.GONE);

      /// then, draw the item's content
      Singer.Header header = singerItem.getHeader();
      Singer.Data data = header.getData();

      mImagesHelper.setImageBitmap(header.getCoverSmall(), mSingerCoverSmall); /// ASYNCHRONOUSLY
      mSingerName.setText(data.getName());
      mSingerGenres.setText(data.getGenres());
      mSingerRating.setRating((float)data.getRating());

      /// split the whole string from 2 numbers + 2 localized strings
      StringBuilder albumsTracks = new StringBuilder(Constants.Singers.ALBUMS_TRACKS_STR_LEN);
      albumsTracks.append(sAlbumsStr);
      albumsTracks.append(data.getAlbums());
      albumsTracks.append(sTracksStr);
      albumsTracks.append(data.getTracks());
      mSingerAlbumsTracks.setText(albumsTracks.toString());
    }

    @Override
    public void onClick(View view) {
      mClickListener.onClick(getAdapterPosition(), view);
    }

    @Override
    public boolean onLongClick(View view) {
      return mClickListener.onLongClick(getAdapterPosition(), view);
    }

  }

  // Provide a suitable constructor (depends on the kind of dataset)
  public RecyclerAdapter(Context context,
                         ImagesHelper imagesHelper,
                         List<Singer> singers,
                         Logic.OnSingerItemClickListener listener) {

    mContext = context;
    mImagesHelper = imagesHelper;
    mSingers = singers;
    mClickListener = listener;

    /// preliminary get "albums" & "tracks" string resources for further multiple use
    sAlbumsStr = mContext.getResources().getString(R.string.tvSingersAlbums);
    sTracksStr = mContext.getResources().getString(R.string.tvSingersTracks);

    sCardDefColor= mContext.getResources().getColor(R.color.card_default_background);
    sCardSelectColor = mContext.getResources().getColor(R.color.card_selected_background);
  }

  @Override
  public RecyclerAdapter.SingerViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
    View v = LayoutInflater.from(parent.getContext()).
        inflate(R.layout.recycler_item, parent, false);
    return new SingerViewHolder(v);
  }

  @Override
  public void onBindViewHolder(SingerViewHolder holder, int position) {
    Log.d(LOG_TAG, "holder.bind(pos:" + position + ", singer:" + mSingers.get(position) +")");
    holder.bind(mSingers.get(position), mSelectedItems.get(position, false));
  }

  @Override
  public int getItemCount() {
  return mSingers.size();
  }


  /**
   * Just notifies.
   *
   * @param pos The index of the item to remove.
   */
  public void removeItem(int pos) {
    notifyItemRemoved(pos);
  }

  /**
   * Just notifies.
   *
   * @param pos The index of the item to add.
   */
  public void addItem(int pos) {
    notifyItemRangeInserted(pos, 1);
  }
/**
 * multiple selection operations
 */
  public void toggleSelection(int pos) {
    if (mSelectedItems.get(pos, false)) {
      mSelectedItems.delete(pos);
    }
    else {
      mSelectedItems.put(pos, true);
    }
    notifyItemChanged(pos);
  }

  public void clearSelections() {
    mSelectedItems.clear();
    notifyDataSetChanged();
  }

  public int getSelectedItemCount() {
    return mSelectedItems.size();
  }

  public List<Integer> getSelectedItems() {
    List<Integer> items = new ArrayList<Integer>(mSelectedItems.size());
    for (int i = 0; i < getSelectedItemCount(); i++) {
      items.add(mSelectedItems.keyAt(i));
    }
    return items;
  }
}
