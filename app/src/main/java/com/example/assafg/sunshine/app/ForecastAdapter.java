package com.example.assafg.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assafg.sunshine.app.Utility.Utility;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

  private static final int VIEW_TYPE_TODAY = 0;
  private static final int VIEW_TYPE_FUTURE_DAY = 1;
  private static final int VIEW_TYPE_COUNT = 2;
  private boolean mShouldUseTodayLayout;

  @Override
  public int getItemViewType(int position) {
    return position == 0 && mShouldUseTodayLayout ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
  }

  public void setShouldUseTodayLayout(boolean shuoldUseTodayLayout) {
    mShouldUseTodayLayout = shuoldUseTodayLayout;
  }

  @Override
  public int getViewTypeCount() {
    return VIEW_TYPE_COUNT;
  }

  public ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  /**
   * Copy/paste note: Replace existing newView() method in ForecastAdapter with this one.
   */
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    // Choose the layout type
    int viewType = getItemViewType(cursor.getPosition());
    int layoutId;

    if (viewType == VIEW_TYPE_TODAY) {
      layoutId = R.layout.list_item_forecast_today;
    } else {
      layoutId = R.layout.list_item_forecast;
    }

    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {

    ViewHolder viewHolder = (ViewHolder) view.getTag();

    // Read weather icon ID from cursor
    int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

    float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
    float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
    // Read user preference for metric or imperial temperature units
    boolean isMetric = Utility.isMetric(context);
    String highTemp = Utility.formatTemperature(context, high, isMetric);
    String lowtemp = Utility.formatTemperature(context, low, isMetric);

    if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
      viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
      viewHolder.dateView.setContentDescription(context.getString(R.string.today_temperature_content_description, highTemp, lowtemp));
    } else {
      viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
    }

    // Read date from cursor
    String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
    // Find TextView and set formatted date on it
    viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));

    // Read weather forecast from cursor
    String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
    // Find TextView and set weather forecast on it
    viewHolder.descriptionView.setText(description);
    viewHolder.iconView.setContentDescription(description);

    viewHolder.highTempView.setText(highTemp);
    viewHolder.lowTempView.setText(lowtemp);
  }

  /**
   * Cache of the children views for a forecast list item.
   */
  public static class ViewHolder {
    public final ImageView iconView;
    public final TextView dateView;
    public final TextView descriptionView;
    public final TextView highTempView;
    public final TextView lowTempView;

    public ViewHolder(View view) {
      iconView = (ImageView) view.findViewById(R.id.list_item_icon);
      dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
      descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
      highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
      lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
    }
  }
}