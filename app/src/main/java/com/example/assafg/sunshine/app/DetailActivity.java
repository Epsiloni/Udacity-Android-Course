package com.example.assafg.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivity extends ActionBarActivity {

  public static final String SUNSHINE_APP_HASHTAG = "#SunshineApp";
  private ShareActionProvider mShareActionProvider;
  private Intent mShareIntent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, new PlaceholderFragment())
          .commit();

      String shareString = getIntent().getExtras().getString(Intent.EXTRA_TEXT) + SUNSHINE_APP_HASHTAG;
      mShareIntent = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareString);
      mShareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_detail, menu);

    // Locate MenuItem with ShareActionProvider
    MenuItem item = menu.findItem(R.id.action_share);

    // Fetch and store ShareActionProvider
    mShareActionProvider = new ShareActionProvider(this);
    MenuItemCompat.setActionProvider(item, mShareActionProvider);

    setShareIntent(mShareIntent);
    return true;
  }

  // Call to update the share intent
  private void setShareIntent(Intent shareIntent) {
    if (mShareActionProvider != null) {
      mShareActionProvider.setShareIntent(shareIntent);
    }
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

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

      String forecast = getActivity().getIntent().getExtras().getString(Intent.EXTRA_TEXT);
      ((TextView)rootView.findViewById(R.id.detail_activity_textview)).setText(forecast);
      return rootView;
    }
  }
}
