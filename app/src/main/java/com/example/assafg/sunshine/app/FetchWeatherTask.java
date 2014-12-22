package com.example.assafg.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.assafg.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

/**
 *
 * Created by Enigma on 12/20/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

  private final String TAG = FetchWeatherTask.class.getSimpleName();

  private static final String QUERY_MODE = "json";
  private static final String QUERY_UNITS = "metric";
  private static final int QUERY_DAYS = 14;

  private final Context mContext;

  public FetchWeatherTask(Context context) {
    mContext = context;
  }

  /**
   * Take the String representing the complete forecast in JSON Format and
   * pull out the data we need to construct the Strings needed for the wireframes.
   *
   * Fortunately parsing is easy:  constructor takes the JSON string and converts it
   * into an Object hierarchy for us.
   */
  private void getWeatherDataFromJson(String forecastJsonStr, int numDays,
                                          String locationSetting)
      throws JSONException {

    // These are the names of the JSON objects that need to be extracted.

    // Location information
    final String OWM_CITY = "city";
    final String OWM_CITY_NAME = "name";
    final String OWM_COORD = "coord";
    final String OWM_COORD_LAT = "lat";
    final String OWM_COORD_LONG = "lon";

    // Weather information.  Each day's forecast info is an element of the "list" array.
    final String OWM_LIST = "list";

    final String OWM_DATETIME = "dt";
    final String OWM_PRESSURE = "pressure";
    final String OWM_HUMIDITY = "humidity";
    final String OWM_WINDSPEED = "speed";
    final String OWM_WIND_DIRECTION = "deg";

    // All temperatures are children of the "temp" object.
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";

    final String OWM_WEATHER = "weather";
    final String OWM_DESCRIPTION = "main";
    final String OWM_WEATHER_ID = "id";

    JSONObject forecastJson = new JSONObject(forecastJsonStr);
    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

    JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
    String cityName = cityJson.getString(OWM_CITY_NAME);
    JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
    double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
    double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

    // Insert the location into the database.
    // The function referenced here is not yet implemented, so we've commented it out for now.
    long locationID = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

    // Get and insert the new weather information into the database
    Vector<ContentValues> cVVector = new Vector<>(weatherArray.length());

    for(int i = 0; i < weatherArray.length(); i++) {
      // These are the values that will be collected.

      long dateTime;
      double pressure;
      int humidity;
      double windSpeed;
      double windDirection;

      double high;
      double low;

      String description;
      int weatherId;

      // Get the JSON object representing the day
      JSONObject dayForecast = weatherArray.getJSONObject(i);

      // The date/time is returned as a long.  We need to convert that
      // into something human-readable, since most people won't read "1400356800" as
      // "this saturday".
      dateTime = dayForecast.getLong(OWM_DATETIME);

      pressure = dayForecast.getDouble(OWM_PRESSURE);
      humidity = dayForecast.getInt(OWM_HUMIDITY);
      windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
      windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

      // Description is in a child array called "weather", which is 1 element long.
      // That element also contains a weather code.
      JSONObject weatherObject =
          dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
      description = weatherObject.getString(OWM_DESCRIPTION);
      weatherId = weatherObject.getInt(OWM_WEATHER_ID);

      // Temperatures are in a child object called "temp".  Try not to name variables
      // "temp" when working with temperature.  It confuses everybody.
      JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
      high = temperatureObject.getDouble(OWM_MAX);
      low = temperatureObject.getDouble(OWM_MIN);

      ContentValues weatherValues = new ContentValues();

      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationID);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
          WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
      weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

      cVVector.add(weatherValues);
    }

    if (cVVector.size() > 0) {
      ContentValues[] cvArray = new ContentValues[cVVector.size()];
      cVVector.toArray(cvArray);
      mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
    }
  }

  private long addLocation(String locationSetting, String cityName, double cityLatitude, double cityLongitude) {

    Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
        new String[]{WeatherContract.LocationEntry._ID},
        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
        new String[]{locationSetting},
        null);

    if (cursor.moveToFirst()) {
      int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
      return cursor.getLong(locationIdIndex);
    } else {
      ContentValues locationValues = new ContentValues();
      locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, cityLatitude);
      locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, cityLongitude);

      Uri locationInsertUri = mContext.getContentResolver()
          .insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);

      return ContentUris.parseId(locationInsertUri);
    }
  }

  @Override
  protected Void doInBackground(String... params) {

    // Verify that we got a param.
    if (params.length == 0) {
      return null;
    }

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String locationQuery = params[0];

    // Will contain the raw JSON response as a string.
    String forecastJsonStr = null;

    try {
      // Construct the URL for the OpenWeatherMap query
      // Possible parameters are available at OWM's forecast API page, at
      // http://openweathermap.org/API#forecast

      Uri.Builder builder = new Uri.Builder();
      builder.scheme("http")
          .authority("api.openweathermap.org")
          .path("data/2.5/forecast/daily")
          .appendQueryParameter("q", locationQuery)
          .appendQueryParameter("mode", QUERY_MODE)
          .appendQueryParameter("units", QUERY_UNITS)
          .appendQueryParameter("cnt", Integer.toString(QUERY_DAYS));
      String queryURL = builder.build().toString();
      URL url = new URL(queryURL);

      // Create the request to OpenWeatherMap, and open the connection
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      StringBuffer buffer = new StringBuffer();
      if (inputStream == null) {
        // Nothing to do.
        return null;
      }
      reader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      while ((line = reader.readLine()) != null) {
        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
        // But it does make debugging a *lot* easier if you print out the completed
        // buffer for debugging.
        buffer.append(line + "\n");
      }

      if (buffer.length() == 0) {
        // Stream was empty.  No point in parsing.
        return null;
      }

      forecastJsonStr = buffer.toString();

      getWeatherDataFromJson(forecastJsonStr, QUERY_DAYS, locationQuery);

    } catch (IOException e) {
      Log.e(TAG, "Error ", e);

    } catch (JSONException e) {
      Log.e(TAG, e.getMessage(), e);

    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(TAG, "Error closing stream", e);
        }
      }
    }

    return null;
  }
}
