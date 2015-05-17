package com.example.bugstick;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * A simple toy view for demo purposes.
 * Displays a circle and a trail that can be controlled via {@link #setVelocity(float, float)}.
 */
public class BugView extends View implements TimeAnimator.TimeListener {

    private static final float BUG_RADIUS_DP = 4f;
    private static final float BUG_TRAIL_DP = 200f;

    private Paint paint;
    private TimeAnimator animator;

    private float density;
    private int width, height;
    private PointF position;
    private PointF velocity;
    private Path path;
    private PathMeasure pathMeasure;

    public BugView(Context context) {
        super(context);
        init(context);
    }

    public BugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BugView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BugView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        animator = new TimeAnimator();
        animator.setTimeListener(this);

        paint = new Paint();
        paint.setColor(Color.WHITE);

        density = getResources().getDisplayMetrics().density;

        path = new Path();
        pathMeasure = new PathMeasure();
        position = new PointF();
        velocity = new PointF();
    }

    /**
     * Start applying velocity as soon as view is on-screen.
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator.start();
    }

    /**
     * Stop animations when the view hierarchy is torn down.
     */
    @Override
    public void onDetachedFromWindow() {
        animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        position.set(width / 2, height / 2);
        path.rewind();
        path.moveTo(position.x, position.y);
    }

    /**
     * Set bug velocity in dips.
     */
    public void setVelocity(float vxDps, float vyDps) {
        velocity.set(vxDps * density, vyDps * density);
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        final float dt = deltaTime / 1000f; // seconds

        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        bound();

        path.lineTo(position.x, position.y);

        invalidate();
    }

    /**
     * Bound position and reflect velocity.
     */
    private void reflect() {
        boolean flipX = false, flipY = false;
        if (position.x > width) {
            position.x = position.x - 2 * (position.x - width);
            flipX = true;
        } else if (position.x < 0) {
            position.x = -position.x;
            flipX = true;
        }
        if (position.y > height) {
            position.y = position.y - 2 * (position.y - height);
            flipY = true;
        } else if (position.y < 0) {
            position.y = -position.y;
            flipY = true;
        }
        if (flipX) velocity.x *= -1;
        if (flipY) velocity.y *= -1;
    }

    /**
     * Bound position.
     */
    private void bound() {
        if (position.x > width) {
            position.x = width;
        } else if (position.x < 0) {
            position.x = 0;
        }
        if (position.y > height) {
            position.y = height;
        } else if (position.y < 0) {
            position.y = 0;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);

        pathMeasure.setPath(path, false);
        float length = pathMeasure.getLength();

        if (length > BUG_TRAIL_DP * density) {
            // Note - this is likely a poor way to accomplish the result. Just for demo purposes.
            @SuppressLint("DrawAllocation")
            PathEffect effect = new DashPathEffect(new float[]{length, length}, -length + BUG_TRAIL_DP * density);
            paint.setPathEffect(effect);
        }

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(position.x, position.y, BUG_RADIUS_DP * density, paint);
    }
}
