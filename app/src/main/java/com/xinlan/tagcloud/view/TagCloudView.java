package com.xinlan.tagcloud.view;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class TagCloudView extends RelativeLayout {
    private final float TOUCH_SCALE_FACTOR = .8f;
    private final float TRACKBALL_SCALE_FACTOR = 10;
    private final float tspeed;
    private final TagCloud mTagCloud;
    private float mAngleX = 0;
    private float mAngleY = 0;
    private final float centerX, centerY;
    private final float radius;
    private final Context mContext;
    private final int shiftLeft;

    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();//速度记录
    private Scroller mScroller;//滑动辅助

    private boolean isTouch = false;

    public TagCloudView(final Context mContext, final int width,
                        final int height, final List<TagView> tagList) {
        this(mContext, width, height, tagList, 6, 30, 5); // default for min/max

        mScroller = new Scroller(getContext());
        mVelocityTracker.clear();
    }


    public TagCloudView(final Context mContext, final int width,
                        final int height, final List<TagView> tagList,
                        final int textSizeMin, final int textSizeMax, final int scrollSpeed) {

        super(mContext);
        this.mContext = mContext;
        setBackgroundColor(Color.WHITE);

        tspeed = scrollSpeed;

        // set the center of the sphere on center of our screen:
        centerX = width / 2;
        centerY = height / 2;
        radius = Math.min(centerX * 0.85f, centerY * 0.85f); // use 95% of
        // screen
        // since we set tag margins from left of screen, we shift the whole tags
        // to left so that
        // it looks more realistic and symmetric relative to center of screen in
        // X direction
        shiftLeft = (int) (Math.min(centerX * 0.15f, centerY * 0.15f));

        // initialize the TagCloud from a list of tags
        // Filter() func. screens tagList and ignores Tags with same text (Case
        // Insensitive)
        mTagCloud = new TagCloud(filter(tagList), (int) radius, textSizeMin,
                textSizeMax);

        // Now Draw the 3D objects: for all the tags in the TagCloud
        for (final TagView tempTag : mTagCloud.getTags()) {

            // tempTag.setParamNo(i); // store the parameter No. related to this
            // // tag

            final LayoutParams params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.setMargins(
                    (int) ((centerX - shiftLeft) + tempTag.getLoc2DX()),
                    (int) (centerY + tempTag.getLoc2DY()), 0, 0);
            tempTag.setParams(params);

            tempTag.setSingleLine(true);

            addView(tempTag);

            // mTextView.get(i).setOnTouchListener(new OnThouch());
            // mTextView.get(i).setOnClickListener(
            // OnTagClickListener(tempTag.getUrl()));
        }

        mTagCloud.setRadius((int) radius);
        mTagCloud.create(true, centerX, centerY, shiftLeft); // to put each Tag
        // at its
        // correct
        // initial
        // location

        // update the transparency/scale of tags
        mTagCloud.setAngleX(mAngleX);
        mTagCloud.setAngleY(mAngleY);
        mTagCloud.update(centerX, centerX, shiftLeft);
    }

//    @Override
//    public boolean onTrackballEvent(final MotionEvent e) {
//        final float x = e.getX();
//        final float y = e.getY();
//
//        mAngleX = (y) * tspeed * TRACKBALL_SCALE_FACTOR;
//        mAngleY = (-x) * tspeed * TRACKBALL_SCALE_FACTOR;
//
//        mTagCloud.setAngleX(mAngleX);
//        mTagCloud.setAngleY(mAngleY);
//        mTagCloud.update(centerX, centerY, shiftLeft);
//
//        return false;
//    }

    private float startX = 0;
    private float startY = 0;

    @Override
    public boolean onTouchEvent(final MotionEvent e) {
        final float x = e.getX();
        final float y = e.getY();

        if (MotionEvent.ACTION_DOWN == e.getAction()) {
            if (!mScroller.isFinished()) {//滑动动画未结束   直接结束
                mScroller.abortAnimation();
            }//end if
            isTouch = true;

            startX = e.getX();
            startY = e.getY();

            mVelocityTracker.clear();
        } else if (MotionEvent.ACTION_MOVE == e.getAction()) {
            // rotate elements depending on how far the selection point is from
            // center of cloud
            isTouch = true;
            final float dx = x - startX;
            final float dy = y - startY;

            tagCloudRotate(dx, dy);
        } else if (MotionEvent.ACTION_UP == e.getAction() ||
                MotionEvent.ACTION_CANCEL == e.getAction()) {
            mVelocityTracker.computeCurrentVelocity(1000);
            mScroller.setFriction(0.07f);
            mScroller.fling((int) e.getX(), (int) e.getY(), (int) mVelocityTracker.getXVelocity(), (int) mVelocityTracker.getYVelocity(),
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            startX = e.getX();
            startY = e.getY();

            isTouch = false;
            //smoothScrollInertia(mVelocityTracker.getXVelocity(), mVelocityTracker.getYVelocity());
        }//end if

        mVelocityTracker.addMovement(e);

        return true;
    }

    private void tagCloudRotate(final float dx, final float dy) {
        mAngleX = (dy / radius) * tspeed * TOUCH_SCALE_FACTOR;
        mAngleY = (-dx / radius) * tspeed * TOUCH_SCALE_FACTOR;

        Log.i("TAG_CLOUD", "DX: " + dx + " DY: " + dy + " Radius: "
                + radius);

        mTagCloud.setAngleX(mAngleX);
        mTagCloud.setAngleY(mAngleY);
        mTagCloud.update(centerX, centerY, shiftLeft);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final float dx = (mScroller.getCurrX() - startX);
            final float dy = (mScroller.getCurrY() - startY);

            mAngleX = (dy / radius);
            mAngleY = (-dx / radius);

            Log.i("TAG_CLOUD", "DX: " + dx + " DY: " + dy + " Radius: "
                    + radius);

            mTagCloud.setAngleX(mAngleX);
            mTagCloud.setAngleY(mAngleY);
            mTagCloud.update(centerX, centerY, shiftLeft);
        }

        if(!isTouch && !mScroller.computeScrollOffset()){
            mTagCloud.setAngleX(0.5f);
            mTagCloud.setAngleY(1f);
            mTagCloud.update(centerX, centerY, shiftLeft);
        }

        postInvalidate();
    }



    public String urlMaker(final String url) {
        if ((url.substring(0, 7).equalsIgnoreCase("http://"))
                || (url.substring(0, 8).equalsIgnoreCase("https://"))) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    // the filter function makes sure that there all elements are having unique
    // Text field:
    public List<TagView> filter(final List<TagView> tagList) {
        // current implementation is O(n^2) but since the number of tags are not
        // that many,
        // it is acceptable.
        final List<TagView> tempTagList = new ArrayList<TagView>();

        for (final TagView tag : tagList) {
            boolean found = false;

            for (final TagView tag2 : tempTagList) {
                if (tag2.getText().toString()
                        .equalsIgnoreCase(tag.getText().toString())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                tempTagList.add(tag);
            }
        }

        return tempTagList;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVelocityTracker.clear();
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

}//end class
