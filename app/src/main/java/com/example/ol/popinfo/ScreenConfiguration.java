package com.example.ol.popinfo;

import android.content.res.Configuration;

/**
 * Created by ol on 7/27/16.
 */
public class ScreenConfiguration {

  private static Constants.ScreenConfigurationState sConfigurationState =
      Constants.ScreenConfigurationState.UNDEF;

  public static void setScreenConfigurationState(Configuration cfg) {
    if (Configuration.ORIENTATION_LANDSCAPE == cfg.orientation) {
      if (Configuration.SCREENLAYOUT_SIZE_LARGE ==
          (cfg.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK))
        sConfigurationState = Constants.ScreenConfigurationState.TABLET_LANDSCAPE;
      else
        sConfigurationState = Constants.ScreenConfigurationState.PHONE_LANDSCAPE;
    }
    else { /// portrait
      if (Configuration.SCREENLAYOUT_SIZE_LARGE ==
          (cfg.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK))
        sConfigurationState = Constants.ScreenConfigurationState.TABLET_PORTRAIT;
      else
        sConfigurationState = Constants.ScreenConfigurationState.PHONE_PORTRAIT;
    }
  }

  public static Constants.ScreenConfigurationState getScreenConfigurationState() {
    return sConfigurationState;
  }
}
