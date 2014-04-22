package com.liqing.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.liqing.activities.MusicActivity;
import com.liqing.bean.Music;
import com.liqing.util.LrcProcess.LrcContent;

import java.util.ArrayList;
import java.util.List;

/**
 * �Զ���滭��ʣ��������Ч��
 */
public class LrcView extends TextView {

	private float width;
	private float high;
	private int frame;
	private Paint CurrentPaint;
	private Paint NotCurrentPaint;
	private Paint timePaint;
	public static float TextHigh = 50;
	private float TextSize = 28;
	private int Index = 0;// ��ǰ���index
	private int x, y;
	

	int rawY = 0;

	private List<LrcContent> mSentenceEntities = new ArrayList<LrcContent>();

	public void setSentenceEntities(List<LrcContent> mSentenceEntities) {
		this.mSentenceEntities = mSentenceEntities;
	}

	public float getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(int y) {

		this.y = y;
	}

	public LrcView(Context context) {
		super(context);
		init();
	}

	public LrcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setFocusable(true);

		// ��������
		CurrentPaint = new Paint();
		CurrentPaint.setAntiAlias(true);
		CurrentPaint.setTextAlign(Paint.Align.CENTER);

		// �Ǹ�������
		NotCurrentPaint = new Paint();
		NotCurrentPaint.setAntiAlias(true);
		NotCurrentPaint.setTextAlign(Paint.Align.CENTER);

		timePaint = new Paint();
		timePaint.setAntiAlias(true);
		timePaint.setTextAlign(Paint.Align.CENTER);

	}

	public void setframe(int frame) {
		this.frame = frame;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (canvas == null) {
			return;
		}

		CurrentPaint.setColor(Color.argb(255, 0, 255, 255));
		NotCurrentPaint.setColor(Color.argb(255, 72, 209, 204));

		CurrentPaint.setTextSize(30);
		CurrentPaint.setTypeface(Typeface.SERIF);

		timePaint.setTextSize(15);
		timePaint.setColor(Color.argb(255, 0, 255, 255));

		NotCurrentPaint.setTextSize(TextSize);
		NotCurrentPaint.setTypeface(Typeface.DEFAULT);

		try {

			if (MusicActivity.drawline) {
				canvas.drawText(Music.toTime(mSentenceEntities.get(Index)
						.getLrc_time()), 20, high / 2 - 5, timePaint);

				canvas.drawLine(0, high / 2, width, high / 2, CurrentPaint);

			}
			if (!MusicActivity.ischangstate) {

				canvas.drawText(mSentenceEntities.get(Index).getLrc(),
						width / 2, high / 2 + TextHigh - frame, CurrentPaint);
				float tempY = high / 2 + TextHigh - frame;

				// ��������֮ǰ�ľ���
				for (int i = Index - 1; i >= 0; i--) {
					// ��������
					tempY = tempY - TextHigh;

					canvas.drawText(mSentenceEntities.get(i).getLrc(),
							width / 2, tempY, NotCurrentPaint);
				}

				tempY = high / 2 + TextHigh - frame;

				// ��������֮��ľ���
				for (int i = Index + 1; i < mSentenceEntities.size(); i++) {
					// ��������
					tempY = tempY + TextHigh;
					canvas.drawText(mSentenceEntities.get(i).getLrc(),
							width / 2, tempY, NotCurrentPaint);
				}
			} else {

				Index = getindex(Index, y);
				canvas.drawText(mSentenceEntities.get(Index).getLrc(),
						width / 2, high / 2 + TextHigh +y%50, CurrentPaint);
				float tempY = high / 2 + TextHigh +y%50;

				// ��������֮ǰ�ľ���
				for (int i = Index - 1; i >= 0; i--) {
					// ��������
					tempY = tempY - TextHigh;

					canvas.drawText(mSentenceEntities.get(i).getLrc(),
							width / 2, tempY, NotCurrentPaint);
				}

				tempY = high / 2 + TextHigh+y%50 ;

				// ��������֮��ľ���
				for (int i = Index + 1; i < mSentenceEntities.size(); i++) {
					// ��������
					tempY = tempY + TextHigh;
					canvas.drawText(mSentenceEntities.get(i).getLrc(),
							width / 2, tempY, NotCurrentPaint);
				}
				System.out.println("y = " + y);
			}

		} catch (Exception e) {
		}
	}

	
  public int  gettime()
  {
	  if(Index >= mSentenceEntities.size()){
		  Index = mSentenceEntities.size() - 1;
	  }
	  if(Index < 0){
		  Index = 0;
	  }
	 return mSentenceEntities.get(Index)
		.getLrc_time();
  }
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		this.width = w;
		this.high = h;
	}

	public int getindex(int rawindex, int y) {
		if (y / 50 != rawY / 50) {
			rawY = y;
			if (y > 0)
				return (rawindex - 1);
			else {
				return (rawindex + 1);
			}
		} else {
			Log.i("test", "meibian");
			rawY = y;
			return rawindex;
		}
	}

	public void SetIndex(int index) {
		this.Index = index;
	}
}
