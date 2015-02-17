package app.ctrlyati.rollmenu;

import java.util.ArrayList;

import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Hello Sire,
 * This class is use for creating circle menu. You can roll, swipe, fling, select, what ever...
 * 
 * So, if you want to use, just use it :)
 * 
 * Ah, before I forgot! if you config or write something down please DO NOT AUTOFOMAT this thing. 
 * this file have many comments with indents to make it easier to learn about this class, file.
 * 
 * @author CTRLYATI
 *
 */
public class RollMenu extends ViewGroup {
	
	// Static Final Value
	public static final float ROLL_DIRECTION_NONE = 0f;
	public static final float ROLL_DIRECTION_LEFT = 1f;
	public static final float ROLL_DIRECTION_RIGHT = -1f;
	
	public static final float ROTATION_MULTIPLEXER_NORMAL = 1f;
	public static final float ROTATION_MULTIPLEXER_TO_CENTER = 90f;
	
	public static final float ROTATION_INCREMENTAL_NORMAL = 0;
	public static final float ROTATION_INCREMENTAL_90 = 90f;
	public static final float ROTATION_INCREMENTAL_270 = 270f;
	
	public static final float ROTATION_FUNCTION_NORMAL = 0;
	
	public boolean mNotifedSelect = false;
	
	// Just a Value
	private Context mContext; 					// just the context
	private boolean mIsSnip = true; 			// is it snip to an item
	private boolean mScrollFree = false; 		// is it scrolling 1by1 or until met
												// the threshold to stop
	private int mThreshold = 30; 				// minimum speed that make this stop (mIsSnip
												// must be true to make it work)
	private int mStartThreshold = 60; 			// minimum speed that make this start
												// scroll freely
	private int mShow = 3; 						// how many item show on the wheel
	private boolean mShowText = true;
	private boolean mPressed = false;

	private boolean mFlingEnabled = true;
	private boolean mTest = false;
	
	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private int mViewTop = 0;
	private int mViewLeft = 0;
	private int mDiameter = Integer.MIN_VALUE;

	private boolean mGonnaNotifySelected = false;
	
	// ViewValue;
	private ViewGroup mViewGroup;
	private ArrayList<View>	mViews = new ArrayList<View>();
	
	// Motion, Animation Value
	private int mPrevSelecting = 0;
	private int mSelecting = 0; 				// currently showing
	
	private float mMinSpeed = 20;				// minimun moving speed
	private float mMaxSpeed = 250f;	 			// as degree/s
	private float mMaxFlingVelocity = 750f;		// as degree/s
	private float mMaxSpeedDt = 20; 			// as degree/dt
	private float mSpeed = 0; 					// as degree/s
	private float mMockSpeed = 0; 				// as degree/s
	private float mMockSpeedDt = 0; 			// as degree/dt
	private float mSpeedDeceasing = 150f;
	
	private float mMultiplexer = ROTATION_MULTIPLEXER_NORMAL;
	private float mIncremental = ROTATION_INCREMENTAL_NORMAL;

	private float mLastRotation = 0;				 	// as degree
	private float mRotation = 0; 						// as degree
	private float mStartRotation = Float.MAX_VALUE; 	// as degree , use when it
														// first touch
	private long mNow = 0; 					// current time in milliseconds
	private long mLast = 0; 				// time of the last interval in milliseconds
	private long mDeltaTime = 0; 			// delta of the time
	private boolean mAnimating = false; 	// is it animating now

	private float mRotationPerElement;
	private float mLastRot;
	private float mRot;

	private Paint mPaintBlack = new Paint();
	private Paint mPaintWhite = new Paint();
	private Paint mPaintRed = new Paint();

	private float mCurrentDirection = ROLL_DIRECTION_NONE;
	
	public String mDogLog = "";
	public String mMiceLog = "";

	// Touch Capture
	private PointerCoords mPointerCoords = new PointerCoords();
	private GestureDetectorCompat mGestureDetector;
	private OnGestureListener mGestureListener = new OnGestureListener() {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {return false;}

		@Override
		public void onShowPress(MotionEvent e) {}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {return false;}

		@Override
		public void onLongPress(MotionEvent e) {}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			float velocity = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY) / 25f;
			mDogLog += "\nvelocity: " + velocity;

			if(velocity > mMaxFlingVelocity){
				velocity = mMaxFlingVelocity;
			}
			
			float dif = mRotation - mLastRotation;

			if (dif > 0) {
				dif = ROLL_DIRECTION_LEFT;
			} else {
				dif = ROLL_DIRECTION_RIGHT;
			}

			startRotate(velocity, dif);

			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			event.getPointerCoords(event.getPointerCount() - 1, mPointerCoords);
			mNotifedSelect = false;

			if (event.getAction() == MotionEvent.ACTION_UP) {
				mNow = mLast = System.currentTimeMillis();
				mPressed = false;
				
			} else {
				mSpeed = 0;
				mPressed = true;
				mNow = System.currentTimeMillis();
				mDogLog = "";
			}

			if(mFlingEnabled) mGestureDetector.onTouchEvent(event);

			mDeltaTime = mNow - mLast;
			update();
			mLast = mNow;

			return true;
		}
	};

	// Adapter
	private RollMenuAdapter mAdapter;

	private ArrayList<RollMenuListener> mListeners = new ArrayList<RollMenuListener>();

	public RollMenu(Context context) {
		super(context);

		mContext = context;
		mNow = mLast = System.currentTimeMillis();
		this.setOnTouchListener(mOnTouchListener);
		mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);

		this.setWillNotDraw(false);
		
		mPaintBlack.setColor(Color.BLACK);
		mPaintRed.setColor(Color.RED);
		mPaintWhite.setColor(Color.WHITE);
		mPaintWhite.setTextSize(50);

	}

	public RollMenu(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mNow = mLast = System.currentTimeMillis();
		this.setOnTouchListener(mOnTouchListener);
		mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);

		this.setWillNotDraw(false);
		
		mPaintBlack.setColor(Color.BLACK);
		mPaintRed.setColor(Color.RED);
		mPaintWhite.setColor(Color.WHITE);
		mPaintWhite.setTextSize(50);
	}

	public Runnable mSnipRunner = new Runnable() {
		@Override
		public void run() {
			
			mAnimating = true;

			mNow = System.currentTimeMillis();
			mDeltaTime = mNow - mLast;

			mMiceLog = "";
			updateSnip(mDeltaTime);

			mLast = mNow;

			if (Math.abs(mSpeed) < mMinSpeed) {
				mSpeed = 0;
			}

			if (mSpeed == 0 || mPressed) {
				if(mSpeed==0) notifySelected();
				return;
			}
			RollMenu.this.post(this);
			
		}
	};

	private Runnable mSpinRunner = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mNow = System.currentTimeMillis();
			mDeltaTime = mNow - mLast;

			mMiceLog = "";
			
			notifyChaning();
			updateAnimation(mDeltaTime);

			mLast = mNow;

			// invalidate();
			// notifyUpdate((int)mDeltaTime);

			if (Math.abs(mSpeed) < mMinSpeed) {
				mSpeed = 0;
			}

			if (mSpeed == 0 || mPressed) {
				if(mPressed) return;
				startSnip();
				return;
			}
			RollMenu.this.post(this);

		}
	};
	
	private int mWidth = 0;
	private int mHeight = 0;

	public void startRotate(float velocity, float direction) {

		mSpeed = velocity * direction;
		
		notifyBegin();
		
		mAnimating = true;
		
		Log.d("Roll.Animate", "Start Speed = "+mSpeed+", "+velocity+", "+direction);
		RollMenu.this.post(mSpinRunner);

	}

	public void startSnip(){
//		RollMenu.this.post(mSnipRunner);
		snip();
	}
	
	/**
	 * call this to update force update
	 */
	public void update() {
		
//		Log.d("Roll.Update", "mAnimating : "+mAnimating);
//		Log.d("Roll.Update", "mPressed : "+mPressed);
//		Log.d("Roll.Update", "_________________________________");

		if(mAnimating){
			startSnip();
		}else{
			if (mPressed) {
				// mLog = "";
	
				float x = mPointerCoords.x;
				float y = mPointerCoords.y;
				
				int w = this.getWidth();
				int h = this.getHeight();
				int ch = w / 2 + mViewLeft;
				int cv = h / 2 + mViewTop;
	
				float dx = x - ch;
				float dy = y - cv;
	
				float r = (float) Math.sqrt(dx * dx + dy * dy);
	
				float cos = dx / r;
				float sin = dy / r;
	
				float dif = 0f;
	
				mDogLog += "\tr: " + r + "\n";
				mDogLog += "\tdx: " + dx + "\n";
				mDogLog += "\tdy: " + dy + "\n";
				mDogLog += "\tcos: " + cos + "\n";
				mDogLog += "\tsin: " + sin + "\n";
	
				float rot = -(float) Math.atan2(dy, dx);
				mRot = (float) Math.toDegrees(rot);
	
				mDogLog += "\trad: " + rot + "\n";
				mDogLog += "\trot: " + mRot + "\n";
				mDogLog += "\tlast rot: " + mLastRot + "\n";
	
				dif = mRot - mLastRot;
				mDogLog += "\tdif rot: " + dif + "\n";
	
				while (Math.abs(mRot - mLastRot) > 180) {
					if (mRot - mLastRot > 180) {
						mRot -= 360;
					} else if (mLast - mRot > 180) {
						mRot += 360;
					}
				}
	
				dif = mRot - mLastRot;
				mDogLog += "\tdif rot: " + dif + "\n";
	
				mDogLog += "\trot: " + mRot + "\n";
				mDogLog += "\tlast rot: " + mLastRot + "\n";
	
				mLastRot = mRot;
	
				mDogLog += "\tmRotation: " + mRotation + "\n";
	
				if (mStartRotation == Float.MAX_VALUE) {
					mStartRotation = mRotation - mRot;
				}
				mRotation = mRot + mStartRotation;
	
				mDogLog += "\tmRotationPerElement: " + mRotationPerElement + "\n";
	
			} else {
				mRotation %= 360;
				mStartRotation = Float.MAX_VALUE;
				startSnip();
			}
		}

		invalidate();
		notifyUpdate((int) mDeltaTime);
	}

	private void updateAnimation(final long dt) {

		// Log.d("Roll.Animation","Speed "+mSpeed);

		mMiceLog += "dt: " + dt;

		float delta = dt / 1000f; // to make it "from millisecond to second"
		mMiceLog += "\ndt (frame): " + delta;

		mMockSpeed = mSpeed;
		if (mMockSpeed > mMaxSpeed) {
			mMockSpeed = mMaxSpeed;
		} else if (mMockSpeed < -mMaxSpeed) {
			mMockSpeed = -mMaxSpeed;
		}
		mMiceLog += "\nmSpeed: " + mSpeed;
		mMiceLog += "\nmMockSpeed: " + mMockSpeed;

		mMockSpeedDt = mMockSpeed * delta;
		if (mMockSpeedDt > mMaxSpeedDt) {
			mMockSpeedDt = mMaxSpeedDt;
		}else if(mMaxSpeedDt < mMaxSpeedDt){
			mMockSpeedDt = -mMaxSpeedDt;
		}
		mMiceLog += "\nmMockSpeedDt: " + mMockSpeedDt;

		mRotation += mMockSpeedDt;
		mMiceLog += "\nmRotation: " + mRotation;

		 invalidate();

		mLastRotation = mRotation;

		if (mSpeed > 0) {
			mSpeed -= mSpeedDeceasing * delta;
		} else {
			mSpeed += mSpeedDeceasing * delta;
		}

		mMiceLog += "\n\n Animating: " + mAnimating;
		
		notifyUpdate((int) dt);
	}

	private void updateSnip(final long dt) {
		mRotationPerElement = 360f / mShow;

		float rot = mRotation - mRotationPerElement / 2;
		float snipTo = 0;
		
		if (rot % (mRotationPerElement) != 0) {
			float a = rot % mRotationPerElement;
			float b = mRotationPerElement - Math.abs(a);
			
			if (Math.min(Math.abs(a), Math.abs(b)) != Math.abs(b)) {
				snipTo = mRotation - a;
			} else {
				if (mRotation < 0) {
					snipTo = mRotation - b;
				} else {
					snipTo = mRotation + b;
				}
			}
		}
		
		mRotation = mRotation + snipTo*dt*2;
		
		invalidate();
	}
	
	public void snip() {
		
//		Log.d("Roll.Snip","snip!");
//		Log.d("Roll.Snip","____________________");
		
		mAnimating = false;
		mGonnaNotifySelected = true;
		
		if (!mIsSnip){
			invalidate();
			return;
		}

		mRotationPerElement = 360f / mShow;

		float rot = mRotation - mRotationPerElement / 2;

		if (rot % (mRotationPerElement) != 0) {
			float a = rot % mRotationPerElement;
			float b = mRotationPerElement - Math.abs(a);

			if (Math.min(Math.abs(a), Math.abs(b)) != Math.abs(b)) {
				mRotation -= a;
			} else {
				if (mRotation < 0) {
					mRotation -= b;
				} else {
					mRotation += b;
				}
			}
		}
		
		mGonnaNotifySelected = true;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = this.getWidth(); 
		if(mViewWidth!=0) width = mViewWidth;
		
		int height = this.getHeight();
		if(mViewHeight!=0) height = mViewHeight;
		
		int scale = Math.min(width, height);
		int radian = scale / 2;
		int gap = scale / 4 / 8;
		gap = 0;
		
		int diameter = radian - radian / 2 - radian / 4;
		if(mDiameter != Integer.MIN_VALUE){
			diameter = mDiameter;
		}

		mRotationPerElement = 360f / mShow;

		int centerHorizontal = getWidth() / 2 + mViewLeft;
		int centerVertical = getHeight() / 2 + mViewTop;

		if (mAdapter == null)
			return;

		if (Math.abs(mLastRotation - mRotation) > 180) {
			if (mLastRotation - mRotation > 180) {
				mRotation -= 360;
				mStartRotation -= 360;
			} else if (mLastRotation - mRotation < -180) {
				mRotation += 360;
				mStartRotation += 360;
			}
		}

		// if(mRotation<0){
		// mRotation+=360;
		// }

		if (Math.abs(mRotation - mLastRotation) > mRotationPerElement) {
			mPressed = false;
			mStartRotation = Float.MAX_VALUE;
		}

		float mockRotation = mRotation % mRotationPerElement
				- mRotationPerElement / 2;
		if (mRotation < 0) {
			mockRotation += mRotationPerElement;
		}

		int mightSelect = (int) (mAdapter.getCount() - (180 + mRotation)
				/ mRotationPerElement);
		while (mightSelect < 0) {
			mightSelect += mAdapter.getCount();
		}
		mightSelect %= mAdapter.getCount();

		for (int i = 0; i < mShow; i++) {

			int pos = 0;
			pos = i + mightSelect;

			if (pos < 0) {
				pos += mAdapter.getCount();
			}
			pos %= mAdapter.getCount();

			float rotationElement = (float) Math.toRadians(mRotationPerElement * i + 90 + mockRotation);
			
			//DRAW VIEW
			canvas.save();
			
			View view = getChildAt(pos);
			view = mAdapter.getView(pos, view, this);
			view.measure(diameter, diameter);
			view.layout(0, 0, diameter, diameter);
			
			float centerElementX = (float) (centerHorizontal - Math.cos(rotationElement) * (radian - diameter / 2f - gap));
			float centerElementY = (float) (centerVertical + Math.sin(rotationElement) * (radian - diameter / 2f - gap));
			
//			canvas.translate(centerElementX - view.getWidth()/2 - view.getWidth()/4, centerElementY - view.getHeight()/2 - view.getHeight()/4);
//			canvas.translate(centerElementX - view.getWidth()/2, centerElementY - view.getHeight()/2);
			
			canvas.translate(centerElementX, centerElementY);
			canvas.rotate((mShow/2 - i) * mRotationPerElement - mockRotation%mRotationPerElement);
			canvas.translate(-diameter/2, -diameter/2);
			
			view.draw(canvas);
			canvas.restore();
			
			
			
			
			if(mTest){
				
				//DRAW DUMMY POINT
				float x = (float) (centerHorizontal - Math.cos(rotationElement) * (radian - diameter / 2f - gap));
				float y = (float) (centerVertical + Math.sin(rotationElement) * (radian - diameter / 2f - gap));
//				float nx = (float) (centerHorizontal - Math.cos(rotationElement+mRotationPerElement) * (radian - diameter / 2f - gap));
//				float ny = (float) (centerVertical + Math.sin(rotationElement+mRotationPerElement) * (radian - diameter / 2f - gap));
				
				
				if (i == mShow / 2) {
					mSelecting = pos;
					canvas.drawCircle(x, y, (float) 5, mPaintRed);
				}else{
					canvas.drawCircle(x, y, (float) 5, mPaintBlack);
				}
				
				canvas.drawLine(centerHorizontal, centerVertical, x, y, mPaintRed);
//				canvas.drawLine(nx, ny, x, y, mPaintRed);
	
//				canvas.Rect(centerElementX -  view.getWidth()/2 - view.getWidth()/4, 
//						centerElementY - view.getHeight()/2 - view.getHeight()/4, 
//						centerElementX  + view.getWidth()/2 + view.getWidth()/4,
//						centerElementY  + view.getHeight()/2 + view.getHeight()/4,
//						);
				
				//DRAW TEXT
				canvas.save();
				canvas.translate(x, y);
				canvas.drawText("" + i, 0, 0, mPaintWhite);
				canvas.restore();
			}
			
			if(i==mShow/2){
				mSelecting = pos;
			}
			
		}

		mLastRotation = mRotation;

		if(mPrevSelecting!=mSelecting){
			notifyChaning();
			mPrevSelecting = mSelecting;
		}
		if(mGonnaNotifySelected){
			notifySelected();
			mGonnaNotifySelected = false;
		}
		
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		if(w>0){
			mWidth = w;
			mHeight = h;
		}
	}
	
	/****************************************************************************************************/

	/**
	 * To Clear all Listener that watching this object, and then add a listener
	 * to this object.<br>
	 * <br>
	 * 
	 * So you can use addRollMenuListener instead.
	 * 
	 * @param listener
	 *            an object that register to this object
	 */
	public void setRollMenuListener(RollMenuListener listener) {
		mListeners.clear();
		mListeners.add(listener);
	}

	public void addRollMenuListener(RollMenuListener listener) {
		mListeners.add(listener);
	}

	public boolean removeRollMenuListener(RollMenuListener listener) {
		return mListeners.remove(listener);
	}

	public RollMenuListener removeRollMenuListener(int i) {
		return mListeners.remove(i);
	}

	public void notifyChaning() {
		for (RollMenuListener l : mListeners) {
			l.onChanging(mSelecting);
		}
	}

	public void notifySelected() {
		if(mNotifedSelect){
			return;
		}
			
		for (RollMenuListener l : mListeners) {
			l.onSelected(getSelecting());
		}
		mNotifedSelect = true;
	}

	public void notifyBegin() {
		for (RollMenuListener l : mListeners) {
			l.onBegin(mSelecting);
		}
	}

	public void notifyUpdate(int dt) {
		for (RollMenuListener l : mListeners) {
			l.onUpdate(mSelecting, dt);
		}
	}

	/****************************************************************************************************/

	public boolean isIsSnip() {
		return mIsSnip;
	}

	public void setIsSnip(boolean isSnip) {
		this.mIsSnip = isSnip;
	}

	public boolean isScrollFree() {
		return mScrollFree;
	}

	public void setScrollFree(boolean scrollFree) {
		this.mScrollFree = scrollFree;
	}

	public int getThreshold() {
		return mThreshold;
	}

	public void setThreshold(int threshold) {
		this.mThreshold = threshold;
	}

	public int getSelecting() {
		return mSelecting;
	}
	
	public void setSelecting(int selecting) {
		this.mSelecting = selecting;
		
		mRotationPerElement = 360f/mShow;
		
//		(mAdapter.getCount() - (180 + mRotation)
//				/ mRotationPerElement)
		
		this.mRotation = -(selecting*mRotationPerElement);
		this.mLastRotation = mRotation;
		
	}

	public float getMaxSpeed() {
		return mMaxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.mMaxSpeed = maxSpeed;
	}

	public float getSpeed() {
		return mSpeed;
	}

	public void setSpeed(int speed) {
		this.mSpeed = speed;
	}

	public float getRollRotation() {
		return mRotation;
	}

	public void setRollRotation(float rotation) {
		this.mRotation = rotation;
	}

	public boolean isAnimating() {
		return mAnimating;
	}

	public void setAnimating(boolean animating) {
		this.mAnimating = animating;
	}

	public RollMenuAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(RollMenuAdapter adapter) {
		this.mAdapter = adapter;
	}

	public boolean isShowText() {
		return mShowText;
	}

	public void setShowText(boolean showText) {
		this.mShowText = showText;
	}

	public int getShow() {
		return mShow;
	}

	public void setShow(int show) {
		this.mShow = show;
	}

	public float[] getCursorLocation() {
		return new float[] { mPointerCoords.x, mPointerCoords.y };
	}

	public boolean isPressed() {
		return mPressed;
	}

	public float getMultiplexer() {
		return mMultiplexer;
	}

	public void setMultiplexer(float multiplexer) {
		this.mMultiplexer = multiplexer;
	}

	public float getIncremental() {
		return mIncremental;
	}

	public void setIncremental(float incremental) {
		this.mIncremental = incremental;
	}
	
	

	public boolean isTest() {
		return mTest;
	}

	public void setTest(boolean test) {
		this.mTest = test;
	}
	
	public boolean isFlingEnabled() {
		return mFlingEnabled;
	}

	public void setFlingEnabled(boolean flingEnabled) {
		mFlingEnabled = flingEnabled;
	}

	
	
	public int getViewWidth() {
		return mViewWidth;
	}

	public void setViewWidth(int mViewWidth) {
		this.mViewWidth = mViewWidth;
	}

	public int getViewHeight() {
		return mViewHeight;
	}

	public void setViewHeight(int mViewHeight) {
		this.mViewHeight = mViewHeight;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		for(int i=0; i< this.getChildCount(); i++){
	         View v = getChildAt(i);
	         v.layout(mAdapter.getCount()*i, 0, (i+1)*mAdapter.getCount(), b-t);
	     }
	}

	public int getViewTop() {
		return mViewTop;
	}

	public void setViewTop(int mViewTop) {
		this.mViewTop = mViewTop;
	}

	public int getViewLeft() {
		return mViewLeft;
	}

	public void setViewLeft(int mViewLeft) {
		this.mViewLeft = mViewLeft;
	}
	
	public int getDiameter() {
		return mDiameter;
	}

	public void setDiameter(int mDiameter) {
		this.mDiameter = mDiameter;
	}
}
