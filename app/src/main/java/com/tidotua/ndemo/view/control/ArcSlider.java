package com.tidotua.ndemo.view.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ti on 15.12.15.
 */
public class ArcSlider extends View {

    public interface ArcSliderListener {
        void onProgressChange(int progress);
    }

    private final float arcGrad = 90f;
    private final int curveEndWidth = 150;
    private final float paddingTop = 50;
    private final float paddingLeft = 50;
    private final float yScale = 0.75f;
    private final float textHeight = 40.f;
    protected final int buttonRadius = 36;
    protected final float buttonTextHeight = buttonRadius * 2;
    private final int borderWidth = 3;
    private final int mainColor = 0xff808080;
    private final int backColor = 0xfff0f0f0;
    private final int currentColor = 0xffc0c0c0;

    private Path borderPath = new Path();
    private Path currentPath = new Path();
    protected Path progressPath = new Path();
    private Paint borderPaint = new Paint();
    private Paint currentPaint = new Paint();
    protected Paint backPaint = new Paint();
    protected Paint progressPaint = new Paint();
    protected Paint buttonPaint = new Paint();
    private RectF outerOval = new RectF();
    private RectF innerOval = new RectF();
    protected int mWidth = 0;
    private double arcEndCorrection = 0;
    protected PointF buttonMinusPosition = new PointF();
    protected PointF buttonPlusPosition = new PointF();
    private PointF textPosition = new PointF();
    protected float buttonsStartY;
    protected int mProgress = 15;
    protected int mCurrentMark = 18;
    protected int mMin = 10;
    protected int mMax = 20;
    protected boolean isMoving = false;
    protected boolean isPlusPressed = false;
    protected boolean isMinusPressed = false;
    private String mText = "Test text";
    private int mColor = 0xff009900;
    private ArcSliderListener arcSliderListener;

    public ArcSlider(Context context) {
        super(context);
        init();
    }

    public ArcSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        borderPaint.setColor(mainColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);

        currentPaint.setAntiAlias(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setColor(currentColor);
        currentPaint.setStrokeWidth(borderWidth);

        progressPaint.setColor(mColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setTextAlign(Paint.Align.CENTER);
        progressPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        progressPaint.setTextSize(textHeight);

        backPaint.setColor(backColor);
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.FILL);

        buttonPaint.setColor(mainColor);
        buttonPaint.setAntiAlias(true);
        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setTextAlign(Paint.Align.CENTER);
        buttonPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        buttonPaint.setTextSize(buttonTextHeight);
    }

    public int getProgress() {
        return mProgress;
    }

    public void setArcSliderListener(ArcSliderListener arcSliderListener) {
        this.arcSliderListener = arcSliderListener;
    }

    public void setProgress(int progress) {
        mProgress = cropProgress(progress, mMin, mMax);
        prepareProgress(mProgress, progressPath);
        postInvalidate();
    }

    public void setMin(int min) {
        mMin = min;
        mProgress = cropProgress(mProgress, mMin, mMax);
        updateControl();
    }

    public void setMax(int max) {
        mMax = max;
        mProgress = cropProgress(mProgress, mMin, mMax);
        updateControl();
    }

    public void setCurrentMark(int currentMark) {
        this.mCurrentMark = cropProgress(currentMark, mMin, mMax);
        prepareProgress(mCurrentMark, currentPath);
        postInvalidate();
    }

    public void setText(String text) {
        this.mText = text;
    }

    public void setColor(int color) {
        this.mColor = color;
        progressPaint.setColor(mColor);
        postInvalidate();
    }

    protected static int cropProgress(int progress, int min, int max) {
        return progress < min ? min : progress > max ? max : progress;
    }

    protected static float getFProgress(int progress, int min, int max) {
        if (max - min == 0) {
            return 0;
        }
        return 1.f * (progress - min) / (max - min);
    }

    protected static int getProgress(float progress, int min, int max) {
        return min + (int) (progress * (max - min));
    }

    protected void updateControl() {
        prepareProgress(mMax, borderPath);
        prepareProgress(mProgress, progressPath);
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        prepareCurve();
        prepareProgress(mCurrentMark, currentPath);
        updateControl();
    }

    private void prepareCurve() {
        float halfWidth = mWidth / 2f;

        float arcRad = (float) (Math.toRadians(arcGrad));
        float ovalRadius = (float) (halfWidth / Math.sin(arcRad / 2f));
        float ovalShift = halfWidth - ovalRadius;

        arcEndCorrection = arcGrad / 2.0 -
                Math.toDegrees(Math.atan((halfWidth - curveEndWidth) /
                        (ovalRadius * Math.cos(arcRad / 2.0))));

        outerOval.set(
                paddingLeft + ovalShift,
                paddingTop,
                paddingLeft + 2f * ovalRadius + ovalShift,
                paddingTop + 2f * ovalRadius);

        float innerOvalRadius = (float) ((halfWidth - curveEndWidth / 2f) / Math.sin(arcRad / 2f));
        float rDelta = ovalRadius - innerOvalRadius;

        float yScaleCorrection = (float) (innerOvalRadius * Math.cos(arcRad / 2f)) * (1 - yScale);
        PointF innerOvalCenter = new PointF(
                halfWidth + (float) (-rDelta * Math.cos(arcRad / 2f)),
                ovalRadius + (float) (-rDelta * Math.sin(arcRad / 2f)) - yScaleCorrection);

        innerOval.set(
                paddingLeft + innerOvalCenter.x - innerOvalRadius,
                paddingTop + innerOvalCenter.y - innerOvalRadius * yScale,
                paddingLeft + innerOvalCenter.x + innerOvalRadius,
                paddingTop + innerOvalCenter.y + innerOvalRadius * yScale);

        float textStartY = paddingTop + ovalRadius * (float) (1.0 - Math.cos(arcRad / 2f))
                + textHeight / 2;
        textPosition.set(halfWidth, textStartY);

        initButtons(textStartY);
    }

    protected void initButtons(float textStartY) {
        buttonsStartY = textStartY + 2.5f * buttonRadius;
        buttonMinusPosition.set(buttonRadius, buttonsStartY);
        buttonPlusPosition.set(mWidth - buttonRadius, buttonsStartY);
    }

    protected void prepareProgress(int progress, Path path) {
        path.reset();

        float fProgress = getFProgress(progress, mMin, mMax);
        float targetGrad = arcGrad * fProgress;
        float targetArcCorrection = (float) (arcEndCorrection *
                //Math.pow(fProgress, 0.55 + fProgress * 0.25));
                Math.pow(fProgress, 0.65 + fProgress * 0.25));

        float startGrad = 270 - arcGrad / 2f;
        if (targetGrad - targetArcCorrection < 0) {
            targetArcCorrection = 0;
        }
        path.arcTo(outerOval, startGrad, targetGrad - targetArcCorrection);
        path.arcTo(innerOval, startGrad + targetGrad, -targetGrad);
    }

    private boolean changeProgress(float x, float y, boolean test) {
        if (test && y > buttonsStartY) {
            return false;
        }
        PointF pressVector = new PointF(x - outerOval.centerX(), y - outerOval.centerY());
        float distance = pressVector.length();
        float delta = outerOval.width() / 2 - distance;

        if (delta > 0 && delta < curveEndWidth) {
            changeProgress(test, pressVector, distance);
            return true;
        }
        return false;
    }

    protected int newProgress(PointF pressVector, float distance) {
        double angle = 45 + Math.toDegrees(Math.asin(pressVector.x / distance));
        return getProgress((float) (angle / (arcGrad - arcEndCorrection)), mMin, mMax);
    }

    protected void changeProgress(boolean test, PointF pressVector, float distance) {
        if (!test) {
            setProgress(newProgress(pressVector, distance));
        } else {
            isMoving = true;
        }
    }

    protected boolean isMoving() {
        return isMoving;
    }

    protected void stopMoving() {
        isMoving = false;
    }

    protected void beforeMovingStop() {
        changeCallback();
    }

    protected boolean clickButtons(float x, float y, boolean test) {
        PointF minusVector = new PointF(x - buttonMinusPosition.x, y - buttonMinusPosition.y);
        PointF plusVector = new PointF(x - buttonPlusPosition.x, y - buttonPlusPosition.y);

        isMinusPressed = checkButton(minusVector, test, isMinusPressed, -1);
        isPlusPressed = checkButton(plusVector, test, isPlusPressed, 1);

        return isMinusPressed || isPlusPressed;
    }

    protected boolean checkButton(PointF vector, boolean test, boolean isPressed, int progressDelta) {
        if (vector.length() < buttonRadius * 1.5) {
            if (!test && isPressed) {
                setProgress(mProgress + progressDelta);
                changeCallback();
            }
            return test;
        }
        return false;
    }

    private void changeCallback() {
        if (null != arcSliderListener && (isMoving || isMinusPressed || isPlusPressed)) {
            arcSliderListener.onProgressChange(getProgress());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (!clickButtons(event.getX(), event.getY(), true)) {
                    changeProgress(event.getX(), event.getY(), true);
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (isMoving()) {
                    beforeMovingStop();
                    stopMoving();
                } else {
                    clickButtons(event.getX(), event.getY(), false);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isMoving()) {
                    changeProgress(event.getX(), event.getY(), false);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                stopMoving();
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        backPaint.setColor(backColor);
        canvas.drawPath(borderPath, backPaint);
        drawProgress(canvas);
        canvas.drawPath(currentPath, currentPaint);
        canvas.drawPath(borderPath, borderPaint);
        drawText(canvas);
        drawButtons(canvas);
    }

    protected void drawProgress(Canvas canvas) {
        canvas.drawPath(progressPath, progressPaint);
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(mText, textPosition.x, textPosition.y, progressPaint);
    }

    protected void drawButtons(Canvas canvas) {
        canvas.drawText("−", buttonMinusPosition.x, buttonMinusPosition.y, buttonPaint);
        canvas.drawText("+", buttonPlusPosition.x, buttonPlusPosition.y, buttonPaint);
    }
}
