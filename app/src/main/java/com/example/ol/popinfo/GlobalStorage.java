package com.example.ol.popinfo;

import com.example.ol.popinfo.Singers.Singer;

/**
 * Created by ol on 27.06.16.
 */

/**
 * app-context storage for interchanging of singer record being detail viewed | edited
 */
public class GlobalStorage extends android.app.Application {

  private static Singer mDetailedSinger = null;

  public static Singer getDetailedSinger() {
    return mDetailedSinger;
  }

  public static void setDetailedSinger(Singer detailedSinger) {
    GlobalStorage.mDetailedSinger = detailedSinger;
  }

}