package yl.demo.pathHelper.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MapView extends View {
	private final static String TAG = "mapView";
	private final static float RATE_MAX = 3;
	private final static float RATE_MIN = 0.4f;
	private final static int MOVE_THRESHLOD = 4;
	
	private Bitmap mSourceBitmap;
	private Bitmap mDestinationBitmap;
	private Bitmap mMapBitmap;
	private float mScaleRate;
	private float mTempRate;
	private float mLeft;
	private float mTop;
	private PointF[] mPrePointF = {new PointF(), new PointF()};
	private PointF mSourcePointF = new PointF();
	private PointF mDestinationPointF= new PointF();
	private boolean hasSetPath;
	private List<PointF> mPaths;
	private Paint mLinePaint;
	private int mPointNo;
	private int mMoveCount;
	private Context mContext;
	
	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		mLeft = mTop = 0;
		hasSetPath = false;
		mLinePaint = new Paint();
		mLinePaint.setColor(Color.RED);
		mLinePaint.setStrokeJoin(Paint.Join.ROUND);
		mLinePaint.setStrokeCap(Paint.Cap.ROUND);
		mLinePaint.setStrokeWidth(7);
	}

	public MapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		mLeft = mTop = 0;
		hasSetPath = false;
		mLinePaint = new Paint();
		mLinePaint.setColor(Color.RED);
		mLinePaint.setStrokeJoin(Paint.Join.ROUND);
		mLinePaint.setStrokeCap(Paint.Cap.ROUND);
		mLinePaint.setStrokeWidth(7);
	}
	
	public void setMapBitmap( Bitmap map ) {
		mMapBitmap = map;
	}
	
	public void setMyPositionBitmap( Bitmap sourceBitmap ) {
		mSourceBitmap = sourceBitmap;
	}
	
	public void setDestinationBitmap( Bitmap destinationBitmap ) {
		mDestinationBitmap = destinationBitmap;
	}
	
	public void setScaleRate(float rate) {
		mScaleRate = rate;
	}
	
	public void upMapScaleRate() {
		mScaleRate +=0.4;
		postInvalidate();
	}
	
	public void downMapScaleRate() {
		mScaleRate -=0.4;
		postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.translate(mLeft, mTop);
		canvas.scale(mScaleRate, mScaleRate);
		canvas.drawBitmap(mMapBitmap, 0, 0, new Paint());
		PointF sourceMapPointF = transformToScreenCoordinate(mSourcePointF);
		canvas.drawBitmap(mSourceBitmap, 0+sourceMapPointF.x-mSourceBitmap.getWidth()/2, 0+sourceMapPointF.y-mSourceBitmap.getHeight()/2, new Paint());
		canvas.drawBitmap(mDestinationBitmap, 0+mDestinationPointF.x-mDestinationBitmap.getWidth()/2, 0+mDestinationPointF.y-mDestinationBitmap.getHeight()/2, new Paint());
		
		if ( hasSetPath ) {
			List<PointF> paths = new ArrayList<PointF>();
			for ( int i = 0; i < mPaths.size();i++ ) {
				paths.add(transformToScreenCoordinate(mPaths.get(i)));
			}
			for ( int i = 0; i < mPaths.size()-1; i++ ) {
				canvas.drawLine(paths.get(i).x, paths.get(i).y, paths.get(i+1).x, paths.get(i+1).y, mLinePaint);
			}
		}	
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mPointNo = 1;
			mMoveCount = 0;
			break;	
		case MotionEvent.ACTION_POINTER_DOWN:
			mMoveCount = 0;
			mPointNo = 2;
			try {
				mPrePointF[1].set(event.getX(1), event.getY(1));
				mTempRate = new PointF(mPrePointF[1].x-mPrePointF[0].x, mPrePointF[1].y-mPrePointF[0].y).length();
			} catch (IllegalArgumentException e) {
				// TODO: handle exception
			}
			break;
		case MotionEvent.ACTION_MOVE:
			mMoveCount++;
			if (mPointNo == 1) {
				mLeft += event.getX()-mPrePointF[0].x;
				mTop += event.getY()-mPrePointF[0].y;
				//setBoundaryInfo();
			} else if (mPointNo == 2 && event.getPointerCount() == 2) {
				mScaleRate *= new PointF(mPrePointF[1].x-mPrePointF[0].x, mPrePointF[1].y-mPrePointF[0].y).length() / mTempRate;
				mTempRate = new PointF(mPrePointF[1].x-mPrePointF[0].x, mPrePointF[1].y-mPrePointF[0].y).length();
				try {
					mPrePointF[1].set(event.getX(1), event.getY(1));
				} catch (IllegalArgumentException e) {
					// TODO: handle exception
				}
				mScaleRate = mScaleRate > RATE_MAX ? RATE_MAX : mScaleRate;
				mScaleRate = mScaleRate < RATE_MIN ? RATE_MIN : mScaleRate;
			}
			postInvalidate();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mPointNo = 0;
			break;
		case MotionEvent.ACTION_UP:
			if ( mMoveCount <= MOVE_THRESHLOD ) {
				mDestinationPointF.set((event.getX()-mLeft)/mScaleRate, (event.getY()-mTop)/mScaleRate);
				clearPath();
				postInvalidate();
			}
			mPointNo = 0;
			break;
		default:
			break;
		}
		mPrePointF[0].set(event.getX(), event.getY());
		return true;
	}
	
	private void setBoundaryInfo() {
		// TODO Auto-generated method stub
		if ( mLeft < mMapBitmap.getWidth() * mScaleRate - getWidth() ) {
			mLeft = mMapBitmap.getWidth() * mScaleRate - getWidth();
		} else if ( mLeft > 0 ) {
			mLeft = 0;
		}
		if ( mTop + mMapBitmap.getHeight() * mScaleRate < getHeight() ) {
			mTop = mMapBitmap.getHeight() * mScaleRate - getHeight();
		} else if ( mTop > 0 ) {
			mTop = 0;
		}
	}

	public void setPath(List<PointF> paths) {
		clearPath();
		hasSetPath = true;
		mPaths = paths;
	}
	
	public void clearPath() {
		hasSetPath = false;
		mPaths = null;
		postInvalidate();
	}
	
	public void setMyPosition( float x, float y ) {
		mSourcePointF.set(x, y);
		postInvalidate();
	}
	
	public void setDestinationPosition( float x, float y ) {
		mDestinationPointF.set(x, y);
		postInvalidate();
	}
	
	public void setPathColor( int color ) {
		mLinePaint.setColor(color);
	}
	
	public void setPathStroke( int stroke ) {
		mLinePaint.setStrokeWidth(stroke);
	}
	
	public PointF getMyPosition() {
		return mSourcePointF;
	}
	
	public PointF getDestinationPosition() {
		return mDestinationPointF;
	}
	
	public PointF transformToScreenCoordinate(PointF pointf) {
		Log.e(TAG, "width:" + getWidth());
		Log.e(TAG, "height:" + getHeight());
		Log.e(TAG, "BitmapWidth:" + mMapBitmap.getWidth());
		Log.e(TAG, "BitmapHeight:" + mMapBitmap.getHeight());
		return pointf;
	}

}
