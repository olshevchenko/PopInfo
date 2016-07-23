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
   * interface to complete response of singers information update
   */
  public interface SingersUpdateProcessor {
    void updateSingers(List<Singer> newList);
  }

  /**
   * interface to process request for singers information update
   */
  public interface SingersRequestInfoProcessor {
    void singersRequestInfo(Context context);
  }

  /**
   * interface to process events on separated singer item
   */
  public interface OnSingerItemClickListener {
    void onClick(int position, View view); /// for starting detail view
    void onLongClick(int position, View view); /// for (multiple) selection
  }

  /**
   * interface to process detailed view of requested Singer
   */
  public interface SingerDetailViewProcessor {
    void singerDetailView(Singer singerDetailed);
  }
}


