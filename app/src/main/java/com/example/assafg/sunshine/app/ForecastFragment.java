package com.example.assafg.sunshine.app;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
  public static final int INVALID_POSITION = -1;
  public static final String SELECTED_POSITION = "SELECTED_POSITION";
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
      LocationEntry.COLUMN_LOCATION_SETTING,
      WeatherEntry.COLUMN_WEATHER_ID,
      LocationEntry.COLUMN_COORD_LAT,
      LocationEntry.COLUMN_COORD_LONG
  };


  // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
  // must change.
  public static final int COL_WEATHER_ID = 0;
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  public static final int COL_LOCATION_SETTING = 5;
  public static final int COL_WEATHER_CONDITION_ID = 6;
  public static final int COL_COORD_LAT = 7;
  public static final int COL_COORD_LONG = 8;

  private ForecastAdapter mForecastAdapter;
  private ListView mWeeklyForecastListView;
  private int mSelectedPosition = INVALID_POSITION;
  private boolean mTwoPane;

  public void setShouldUseTodayLayout(boolean twoPane) {

    mTwoPane = twoPane;
    if (mForecastAdapter != null) {
      mForecastAdapter.setShouldUseTodayLayout(twoPane);
    }
  }

  /**
   * A callback interface that all activities containing this fragment must
   * implement. This mechanism allows activities to be notified of item
   * selections.
   */
  public interface Callback {
    /**
     * Callback for when an item has been selected.
     */
    public void onItemSelected(String date);
  }

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
    if (id == R.id.action_map) {
      openPreferredLocationInMap();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void openPreferredLocationInMap() {
    // Using the URI scheme for showing a location found on a map.  This super-handy
    // intent can is detailed in the "Common Intents" page of Android's developer site:
    // http://developer.android.com/guide/components/intents-common.html#Maps
    if ( null != mForecastAdapter ) {
      Cursor c = mForecastAdapter.getCursor();
      if ( null != c ) {
        c.moveToPosition(0);
        String posLat = c.getString(COL_COORD_LAT);
        String posLong = c.getString(COL_COORD_LONG);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
          startActivity(intent);
        } else {
          Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
      }

    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    // The SimpleCursorAdapter will take data from the database through the
    // Loader and use it to populate the ListView it's attached to.
    mForecastAdapter = new ForecastAdapter(
        getActivity(),
        null,
        0);

    mForecastAdapter.setShouldUseTodayLayout(mTwoPane);

    mWeeklyForecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
    mWeeklyForecastListView.setAdapter(mForecastAdapter);

    mWeeklyForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mSelectedPosition = position;
        ForecastAdapter adapter = (ForecastAdapter)parent.getAdapter();
        Cursor cursor = adapter.getCursor();

        if (cursor != null && cursor.moveToPosition(position)) {
          String date = cursor.getString(COL_WEATHER_DATE);

          ((Callback)getActivity()).onItemSelected(date);
        }
      }
    });

    if (savedInstanceState != null) {
      mSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION, INVALID_POSITION);
    }
    return rootView;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(SELECTED_POSITION, mSelectedPosition);
    super.onSaveInstanceState(outState);
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

    if (mSelectedPosition != INVALID_POSITION) {
      mWeeklyForecastListView.smoothScrollToPosition(mSelectedPosition);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mForecastAdapter.swapCursor(null);
  }
}
