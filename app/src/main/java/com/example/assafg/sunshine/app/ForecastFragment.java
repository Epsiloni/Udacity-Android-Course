package com.example.assafg.sunshine.app;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.assafg.sunshine.app.Utility.Utility;
import com.example.assafg.sunshine.app.data.WeatherContract;
import com.example.assafg.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.assafg.sunshine.app.data.WeatherContract.WeatherEntry;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String TAG = ForecastFragment.class.getSimpleName();

  private static final int FORECAST_LOADER = 0;
  private String mLocation;

  // For the forecast view we're showing only a small subset of the stored data.
  // Specify the columns we need.
  private static final String[] FORECAST_COLUMNS = {
      // In this case the id needs to be fully qualified with a table name, since
      // the content provider joins the location & weather tables in the background
      // (both have an _id column)
      // On the one hand, that's annoying.  On the other, you can search the weather table
      // using the location set by the user, which is only in the Location table.
      // So the convenience is worth it.
      WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
      WeatherEntry.COLUMN_DATETEXT,
      WeatherEntry.COLUMN_SHORT_DESC,
      WeatherEntry.COLUMN_MAX_TEMP,
      WeatherEntry.COLUMN_MIN_TEMP,
      LocationEntry.COLUMN_LOCATION_SETTING
  };

  // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
  // must change.
  public static final int COL_WEATHER_ID = 0;
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  public static final int COL_LOCATION_SETTING = 5;

  private SimpleCursorAdapter mForecastAdapter;
  private ListView mWeeklyForecastListView;

  public ForecastFragment() {
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Adding this line in order for the fragment to handle menu events.
    setHasOptionsMenu(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
      getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.forecastfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      updateWeather();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateWeather() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String location = Utility.getPreferredLocation(getActivity());

    String unit = preferences.getString(getString(R.string.pref_units_key),
        getString(R.string.pref_units_default));

    new FetchWeatherTask(getActivity()).execute(location, unit);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    // The SimpleCursorAdapter will take data from the database through the
    // Loader and use it to populate the ListView it's attached to.
    mForecastAdapter = new SimpleCursorAdapter(
        getActivity(),
        R.layout.list_item_forecast,
        null,
        // the column names to use to fill the textviews
        new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        },
        // the textviews to fill with the data pulled from the columns above
        new int[]{R.id.list_item_date_textview,
            R.id.list_item_forecast_textview,
            R.id.list_item_high_textview,
            R.id.list_item_low_textview
        },
        0);

    mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
      @Override
      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        boolean isMetric = Utility.isMetric(getActivity());
        switch (columnIndex) {
          case COL_WEATHER_MAX_TEMP:
          case COL_WEATHER_MIN_TEMP: {
            // we have to do some formatting and possibly a conversion
            ((TextView) view).setText(Utility.formatTemperature(
                cursor.getDouble(columnIndex), isMetric));
            return true;
          }
          case COL_WEATHER_DATE: {
            String dateString = cursor.getString(columnIndex);
            TextView dateView = (TextView) view;
            dateView.setText(Utility.formatDate(dateString));
            return true;
          }
        }
        return false;
      }
    });

    mWeeklyForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
    mWeeklyForecastListView.setAdapter(mForecastAdapter);

    mWeeklyForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SimpleCursorAdapter adapter = (SimpleCursorAdapter)parent.getAdapter();
        Cursor cursor = adapter.getCursor();

        if (cursor != null && cursor.moveToPosition(position)) {
          String date = cursor.getString(COL_WEATHER_DATE);
          Intent i = new Intent(getActivity(), DetailActivity.class);
          i.putExtra(Intent.EXTRA_TEXT, date);
          startActivity(i);
        }
      }
    });

    return rootView;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // This is called when a new Loader needs to be created.  This
    // fragment only uses one loader, so we don't care about checking the id.

    // To only show current and future dates, get the String representation for today,
    // and filter the query to return weather only for dates after or including today.
    // Only return data after today.
    String startDate = WeatherContract.getDbDateString(new Date());

    // Sort order:  Ascending, by date.
    String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

    mLocation = Utility.getPreferredLocation(getActivity());
    Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
        mLocation, startDate);

    // Now create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed.
    return new CursorLoader(
        getActivity(),
        weatherForLocationUri,
        FORECAST_COLUMNS,
        null,
        null,
        sortOrder
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mForecastAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mForecastAdapter.swapCursor(null);
  }
}
