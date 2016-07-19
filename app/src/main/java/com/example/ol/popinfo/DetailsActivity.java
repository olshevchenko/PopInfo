package com.example.ol.popinfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ol.popinfo.Images.ImagesHelper;
import com.example.ol.popinfo.Singers.Singer;
import com.example.ol.popinfo.Singers.SingerHelper;

public class DetailsActivity extends AppCompatActivity {

  private SingerHelper mSingerHelper;
  private Singer mDetailedSinger;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);
    DetailsFragment detailsFragment = (DetailsFragment)
        getSupportFragmentManager().findFragmentById(R.id.fragmentDetails);

    mSingerHelper = SingerHelper.getInstance(getApplicationContext());
    GlobalStorage globalStorage = (GlobalStorage)getApplicationContext();
    mDetailedSinger = globalStorage.getDetailedSinger();
    detailsFragment.setDetailedSinger(mDetailedSinger);
  }
}

