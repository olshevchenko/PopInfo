package com.example.ol.popinfo.Singers;

import android.content.Context;
import android.util.Log;

import com.example.ol.popinfo.Constants;
import com.example.ol.popinfo.json.JsonSerializer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ol on 15.04.16.
 */

public class SingerHelper {
  //for logging
  private static final String LOG_TAG = SingerHelper.class.getName();

  /**
   * Sorting logic of the helper
   */
 public static class Sorting {
    private static Constants.SortingState sSortingState = Constants.SortingState.NOT;

    public static void sort() {
      switch (sSortingState) { /// sorts by <sSortingState> saved field
        case BY_NAME:
          sortByName();
          break;
        case BY_GENRES:
          sortByGenres();
          break;
        case NOT:
        default:
          sortById();
      }
    }

    public static void sortById() {
      sSortingState = Constants.SortingState.NOT;
      Collections.sort(Lists.sCommonList, Singer.SORTED_BY_ID);
    }

    public static void sortByName() {
      sSortingState = Constants.SortingState.BY_NAME;
      Collections.sort(Lists.sCommonList, Singer.SORTED_BY_NAME);
    }

    public static void sortByGenres() {
      sSortingState = Constants.SortingState.BY_GENRES;
      Collections.sort(Lists.sCommonList, Singer.SORTED_BY_GENRES);
    }

    public static Constants.SortingState getsSortingState() {
      return sSortingState;
    }
  } // class Sorting


  /**
   * Searching logic of the helper
   */
  public static class Searching {
    private static Boolean sIsSearchingState = false;
    private static List<Singer> searchList = new ArrayList<>(Constants.Singers.NUMBER_OF);

    /**
     * @param name - search criteria (MASK for the singer's name ).
     * creates partial list of copies from original sCommonList items that corresponds to <name> mask
     */
    public static void makeSearchByName(String name) {
      sIsSearchingState = true;
      for (Singer singer : Lists.sCommonList) {
        if (singer.getHeader().getData().getName().contains(name))
          searchList.add(singer);
      }
    }

    public static List<Singer> getSearchedList() {
      return searchList;
    }

    public static Boolean getSearchingState() {
      return sIsSearchingState;
    }

    public static void clearSearch() {
      searchList.clear();
      sIsSearchingState = false;
    }
  } // class Searching


  /**
   * Lists operation logic + mechanics of the helper
   */
  public static class Lists {
    private static List<Singer> sCommonList = new ArrayList<>(Constants.Singers.NUMBER_OF);

    public static List<Singer> getsCommonList() {
      return sCommonList;
    }

    public static void resetCommonList(List<Singer> singerList) {
      if (sCommonList == singerList)
        ; /// case for call after cfg change
      else
        sCommonList = singerList;
    }

    /**
     * removes LIST OF singers items from common & favorite (if there are) lists
     */
    public static void removeSingers(List<Singer> delList) {
      sCommonList.removeAll(delList);
      if (null != Favors.sFavList)
        Favors.sFavList.removeAll(delList);
    }

    /**
     * merges common list with favorite one:
     * 1) transmits rating from favorites into the same records in the common list
     * 2) adds all remained favorite records into the common list that aren't there yet
     */
    public static void mergeLists() {
      if (null == Favors.sFavList)
        return;
      sCommonList.removeAll(Favors.sFavList); /// remove duplicates w/O rating scores, if exist
      sCommonList.addAll(Favors.sFavList); /// all ALL favorites
    }
  } // class Lists


  /**
   * Favorites operation logic + mechanics of the helper
   */
  public static class Favors {
    private static JsonSerializer sSerializer;
    private static List<Singer> sFavList = null;

    public static void loadFavoriteSingers(Context context) {
      sSerializer = new JsonSerializer(context, Constants.IO.SINGERS_FILENAME);

      /// try to load favorites locally
      try {
        sFavList = sSerializer.loadSingers();
        Log.d(LOG_TAG, "Favorites (" + sFavList.size() + "it.) has been successfully loaded.");
      }
      catch (FileNotFoundException ex) { /// application 1'st run - it's ok
        sFavList = new ArrayList<>(Constants.Singers.NUMBER_OF_FAV);
      }
      catch (Exception ex) {
        Log.w(LOG_TAG, "Failed to load favorite singers - use empty list..");
        sFavList = new ArrayList<>(Constants.Singers.NUMBER_OF_FAV);
      }
    }

    public static boolean saveFavoriteSingers() {
//      Log.d(LOG_TAG, "Gonna save favorites (" + sFavList.size() + "it.)...");
      try {
        sSerializer.saveSingers(sFavList);
      }
      catch (Exception e) {
        Log.w(LOG_TAG, "Failed to save favorite singers - leave them unsaved..");
        return false;
      }
      return true;
    }

    /**
     * adds new rating record to the list or deletes it if existed and now got 0 stars
     * @param singer - the whole singer record
     * @param value  - new rating value
     */
    public static void updateFavorite(Singer singer, int value) {
      if (null == sFavList)
        return;
      for (Singer fav:sFavList) {
        if (fav.equals(singer)) {
          if (0 == value) { /// got 0 for existed record => remove it..
            //          Log.d(LOG_TAG, "Remove favorite " + fav + " due to rating zeroing");
            sFavList.remove(fav);
            return;
          }
          else { /// update rating value
            //          Log.d(LOG_TAG, "Update favorite " + fav + " rating to: " + value);
            fav.getHeader().getData().setRating(value);
            return;
          }
        }
      }
      /// the singer record is not existed yet
      if (0 == value) { /// nothing to add..
        return;
      }
      //    Log.d(LOG_TAG, "Add favorite " + singer);
      sFavList.add(singer);
    }
  }

  /// COMMON independent methods
  /**
   * gets singer item from APPROPRIATE list (whether common or search) by its position
   * @param position - visible item number from adapter point of view
   * @return singer record
   */
  public static Singer getSingerForAdapterPosition(int position) {
    if (Searching.sIsSearchingState)
      return Searching.searchList.get(position);
    else
      return Lists.sCommonList.get(position);
  }

}

