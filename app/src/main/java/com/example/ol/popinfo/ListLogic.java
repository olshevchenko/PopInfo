package com.example.ol.popinfo;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ol on 22.06.16.
 */

/**
 * Core logic for singer list
 */
public class ListLogic {
  private Context mContext;
  private RecyclerAdapter mAdapter;


  public ListLogic(final Context context,
                   final RecyclerAdapter adapter) {
    mContext = context;
    mAdapter = adapter;
  }

  /**
   * sorts list of viewing singers according sort type checked in menu
   * @param item - menu item checked
   */
  public void sortSingers(MenuItem item) {
    item.setChecked(true);
    switch (item.getItemId()) {
      case R.id.action_sort_by_name:
        SingerHelper.Sorting.sortByName();
        break;
      case R.id.action_sort_by_genres:
        SingerHelper.Sorting.sortByGenres();
        break;
      case R.id.action_sort_by_none:
      default:
        SingerHelper.Sorting.sortById();
        break;
    }
    mAdapter.notifyDataSetChanged();
  }
  /**
   * deletes selected part of singers list (both in singers data storage and adapter)
   * allows to undo deletion by using snackbar w.action
   *
   * @param cABMode - context action bar with selected items deletion engine
   */
  public void deleteSelectedSingers(final ActionMode cABMode, final CoordinatorLayout coordinatorLayout) {
    final List<Integer> selectedItemPositions = mAdapter.getSelectedItemsList(); /// got selected position list
    if (null == selectedItemPositions)
      return;
    int selectedNum = selectedItemPositions.size();
    if (0 == selectedNum)
      return;

    final List<Singer> fromList = mAdapter.getList(); /// gonna delete from current viewing list
    final List<Singer> undoList = new ArrayList<>(fromList);
    Collections.sort(selectedItemPositions);
    final SparseBooleanArray undoSelectedItemsArray = mAdapter.getSelectedItemsSBArray().clone();
    /// now we got:
    /// 1) source list <fromList> (whether common singer list or found one) to deletion from
    /// 2) its copy for possible undo
    /// 3) selected list in it of WHAT exactly to delete
    /// 4) copy of selected ARRAY for possible undo

    /// prepare list of deleting items
    final List<Singer> singers2Remove = new ArrayList<>(selectedNum);
    int currPos;
    for (int i = selectedNum-1; i >=0 ; i--) { /// shift remainder up
      currPos = selectedItemPositions.get(i);
      singers2Remove.add(fromList.get(currPos));
      fromList.remove(currPos);
      mAdapter.removeSelectedItem(currPos); /// update view
    }

    /// show UNDO case
    Snackbar snackbar = Snackbar
        .make(coordinatorLayout, mContext.getString(R.string.sb_count_deleted, selectedNum), Snackbar.LENGTH_LONG)
        .setCallback(new Snackbar.Callback() {
          @Override
          public void onDismissed(Snackbar snackbar, int event) {
            if (DISMISS_EVENT_ACTION == event)
              ;
            else {
              SingerHelper.Lists.removeSingers(singers2Remove);
              singers2Remove.clear();
              undoList.clear();
              undoSelectedItemsArray.clear();
              selectedItemPositions.clear();
              cABMode.finish();
            }
          }
        })
        .setAction(mContext.getString(R.string.sb_undo), new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            if (SingerHelper.Searching.getSearchingState())
              ; /// if delete from search - nothing to restore in singer (common) storage
            else
              SingerHelper.Lists.resetCommonList(undoList);
            mAdapter.resetList(undoList); /// restore view with original list from backup
            mAdapter.setSelectedItemsSBArray(undoSelectedItemsArray); /// restore selections too
            singers2Remove.clear();
            fromList.clear();
            selectedItemPositions.clear();
          }
        });

    // Changing message text color
    snackbar.setActionTextColor(Color.RED);
    snackbar.getView().setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
    snackbar.setDuration(Snackbar.LENGTH_SHORT);
    snackbar.show();
  }

  /**
   * gets new list of singers, stores it and redraws view
   * @param newList
   */
  public void listUpdate(List<Singer> newList) {
    SingerHelper.Lists.resetCommonList(newList);
    SingerHelper.Lists.mergeLists(); /// merge with local favorite ones
    SingerHelper.Sorting.sort(); /// resort the list just assembled accordingly
    mAdapter.resetList(SingerHelper.Lists.getsCommonList()); /// update view

    /// show up suggestion to click
    Toast toast = Toast.makeText(mContext,
        mContext.getString(R.string.tstClickSuggestion),
        Toast.LENGTH_LONG);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }

}
