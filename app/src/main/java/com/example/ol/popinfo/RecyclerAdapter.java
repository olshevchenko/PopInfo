package com.example.ol.popinfo;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.popinfo.Images.ImagesHelper;
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
  private RecyclerView mRecyclerView = null;
  private static ImagesHelper mImagesHelper;

  private static Interfaces.OnSingerItemClickListener mClickListener;
  private int mClickedPosition = -1;

  private List<Singer> mSingers; /// current list
  private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

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
    TextView mSingerAlbums;
    TextView mSingerTracks;
    ImageView mSelector;
    ImageView mClicker;
    ImageView mSingerFavorite;

    SingerViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
      mCV = (CardView)itemView.findViewById(R.id.cardView);
      mSingerCoverSmall = (ImageView)itemView.findViewById(R.id.ivCoverSmall);
      mSingerName = (TextView)itemView.findViewById(R.id.tvSingerName);
      mSingerGenres = (TextView)itemView.findViewById(R.id.tvSingerGenres);
      mSingerAlbums = (TextView)itemView.findViewById(R.id.tvSingerAlbums);
      mSingerTracks = (TextView)itemView.findViewById(R.id.tvSingerTracks);
      mSelector = (ImageView)itemView.findViewById(R.id.ivSelector);
      mClicker = (ImageView)itemView.findViewById(R.id.ivClicker);
      mSingerFavorite = (ImageView)itemView.findViewById(R.id.ivFavorite);
    }

    /**
     * binds singer item to the holder
     * elevates | colorizes | makes parts visible depending on item state
     * @param singerItem list item
     * @param isSelected whether the item was selected already or not yet
     * @param isClicked sign of the item has been clicked
     * @param isDualPanelMode sign of TABLET_LANDSCAPE screen configuration
     */
    public void bind(final Singer singerItem,
                     boolean isSelected,
                     boolean isClicked,
                     boolean isDualPanelMode) {

      /// 1'st, elevate the item accordingly to his state (if poss, otherwise just show arrow)
      if (isClicked && isDualPanelMode) {
          mClicker.setVisibility(View.VISIBLE);
      }
      else {
          mClicker.setVisibility(View.GONE);
      }

      /// 2'nd, set the item color & switch visibility for selector
      if ( isSelected ) {
        mCV.setCardBackgroundColor(sCardSelectColor);
        mSelector.setVisibility(View.VISIBLE);
      }
      else {
        mCV.setCardBackgroundColor(sCardDefColor);
        mSelector.setVisibility(View.GONE);
      }

      /// then, draw the item's content
      Singer.Header header = singerItem.getHeader();
      Singer.Data data = header.getData();

      mImagesHelper.setImageBitmap(header.getCoverSmall(), mSingerCoverSmall); /// ASYNCHRONOUSLY
      mSingerName.setText(data.getName());
      mSingerGenres.setText(data.getGenres());
      mSingerFavorite.setVisibility(data.getRating() > 0? View.VISIBLE : View.GONE);

      mSingerAlbums.setText(sAlbumsStr + Integer.toString(data.getAlbums()));
      mSingerTracks.setText(sTracksStr + Integer.toString(data.getTracks()));
    }

    @Override
    public void onClick(View view) {
      if (null != mClickListener)
        mClickListener.onClick(getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View view) {
      if (null != mClickListener)
        mClickListener.onLongClick(getAdapterPosition());
      return true;
    }

  }

  @SuppressWarnings("deprecation")
  public RecyclerAdapter(Context context,
                         ImagesHelper imagesHelper,
                         List<Singer> singers,
                         Interfaces.OnSingerItemClickListener listener) {

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

  /**
   * resets the data with the new one (e.g. to show search results)
    */
  public void resetList(List<Singer> newSingers) {
    mSingers = newSingers;
    notifyDataSetChanged();
  }


  public List<Singer> getList() {
    return mSingers;
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
    holder.bind(mSingers.get(position),
        mSelectedItems.get(position, false),
        position == mClickedPosition,
        ScreenConfiguration.getScreenConfigurationState()==
            Constants.ScreenConfigurationState.TABLET_LANDSCAPE);
  }

  public Singer getItem(int position) {
    return mSingers.get(position);
  }

  @Override
  public int getItemCount() {
    return mSingers.size();
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    mRecyclerView = recyclerView;
  }

  @Override
  public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
    super.onDetachedFromRecyclerView(recyclerView);
    mRecyclerView = null;
  }

  /**
   * Emulates item click by customer
   */
  public void clickItem(int position) {
    if (null == mRecyclerView)
      return;
    SingerViewHolder holder = (SingerViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
    if (null == holder)
      return;
    holder.itemView.performClick();
  }


  /**
   * Deletes selected item & notifies view.
   */
  public void removeSelectedItem(int pos) {
    mSelectedItems.delete(pos);
    notifyItemRemoved(pos);
  }

  /**
   * single item click processing
   */
  public void toggleClick(int oldPos, int newPos) {
    mClickedPosition = newPos; /// store here for correct drawing in onBindViewHolder()
    if (oldPos >= 0)
      /// remove 'click' transformation from old item (if there was one)
      notifyItemChanged(oldPos);

    notifyItemChanged(mClickedPosition);
  }

  /**
   * multiple item selection processing
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

  public SparseBooleanArray getSelectedItemsSBArray() {
    return mSelectedItems;
  }

  public void setSelectedItemsSBArray(SparseBooleanArray newSelectedItems) {
    this.mSelectedItems = newSelectedItems;
  }

  public List<Integer> getSelectedItemsList() {
    List<Integer> items = new ArrayList<>(mSelectedItems.size());
    for (int i = 0; i < mSelectedItems.size(); i++) {
      items.add(mSelectedItems.keyAt(i));
    }
    return items;
  }

  public int getSelectedItemCount() {
    return mSelectedItems.size();
  }
}
