package com.example.assafg.sunshine.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.assafg.sunshine.app.data.WeatherContract;
import com.example.assafg.sunshine.app.sync.SunshineSyncAdapter;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
    implements Preference.OnPreferenceChangeListener {

  // since we use the preference change initially to populate the summary
  // field, we'll ignore that change at start of the activity
  boolean mBindingPreference;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
    final Activity settings = this;
    Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
    root.addView(bar, 0); // insert at top
    bar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        NavUtils.navigateUpFromSameTask(settings);
      }
    });
    // Add 'general' preferences, defined in the XML file
    addPreferencesFromResource(R.xml.pref_general);

    // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
    // updated when the preference changes.
    bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
    bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
  }

  /**
   * Attaches a listener so the summary is always updated with the preference value.
   * Also fires the listener once, to initialize the summary (so it shows up before the value
   * is changed.)
   */
  private void bindPreferenceSummaryToValue(Preference preference) {

    mBindingPreference = true;

    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(this);

    // Trigger the listener immediately with the preference's
    // current value.
    onPreferenceChange(preference,
        PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getString(preference.getKey(), ""));

    mBindingPreference = false;
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object value) {
    String stringValue = value.toString();

    // are we starting the preference activity?
    if ( !mBindingPreference ) {
      if (preference.getKey().equals(getString(R.string.pref_location_key))) {
        SunshineSyncAdapter.syncImmediately(getApplicationContext());
      } else {
        // notify code that weather may be impacted
        getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
      }
    }

    if (preference instanceof ListPreference) {
      // For list preferences, look up the correct display value in
      // the preference's 'entries' list (since they have separate labels/values).
      ListPreference listPreference = (ListPreference) preference;
      int prefIndex = listPreference.findIndexOfValue(stringValue);
      if (prefIndex >= 0) {
        preference.setSummary(listPreference.getEntries()[prefIndex]);
      }
    } else {
      // For other preferences, set the summary to the value's simple string representation.
      preference.setSummary(stringValue);
    }
    return true;
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  @Nullable
  @Override
  public Intent getParentActivityIntent() {
    return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  }
}