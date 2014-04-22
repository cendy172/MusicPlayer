package com.liqing.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.liqing.R;
import com.liqing.activities.MusicListActivity;

public class ExListView extends ExpandableListView implements OnScrollListener {

	ExpandableListAdapter _exAdapter = null;

	@Override
	public void setAdapter(ExpandableListAdapter adapter) {
		_exAdapter = adapter;
		super.setAdapter(adapter);
	}

	private LinearLayout _groupLayout;
	public int _groupIndex = -1;

	/**
	 * @param context
	 */
	public ExListView(Context context) {
		super(context);
		super.setOnScrollListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ExListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnScrollListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ExListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		super.setOnScrollListener(this);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		if (_exAdapter == null)
			_exAdapter = this.getExpandableListAdapter();

		int ptp = view.pointToPosition(0, 0);
		if (ptp != AdapterView.INVALID_POSITION) {
			ExListView qExlist = (ExListView) view;
			long pos = qExlist.getExpandableListPosition(ptp);
			int groupPos = ExpandableListView.getPackedPositionGroup(pos);
			int childPos = ExpandableListView.getPackedPositionChild(pos);

			if (childPos < 0) {
				groupPos = -1;
			}
			if (groupPos < _groupIndex) {

				_groupIndex = groupPos;

				if (_groupLayout != null) {
					_groupLayout.removeAllViews();
					_groupLayout.setVisibility(GONE);// 这里设置Gone 为了不让它遮挡后面header
				}
			} else if (groupPos > _groupIndex) {
				final FrameLayout fl = (FrameLayout) getParent();
				_groupIndex = groupPos;
				if (_groupLayout != null)
					fl.removeView(_groupLayout);

				_groupLayout = (LinearLayout) getExpandableListAdapter()
						.getGroupView(groupPos, true, null, null);
				_groupLayout.setBackgroundResource(R.drawable.listitem_background);
				
				_groupLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						collapseGroup(_groupIndex);
						MusicListActivity._viewHandler.post(new Runnable() {
							@Override
							public void run() {
								fl.removeView(_groupLayout);
								fl.addView(_groupLayout, new LayoutParams(
										android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
							}
						});
					}
				});

				fl.addView(_groupLayout, fl.getChildCount(), new LayoutParams(
						android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

}