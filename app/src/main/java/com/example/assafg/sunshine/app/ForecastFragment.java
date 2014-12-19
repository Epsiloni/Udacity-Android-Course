package com.example.assafg.sunshine.app;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

  private static final String TAG = ForecastFragment.class.getSimpleName();

  private ArrayAdapter<String> mForecastAdapter;
  private ListView mWeeklyForecastListView;

  public ForecastFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Adding this line in order for the fragment to handle menu events.
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.forecastfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();
    if (id == R.id.action_refresh) {

      new FetchWeatherTask().execute("94043");
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    //Dummy data for the ListView, represented as "Day - Weather - High/Low".
    String[] fakeForecastArray = {
        "Today - Sunny - 88/63",
        "Tomorrow - Foggy - 70/46",
        "Weds - Cloudy - 72/63",
        "Thurs - Rainy - 64/51",
        "Fri - Foggy - 70/46",
        "Sat - Sunny - 76/68",
    };
    List<String> weekForecast = new ArrayList<>(Arrays.asList(fakeForecastArray));

    mForecastAdapter = new ArrayAdapter<>(getActivity(),
        R.layout.list_item_forecast,
        R.id.list_item_forecast_textview,
        weekForecast);

    mWeeklyForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
    mWeeklyForecastListView.setAdapter(mForecastAdapter);

    return rootView;
  }

  public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String TAG = FetchWeatherTask.class.getSimpleName();

    private static final String QUERY_MODE = "json";
    private static final String QUERY_UNITS = "metric";
    private static final int QUERY_DAYS = 7;


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
      // Because the API returns a unix timestamp (measured in seconds),
      // it must be converted to milliseconds in order to be converted to valid date.
      Date date = new Date(time * 1000);
      SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
      return format.format(date);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
      // For presentation, assume the user doesn't care about tenths of a degree.
      long roundedHigh = Math.round(high);
      long roundedLow = Math.round(low);

      String highLowStr = roundedHigh + "/" + roundedLow;
      return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
        throws JSONException {

      // These are the names of the JSON objects that need to be extracted.
      final String OWM_LIST = "list";
      final String OWM_WEATHER = "weather";
      final String OWM_TEMPERATURE = "temp";
      final String OWM_MAX = "max";
      final String OWM_MIN = "min";
      final String OWM_DATETIME = "dt";
      final String OWM_DESCRIPTION = "main";

      JSONObject forecastJson = new JSONObject(forecastJsonStr);
      JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

      String[] resultStrs = new String[numDays];
      for(int i = 0; i < weatherArray.length(); i++) {
        // For now, using the format "Day, description, hi/low"
        String day;
        String description;
        String highAndLow;

        // Get the JSON object representing the day
        JSONObject dayForecast = weatherArray.getJSONObject(i);

        // The date/time is returned as a long.  We need to convert that
        // into something human-readable, since most people won't read "1400356800" as
        // "this saturday".
        long dateTime = dayForecast.getLong(OWM_DATETIME);
        day = getReadableDateString(dateTime);

        // description is in a child array called "weather", which is 1 element long.
        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
        description = weatherObject.getString(OWM_DESCRIPTION);

        // Temperatures are in a child object called "temp".  Try not to name variables
        // "temp" when working with temperature.  It confuses everybody.
        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        double high = temperatureObject.getDouble(OWM_MAX);
        double low = temperatureObject.getDouble(OWM_MIN);

        highAndLow = formatHighLows(high, low);
        resultStrs[i] = day + " - " + description + " - " + highAndLow;
      }

      return resultStrs;
    }

    @Override
    protected String[] doInBackground(String... params) {

      // Verify that we got a param.
      if (params.length == 0) {
        return null;
      }

      // These two need to be declared outside the try/catch
      // so that they can be closed in the finally block.
      HttpURLConnection urlConnection = null;
      BufferedReader reader = null;
      String postalCode = params[0];

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
            .appendQueryParameter("q", postalCode)
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

        return getWeatherDataFromJson(forecastJsonStr, QUERY_DAYS);

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

    @Override
    protected void onPostExecute(String[] strings) {

      mForecastAdapter.clear();
      mForecastAdapter.addAll(Arrays.asList(strings));
    }
  }
}
