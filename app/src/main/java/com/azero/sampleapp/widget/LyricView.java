package com.azero.sampleapp.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.azero.sampleapp.R;
import com.azero.sdk.util.log;
import com.google.android.gms.common.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class LyricView extends View {

    private int mHintColor = 0;
    private int mDefaultColor = 0;
    private int mHighLightColor = 0;

    private int mLineCount = 0;
    private float mTextSize = 0;
    private float mLineHeight = 0f;
    private LyricInfo mLyricInfo;
    private String mDefaultHint;
    private int mMaxLength = 0;

    private Paint mTextPaint;

    private boolean mFling = false;
    private ValueAnimator mFlingAnimator;
    private float mScrollY = 0f;
    private float mLineSpace = 0f;
    private boolean mIsShade = false;
    private float mShaderWidth = 0f;
    private int mCurrentPlayLine = 0;

    private VelocityTracker mVelocityTracker;
    private float mVelocity = 0f;
    private float mDownY = 0f;
    private float mLastScrollY = 0f;
    private float maxVelocity = 0;

    private ArrayList<Integer> mLineFeedRecord = new ArrayList<>();
    private boolean mEnableLineFeed = false;
    private float mExtraHeight = 0;

    private float mTextHeight = 0;

    private String mCurrentLyricFilePath;

    private int mWidth = getResources().getDisplayMetrics().widthPixels;

    private static final float SLIDE_COEFFICIENT = 0.2f;
    private static final int UNITS_SECOND = 1000;
    private static final long UNITS_MILLISECOND = 1;
    private static final long FLING_ANIMATOR_DURATION = 500 * UNITS_MILLISECOND;
    private static final long THRESHOLD_Y_VELOCITY = 1600;
    private static final long DEFAULT_TEXT_SIZE = 16;
    private static final long DEFAULT_LINE_SPACE = 20;

    public LyricView(Context context) {
        super(context);
        initLyricView(context);
    }

    public LyricView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        getAttrs(context, attributeSet);
        initLyricView(context);
    }

    public LyricView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        getAttrs(context, attributeSet);
        initLyricView(context);
    }

    private void getAttrs(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.LyricView);

        mIsShade = typedArray.getBoolean(R.styleable.LyricView_fadeInFadeOut, false);
        if (typedArray.getString(R.styleable.LyricView_hint) != null) {
            mDefaultHint = typedArray.getString(R.styleable.LyricView_hint);
        } else {
            mDefaultHint = "暂无歌词";
        }

        mHintColor = typedArray.getColor(R.styleable.LyricView_hintColor, Color.parseColor("#FFFFFF"));
        mDefaultColor = typedArray.getColor(R.styleable.LyricView_textColor, Color.parseColor("#8D8D8D"));
        mHighLightColor = typedArray.getColor(R.styleable.LyricView_highlightColor, Color.parseColor("#1E90FF"));

        mTextSize = typedArray.getDimensionPixelSize(
                R.styleable.LyricView_textSize,
                (int) getRawSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE)
        );
        mMaxLength = typedArray.getDimensionPixelSize(
                R.styleable.LyricView_maxLength,
                (int) getRawSize(TypedValue.COMPLEX_UNIT_PX, mWidth * 0.86f)
        );
        mLineSpace = typedArray.getDimensionPixelSize(
                R.styleable.LyricView_lineSpace,
                (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_SPACE)
        );
        typedArray.recycle();
    }

    private void initLyricView(Context context) {
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        initPaint();
        initAllBounds();
    }

    private void initPaint() {
        mTextPaint = new TextPaint();
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initAllBounds() {
        setRawTextSize(mTextSize);
        setLineSpace(mLineSpace);
        measureLineHeight();
    }

    private void setRawTextSize(float size) {
        if (size != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(size);
            measureLineHeight();
            mScrollY = measureCurrentScrollY(mCurrentPlayLine);
            invalidateView();
        }
    }

    private float measureCurrentScrollY(int line) {
        if (mEnableLineFeed && line > 1) {
            return (line - 1) * mLineHeight + mLineFeedRecord.get(line - 1);
        } else {
            return (line - 1) * mLineHeight;
        }
    }

    private void setLineSpace(float lineSpace) {
        BigDecimal lineSpaceBigDecimal = new BigDecimal(lineSpace);
        BigDecimal mLineSpaceBigDecimal = new BigDecimal(mLineSpace);

        if (lineSpaceBigDecimal.equals(mLineSpaceBigDecimal)) {
            mLineSpace = getRawSize(TypedValue.COMPLEX_UNIT_DIP, lineSpace);
            measureLineHeight();
            mScrollY = measureCurrentScrollY(mCurrentPlayLine);
            invalidateView();
        }
    }

    private float getRawSize(int index, float size) {
        Context context = getContext();
        Resources resources;
        if (context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }
        return TypedValue.applyDimension(index, size, resources.getDisplayMetrics());
    }

    private void measureLineHeight() {
        Rect lineBound = new Rect();
        mTextPaint.getTextBounds(mDefaultHint, 0, mDefaultHint.length(), lineBound);
        mTextHeight = lineBound.height();
        mLineHeight = mTextHeight + mLineSpace;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                actionCancel();
                break;
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp();
                break;
            default:
                break;
        }
        invalidateView();
        return true;
    }

    private void actionCancel() {
        releaseVelocityTracker();
    }

    private void actionDown(MotionEvent event) {
        removeCallbacks(hideIndicator);
        mLastScrollY = mScrollY;
        mDownY = event.getY();
        if (mFlingAnimator != null) {
            mFlingAnimator.cancel();
            mFlingAnimator = null;
        }
    }

    private void actionMove(MotionEvent event) {
        if (scrollable()) {
            VelocityTracker tracker = mVelocityTracker;
            tracker.computeCurrentVelocity(UNITS_SECOND, maxVelocity);
            mScrollY = mLastScrollY + mDownY - event.getY();
            mVelocity = tracker.getYVelocity();
        }
    }

    private void actionUp() {
        postDelayed(hideIndicator, 3 * UNITS_SECOND);
        releaseVelocityTracker();
        if (scrollable()) {
            if (overScrolled() && mScrollY < 0) {
                smoothScrollTo(0f);
                return;
            }
            if (overScrolled() && mScrollY > mLineHeight * (mLineCount - 1) + mLineFeedRecord.get(mLineCount - 1) + (mEnableLineFeed ? mTextHeight : 0)) {
                smoothScrollTo(mLineHeight * (mLineCount - 1) + mLineFeedRecord.get(mLineCount - 1) + (mEnableLineFeed ? mTextHeight : 0));
                return;
            }
            if (abs(mVelocity) > THRESHOLD_Y_VELOCITY) {
                doFlingAnimator(mVelocity);
            }
        }
    }

    private void doFlingAnimator(float velocity) {
        float distance = velocity / abs(velocity) * (abs(velocity) * SLIDE_COEFFICIENT);
        float to = min(max(0, (mScrollY - distance)), (mLineCount - 1) * mLineHeight + mLineFeedRecord.get(mLineCount - 1) + (mEnableLineFeed ? mTextHeight : 0));

        mFlingAnimator = ValueAnimator.ofFloat(mScrollY, to);
        mFlingAnimator.addUpdateListener(animation -> {
            mScrollY = (float) animation.getAnimatedValue();
            invalidateView();
        });


        mFlingAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mVelocity = 0;
                mFling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }
        });

        mFlingAnimator.setDuration(FLING_ANIMATOR_DURATION);
        mFlingAnimator.setInterpolator(new DecelerateInterpolator());
        mFlingAnimator.start();
    }

    private void smoothScrollTo(float toY) {
        ValueAnimator animator = ValueAnimator.ofFloat(mScrollY, toY);
        animator.addUpdateListener(animation -> {
            mScrollY = (float) animation.getAnimatedValue();
            invalidateView();
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFling = false;
                invalidateView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(640);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private boolean overScrolled() {
        return scrollable() && (mScrollY > mLineHeight * (mLineCount - 1) + mLineFeedRecord.get(mLineCount - 1) + (0) || mScrollY < 0);
    }

    private boolean scrollable() {
        if (mLyricInfo != null) {
            return !CollectionUtils.isEmpty(mLyricInfo.songLines);
        } else {
            return false;
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mShaderWidth = getWidth() * 0.141f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (scrollable()) {
            for (int i = 0; i < mLineCount; i++) {
                float x = getWidth() * 0.5f;

                float y;
                if (mEnableLineFeed && i > 0) {
                    y = getMeasuredHeight() * 0.5f + i * mLineHeight - mScrollY + mLineFeedRecord.get(i - 1);
                } else {
                    y = getMeasuredHeight() * 0.5f + i * mLineHeight - mScrollY;
                }
                if (y < 0) {
                    continue;
                }
                if (y > getHeight()) {
                    break;
                }
                if (i == mCurrentPlayLine - 1) {
                    mTextPaint.setColor(mHighLightColor);
                } else {
                    mTextPaint.setColor(mDefaultColor);
                }
                boolean showShade = mIsShade && (y > getHeight() - mShaderWidth || y < mShaderWidth);
                if (showShade) {
                    if (y < mShaderWidth) {
                        mTextPaint.setAlpha(26 + (int) (23000.0f * y / mShaderWidth * 0.01f));
                    } else {
                        mTextPaint.setAlpha(26 + (int) (23000.0f * (getHeight() - y) / mShaderWidth * 0.01f));
                    }
                } else {
                    mTextPaint.setAlpha(255);
                }

                if (mEnableLineFeed) {
                    @SuppressLint("DrawAllocation") StaticLayout staticLayout =
                            new StaticLayout(
                                    mLyricInfo.songLines.get(i).content, (TextPaint) mTextPaint,
                                    mMaxLength,
                                    Layout.Alignment.ALIGN_NORMAL,
                                    1.0f,
                                    0.0f,
                                    false
                            );
                    canvas.save();
                    canvas.translate(x, y);
                    staticLayout.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.drawText(mLyricInfo.songLines.get(i).content, x, y, mTextPaint);
                }
            }
        } else {
            mTextPaint.setColor(mHintColor);
            canvas.drawText(mDefaultHint, getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, mTextPaint);
        }
    }

    public void setLyricFile(File file) {
        if (file == null || !file.exists()) {
            reset();
            mCurrentLyricFilePath = "";
            return;
        } else if (file.getPath().equals(mCurrentLyricFilePath)) {
            return;
        } else {
            mCurrentLyricFilePath = file.getPath();
            reset();
        }

        if (file.exists()) {
            try {
                setupLyricResource(new FileInputStream(file));

                for (int i = 0; i < mLyricInfo.songLines.size(); i++) {

                    StaticLayout staticLayout =
                            new StaticLayout(
                                    mLyricInfo.songLines.get(i).content,
                                    (TextPaint) mTextPaint,
                                    (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, mWidth * 0.86f),
                                    Layout.Alignment.ALIGN_NORMAL,
                                    1.0f,
                                    0.0f,
                                    false
                            );

                    if (staticLayout.getLineCount() > 1) {
                        mEnableLineFeed = true;
                        mExtraHeight = mExtraHeight + (staticLayout.getLineCount() - 1) * mTextHeight;
                    }

                    mLineFeedRecord.add(i, (int) mExtraHeight);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            invalidateView();
        }
    }

    private void setupLyricResource(InputStream inputStream) {
        if (inputStream != null) {
            try {
                LyricInfo lyricInfo = new LyricInfo();
                lyricInfo.songLines = new ArrayList<>();
                BufferedInputStream inputStreamReader = new BufferedInputStream(inputStream);
                BufferedReader bufferedReader = getBufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    analyzeLyric(lyricInfo, line);
                }
                bufferedReader.close();
                inputStream.close();
                inputStreamReader.close();
                mLyricInfo = lyricInfo;
                mLineCount = mLyricInfo.songLines.size();
                invalidateView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            invalidateView();
        }
    }

    /**
     * 逐行解析歌词文件
     */
    private void analyzeLyric(LyricInfo lyricInfo, String line) {
        int index = line.lastIndexOf("]");
        String[] timestampStr = line.split("]");
        if (line.startsWith("[offset:")) {
            // time offset
            lyricInfo.songOffset = Long.parseLong(line.substring(8, index).trim());
            return;
        }
        if (line.startsWith("[ti:")) {
            // title
            lyricInfo.songTitle = line.substring(4, index).trim();
            return;
        }
        if (line.startsWith("[ar:")) {
            // artist
            lyricInfo.songArtist = line.substring(4, index).trim();
            return;
        }
        if (line.startsWith("[al:")) {
            // album
            lyricInfo.songAlbum = line.substring(4, index).trim();
            return;
        }
        if (line.startsWith("[by:")) {
            return;
        }
        if (timestampStr.length > 1) {
            for (int i = 0; i < timestampStr.length - 1; i++) {
                LineInfo lineInfo = new LineInfo();
                String temp = timestampStr[i];
                long millisecond = measureStartTimeMillis(temp.replace("[", ""));
                log.d("millisecond: " + millisecond);
                lineInfo.start = millisecond;
                lineInfo.content = timestampStr[timestampStr.length - 1];
                lyricInfo.songLines.add(lineInfo);
            }
        }
    }

    /**
     * 从字符串中获得时间值
     */
    private long measureStartTimeMillis(String string) {
        log.d("string: " + string);
        try {
            String[] s1 = string.split(":");
            long minute = Long.parseLong(s1[0]);
            String[] s2 = s1[1].split("\\.");
            long second = Long.parseLong(s2[0]);
            long millisecond = 0;
            if (s2.length > 1) {
                millisecond = Long.parseLong(s2[1]);
            }
            return millisecond + second * 1000 + minute * 60 * 1000;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 动态识别歌词编码格式
     * 防止出现乱码问题
     */
    private BufferedReader getBufferedReader(BufferedInputStream bufferedInputStream) throws IOException {
        String charset = "GBK";
        byte[] first3bytes = new byte[3];
        boolean checked = false;
        try {
            bufferedInputStream.mark(-1);
            int read = bufferedInputStream.read(first3bytes);
            if (read == -1) {
                bufferedInputStream.reset();
                return new BufferedReader(new InputStreamReader(bufferedInputStream, charset));
            }
            // utf-8
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {
                log.e("Lyric text type = utf-8");
                checked = true;
                charset = "UTF-8";

            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {
                log.e("Lyric text type = utf-unicode");
                charset = "unicode";
                checked = true;
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {
                log.e("Lyric text type = utf-16be");
                charset = "UTF-16BE";
                checked = true;
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {
                charset = "UTF-16LE";
                checked = true;
            }
            bufferedInputStream.reset();
            if (!checked) {
                while ((read = bufferedInputStream.read()) != -1) {
                    if (read >= 0xF0) {
                        break;
                    }
                    if (0x80 <= read && read <= 0xBF) {
                        break;
                    }
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bufferedInputStream.read();
                        if (0x80 <= read && read <= 0xBF) {
                            log.d("0x80 <= read && read <= 0xBF");
                        } else {
                            break;
                        }
                    } else if (0xE0 <= read) {
                        read = bufferedInputStream.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bufferedInputStream.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        bufferedInputStream.reset();
        return new BufferedReader(new InputStreamReader(bufferedInputStream, charset));
    }

    public void setCurrentTimeMillis(long current) {

        log.d("Lyric, current: " + current);

        int position = 0;
        if (scrollable()) {
            for (int i = 0, size = mLineCount; i < size; i++) {
                LineInfo lineInfo = mLyricInfo.songLines.get(i);
                if (lineInfo != null && lineInfo.start >= current) {
                    position = i;
                    break;
                }
                if (i == mLineCount - 1) {
                    position = mLineCount;
                }
            }
        }
        if (mCurrentPlayLine != position) {
            mCurrentPlayLine = position;
            if (!mFling) {
                smoothScrollTo(measureCurrentScrollY(position));
            }
        }
    }

    private void reset() {
        mCurrentPlayLine = 0;
        resetLyricInfo();
        invalidateView();
        mLineCount = 0;
        mScrollY = 0f;
        mEnableLineFeed = false;
        mLineFeedRecord.clear();
        mExtraHeight = 0;
    }

    private void resetLyricInfo() {
        if (mLyricInfo != null) {
            if (mLyricInfo.songLines != null) {
                mLyricInfo.songLines.clear();
                mLyricInfo.songLines = null;
            }
            mLyricInfo = null;
        }
    }

    private class LyricInfo {
        ArrayList<LineInfo> songLines;
        String songArtist;
        String songTitle;
        String songAlbum;
        long songOffset;
    }

    private class LineInfo {
        String content;
        long start;
    }

    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    private Runnable hideIndicator = this::invalidateView;
}
