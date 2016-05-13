package com.example.ol.popinfo;

/**
 * Constants necessary for operation
 */
public final class Constants {
  public class Languages {
    public static final String ENG = "en";
    public static final String RUS = "ru";
  }

  public class Singers {
    public static final short NUMBER_OF = 32; /// initial number of singers
    public static final short GENRES_STR_LEN = 96; /// initial size of 'genres' list size in String
    public static final short ALBUMS_TRACKS_STR_LEN = 64; /// initial size of 'albums, tracks' line
  }

  public class SavedParams {
    public static final String VENUE_NUMBER = "venueNumber";
    public static final String LATLNG = "LatLng";
  }

  public class Url {
    public static final String ENDPOINT_URL = "http://download.cdn.yandex.net/mobilization-2016/";
    public static final String ENDPOINT_ACTION_ARTISTS = "artists.json";
  }
} //class Constants



