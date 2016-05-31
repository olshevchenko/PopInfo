package com.example.ol.popinfo.Singers;

/**
 * Created by ol on 07.04.16.
 */

import com.example.ol.popinfo.Constants;
import com.example.ol.popinfo.http.dto.SingerDto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

/**
 * main singer's data
 */
public class Singer {
  //for logging
  private static final String LOG_TAG = Singer.class.getName();

  private Data data;
  private Header header;
  private Details details;

  /**
   * original data fields
   */
  public class Data {
    private int id;
    private String name;
    private String genres;
    private int tracks;
    private int albums;
    private int rating;

    private Data(int id, String name, String genres, int tracks, int albums, int rating) {
      this.id = id;
      this.name = name;
      this.genres = genres;
      this.tracks = tracks;
      this.albums = albums;
      this.rating = rating;
    }

    /**
     * checks equality w/O rating value
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Data data = (Data) o;

      if (id != data.id) return false;
      if (tracks != data.tracks) return false;
      if (albums != data.albums) return false;
      if (name != null ? !name.equals(data.name) : data.name != null) return false;
      return genres != null ? genres.equals(data.genres) : data.genres == null;

    }

    /**
     * evaluation w/O rating value
     */
    @Override
    public int hashCode() {
      int result = id;
      result = 31 * result + (name != null ? name.hashCode() : 0);
      result = 31 * result + (genres != null ? genres.hashCode() : 0);
      result = 31 * result + tracks;
      result = 31 * result + albums;
      return result;
    }

    public int getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getGenres() {
      return genres;
    }

    public int getTracks() {
      return tracks;
    }

    public int getAlbums() {
      return albums;
    }

    public int getRating() {
      return rating;
    }

    public void setRating(int rating) {
      this.rating = rating;
    }
  }

  /**
   * header interface
   * the most of the fields are just reference to the original one (in the Data)
   */
  public class Header {
    private Data data;
    private String coverSmall;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Header header = (Header) o;

      if (data != null ? !data.equals(header.data) : header.data != null) return false;
      return coverSmall != null ? coverSmall.equals(header.coverSmall) : header.coverSmall == null;

    }

    @Override
    public int hashCode() {
      int result = data != null ? data.hashCode() : 0;
      result = 31 * result + (coverSmall != null ? coverSmall.hashCode() : 0);
      return result;
    }

    public Data getData() {
      return data;
    }

    public String getCoverSmall() {
      return coverSmall;
    }
  }

  /**
   * details interface w. reference fields
   */
  public class Details {
    private Data data;
    private String link;
    private String description;
    private String coverBig;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Details details = (Details) o;

      if (data != null ? !data.equals(details.data) : details.data != null) return false;
      if (link != null ? !link.equals(details.link) : details.link != null) return false;
      if (description != null ? !description.equals(details.description) : details.description != null)
        return false;
      return coverBig != null ? coverBig.equals(details.coverBig) : details.coverBig == null;

    }

    @Override
    public int hashCode() {
      int result = data != null ? data.hashCode() : 0;
      result = 31 * result + (link != null ? link.hashCode() : 0);
      result = 31 * result + (description != null ? description.hashCode() : 0);
      result = 31 * result + (coverBig != null ? coverBig.hashCode() : 0);
      return result;
    }

    public Data getData() {
      return data;
    }

    public String getLink() {
      return link;
    }

    public String getDescription() {
      return description;
    }

    public String getCoverBig() {
      return coverBig;
    }
  }

  @Override
  public String toString() {
    return "{" +
        "id:" + data.id +
        ", name:" + data.name +
        ", genres:[" + data.genres +
        "], tracks:" + data.tracks +
        ", albums:" + data.albums +
        "}";
  }

  /**
   * creates instance from (deserialized local) JSON object
   * @param json
   * @throws JSONException
   */
  public Singer(JSONObject json) throws JSONException {
    int id = json.getInt(Constants.JSON.ID);
    String name = json.getString(Constants.JSON.NAME);
    String genres = json.getString(Constants.JSON.GENRES);
    int tracks = json.getInt(Constants.JSON.TRACKS);
    int albums = json.getInt(Constants.JSON.ALBUMS);
    int rating = json.getInt(Constants.JSON.RATING);

    this.data = new Data(id, name, genres, tracks, albums, rating);

    this.header = new Header();
    header.data = this.data; /// use the reference
    header.coverSmall = json.getString(Constants.JSON.COVER_SMALL);

    this.details = new Details();
    details.data = this.data; /// the reference
    details.link = json.getString(Constants.JSON.LINK);
    details.description = json.getString(Constants.JSON.DESCRIPTION);
    details.coverBig = json.getString(Constants.JSON.COVER_BIG);
  }

  /**
   *
   * @param singerDto
   */
  public Singer(SingerDto singerDto) {
    /**
     * convert genres list to united string
     */
    List<String> genresList = singerDto.getGenres();
    StringBuilder genresSB = new StringBuilder(Constants.Singers.GENRES_STR_LEN);
    for (String genre : genresList) {
      genresSB.append(genre);
      genresSB.append(", ");
    }
    int genresSBLlen = genresSB.length();
    if (genresSB.length() != 0)
      genresSB.setLength(genresSBLlen - 2); /// trim last 2-symbols ", " from the resulting chain
                                            /// IF NON-EMPTY

    this.data = new Data(singerDto.getId(), singerDto.getName(), genresSB.toString(),
        singerDto.getTracks(), singerDto.getAlbums(), Constants.Singers.DEFLT_RATING);

    this.header = new Header(); /// use default constructor
    header.data = this.data; /// use the reference
    header.coverSmall =  singerDto.getCover().getSmall();

    this.details = new Details(); /// default constructor
    details.data = this.data; /// the reference
    details.link = singerDto.getLink();
    details.description = singerDto.getDescription();
    details.coverBig = singerDto.getCover().getBig();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Singer singer = (Singer) o;

    if (data != null ? !data.equals(singer.data) : singer.data != null) return false;
    if (header != null ? !header.equals(singer.header) : singer.header != null) return false;
    return details != null ? details.equals(singer.details) : singer.details == null;
  }

  @Override
  public int hashCode() {
    int result = data != null ? data.hashCode() : 0;
    result = 31 * result + (header != null ? header.hashCode() : 0);
    result = 31 * result + (details != null ? details.hashCode() : 0);
    return result;
  }

  public Header getHeader() {
    return header;
  }

  public Details getDetails() {
    return details;
  }

  /**
   * comparator instance for sorting by id
   */
  public static Comparator<Singer> SORTED_BY_ID = new Comparator<Singer>() {

    public int compare(Singer obj1, Singer obj2) {

      int id1 = obj1.getHeader().getData().getId();
      int id2 = obj2.getHeader().getData().getId();

      return id2 - id1;
    }
  };

  /**
   * ... for sorting by name
   */
  public static Comparator<Singer> SORTED_BY_NAME = new Comparator<Singer>() {

    public int compare(Singer obj1, Singer obj2) {

      String name1 = obj1.getHeader().getData().getName();
      String name2 = obj2.getHeader().getData().getName();

      return name1.compareTo(name2);
    }
  };

  /**
   * ... for sorting by genres
   */
  public static Comparator<Singer> SORTED_BY_GENRES = new Comparator<Singer>() {

    public int compare(Singer obj1, Singer obj2) {

      String genres1 = obj1.getHeader().getData().getGenres();
      String genres2 = obj2.getHeader().getData().getGenres();

      return genres1.compareTo(genres2);
    }
  };

  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(Constants.JSON.ID, Integer.toString(data.id));
    json.put(Constants.JSON.NAME, data.name);
    json.put(Constants.JSON.GENRES, data.genres);
    json.put(Constants.JSON.TRACKS, Integer.toString(data.tracks));
    json.put(Constants.JSON.ALBUMS, Integer.toString(data.albums));
    json.put(Constants.JSON.RATING, Integer.toString(data.rating));
    json.put(Constants.JSON.COVER_SMALL, header.coverSmall);
    json.put(Constants.JSON.LINK, details.link);
    json.put(Constants.JSON.DESCRIPTION, details.description);
    json.put(Constants.JSON.COVER_BIG, details.coverBig);
    return json;
  }

}







