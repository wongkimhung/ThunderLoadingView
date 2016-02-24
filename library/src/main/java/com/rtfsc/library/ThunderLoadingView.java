package com.rtfsc.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by HuangJianxiong on 16-2-23.
 * Email: kh_wong@163.com
 */
public class ThunderLoadingView extends View {
    private Path mThunderPath;
    private Paint mPaint;

    /*闪电背景颜色*/
    private int mBackgroundColor = 0xfff96c0e;
    /*闪电的覆盖色*/
    private int mCoverColor = 0xff2d6de1;
    /*圆角矩形的背景色*/
    private int mViewBackGroundColor = Color.WHITE;


    private final float DEFAULT_WIDTH = dp2px(40);
    private final float DEFAULT_HEIGHT = dp2px(60);
    private final float DEFAULT_VIEW_SIZE = dp2px(70);

    /**
     * 闪电的宽高
     */
    private float mDefaultWidth = DEFAULT_WIDTH;
    private float mDefaultHeight = DEFAULT_HEIGHT;
    /**
     * 闪电后面的圆角矩形的宽高
     */
    private float mViewMinWidth = DEFAULT_VIEW_SIZE;
    private float mViewMinHeight = DEFAULT_VIEW_SIZE;

    private int mScanTop, mScanBottom;
    private RectF mBounds;
    private Size mSize = Size.MEDIUM;
    private Runnable mRunnable;
    private int mGap = 10;


    public enum Size {

        SMALL(0.5f), MEDIUM(3f / 4f), LARGE(1f);

        private Size(float value) {
            this.value = value;
        }

        private float value;

        public float getValue() {
            return value;
        }
    }

    public ThunderLoadingView(Context context) {
        super(context);
        init();
    }

    public ThunderLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
        init();
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ThunderLoadingView);
        int size = array.getInt(R.styleable.ThunderLoadingView_thunder_size, 0);
        switch (size) {
            case 0:
                mSize = Size.SMALL;
                break;
            case 1:
                mSize = Size.MEDIUM;
                break;
            case 2:
                mSize = Size.LARGE;
        }
        array.recycle();
    }

    boolean flag = false;
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        initConfig();
        startAnim();
    }


    private void initConfig() {
        mViewMinWidth = mViewMinHeight = DEFAULT_VIEW_SIZE * mSize.getValue();
        mDefaultWidth = DEFAULT_WIDTH * mSize.getValue();
        mDefaultHeight = DEFAULT_HEIGHT * mSize.getValue();
        switch (mSize) {
            case LARGE:
                mGap = 7;
                break;
            case MEDIUM:
                mGap = 5;
                break;
            case SMALL:
                mGap = 3;
                break;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPath(mSize.getValue());
        if (getWidth() < (int) mViewMinWidth || getHeight() < (int) mViewMinHeight) {
            throw new RuntimeException("we suggest you use wrap_content for best performance");
        }
        mBounds = new RectF(getPaddingLeft(), getPaddingTop(), mViewMinWidth + getPaddingLeft(), mViewMinHeight + getPaddingTop());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;
        width = (int) (mViewMinWidth + getPaddingLeft() + getPaddingRight());
        height = (int) (mViewMinHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(getMeasuredSize(widthMeasureSpec, width), getMeasuredSize(heightMeasureSpec, height));
    }


    private void startAnim() {
        mRunnable = new AnimRunnable();
        post(mRunnable);
    }


    private void initPath(float ratio) {
        mThunderPath = new Path();
        mThunderPath.moveTo(dp2px(35) * ratio + getPaddingLeft(), 0f + getPaddingTop());
        mThunderPath.lineTo(0f + getPaddingLeft(), dp2px(35) * ratio + getPaddingTop());
        mThunderPath.lineTo(dp2px(17.5f) * ratio + getPaddingLeft(), dp2px(35) * ratio + getPaddingTop());
        mThunderPath.lineTo(dp2px(5f) * ratio + getPaddingLeft(), dp2px(60) * ratio + getPaddingTop());
        mThunderPath.lineTo(dp2px(40) * ratio + getPaddingLeft(), dp2px(25f) * ratio + getPaddingTop());
        mThunderPath.lineTo(dp2px(22.5f) * ratio + getPaddingLeft(), dp2px(25f) * ratio + getPaddingTop());
        mThunderPath.close();
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getMeasuredSize(int measureSpec, int desiredSize) {

        int result;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            default:
                result = desiredSize;
                if (mode == MeasureSpec.AT_MOST)
                    result = Math.min(result, size);
                break;
        }

        return result;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(mViewBackGroundColor);
        //如果xml中设置layout_width/layout_height大于默认宽高，那么居中(不允许小于默认宽高)
        if(getWidth()-getPaddingLeft()-getPaddingRight() > (int)mViewMinWidth || getHeight()-getPaddingTop()-getPaddingBottom() > (int)mViewMinHeight){
            canvas.translate((getWidth()-mViewMinWidth)/2.0f,(getHeight()-mViewMinHeight)/2.0f);
        }

        //画圆角矩形
        canvas.drawRoundRect(mBounds,dp2px(5),dp2px(5),mPaint);
        //平移到圆角矩形中心点，画闪电
        canvas.translate((mViewMinWidth - mDefaultWidth) / 2.0f, (mViewMinHeight - mDefaultHeight) / 2.0f);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mThunderPath, mPaint);
        //通过clicpRect的方式控制可绘制区域(在外界看来好像有闪动的动画效果)
        mPaint.setColor(mCoverColor);
        canvas.clipRect(getPaddingLeft(), mScanTop + getPaddingTop(), mDefaultWidth + getPaddingLeft(), mScanBottom + getPaddingTop());
        canvas.drawPath(mThunderPath, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mRunnable);
    }

    public void setSize(Size mSize) {
        this.mSize = mSize;
        initConfig();
    }

    class AnimRunnable implements Runnable{
        @Override
        public void run() {
            if (!flag) {
                mScanBottom += mGap;
                if (mScanBottom >= mDefaultHeight) {
                    mScanBottom = (int) mDefaultHeight;
                    flag = true;
                }
                postInvalidate();
                post(this);
            } else {
                mScanTop += mGap;
                if (mScanTop >= mDefaultHeight) {
                    mScanTop = mScanBottom = 0;
                    flag = false;
                    postInvalidate();
                    postDelayed(this, 700);
                } else {
                    postInvalidate();
                    post(this);
                }
            }
        }
    }
}
