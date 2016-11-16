package cn.xlink.admin.myscaleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.view.MotionEvent.ACTION_POINTER_DOWN;

/**
 * Created by admin on 2016/8/24.
 */
public class TimePickerView extends View {

    private Context mContext ;
    // 一整天有多少个毫秒
    private static final long MILLIS_WHOLE_DAY= 24*60*60*1000 ;

    private static final long OffSet_MILLIS_FOUR_HOUR= 4*60*60*1000 ;
    private static final long OffSet_MILLIS_TWO_HOUR= 2*60*60*1000 ;
    private static final long OffSet_MILLIS_ONE_HOUR= 1*60*60*1000 ;
    private static final long OffSet_MILLIS_HALF_HOUR= 30*60*1000 ;
    private static final long OffSet_MILLIS_QUARTER_HOUR = 15*60*1000 ;

    private long mDefaultTimeOffSet = OffSet_MILLIS_ONE_HOUR ;
    private int mScreenWidth;
    // 辅助view滑动的
    private OverScroller mOverScroller;
    // 判定为拖动的最小移动像素数和速度
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    // 刻度线
    private Paint mIndicatePaint;
    // 标识文字
    private Paint mTextPaint;
    private Paint mBgPaint;
    // 间隔宽度
    private int mDefaultWidthOffSet;
    // view的高度
    private int mHeight;
    // 滑动速度用的
    private VelocityTracker mVelocityTracker;
    // 是否拖拽的一个标识，没啥用
    private boolean mIsDragged;
    private int mLastMotionX;
    // 填充矩形数据
    private List<DayBean> mDayBeans;
    // 矩形的画笔
    private Paint mRectPaint;
    private Paint mMidLinePaint;
    // 回调接口
    private OnScaleListener mOnScaleListener;
    // 缩放的手势
    private ScaleGestureDetector mScaleGestureDetector;
    // 每一个大格里面有多少个小格（偶数偶数偶数，重要的事情说三遍）
    private int innerSpaceNum = 6 ;
    private int mStartWidthOffSet;
    // 保存事件类型和表现颜色
    private SparseArray<Integer> eventType ;
    // 是否显示两边空白地方的刻度线
    private boolean mShowBorderLine ;

    // 当天的开始和结束时间戳
    private long mStartTimeStemp ;
    private long mEndTimeStemp ;

    private boolean mAllowScroll ;
    private long endScale;

    public TimePickerView(Context context) {
        this(context,null);
    }

    public TimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValue(context) ;
    }

    private void initValue(Context context) {
        this.mContext = context ;
        mScreenWidth = UIUtils.getScreenWidth(context) ;
        mOverScroller= new OverScroller(context) ;
        // 设置这个可以滑动到了尽头，可以再偏移回弹
//        setOverScrollMode(OVER_SCROLL_ALWAYS);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        mScaleGestureDetector = new ScaleGestureDetector(context,mOnGesture) ;

        eventType = new SparseArray<>() ;

        int mTextColor = Color.BLACK ;
        int mTextSize = UIUtils.sp2px(context,18) ;
        mDefaultWidthOffSet = UIUtils.dip2px(context,30) ;
        mStartWidthOffSet = mDefaultWidthOffSet ;


        mBgPaint = new Paint() ;
        mBgPaint.setColor(Color.parseColor("#883A3A3A"));

        mRectPaint = new Paint() ;
        mRectPaint.setColor(Color.GREEN);

        mMidLinePaint = new Paint() ;
        mMidLinePaint.setColor(Color.WHITE);

        mIndicatePaint = new Paint();
        mIndicatePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        setBackgroundColor(Color.parseColor("#F5F5DC"));


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setLayoutWidth();

        int count = canvas.save() ;
        // 刻度线内背景
        drawBg(canvas) ;
        // 刻度线
        drawScaleLine(canvas) ;
        // 填充事件的时间段
        if (mDayBeans != null && mDayBeans.size() != 0) {
//            drawRect(canvas,mDayBeans) ;
            drawRect2(canvas,mDayBeans);
        }
        // 中间的那条中轴线
        drawMiddleLine(canvas) ;

        canvas.restoreToCount(count);
    }

    private void setLayoutWidth() {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = getRange()*innerSpaceNum*mDefaultWidthOffSet;
        setLayoutParams(params);
        L.i("getWidth==>>"+getRange()*innerSpaceNum*mDefaultWidthOffSet);
        L.i("getWidth=="+getWidth());
    }

    private void drawMiddleLine(Canvas canvas) {
        canvas.drawRect(mScreenWidth/2+getScrollX()-2,0,mScreenWidth/2+getScrollX()+2,getHeight(),mMidLinePaint);
    }

    private void drawRect(Canvas canvas, List<DayBean> mDayBean) {
        for (DayBean dayBean:mDayBean) {

            Rect rect = new Rect(getScrollByPosition(dayBean.getStartNum())+mScreenWidth/2,0,
                    getScrollByPosition(dayBean.getEndNum())+mScreenWidth/2, UIUtils.dip2px(mContext,30));

            if (eventType.get(dayBean.getType()) != null)
                mRectPaint.setColor(eventType.get(dayBean.getType()));

            canvas.drawRect(rect,mRectPaint);
        }
        invalidateView();
    }

    private void drawRect2(Canvas canvas, List<DayBean> mDayBean) {
        for (DayBean dayBean:mDayBean) {

            if (dayBean.getStartNum()> mStartTimeStemp && dayBean.getEndNum()< mEndTimeStemp) {

                Rect rect = new Rect(getScrollByPosition(dayBean.getStartNum()-mStartTimeStemp)+mScreenWidth/2,0,
                        getScrollByPosition(dayBean.getEndNum()-mStartTimeStemp)+mScreenWidth/2, UIUtils.dip2px(mContext,30));

                if (eventType.get(dayBean.getType()) != null)
                    mRectPaint.setColor(eventType.get(dayBean.getType()));

                canvas.drawRect(rect,mRectPaint);
            }

        }
        invalidateView();
    }



    /**
     * 说白了，就是画一个rect，从半个屏幕的地方开始，半个屏幕+刻度方格的像素*数目这个地方 结束
     * @param canvas  画布
     */
    private void drawBg(Canvas canvas) {
        canvas.drawRect(mScreenWidth / 2, getPaddingTop(), mScreenWidth / 2 + getRange() * innerSpaceNum * mDefaultWidthOffSet, mHeight, mBgPaint);
    }

    /**
     *  画刻度线，从左到右画，顺便把文字也画出来了
     * @param canvas 画布
     */
    private void drawScaleLine(Canvas canvas) {
        for (int i = 0; i<=getRange();i++) {
            for (int j=0;j<innerSpaceNum;j++) {
                if (j % innerSpaceNum == 0) {   // 画大刻度线
                    canvas.drawRect(obtainScaleLinePointX(i, j) -2,getHeight()-getPaddingBottom()-30,
                            mScreenWidth/2+mDefaultWidthOffSet*(i*innerSpaceNum+j)+2,getHeight(),mIndicatePaint);

                    double t = i*24.0/getRange() ;
                    String text = UIUtils.toHour((long) (3600 * 1000 * t));
                    canvas.drawText(text, obtainScaleLinePointX(i, j),getHeight()/2,mTextPaint);
                    // 这里就是画到最后的一条刻度线了，break退出循环
                    if (i== getRange()) {
                        break;
                    }
                } else if (j % innerSpaceNum == innerSpaceNum/2) {    // 画大刻度线中的中间的那条刻度线
                    canvas.drawLine(obtainScaleLinePointX(i, j),getHeight()-getPaddingBottom()-15,
                            obtainScaleLinePointX(i, j),getHeight()-getPaddingBottom(),mIndicatePaint);
                } else {    // 画其余的刻度线
                    canvas.drawLine(obtainScaleLinePointX(i, j),getHeight()-getPaddingBottom()-15,
                            obtainScaleLinePointX(i, j),getHeight()-getPaddingBottom(),mIndicatePaint);
                }
            }
        }

        // 刻度线外两边的空白地方  也给它画个刻度线吧    别太空白
        if (mShowBorderLine) {
            int lineNum = mScreenWidth/2/mDefaultWidthOffSet ;
            for (int y=1;y<=lineNum;y++) {
                canvas.drawLine(mScreenWidth/2-y*mDefaultWidthOffSet,getHeight()-getPaddingBottom()-15,
                        mScreenWidth/2-y*mDefaultWidthOffSet,getHeight()-getPaddingBottom(),mIndicatePaint);

                canvas.drawLine(mScreenWidth/2+mDefaultWidthOffSet*getRange()*innerSpaceNum+y*mDefaultWidthOffSet,getHeight()-getPaddingBottom()-15,
                        mScreenWidth/2+mDefaultWidthOffSet*getRange()*innerSpaceNum+y*mDefaultWidthOffSet,getHeight()-getPaddingBottom(),mIndicatePaint);

            }
        }
    }

    /**
     * @param i  大刻度
     * @param j  小刻度
     * @return  某一条刻度线的x坐标
     */
    private int obtainScaleLinePointX(int i, int j) {
        return mScreenWidth/2+mDefaultWidthOffSet*(i*innerSpaceNum+j);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 先判断是不是缩放操作
        mAllowScroll = true ;
        mScaleGestureDetector.onTouchEvent(event);
        if (mScaleGestureDetector.isInProgress()) {
            return true;
        }



        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                if (mIsDragged = !mOverScroller.isFinished()) {
                    // 看你有没有老爸，有的话就拦住你
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (!mAllowScroll)
                    return true;

                if (!mOverScroller.isFinished())
                    mOverScroller.abortAnimation();

                mLastMotionX = (int) event.getX();
                if (mOnScaleListener != null) {
                    mOnScaleListener.onScaleStart(scrollX2Long());
                }

                return true;

            case MotionEvent.ACTION_MOVE:

                if (!mAllowScroll)
                    return true;

                int curX = (int) event.getX();
                int deltaX = mLastMotionX - curX;

                if (!mIsDragged && Math.abs(deltaX) > mTouchSlop) {
                    if (getParent() != null)
                        getParent().requestDisallowInterceptTouchEvent(true);

                    mIsDragged = true;

                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }

                if (mIsDragged) {
                    mLastMotionX = curX;

                    if (getScrollX() <= 0 || getScrollX() >= getMaximumScroll())
                        deltaX *= 0.7;

                    if (overScrollBy(deltaX, 0, getScrollX(), getScrollY(), getMaximumScroll(), 0, getWidth(), 0, true)) {
                        mVelocityTracker.clear();
                    }

                }

                break;
            case MotionEvent.ACTION_UP: {

                if (!mAllowScroll)
                    return true;

                if (mIsDragged) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) mVelocityTracker.getXVelocity();

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        fling(-initialVelocity);
                    } else {
                        //alignCenter();
                        sprintBack();
                    }
                }

                mIsDragged = false;
                recycleVelocityTracker();
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                mAllowScroll = true ;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mAllowScroll = false ;
                return mScaleGestureDetector.onTouchEvent(event) ;

            case MotionEvent.ACTION_CANCEL: {

                if (mIsDragged && mOverScroller.isFinished()) {
                    sprintBack();
                }

                mIsDragged = false;

                recycleVelocityTracker();
                break;
            }
        }

        return true;
    }

    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mOverScroller.getCurrX();
            int y = mOverScroller.getCurrY();
            overScrollBy(x - oldX, y - oldY, oldX, oldY, getMaximumScroll(), 0, getWidth(), 0, false);
            invalidateView();
        }
//        else if (!mIsDragged && mIsAutoAlign) {
//            adjustIndicate();
//        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (!mOverScroller.isFinished()) {
            final int oldX = getScrollX();
            final int oldY = getScrollY();
            setScrollX(scrollX);
            onScrollChanged(scrollX, scrollY, oldX, oldY);
//            if (clampedX) {
//                sprintBack();
//            }
        } else {
            if (mOnScaleListener != null) {
                endScale = scrollX2Long() ;
                mOnScaleListener.onScaleEnd(endScale);
            }
            super.scrollTo(scrollX, scrollY);
        }
        if(mOnScaleListener != null){
//            mOnScaleListener.onScaleChanged(getScrollX()*MILLIS_WHOLE_DAY/getMaximumScroll());
            mOnScaleListener.onScaleChanged(scrollX2Long());
        }

    }

    /**
     * 根据移动的距离得到时间戳
     * @return
     */
    private long scrollX2Long() {
        return mStartTimeStemp+getScrollX()*MILLIS_WHOLE_DAY/getMaximumScroll();
    }


    public void setCurrentData(int year,int monthInYear,int dayInMonth) {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd");

        StringBuilder sb = new StringBuilder() ;
        sb.append(year)
                .append("-")
                .append(monthInYear)
                .append("-")
                .append(dayInMonth) ;

        try {
            Date date = simpleDateFormat.parse(sb.toString());
            mStartTimeStemp = date.getTime();
            mStartTimeStemp = mStartTimeStemp -29*MILLIS_WHOLE_DAY ;
            mEndTimeStemp = mStartTimeStemp+MILLIS_WHOLE_DAY ;
            invalidateView();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void setShowBorderLine(boolean isShow) {
        this.mShowBorderLine = isShow ;
    }

    public void addEventType(int keyType,int color) {
        eventType.put(keyType,color);
    }

    public void setData(List<DayBean> dayBean) {
        this.mDayBeans = dayBean ;
        invalidateView();
    }

    public void smoothScrollTo(long position){
        if(position < 0 || position > MILLIS_WHOLE_DAY)
            return;

        if(!mOverScroller.isFinished())
            mOverScroller.abortAnimation();

        int scrollX = getScrollByPosition(position);
        mOverScroller.startScroll(getScrollX(), getScrollY(), scrollX - getScrollX() , 0);
        invalidateView();
    }

    public void smoothScrollTo2(long position) {
        if(position < mStartTimeStemp || position > mEndTimeStemp) {
            Toast.makeText(mContext,"时间戳不是当天的！！！",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!mOverScroller.isFinished())
            mOverScroller.abortAnimation();

        int scrollX = getScrollByPosition(position-mStartTimeStemp);
        mOverScroller.startScroll(getScrollX(), getScrollY(), scrollX - getScrollX() , 0);
//        scrollTo(scrollX - getScrollX(),0);
        invalidateView();
    }

    /**
     * 快速滚动到当前位置，适合刚刚初始化的时候，滚动到系统当前时间
     * @param position
     */
    public void immediatelyScrollTo(long position) {
        if(position < mStartTimeStemp || position > mEndTimeStemp)
            return;

        if(!mOverScroller.isFinished())
            mOverScroller.abortAnimation();

        int scrollX = getScrollByPosition(position-mStartTimeStemp);
        scrollTo(scrollX - getScrollX(),0);
        invalidateView();
    }

    private int getScrollByPosition(long position) {
        return (int) (position*getMaximumScroll()/MILLIS_WHOLE_DAY) ;
    }

    /**
     * 惯性，速度随着时间慢慢变小，直到为0
     * @param velocityX
     */
    public void fling(int velocityX) {
        mOverScroller.fling(getScrollX(), getScrollY(), velocityX, 0, getMinimumScroll(), getMaximumScroll(), 0, 0, getWidth() / 2, 0);
        invalidateView();
    }

    /**
     * 回弹， 在getMinimumScroll()~getMaximumScroll()这个x方向范围内滚动，超出或小于这个范围，就会触发computeScroll()，返回到最小或最大边界
     */
    public void sprintBack() {
        mOverScroller.springBack(getScrollX(), getScrollY(), getMinimumScroll(), getMaximumScroll(), 0, 0);
        invalidateView();
    }

    /**
     * 获取最小滚动值。
     * @return
     */
    private int getMinimumScroll(){
        return  0 ;
    }

    /**
     * 获取最大滚动值。可以移动的距离也就是刻度尺的除了空白地方的长度（这两个return返回是一样的，第二个return的效率更高）
     * @return
     */
    private int getMaximumScroll(){
        return mDefaultWidthOffSet*getRange()*innerSpaceNum ;
//        return getWidth()-mScreenWidth ;
    }

    /**
     * 刷新界面
     */
    public void invalidateView() {
        if (Build.VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else
            invalidate();
    }


    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeight = heightMeasureSpec ;
        setMeasuredDimension(measureWidth(widthMeasureSpec), heightMeasureSpec);
    }


    /**
     * 计算时间轴的宽度，左右都预留半个屏幕的宽度
     */
    private int measureWidth(int widthMeasureSpec) {
        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = MeasureSpec.getSize(widthMeasureSpec);
        int result = getSuggestedMinimumWidth();
        switch (measureMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = measureSize + mScreenWidth;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setOnScaleListener(OnScaleListener onScaleListener) {
        this.mOnScaleListener = onScaleListener ;
    }

    private int getRange() {
        return (int) (MILLIS_WHOLE_DAY/mDefaultTimeOffSet) ;
    }

    private void setTimeOffSet(long offSet) {
        this.mDefaultTimeOffSet = offSet ;
    }

    private long getTimeOffSet() {
        return mDefaultTimeOffSet ;
    }

    public interface OnScaleListener {
        void onScaleChanged(long scale);

        void onScaleStart(long start) ;
        void onScaleEnd(long end) ;
    }

    private ScaleGestureDetector.OnScaleGestureListener mOnGesture = new ScaleGestureDetector.OnScaleGestureListener() {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();  //缩放比例
//            float scale = getScale();

            if (mScaleFactor <1 && mDefaultTimeOffSet == OffSet_MILLIS_FOUR_HOUR) {
                mAllowScroll = true ;
                return true;
            }

            if (mScaleFactor >1 && mDefaultTimeOffSet == OffSet_MILLIS_QUARTER_HOUR) {
                mAllowScroll = true ;
                return true;
            }

            if (mScaleFactor <1 && mDefaultWidthOffSet<=mStartWidthOffSet/2) {
                mDefaultWidthOffSet = mStartWidthOffSet ;

                if (mDefaultTimeOffSet == OffSet_MILLIS_QUARTER_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_HALF_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_HALF_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_ONE_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_ONE_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_TWO_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_TWO_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_FOUR_HOUR ;
                } else {
                    mDefaultTimeOffSet = OffSet_MILLIS_FOUR_HOUR ;
                }
                return true ;
            }

            if (mScaleFactor >1 && mDefaultWidthOffSet>=mStartWidthOffSet*2) {
                mDefaultWidthOffSet = mStartWidthOffSet ;

                if (mDefaultTimeOffSet == OffSet_MILLIS_FOUR_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_TWO_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_TWO_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_ONE_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_ONE_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_HALF_HOUR ;
                } else if (mDefaultTimeOffSet == OffSet_MILLIS_HALF_HOUR) {
                    mDefaultTimeOffSet = OffSet_MILLIS_QUARTER_HOUR ;
                } else {
                    mDefaultTimeOffSet = OffSet_MILLIS_QUARTER_HOUR ;
                }
            }
            mDefaultWidthOffSet *= mScaleFactor ;
            mAllowScroll = false ;
            handleScale(mScaleFactor) ;
            invalidateView();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private void handleScale(float mScaleFactor) {
    }
}
