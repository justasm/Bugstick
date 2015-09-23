package com.jmedeisis.bugstick;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

/**
 * A simple and flexible joystick.
 * Extends FrameLayout and should host one direct child to act as the draggable stick.
 * Use {@link #setJoystickListener(JoystickListener)} to observe user inputs.
 */
public class Joystick extends FrameLayout {
    private static final String LOG_TAG = Joystick.class.getSimpleName();

    private static final int STICK_SETTLE_DURATION_MS = 100;
    private static final Interpolator STICK_SETTLE_INTERPOLATOR = new DecelerateInterpolator();

    private int touchSlop;

    private float centerX, centerY;
    private float radius;

    private View draggedChild;
    private boolean detectingDrag;
    private boolean dragInProgress;

    private float downX, downY;
    private static final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;

    private boolean locked;

    private boolean startOnFirstTouch = true;
    private boolean forceSquare = true;
    private boolean hasFixedRadius = false;

    public enum MotionConstraint {
        NONE,
        HORIZONTAL,
        VERTICAL
    }

    private MotionConstraint motionConstraint = MotionConstraint.NONE;

    private JoystickListener listener;

    public Joystick(Context context) {
        super(context);
        init(context, null);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Joystick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Joystick(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();

        if (null != attrs) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Joystick);
            startOnFirstTouch = a.getBoolean(R.styleable.Joystick_start_on_first_touch, startOnFirstTouch);
            forceSquare = a.getBoolean(R.styleable.Joystick_force_square, forceSquare);
            hasFixedRadius = a.hasValue(R.styleable.Joystick_radius);
            if (hasFixedRadius) {
                radius = a.getDimensionPixelOffset(R.styleable.Joystick_radius, (int) radius);
            }
            motionConstraint = MotionConstraint.values()[a.getInt(R.styleable.Joystick_motion_constraint,
                    motionConstraint.ordinal())];
            a.recycle();
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = (float) w / 2;
        centerY = (float) h / 2;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && !hasFixedRadius) {
            recalculateRadius(right - left, bottom - top);
        }
    }

    private void recalculateRadius(int width, int height) {
        float stickHalfWidth = 0;
        float stickHalfHeight = 0;
        if (hasStick()) {
            final View stick = getChildAt(0);
            stickHalfWidth = (float) stick.getWidth() / 2;
            stickHalfHeight = (float) stick.getHeight() / 2;
        }

        switch (motionConstraint) {
            case NONE:
                radius = (float) Math.min(width, height) / 2 - Math.max(stickHalfWidth, stickHalfHeight);
                break;
            case HORIZONTAL:
                radius = (float) width / 2 - stickHalfWidth;
                break;
            case VERTICAL:
                radius = (float) height / 2 - stickHalfHeight;
                break;
        }
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;

        if (!hasStick()) {
            Log.w(LOG_TAG, LOG_TAG + " has no draggable stick, and is therefore not functional. " +
                    "Consider adding a child view to act as the stick.");
        }
    }

    /**
     * Locks the stick position when next the user releases it.
     * Note that {@link JoystickListener#onUp()} will not be called upon release.
     * Resets to unlocked state after subsequent touch.
     */
    @SuppressWarnings("unused")
    public void lock() {
        locked = true;
    }

    /**
     * @return Distance in pixels a touch can wander before the joystick thinks the user is
     * manipulating the stick.
     */
    @SuppressWarnings("unused")
    public int getTouchSlop() {
        return touchSlop;
    }

    /**
     * @param touchSlop Distance in pixels a touch can wander before the joystick thinks the user is
     *                  manipulating the stick.
     */
    @SuppressWarnings("unused")
    public void setTouchSlop(int touchSlop) {
        this.touchSlop = touchSlop;
    }

    @SuppressWarnings("unused")
    public MotionConstraint getMotionConstraint() {
        return motionConstraint;
    }

    @SuppressWarnings("unused")
    public void setMotionConstraint(MotionConstraint motionConstraint) {
        this.motionConstraint = motionConstraint;

        if (!hasFixedRadius) recalculateRadius(getWidth(), getHeight());
    }

    @SuppressWarnings("unused")
    public float getRadius() {
        return radius;
    }

    /**
     * @param radius The maximum offset in pixels from the center that the stick is allowed to move.
     */
    @SuppressWarnings("unused")
    public void setRadius(float radius) {
        this.radius = radius;
    }

    @SuppressWarnings("unused")
    public boolean isStartOnFirstTouch() {
        return startOnFirstTouch;
    }

    /**
     * @param startOnFirstTouch If true, the stick activates immediately on the initial touch.
     *                          Else, the user must begin to drag their finger across the joystick
     *                          for the stick to activate.
     */
    @SuppressWarnings("unused")
    public void setStartOnFirstTouch(boolean startOnFirstTouch) {
        this.startOnFirstTouch = startOnFirstTouch;
    }

    /*
    TOUCH EVENT HANDLING
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (detectingDrag || !hasStick()) return false;

                downX = event.getX(0);
                downY = event.getY(0);
                activePointerId = event.getPointerId(0);

                onStartDetectingDrag();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (INVALID_POINTER_ID == activePointerId) break;
                if (detectingDrag && dragExceedsSlop(event)) {
                    onDragStart();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId != activePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnded();

                onStopDetectingDrag();
                break;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (!detectingDrag) return false;
                if (startOnFirstTouch) onDragStart();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (INVALID_POINTER_ID == activePointerId) break;

                if (dragInProgress) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    float latestX = event.getX(pointerIndex);
                    float latestY = event.getY(pointerIndex);

                    float deltaX = latestX - downX;
                    float deltaY = latestY - downY;

                    onDrag(deltaX, deltaY);
                    return true;
                } else if (detectingDrag && dragExceedsSlop(event)) {
                    onDragStart();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId != activePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnded();

                if (dragInProgress) {
                    onDragStop();
                } else {
                    onStopDetectingDrag();
                }
                return true;
            }
        }

        return false;
    }

    private boolean dragExceedsSlop(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(activePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);
        final float dx = Math.abs(x - downX);
        final float dy = Math.abs(y - downY);

        switch (motionConstraint) {
            case NONE:
                return dx * dx + dy * dy > touchSlop * touchSlop;
            case HORIZONTAL:
                return dx > touchSlop;
            case VERTICAL:
                return dy > touchSlop;
        }
        return false;
    }

    private void onTouchEnded() {
        activePointerId = INVALID_POINTER_ID;
    }

    private boolean hasStick() {
        return getChildCount() > 0;
    }

    private void onStartDetectingDrag() {
        detectingDrag = true;
        if (null != listener) listener.onDown();
    }

    private void onStopDetectingDrag() {
        detectingDrag = false;
        if (!locked && null != listener) listener.onUp();

        locked = false;
    }

    private void onDragStart() {
        dragInProgress = true;
        draggedChild = getChildAt(0);
        draggedChild.animate().cancel();
        onDrag(0, 0);
    }

    private void onDragStop() {
        dragInProgress = false;

        if (!locked) {
            draggedChild.animate()
                    .translationX(0).translationY(0)
                    .setDuration(STICK_SETTLE_DURATION_MS)
                    .setInterpolator(STICK_SETTLE_INTERPOLATOR)
                    .start();
        }

        onStopDetectingDrag();
        draggedChild = null;
    }

    /**
     * Where most of the magic happens. What, basic trigonometry isn't magic?!
     */
    private void onDrag(float dx, float dy) {
        float x = downX + dx - centerX;
        float y = downY + dy - centerY;

        switch (motionConstraint) {
            case HORIZONTAL:
                y = 0;
                break;
            case VERTICAL:
                x = 0;
                break;
        }

        float offset = (float) Math.sqrt(x * x + y * y);
        if (x * x + y * y > radius * radius) {
            x = radius * x / offset;
            y = radius * y / offset;
            offset = radius;
        }

        final double radians = Math.atan2(-y, x);
        final float degrees = (float) (180 * radians / Math.PI);

        if (null != listener) listener.onDrag(degrees, 0 == radius ? 0 : offset / radius);

        draggedChild.setTranslationX(x);
        draggedChild.setTranslationY(y);
    }

    /*
    FORCE SQUARE
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!forceSquare) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }

    /*
    CENTER CHILD BY DEFAULT
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        LayoutParams params = new LayoutParams(getContext(), attrs);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        LayoutParams params = new LayoutParams(p);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /*
    ENSURE MAX ONE DIRECT CHILD
     */
    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(LOG_TAG + " can host only one direct child");
        }

        super.addView(child, index, params);
    }
}
