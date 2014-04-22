package com.liqing.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.liqing.R;
import com.liqing.musicService.MusicService;
import com.liqing.util.FileUtil;

public class MusicView extends View {

	private Context context = null;
	private GestureDetector myGestureDetector = null ;//手势处理，暂时没有手势处理
	private Bitmap bitmap = null,tag = null ;
	private Paint myPaint = null ;
	private float windowWidth = 0 ;
	
	//从Activity初始化调用的方法
	public MusicView(Context context) {
		super(context);
		this.context = context;
		this.myGestureDetector = new GestureDetector(this.context,new MyGestureDetectorListener());
		this.myPaint = new Paint();
		refreshBitmap(MusicService.albumArt);
		this.tag = BitmapFactory.decodeResource(getResources(), R.drawable.tag);
	}
	
	//从xml布局文件初始化的回调方法
	public MusicView(Context context,AttributeSet attributeSet){
		super(context,attributeSet);
		this.context = context;
		this.myGestureDetector = new GestureDetector(this.context,new MyGestureDetectorListener());
		this.myPaint = new Paint();
		refreshBitmap(MusicService.albumArt);
		this.tag = BitmapFactory.decodeResource(getResources(), R.drawable.tag);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		this.windowWidth = canvas.getWidth();
		myPaint.setAlpha(200);
		Matrix matrix = new Matrix();
		matrix.setScale(windowWidth/bitmap.getWidth(), windowWidth/bitmap.getWidth());
		canvas.drawBitmap(bitmap, matrix, myPaint);
		canvas.drawBitmap(tag, 0,0, myPaint);
	}
	
	public void refreshBitmap(String albumArt){
		if(albumArt != null && FileUtil.isFileExist(albumArt + ".png", FileUtil.ALBUMPATH )){
			this.bitmap = BitmapFactory.decodeFile(FileUtil.SDCardRoot + FileUtil.ALBUMPATH + albumArt + ".png");
			if(this.bitmap == null){
				this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.adele1);
			}
		}else{
			this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.adele1);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent){
		return this.myGestureDetector.onTouchEvent(motionEvent);
	}
	
	class MyGestureDetectorListener implements GestureDetector.OnGestureListener{

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			
			//在这儿处理点击事件
			
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return true;
		}
	}
}
