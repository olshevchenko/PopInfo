package com.example.ol.popinfo;


import android.content.Context;
import android.view.View;

import com.example.ol.popinfo.Singers.Singer;

import java.util.List;

/**
 * superclass for operations abstract interfaces
 */
public class Interfaces {

  /**
   * interface to process request for whole singer list information update
   */
  public interface OnSingerListRequestProcessor {
    void listRequest(Context context);
  }

  /**
   * interface to process response from whole singer list information update
   */
  public interface OnSingerListUpdateProcessor {
    void listUpdate(List<Singer> newList);
  }


  /**
   * interface to process events on separated singer item (in RecyclerView)
   */
  public interface OnSingerItemClickListener {
    void onClick(int position); /// for starting detail view
    void onLongClick(int position); /// for (multiple) selection
  }

  /**
   * interface to process detailed view of singer pointed (list=>details)
   */
  public interface OnSingerDetailViewProcessor {
    void onDetailView(int position);
  }

  /**
   * interface to process rating (favorite) change event on singer (list<=details)
   */
  public interface OnSingerRatingChangeListener {
    void onRatingChange(Singer singer, int newRating);
  }
}


