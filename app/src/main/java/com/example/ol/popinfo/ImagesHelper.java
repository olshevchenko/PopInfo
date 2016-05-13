package com.example.ol.popinfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by ol on 05.05.16.
 */
public class ImagesHelper {

  /// for logging
  private static final String LOG_TAG = RecyclerAdapter.class.getName();

  private LruCache<String, Bitmap> mMemoryCache;

  private static ImagesHelper sImagesHelper = new ImagesHelper();

  public static ImagesHelper getInstance() {
    return sImagesHelper;
  }

  private ImagesHelper() {

    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than
        // number of items.
        return bitmap.getByteCount() / 1024;
      }
    };
  }

  private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      mMemoryCache.put(key, bitmap);
    }
  }

  private Bitmap getBitmapFromMemCache(String key) {
    return mMemoryCache.get(key);
  }


  public void setImageBitmap(String imageUrl, ImageView imageView) {
    String imageKey = String.valueOf(imageUrl.hashCode());

    Bitmap bitmap = getBitmapFromMemCache(imageKey);
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap);
    } else {
      imageView.setImageResource(R.drawable.ic_photo_white_36dp);
      BitmapWorkerTask task = new BitmapWorkerTask(imageKey, imageView);
      task.execute(imageUrl, imageKey);
    }
  }


  // Decode image in background.
  class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView mImageView;
    private String mImageKey;

    BitmapWorkerTask(String imageKey, ImageView imageView) {
      this.mImageKey = imageKey;
      this.mImageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
      String url = urls[0];
      Bitmap bitmap = null;
      try {
        Log.d(LOG_TAG, "Going open + decode Stream for url: " + url);
        InputStream in = new java.net.URL(url).openStream();
        bitmap = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error", e.getMessage());
        e.printStackTrace();
      }
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      Log.d(LOG_TAG, "Going set image '" + result + "' and store to cache with key: " + mImageKey);
      mImageView.setImageBitmap(result);
      addBitmapToMemoryCache(mImageKey, result);
    }
  }

}
