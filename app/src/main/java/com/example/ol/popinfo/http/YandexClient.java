package com.example.ol.popinfo.http;

/**
 * Created by ol on 07.04.16.
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Informing;
import com.example.ol.popinfo.Interfaces;
import com.example.ol.popinfo.R;
import com.example.ol.popinfo.http.dto.SingerDto;


/**
 * class for Retrofit operation with Yandex HTTP server
 * requests for singers list
 */
public class YandexClient implements Interfaces.OnSingerListRequestProcessor {
  //for logging
  private static final String LOG_TAG = YandexClient.class.getName();

  private FragmentManager mFM;
  private YandexApi.Api mApi;

  /// singers update processor interface
  private Interfaces.OnSingerListUpdateProcessor mListUpdateProcessor = null;

  public YandexClient(Interfaces.OnSingerListUpdateProcessor listUpdateProcessor,
                      FragmentManager fm) {
    mFM = fm;
    mListUpdateProcessor = listUpdateProcessor;
    mApi = YandexApi.getApi();
  }

  public void setFragmentManager(FragmentManager newFM) {
    mFM = newFM;
  }

  @Override
  public void listRequest(Context context) {

    final ProgressDialog dialog = ProgressDialog.show(context, "",
        context.getString(R.string.dlgGettingSingersList), false, false);

    Call<List<SingerDto>> call = mApi.getArtists();

    /// make asynchronous request for singers list
    call.enqueue(new Callback<List<SingerDto>>() {
      @Override
      public void onResponse(Call<List<SingerDto>> call, Response<List<SingerDto>> response) {
        dialog.dismiss();
        if (response.isSuccessful()) { /// request successful (status code 200, 201)
          /// get original DTO singers list from Yandex & create work singers list based on DTO one
          List<Singer> singerList = new ArrayList<>(response.body().size());
          Singer singer;
          for (SingerDto singerDto : response.body()) {
            singer = new Singer(singerDto); /// conversion DTO => model
            singerList.add(singer);
          }
          Log.d(LOG_TAG, "Got new [" + singerList.size() + "] singers");

          if (null != mListUpdateProcessor)
            mListUpdateProcessor.listUpdate(singerList);

        } else {
          /// response received but request not successful (4xx client HTTP error)
          Log.w(LOG_TAG, "Got unsuccessful HTTP response, code: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<List<SingerDto>> call, Throwable t) {
        dialog.dismiss();
        /// it's a network crash!
        Informing.ServiceFailedDialogFragment sfDialogFragment =
            Informing.ServiceFailedDialogFragment.newInstance(
                R.string.dlgHTTPServiceFailedTitle,
                R.string.dlgHTTPServiceFailedMessage,
                R.drawable.ic_sync_problem_white_36dp);
        sfDialogFragment.show(mFM, "dialog");
        Log.w(LOG_TAG, "Got FAILURE while getting singers list: " + t.getMessage());
      }
    });
  }

}

