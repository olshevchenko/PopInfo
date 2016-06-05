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

public class SingersHelper {
  //for logging
  private static final String LOG_TAG = SingersHelper.class.getName();

  private List<Singer> singerList = new ArrayList<>(Constants.Singers.NUMBER_OF);
  private List<Singer> favList;
  private Constants.SortingState sortingState = Constants.SortingState.NOT;
  private JsonSerializer mSerializer;

  public SingersHelper(Context context) {
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

  public void setCommonList(List<Singer> singerList) {
    if (this.singerList == singerList)
      ; /// case for call after cfg change
    else {
      this.singerList.clear();
      this.singerList.addAll(singerList);
    }
  }

  /**
   * merges common list with favorite one:
   * 1) transmits rating from favorites into the same records in the common list
   * 2) adds all remained favorite records into the common list that aren't there yet
   */
  public void mergeLists() {
    singerList.removeAll(favList); /// remove duplicates w/O rating scores, if exist
    singerList.addAll(favList); /// all ALL favorites
  }

  public List<Singer> getSingerList() {
    return singerList;
  }

  public Singer getSinger(int number) throws IndexOutOfBoundsException {
    return singerList.get(number);
  }


  /**
   * removes ONE singer item from both (common & favorite if one has rating rank) lists
   */
  public void removeSinger(int number) throws IndexOutOfBoundsException {
    Singer singer = singerList.get(number);
    for (Singer fav:favList) {
      if (fav.equals(singer)) {
        favList.remove(fav);
        break;
      }
    }
    singerList.remove(number);
  }


  /**
   * Adds the singer to the common list at the exact position
   * copy it to the favorite list, if necessary
   */
  public void addSinger(Singer newSinger, int pos) {
    singerList.add(pos, newSinger);
    changeFavorite(newSinger, newSinger.getHeader().getData().getRating());
  }


  /**
   * removes LIST OF singers items from both (common & favorite if one has rating rank) lists

   * erges common list with favorite one:
   * 1) transmits rating from favorites into the same records in the common list
   * 2) adds all remained favorite records into the common list that aren't there yet
   */
  public void removeSingers(List<Singer> delList) {
    singerList.removeAll(delList);
    favList.removeAll(delList); /// as well as from common
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
    Collections.sort(singerList, Singer.SORTED_BY_ID);
  }

  public void sortByName() {
    Collections.sort(singerList, Singer.SORTED_BY_NAME);
  }

  public void sortByGenres() {
    Collections.sort(singerList, Singer.SORTED_BY_GENRES);
  }

  public Constants.SortingState getSortingState() {
    return sortingState;
  }

  public void setSortingState(Constants.SortingState sortingState) {
    this.sortingState = sortingState;
  }

  /**
   * wrapper for the common singer rating change
   * additionally, processes possible favorite records change
   * @param singer - the whole singer record
   * @param value  - new rating value
   */
  public void changeRating(Singer singer, int value) {
    Log.d(LOG_TAG, "Change singer " + singer + " rating to: " + value);
    singer.getHeader().getData().setRating(value);
    changeFavorite(singer, value);
  }

  /**
   * adds new rating record to the list or deletes it if existed and now got 0 stars
   * @param singer - the whole singer record
   * @param value  - new rating value
   */
  private void changeFavorite(Singer singer, int value) {
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
