package com.tidotua.ndemo.view.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;

/**
 * Created by ti on 16.12.15.
 */
public class IntervalArcSlider extends ArcSlider {

    public interface IntervalArcSliderListener extends ArcSlider.ArcSliderListener {
        void onStartProgressChange(int startProgress);
    }

    private Path startProgressPath = new Path();
    private int minInterval = 1;
    private int startProgress = 12;
    private boolean isStartMoving = false;
    protected PointF buttonStartMinusPosition = new PointF();
    protected PointF buttonStartPlusPosition = new PointF();
    protected boolean isStartPlusPressed = false;
    protected boolean isStartMinusPressed = false;
    private IntervalArcSliderListener intervalArcSliderListener;

    public IntervalArcSlider(Context context) {
        super(context);
    }

    public IntervalArcSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntervalArcSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();

    }

    public void setArcSliderListener(IntervalArcSliderListener intervalArcSliderListener) {
        this.intervalArcSliderListener = intervalArcSliderListener;
        super.setArcSliderListener(intervalArcSliderListener);
    }

    public void setMinInterval(int minInterval) {
        this.minInterval = minInterval;
    }

    public void setStartProgress(int progress) {
        startProgress = cropProgress(progress, mMin, mProgress - minInterval);
        prepareProgress(startProgress, startProgressPath);
        postInvalidate();
    }

    @Override
    public void setProgress(int progress) {
        mProgress = cropProgress(progress, startProgress + minInterval, mMax);
        prepareProgress(mProgress, progressPath);
        postInvalidate();
    }

    public int getStartProgress() {
        return startProgress;
    }

    protected void updateControl() {
        prepareProgress(startProgress, startProgressPath);
        super.updateControl();
    }

    protected void changeProgress(boolean test, PointF pressVector, float distance) {
        int newProgress = newProgress(pressVector, distance);
        if (!test) {
            if (isMoving) {
                setProgress(newProgress);
            } else if (isStartMoving) {
                setStartProgress(newProgress);
            }
        } else {
            if (Math.abs(mProgress - newProgress) < Math.abs(startProgress - newProgress)) {
                isMoving = true;
            } else {
                isStartMoving = true;
            }
        }
    }

    protected boolean isMoving() {
        return isMoving || isStartMoving;
    }

    protected void stopMoving() {
        isMoving = false;
        isStartMoving = false;
    }


    protected void beforeMovingStop() {
        super.beforeMovingStop();
        intervalChangeCallback();
    }

    protected void initButtons(float textStartY) {
        buttonsStartY = textStartY + 3 * buttonRadius;
        buttonStartMinusPosition.set(buttonRadius, buttonsStartY);
        buttonStartPlusPosition.set(5 * buttonRadius, buttonsStartY);
        buttonMinusPosition.set(mWidth  - 5 * buttonRadius, buttonsStartY);
        buttonPlusPosition.set(mWidth  - buttonRadius, buttonsStartY);
    }

    protected boolean clickButtons(float x, float y, boolean test) {
        if (super.clickButtons(x, y, test)) {
            return true;
        }

        PointF startMinusVector = new PointF(x - buttonStartMinusPosition.x,
                y - buttonStartMinusPosition.y);
        PointF startPlusVector = new PointF(x - buttonStartPlusPosition.x,
                y - buttonStartPlusPosition.y);

        isStartPlusPressed = checkStartButton(startMinusVector, test, isStartPlusPressed, -1);
        isStartMinusPressed = checkStartButton(startPlusVector, test, isStartMinusPressed, 1);

        return isStartPlusPressed || isStartMinusPressed;
    }

    protected boolean checkStartButton(PointF vector, boolean test, boolean isPressed,
                                       int progressDelta) {
        if (vector.length() < buttonRadius * 1.5) {
            if (!test && isPressed) {
                setStartProgress(startProgress + progressDelta);
                intervalChangeCallback();
            }
            return test;
        }
        return false;
    }

    private void intervalChangeCallback() {
        if (null != intervalArcSliderListener
                && (isStartMoving || isStartMinusPressed || isStartPlusPressed)) {
            intervalArcSliderListener.onStartProgressChange(getStartProgress());
        }
    }

    protected void drawProgress(Canvas canvas) {
        super.drawProgress(canvas);
        canvas.drawPath(startProgressPath, backPaint);
    }

    protected void drawButtons(Canvas canvas) {
        super.drawButtons(canvas);
        canvas.drawText("−", buttonStartMinusPosition.x, buttonStartMinusPosition.y, buttonPaint);
        canvas.drawText("+", buttonStartPlusPosition.x, buttonStartPlusPosition.y, buttonPaint);
    }
}
