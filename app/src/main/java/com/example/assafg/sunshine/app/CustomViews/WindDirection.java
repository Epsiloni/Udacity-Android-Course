package com.example.assafg.sunshine.app.CustomViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.example.assafg.sunshine.app.R;

/**
 *
 * Created by Enigma on 12/23/14.
 */
public class WindDirection extends View {

  private Paint mDirectionPaint;
  private float mDirectionHeight;
  private int mWindSpeed;
  private int mWindDirection;

  private static final int N = 0;
  private static final int NE = 1;
  private static final int E = 2;
  private static final int SE = 3;
  private static final int S = 4;
  private static final int SW = 5;
  private static final int W = 6;
  private static final int NW = 7;

  private static final String[] windDirections = new String[] {
      "North",
      "North East",
      "East",
      "South East",
      "South",
      "South West",
      "West",
      "North West",
  };

  public WindDirection(Context context) {
    super(context);
    init();
  }

  public WindDirection(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public WindDirection(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    mDirectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mDirectionPaint.setColor(getResources().getColor(R.color.sunshine_accents));
    mDirectionPaint.setStrokeWidth(4);

    if (mDirectionHeight == 0) {
      mDirectionHeight = mDirectionPaint.getTextSize();
    } else {
      mDirectionPaint.setTextSize(mDirectionHeight);
    }
  }

  public void setWindSpeed(int windSpeed) {
    mWindSpeed = windSpeed;
  }

  public void setWindDirection(int windDirection) {
    mWindDirection = windDirection;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Try for a width based on our minimum
    int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
    int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

    // Whatever the width ends up being, ask for a height that would let the pie
    // get as big as it can
    int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
    int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

    setMeasuredDimension(w, h);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int startX = 0;
    int startY = 0;
    int stopX = 0;
    int stopY = 0;

    switch (mWindDirection) {
      case N:
        startX = 25;
        startY = 0;
        stopX = 25;
        stopY = 50;
        break;
      case NE:
        startX = 0;
        startY = 50;
        stopX = 50;
        stopY = 0;
        break;
      case E:
        startX = 0;
        startY = 25;
        stopX = 50;
        stopY = 25;
        break;
      case SE:
        startX = 0;
        startY = 0;
        stopX = 50;
        stopY = 50;
        break;
      case S:
        startX = 25;
        startY = 0;
        stopX = 25;
        stopY = 50;
        break;
      case SW:
        startX = 0;
        startY = 0;
        stopX = 50;
        stopY = 50;
        break;
      case W:
        startX = 0;
        startY = 25;
        stopX = 50;
        stopY = 25;
        break;
      case NW:
        startX = 0;
        startY = 0;
        stopX = 50;
        stopY = 50;
        break;
    }

    canvas.drawLine(2*startX, 2*startY, 2*stopX, 2*stopY, mDirectionPaint);

    canvas.drawCircle(2*startX, 2*startY, 15.0f, mDirectionPaint);
  }

  @Override
  public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
    event.getText().add(windDirections[mWindDirection]);
    return true;
  }
}
