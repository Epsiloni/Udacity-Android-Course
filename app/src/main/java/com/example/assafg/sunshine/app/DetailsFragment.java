package com.example.assafg.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assafg.sunshine.app.Utility.Utility;
import com.example.assafg.sunshine.app.data.WeatherContract;

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final int DETAILS_LOADER = 1;
  public static final String SUNSHINE_APP_HASHTAG = "#SunshineApp";
  private TextView mDayTextView;
  private TextView mDescTextView;
  private TextView mMaxTextView;
  private TextView mMinTextView;
  private String mForecast;
  private ShareActionProvider mShareActionProvider;

  // For the forecast view we're showing only a small subset of the stored data.
  // Specify the columns we need.
  private static final String[] FORECAST_COLUMNS = {
      // In this case the id needs to be fully qualified with a table name, since
      // the content provider joins the location & weather tables in the background
      // (both have an _id column)
      // On the one hand, that's annoying.  On the other, you can search the weather table
      // using the location set by the user, which is only in the Location table.
      // So the convenience is worth it.
      WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
      WeatherContract.WeatherEntry.COLUMN_DATETEXT,
      WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
      WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
      WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
      WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
      WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
      WeatherContract.WeatherEntry.COLUMN_DEGREES,
      WeatherContract.WeatherEntry.COLUMN_PRESSURE,
      WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
      WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
  };

  // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
  // must change.
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  public static final int COL_WEATHER_HUMIDITY = 5;
  public static final int COL_WEATHER_WIND_SPEED = 6;
  public static final int COL_WEATHER_WIND_DEGREES = 7;
  public static final int COL_WEATHER_PRESSURE = 8;
  public static final int COL_WEATHER_ID = 9;

  private TextView mHumidityTextView;
  private TextView mWindTextView;
  private TextView mPressureTextView;
  private TextView mDateTextView;
  private ImageView mDescIcon;

  public DetailsFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

    getLoaderManager().initLoader(DETAILS_LOADER, getActivity().getIntent().getExtras(), this);

    mDayTextView = (TextView) rootView.findViewById(R.id.detail_activity_day);
    mDateTextView = (TextView) rootView.findViewById(R.id.detail_activity_date);
    mDescIcon = (ImageView) rootView.findViewById(R.id.detail_activity_icon);
    mDescTextView = (TextView) rootView.findViewById(R.id.detail_activity_desc);
    mMaxTextView = (TextView) rootView.findViewById(R.id.detail_activity_max);
    mMinTextView = (TextView) rootView.findViewById(R.id.detail_activity_min);
    mHumidityTextView = (TextView) rootView.findViewById(R.id.detail_activity_humidity);
    mWindTextView = (TextView) rootView.findViewById(R.id.detail_activity_wind);
    mPressureTextView = (TextView) rootView.findViewById(R.id.detail_activity_pressure);
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.details_fragment, menu);

    // Retrieve the share menu item
    MenuItem menuItem = menu.findItem(R.id.action_share);

    // Get the provider and hold onto it to set/change the share intent.
    mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

    // If onLoadFinished happens before this, we can go ahead and set the share intent now.
    if (mForecast != null) {
      mShareActionProvider.setShareIntent(createShareForecastIntent());
    }
  }

  private Intent createShareForecastIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + SUNSHINE_APP_HASHTAG);
    return shareIntent;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {

    String location = Utility.getPreferredLocation(getActivity());
    Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
        location, args.getString(Intent.EXTRA_TEXT));
    Loader<Cursor> loader = new CursorLoader(
        getActivity(),
        weatherForLocationUri,
        FORECAST_COLUMNS,
        null,
        null,
        null
    );

    return loader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (data != null && data.moveToFirst()) {
      boolean metric = Utility.isMetric(getActivity());

      int weatherId = data.getInt(COL_WEATHER_ID);
      mDescIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

      String dateString = data.getString(COL_WEATHER_DATE);
      mDayTextView.setText(Utility.getDayName(getActivity(), dateString));
      mDateTextView.setText(Utility.getFormattedMonthDay(getActivity(), dateString));
      String weatherDescription = data.getString(COL_WEATHER_DESC);
      mDescTextView.setText(weatherDescription);
      String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), metric);
      mMaxTextView.setText(high);
      String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), metric);
      mMinTextView.setText(low);
      float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
      mHumidityTextView.setText(getString(R.string.format_humidity, humidity));
      float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
      float windDegrees = data.getFloat(COL_WEATHER_WIND_DEGREES);
      mWindTextView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDegrees));
      float pressure = data.getFloat(COL_WEATHER_PRESSURE);
      mPressureTextView.setText(getString(R.string.format_pressure, pressure));


      // We still need this for the share intent
      mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

      // If onCreateOptionsMenu has already happened, we need to update the share intent now.
      if (mShareActionProvider != null) {
        mShareActionProvider.setShareIntent(createShareForecastIntent());
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    loader.reset();
  }
}
