package com.example.ol.popinfo.Singers;

import com.example.ol.popinfo.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ol on 15.04.16.
 */
public class SingersHelper {
  private List<Singer> singerList = new ArrayList<>(Constants.Singers.NUMBER_OF);

  public SingersHelper() {}

  public void setSingerList(List<Singer> singerList) {
///    this.singerList = singerList; /// not suitable way for further list notifyDataSetChanged()
    if (this.singerList == singerList)
      ; /// case for placesUpdate() call after cfg change
    else {
      this.singerList.clear();
      this.singerList.addAll(singerList);
    }
  }

  public List<Singer> getSingerList() {
    return singerList;
  }

  public Singer getSinger(int number) throws IndexOutOfBoundsException {
    return singerList.get(number);
  }

}
