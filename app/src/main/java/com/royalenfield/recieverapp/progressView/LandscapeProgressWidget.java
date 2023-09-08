package com.royalenfield.recieverapp.progressView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.royalenfield.recieverapp.R;

/**
 * Created by Agiliz on 5/24/2016.
 */
public class LandscapeProgressWidget extends View {
    private int progressColor;
    private int percent = 70;
    private int radiusOuter = 110, radiusInner = 115;
    private Paint mPaintOuter;
    private Paint mPaintPercent;
    private Paint mInnerCircle, mTextPaint;
    private int mCenterX, mCenterY;
    private int textSize;
    private String mTimedText = String.valueOf(percent);
    private int desiredWidth = 300;
    private int desiredHeight = 300;
    private boolean isRunning;
    private boolean isMeasured;
    private int lineWidth, lineSpace;
    private float strokeWidth;

    public LandscapeProgressWidget(Context context) {
        super(context);
        initialization();
    }

    public LandscapeProgressWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ec_arc_progress);
        progressColor = a.getColor(R.styleable.ec_arc_progress_ec_arc_progress_color, Color.CYAN);
        percent =  a.getInt(R.styleable.ec_arc_progress_ec_vb_progress, 20);
        a.recycle();
        initialization();
    }

    public LandscapeProgressWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialization();
    }

    private void initialization() {

        strokeWidth = 22;
        this.lineWidth = getSizeInPixels(10, getContext());
        this.lineSpace = getSizeInPixels(2, getContext());

        mPaintOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOuter.setAntiAlias(true);
        mPaintOuter.setColor(Color.GRAY);
        mPaintOuter.setStrokeWidth(strokeWidth);
        mPaintOuter.setStyle(Paint.Style.STROKE);
        mPaintOuter.setPathEffect(new DashPathEffect(new float[]{lineWidth, lineSpace}, 0));

        mPaintPercent = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintPercent.setAntiAlias(true);
        mPaintPercent.setStrokeWidth(strokeWidth);
        mPaintPercent.setColor(progressColor);
        // mPaintPercent.setShader(new RadialGradient(mCenterX,mCenterY,radiusInner,Color.YELLOW,progressColor, Shader.TileMode.REPEAT));

        mPaintPercent.setStyle(Paint.Style.STROKE);
        mPaintPercent.setPathEffect(new DashPathEffect(new float[]{lineWidth, lineSpace}, 0));

        mInnerCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCircle.setColor(Color.TRANSPARENT);


        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.textLightColor));
        mTextPaint.setTextSize(textSize);
    }


    public void getUpdateRadius() {
        if (!isMeasured) {
            isMeasured = true;
            int size = getWidgetWidth() < getWidgetHeight() ? getWidgetWidth() : getWidgetHeight();

            radiusOuter = (int) (size * 0.4f);
            radiusInner = (int) (size * 0.4f);

            textSize = (int) (size * 0.25f);
            setTimedTextSize(textSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCenterX = getWidgetWidth() / 2;
        mCenterY = getWidgetHeight() / 2;
        radiusOuter = getWidgetHeight() /2 - 15 ;


        drawSecCircle(canvas);
        drawInnerCircle(canvas);
        //drawPercentageText(canvas);
    }

    private void drawInnerCircle(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, radiusInner, mInnerCircle);
    }

    private void drawSecCircle(Canvas canvas) {

        //canvas.drawCircle(mCenterX, mCenterY, radiusOuter, mPaintOuter);
        //canvas.drawArc(mCenterX - radiusOuter, mCenterY - radiusOuter, mCenterX + radiusOuter, mCenterY + radiusOuter,105, 240, true, mPaintOuter );
        canvas.drawArc(new RectF(mCenterX - radiusOuter, mCenterY - radiusOuter, mCenterX + radiusOuter, mCenterY + radiusOuter), 135, 140, false, mPaintOuter);
        if(percent > 0)
            canvas.drawArc(new RectF(mCenterX - radiusOuter, mCenterY - radiusOuter, mCenterX + radiusOuter, mCenterY + radiusOuter), 140, percent*2.6f, false, mPaintPercent);
    }

    public void drawPercentageText(Canvas canvas) {
        RectF areaRect = new RectF(mCenterX - radiusInner, mCenterY - radiusInner, mCenterX + radiusInner, mCenterY + radiusInner);
        RectF bounds = new RectF(areaRect);

        // measure text width
        bounds.right = mTextPaint.measureText(mTimedText, 0, mTimedText.length());
        // measure text height
        bounds.bottom = mTextPaint.descent() - mTextPaint.ascent();

        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        canvas.drawText(mTimedText, bounds.left, bounds.top - mTextPaint.ascent(), mTextPaint);
    }

    public void setTimedTextSize(int textSize) {
        this.textSize = textSize;
        mTextPaint.setTextSize(textSize);
    }

    public void setTimedText(String timedText) {
        this.mTimedText = timedText;
        invalidate();
    }

    public void setPercentage(int percent) {
        this.percent = percent;

        // mTimedText = String.valueOf(percent)+"%";
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        setWidgetWidth((int) (widthSize * 0.6));
        setWidgetHeight((int) (heightSize * 0.6));

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(getWidgetWidth(), widthSize);
        } else {
            width = getWidgetWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(getWidgetHeight(), heightSize);
        } else {
            height = getWidgetHeight();
        }

        setWidgetWidth(width);
        setWidgetHeight(height);

        getUpdateRadius();

        setMeasuredDimension(width, height);
    }


    public int getWidgetWidth() {
        return desiredWidth;
    }

    public void setWidgetWidth(int clockWidgetWidth) {

        this.desiredWidth = clockWidgetWidth;
    }

    public int getWidgetHeight() {
        return desiredHeight;
    }

    public void setWidgetHeight(int clockWidgetHeight) {
        this.desiredHeight = clockWidgetHeight;
    }

    public static int getSizeInPixels(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float pixels = metrics.density * dp;
        return (int) (pixels + 0.5f);
    }
}
