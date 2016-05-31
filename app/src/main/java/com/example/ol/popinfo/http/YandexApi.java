package com.example.ol.popinfo.http;

/**
 * Created by ol on 07.04.16.
 */

import com.example.ol.popinfo.Constants;
import com.example.ol.popinfo.http.dto.SingerDto;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * describes Retrofit annotations for 'search' venues response & programming request for it
 */
public class YandexApi {
  //for logging
  private static final String LOG_TAG = YandexApi.class.getName();

  /**
   * URL example:
   * download.cdn.yandex.net/mobilization-2016/artists.json
   */
  public interface Api {
    @GET(Constants.Url.ENDPOINT_ACTION_ARTISTS)
    Call<List<SingerDto>> getArtists();
  }

  private static Api sApi = null;

  private static Retrofit providesRetrofitClient(String baseUrl) {
    /// 4 Retrofit 2.x requests logging
//    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//    httpClient.addInterceptor(logging);

    Retrofit client = new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient.build())
        .build();
    return client;
  }


  private static void initApi(){
    if (null == sApi) {
      Retrofit client = providesRetrofitClient(Constants.Url.ENDPOINT_URL);
      sApi = client.create(Api.class);
    }
  }

  public static void releaseApi(){
    if (null != sApi) {
//      ;
      sApi = null;
    }
  }

  public static Api getApi() {
    if (null == sApi)
      initApi();
    return sApi;
  }

}
