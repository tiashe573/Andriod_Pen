package com.raghav.paint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.nio.file.Paths;
import java.util.ArrayList;


public class DrawView extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private static final int DEFAULT_COLOR = Color.RED;
    private static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final String TAG = "";
    private static int Brush_size = 30;
    public Color C;
    private float mX, mY;
    private Path mPath;
    //the Paint class encapsulates the color and style information about
    //how to draw the geometries,text and bitmaps
    private Paint mPaint;
    //ArrayList to store all the strokes drawn by the user on the Canvas
    private ArrayList<Stroke> paths = new ArrayList<>();
    private boolean erase = false;
    private boolean dot = false;
    //private MaskFilter mEmboss;
    //private MaskFilter mBlur;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int currentColor;
    private int strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private int mode = 0;
    private boolean isEraser;

    //Constructors to initialise all the attributes
    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPath = new Path();
        mCanvas = new Canvas();
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZ(10); //necessary
        //the below methods smoothens the drawings of the user
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //0xff=255 in decimal
        mPaint.setAlpha(0xff);
        //mPaint.setAlpha(70); // you can change number to change the transparency level
        //Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.tianhao);
        //mCanvas.drawBitmap(image, 1000, 1000, mPaint);

    }

    //this method instantiate the bitmap and object
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //set an initial color of the brush
        currentColor = DEFAULT_COLOR;
        //set an initial brush size
        strokeWidth = Brush_size;
    }

    //sets the current color of stroke
    public void setColor(int color) {
        currentColor = color;
    }

    //sets the stroke width
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void normal() {
        erase = false;
        dot = false;
    }

    public void erase() {
        erase = true;
        dot = false;
    }

    public void dot() {
        erase = false;
        dot = true;
    }

    public void undo() {
        //check whether the List is empty or not
        //if empty, the remove method will return an error
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void clear() {
        //check whether the List is empty or not
        //if empty, the remove method will return an error
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        invalidate();
    }

    //this methods returns the current bitmap
    public Bitmap save() {
        return mBitmap;
    }

    public void setd() {
        mode = 0;
    }

    public void sete() {
        mode = 1;
    }

    //this is the main method where the actual drawing takes place
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onDraw(Canvas canvas) {

        //save the current state of the canvas before,
        //to draw the background of the canvas


        canvas.save();
        //DEFAULT color of the canvas

        mCanvas.drawColor(backgroundColor);
        //mPaint.setAlpha(10);
        mCanvas.enableZ();
        //Canvas.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tianhao));

        for (Stroke fp : paths) {
            if (fp.erase) {
                Log.i(TAG, "Erase: ");
                //mBitmap.eraseColor(Color.TRANSPARENT);


                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.DST_OVER);
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            } else {
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
                mPaint.setColor(fp.color);

            }
            if (fp.dot) {
                mPaint.setPathEffect(new DashPathEffect(new float[]{10, 50}, 0));
            } else {
                mPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
            }


            mPaint.setStrokeWidth(strokeWidth);
            mCanvas.drawPath(fp.path, mPaint);

        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);


        canvas.restore();

        // canvas.drawPath(mPreviewPath, mPaint);
        //now, we iterate over the list of paths and draw each path on the canvas

    }
    /*public void setEraser(boolean isEraser) {
        //this.isEraser = isEraser;
        if (isEraser) {
            Log.i(TAG, "setEraser: ");
            //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
            mPaint.setPathEffect(new DashPathEffect(new float[] {10,50}, 0));
        } else {
            mPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
        }
    }*/

    //the below methods manages the touch response of the user on the screen

    //firstly, we create a new Stroke and add it to the paths list
    private void touchStart(float x, float y) {
        mPath = new Path();
        //Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath, erase, dot);
        paths.add(fp);

        //finally remove any curve or line from the path
        mPath.reset();
        //this methods sets the starting point of the line being drawn
        mPath.moveTo(x, y);
        //we save the current coordinates of the finger
        mX = x;
        mY = y;
    }

    //in this method we check if the move of finger on the
    // screen is greater than the Tolerance we have previously defined,
    //then we call the quadTo() method which actually smooths the turns we create,
    //by calculating the mean position between the previous position and current position
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    //at the end, we call the lineTo method which simply draws the line until
    //the end position
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    //the onTouchEvent() method provides us with the information about the type of motion
    //which has been taken place, and according to that we call our desired methods
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}