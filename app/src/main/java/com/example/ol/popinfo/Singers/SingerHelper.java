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

  private List<Singer> commonList = new ArrayList<>(Constants.Singers.NUMBER_OF);
  private List<Singer> favList;
  private Boolean isSearchingState = false;
  private Constants.SortingState sortingState = Constants.SortingState.NOT;
  private JsonSerializer mSerializer;

  public SingerHelper(Context context) {
    mSerializer = new JsonSerializer(context, Constants.IO.SINGERS_FILENAME);

    /// try to load favorites locally
    try {
      favList = mSerializer.loadSingers();
      Log.d(LOG_TAG, "Favorites (" + favList.size() + "it.) has been successfully loaded.");
    }
    catch (FileNotFoundException ex) { /// application 1'st run - it's ok
      favList = new ArrayList<>(Constants.Singers.NUMBER_OF_FAV);
    }
    catch (Exception ex) {
      Log.w(LOG_TAG, "Failed to load favorite singers - use empty list..");
      favList = new ArrayList<>(Constants.Singers.NUMBER_OF_FAV);
    }
  }

  public void resetCommonList(List<Singer> singerList) {
    if (this.commonList == singerList)
      ; /// case for call after cfg change
    else
      this.commonList = singerList;
  }

  /**
   * merges common list with favorite one:
   * 1) transmits rating from favorites into the same records in the common list
   * 2) adds all remained favorite records into the common list that aren't there yet
   */
  public void mergeLists() {
    commonList.removeAll(favList); /// remove duplicates w/O rating scores, if exist
    commonList.addAll(favList); /// all ALL favorites
  }

  public List<Singer> getCommonList() {
    return commonList;
  }


  /**
   * removes LIST OF singers items from common & favorite (if there are) lists
   */
  public void removeSingers(List<Singer> delList) {
    commonList.removeAll(delList);
    if (null != favList)
      favList.removeAll(delList);
  }


  /**
   * sorts by <sortingState> saved field
   */
  public void sort() {
    switch (sortingState) {
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

  public void sortById() {
    Collections.sort(commonList, Singer.SORTED_BY_ID);
  }

  public void sortByName() {
    Collections.sort(commonList, Singer.SORTED_BY_NAME);
  }

  public void sortByGenres() {
    Collections.sort(commonList, Singer.SORTED_BY_GENRES);
  }

  public Constants.SortingState getSortingState() {
    return sortingState;
  }

  public void setSortingState(Constants.SortingState sortingState) {
    this.sortingState = sortingState;
  }

  /**
   * @param name - search criteria (MASK).
   * @return partial list of copies from original commonList items that corresponds to <name> mask
   */
  public List<Singer> makeSearchByName(String name) {
    isSearchingState = true;
    List <Singer> searchList = new ArrayList<>(commonList.size());
    for (Singer singer : commonList) {
      if (singer.getHeader().getData().getName().contains(name))
        searchList.add(singer);
    }
    return searchList;
  }

  public Boolean getSearchingState() {
    return isSearchingState;
  }

  public void clearSearch() {
    isSearchingState = false;
  }

  /**
   * wrapper for the common singer rating change
   * additionally, processes possible favorite records change
   * @param singer - the whole singer record
   * @param value  - new rating value
   */
  public void changeSingerRating(Singer singer, int value) {
    Log.d(LOG_TAG, "Change singer " + singer + " rating to: " + value);
    singer.getHeader().getData().setRating(value);
    updateFavorite(singer, value);
  }

  /**
   * adds new rating record to the list or deletes it if existed and now got 0 stars
   * @param singer - the whole singer record
   * @param value  - new rating value
   */
  private void updateFavorite(Singer singer, int value) {
    for (Singer fav:favList) {
      if (fav.equals(singer)) {
        if (0 == value) { /// got 0 for existed record => remove it..
          Log.d(LOG_TAG, "Remove favorite " + fav + " due to rating zeroing");
          favList.remove(fav);
          return;
        }
        else { /// update rating value
          Log.d(LOG_TAG, "Update favorite " + fav + " rating to: " + value);
          fav.getHeader().getData().setRating(value);
          return;
        }
      }
    }
    /// the singer record is not existed yet
    if (0 == value) { /// nothing to add..
      return;
    }
    Log.d(LOG_TAG, "Add favorite " + singer);
    favList.add(singer);
  }

  public boolean saveFavoriteSingers() {
    Log.d(LOG_TAG, "Gonna save favorites (" + favList.size() + "it.)...");
    try {
      mSerializer.saveSingers(favList);
    }
    catch (Exception e) {
      Log.w(LOG_TAG, "Failed to save favorite singers - leave them unsaved..");
      return false;
    }
    return true;
  }
}
