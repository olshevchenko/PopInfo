package com.example.ol.popinfo.Singers;

/**
 * Created by ol on 07.04.16.
 */

import com.example.ol.popinfo.Constants;
import com.example.ol.popinfo.http.dto.SingerDto;

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

    private Data(int id, String name, String genres, int tracks, int albums) {
      this.id = id;
      this.name = name;
      this.genres = genres;
      this.tracks = tracks;
      this.albums = albums;
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
  }

  /**
   * header interface
   * the most of the fields are just reference to the original one (in the Data)
   */
  public class Header {
    private Data data;
    private String coverSmall;

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
        singerDto.getTracks(), singerDto.getAlbums());

    ///ToDo CHECK behaviour
    this.header = new Header(); /// use default constructor
    header.data = this.data; /// use the reference
    header.coverSmall =  singerDto.getCover().getSmall();

    this.details = new Details(); /// default constructor
    details.data = this.data; /// the reference
    details.link = singerDto.getLink();
    details.description = singerDto.getDescription();
    details.coverBig = singerDto.getCover().getBig();
  }

  public Header getHeader() {
    return header;
  }

  public Details getDetails() {
    return details;
  }
}









