package com.liqing.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.liqing.R;
import com.liqing.mediaplayer_music.MainActivity;

public class VolumeView extends View {

	private Bitmap backbar = null;
	private Bitmap frontbar = null;
	private Bitmap thumb = null;
	private Matrix matrix = null;
	private int height = 0;
	private int width = 0;
	private Paint mypPaint = null;

	public VolumeView(Context context) {
		super(context);
		matrix = new Matrix();
		this.mypPaint = new Paint();
	}

	public VolumeView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		matrix = new Matrix();
		this.mypPaint = new Paint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (width == 0 || height == 0) {
			width = getMeasuredWidth();
			height = getMeasuredHeight();

			//背景
			Bitmap temp = BitmapFactory.decodeResource(getResources(),
					R.drawable.progress_background);
			matrix.reset();
			matrix.setScale((float) width/2 / temp.getWidth(), (float) height
					/ temp.getHeight());
			backbar = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
					temp.getHeight(), matrix, true);

			//滑动块
			temp = BitmapFactory.decodeResource(getResources(),
					R.drawable.volume_thumb);
			matrix.reset();
			matrix.setScale((float) width / temp.getWidth(), (float) width
					/ temp.getWidth());
			thumb = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
					temp.getHeight(), matrix, true);
			
			//已滑动部分
			frontbar = BitmapFactory.decodeResource(getResources(),
					R.drawable.progress_pro);
		}
		matrix.reset();
		int now = MainActivity.audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int max = MainActivity.Volume_Max;

		matrix.setScale((float) width/2 / frontbar.getWidth(), (float) now / max
				* height / frontbar.getHeight());
		Bitmap tempFront = Bitmap.createBitmap(frontbar, 0, 0,
				frontbar.getWidth(), frontbar.getHeight(), matrix, true);

		canvas.drawBitmap(backbar, width/4, 0, mypPaint);
		canvas.drawBitmap(tempFront, width/4, height - tempFront.getHeight(),
				mypPaint);
		if(height - tempFront.getHeight() <= thumb.getHeight()/2){
			canvas.drawBitmap(thumb, 0, thumb.getHeight()/2, mypPaint);
		}else if(tempFront.getHeight() < thumb.getHeight()/2){
			canvas.drawBitmap(thumb, 0, height - thumb.getHeight(), mypPaint);
		}else{
			canvas.drawBitmap(thumb, 0, height - tempFront.getHeight() + thumb.getHeight()/2, mypPaint);
		}
	}

	float srcY = 0, dstY = 0;
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		boolean consume = false;
		switch (motionEvent.getAction()) {
		case MotionEvent.ACTION_UP:
			dstY = motionEvent.getY();
			consume = false;
			break;
		case MotionEvent.ACTION_DOWN:
			srcY = motionEvent.getY();
			consume = true;
			break;
		default:
			break;
		}
		if (srcY != 0 && dstY != 0) {
			if (srcY - dstY > 0) {
				// 向上滑动
				float f = srcY - dstY;
				float add = f / height * 15;
				MainActivity.audioManager
						.setStreamVolume(
								AudioManager.STREAM_MUSIC,
								(int) (MainActivity.audioManager
										.getStreamVolume(AudioManager.STREAM_MUSIC) + add),
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				this.invalidate();
			} else {
				// 向下滑动
				float f = dstY - srcY;
				float add = f / height * 15;
				MainActivity.audioManager
						.setStreamVolume(
								AudioManager.STREAM_MUSIC,
								(int) (MainActivity.audioManager
										.getStreamVolume(AudioManager.STREAM_MUSIC) - add),
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				this.invalidate();
				this.srcY = 0;
				this.dstY = 0;
			}
		}
		return consume;
	}
}
