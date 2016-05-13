package com.example.ol.popinfo.http.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ol on 07.04.16.
 */
public class CoverDto implements Parcelable {
  private String small;
  private String big;

  public CoverDto(String small, String big) {
    this.small = small;
    this.big = big;
  }

  public CoverDto(Parcel in) {
    small = in.readString();
    big = in.readString();
  }

  public static final Parcelable.Creator<CoverDto> CREATOR
      = new Parcelable.Creator<CoverDto>() {
    public CoverDto createFromParcel(Parcel in) {
      return new CoverDto(in);
    }

    public CoverDto[] newArray(int size) {
      return new CoverDto[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(small);
    out.writeString(big);
  }

  public String getSmall() {
    return small;
  }

  public void setSmall(String small) {
    this.small = small;
  }

  public String getBig() {
    return big;
  }

  public void setBig(String big) {
    this.big = big;
  }

}
