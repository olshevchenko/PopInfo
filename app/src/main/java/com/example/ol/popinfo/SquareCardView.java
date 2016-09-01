package com.example.ol.popinfo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ol on 8/15/16.
 */
public class SquareCardView extends CardView {

  public SquareCardView(Context context) {
    super(context);
  }

  public SquareCardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    int size = width > height ? width : height; /// set MAXIMAL
//    int size = width > height ? height : width; /// set MINIMAL
    setMeasuredDimension(size, size);
  }
}
