package com.example.ol.popinfo;


import com.example.ol.popinfo.Singers.Singer;

import java.util.List;

/**
 * superclass for operations abstract interfaces
 */
public class Logic {

  /**
   * interface to complete response of singers information update
   */
  public interface SingersUpdateProcessor {
    void singersUpdate(List<Singer> newList);
  }

  /**
   * interface to process request for singers information update
   */
  public interface SingersRequestInfoProcessor {
    void singersRequestInfo();
  }

}


