package com.example.ol.popinfo.http.dto;

/**
 * Created by ol on 07.04.16.
 */

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Fragment of the json response from
 * [download.cdn.yandex.net/mobilization-2016/artists.json]
 *
 * [
 *  {
 *    "id":1080505,
 *    "name":"Tove Lo",
 *    "genres":["pop","dance","electronics"],
 *    "tracks":81,
 *    "albums":22,
 *    "link":"http://www.tove-lo.com/",
 *    "description":"шведская певица и автор песен. Она привлекла к себе внимание в 2013 году
 *      с выпуском сингла «Habits», но настоящего успеха добилась с ремиксом хип-хоп продюсера
 *      Hippie Sabotage на эту песню, который получил название «Stay High». 4 марта 2014 года
 *      вышел её дебютный мини-альбом Truth Serum, а 24 сентября этого же года дебютный студийный
 *      альбом Queen of the Clouds. Туве Лу является автором песен таких артистов, как Icona Pop,
 *      Girls Aloud и Шер Ллойд."
 *    "cover":
 *    {
 *      "small":"http://avatars.yandex.net/get-music-content/dfc531f5.p.1080505/300x300",
 *      "big":"http://avatars.yandex.net/get-music-content/dfc531f5.p.1080505/1000x1000"
 *    }
 *  }
 *  ...
 * ]
 */
public class SingerDto implements Parcelable {
  private int id;
  private String name;
  private List<String> genres;
  private int tracks;
  private int albums;
  private String link;
  private String description;
  private CoverDto cover;

  public SingerDto(int id, String name, List<String> genres, int tracks, int albums, String link, String description, CoverDto cover) {
    this.id = id;
    this.name = name;
    this.genres = genres;
    this.tracks = tracks;
    this.albums = albums;
    this.link = link;
    this.description = description;
    this.cover = cover;
  }

  public SingerDto(Parcel in) {
    id = in.readInt();
    name = in.readString();
    in.readStringList(genres);
    tracks = in.readInt();
    albums = in.readInt();
    link = in.readString();
    description = in.readString();
    try {
      cover = in.readParcelable(null);
    }
    catch (BadParcelableException ex) {
      /// just ignore images attached
      cover = new CoverDto("", "");
    }

  }

  public static final Parcelable.Creator<SingerDto> CREATOR
      = new Parcelable.Creator<SingerDto>() {
    public SingerDto createFromParcel(Parcel in) {
      return new SingerDto(in);
    }

    public SingerDto[] newArray(int size) {
      return new SingerDto[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(id);
    out.writeString(name);
    out.writeStringList(genres);
    out.writeInt(tracks);
    out.writeInt(albums);
    out.writeString(link);
    out.writeString(description);
    out.writeParcelable(cover, flags);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public int getTracks() {
    return tracks;
  }

  public void setTracks(int tracks) {
    this.tracks = tracks;
  }

  public int getAlbums() {
    return albums;
  }

  public void setAlbums(int albums) {
    this.albums = albums;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CoverDto getCover() {
    return cover;
  }

  public void setCover(CoverDto cover) {
    this.cover = cover;
  }

}









