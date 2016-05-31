package com.example.ol.popinfo.json;

import android.content.Context;

import com.example.ol.popinfo.Constants;
import com.example.ol.popinfo.Singers.Singer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ol on 20.05.16.
 */
public class JsonSerializer {
  private Context mContext;
  private String mFilename;

  public JsonSerializer(Context context, String filename) {
    mContext = context;
    mFilename = filename;
  }

  public void saveSingers(List<Singer> singers) throws JSONException, IOException {

    /// Build an array in JSON
    JSONArray array = new JSONArray();
    for (Singer s : singers)
      array.put(s.toJSON());

    /// Write the file to disk
    Writer writer = null;
    try {
      OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
      writer = new OutputStreamWriter(out);
      writer.write(array.toString());
    } finally {
      if (writer != null)
        writer.close();
    }
  }

  public List<Singer> loadSingers() throws IOException, JSONException {
    List<Singer> Singers = new ArrayList<>(Constants.Singers.NUMBER_OF_FAV);
    BufferedReader reader = null;

    try {
      /// Open and read the file into a StringBuilder
      InputStream in = mContext.openFileInput(mFilename);
      reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder jsonString = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        /// Line breaks are omitted and irrelevant
        jsonString.append(line);
      }

      /// Parse the JSON using JSONTokener
      JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

      /// Build the array of Singers from JSONObjects
      for (int i = 0; i < array.length(); i++) {
        Singers.add(new Singer(array.getJSONObject(i)));
      }
    } catch (FileNotFoundException e) {
    /// Ignore this one; it happens when starting fresh
    } finally {
      if (reader != null)
        reader.close();
    }
    return Singers;
  }
}
