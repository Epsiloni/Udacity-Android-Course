package com.example.assafg.sunshine.app.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.assafg.sunshine.app.R;
import com.example.assafg.sunshine.app.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

public class Utility {
  public static String getPreferredLocation(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_location_key),
        context.getString(R.string.pref_location_default));
  }

  public static boolean isMetric(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_units_key),
        context.getString(R.string.pref_units_default))
        .equals(context.getString(R.string.pref_units_default));
  }

  public static String formatTemperature(double temperature, boolean isMetric) {
    double temp;
    if ( !isMetric ) {
      temp = 9*temperature/5+32;
    } else {
      temp = temperature;
    }
    return String.format("%.0f", temp);
  }

  public static String formatDate(String dateString) {
    Date date = WeatherContract.getDateFromDb(dateString);
    return DateFormat.getDateInstance().format(date);
  }
}
