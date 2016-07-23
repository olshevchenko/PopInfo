package com.example.ol.popinfo;

/**
 * Constants necessary for operation
 */
public final class Constants {
  public class Languages {
    public static final String ENG = "en";
    public static final String RUS = "ru";
  }

  public class Extras {
    public static final String DETAIL_VENUE_NUMBER = "venueNumber";
  }

  public final class IO {
    public static final String SINGERS_FILENAME = "singers.json";
  }

  public class JSON {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String GENRES = "genres";
    public static final String TRACKS = "tracks";
    public static final String ALBUMS = "albums";
    public static final String RATING = "rating";
    public static final String COVER_SMALL = "small";
    public static final String LINK = "link";
    public static final String DESCRIPTION = "description";
    public static final String COVER_BIG = "big";
  }

  public class Singers {
    public static final short DEFLT_RATING = 0; /// initial singer's rating
    public static final short NUMBER_OF = 100; /// initial number of singers
    public static final short NUMBER_OF_FAV = 33; /// initial number of favorite singers
    public static final short GENRES_STR_LEN = 96; /// initial size of 'genres' list size in String
    public static final int   COVER_BIG_IMG_SIZE = 1000; /// square image size (in px)
  }

  public enum SortingState { NOT, BY_NAME, BY_GENRES }


  public class Url {
    public static final String ENDPOINT_URL = "http://download.cdn.yandex.net/mobilization-2016/";
    public static final String ENDPOINT_ACTION_ARTISTS = "artists.json";
  }

} //class Constants



