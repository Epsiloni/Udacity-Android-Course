package com.example.assafg.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.assafg.sunshine.app.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {

  private static final String TAG = MainActivity.class.getSimpleName();
  private boolean mTwoPane;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (findViewById(R.id.weather_detail_container) != null) {
      // If this is present then the activity should be in two-pane mode.

      mTwoPane = true;

      // In two-pane mode, show the detail view in this activity by adding or replacing
      // the detail fragment using a fragment transaction.
      if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.weather_detail_container, new DetailsFragment())
            .commit();
      }
    } else {
      mTwoPane = false;
    }

    ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
    fragment.setShouldUseTodayLayout(!mTwoPane);

    // Make sure we've gotten an account created and we're syncing.
    SunshineSyncAdapter.initializeSyncAdapter(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onItemSelected(String date) {

    if (mTwoPane) {

      DetailsFragment detailsFragment = new DetailsFragment();

      Bundle b = new Bundle();
      b.putString(DetailActivity.DATE_KEY, date);
      detailsFragment.setArguments(b);

      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.weather_detail_container, detailsFragment)
          .commit();
    } else {
      Intent i = new Intent(this, DetailActivity.class);
      i.putExtra(DetailActivity.DATE_KEY, date);
      startActivity(i);
    }
  }
}
