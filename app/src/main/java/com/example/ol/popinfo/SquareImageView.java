package com.example.ol.popinfo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by ol on 8/15/16.
 */
public class SquareImageView extends ImageView {

  public SquareImageView(Context context) {
    super(context);
  }

  public SquareImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
/*
  public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }
*/
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    int size = width > height ? width : height; /// set MAXIMAL
    setMeasuredDimension(size, size);
  }
}
