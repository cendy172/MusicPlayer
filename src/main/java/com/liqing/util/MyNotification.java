package com.liqing.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.liqing.R;
import com.liqing.mediaplayer_music.MainActivity;

public class MyNotification {
	private Context context;

	public MyNotification(Context context) {
		this.context = context;
	}

	// 显示Notification
	public void showNotification(String musicname,String singer) {
	        // 创建一个NotificationManager的引用
	        NotificationManager notificationManager = (
	                NotificationManager)context.getSystemService(
	                        Context.NOTIFICATION_SERVICE);
	        
	        // 定义Notification的各种属性
	        Notification notification = new Notification(
	                R.drawable.ic_launcher,musicname+"-"+singer,
	                System.currentTimeMillis());
	        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
	        notification.flags |= Notification.FLAG_ONGOING_EVENT;
	        // 表明在点击了通知栏中的"清除通知"后，此通知自动清除。
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        // 设置通知的事件消息
	        CharSequence contentTitle = "MusicPlayer"; // 通知栏标题
	        CharSequence contentText = musicname+"-"+singer; // 通知栏内容
	        
	        Intent notificationIntent = new Intent(context,MainActivity.class);
	        notificationIntent.setAction(Intent.ACTION_MAIN);
	        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	        PendingIntent contentIntent = PendingIntent.getActivity(
	         context, 0, notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
	        notification.setLatestEventInfo(
	         context, contentTitle, contentText, contentIntent);
	        // 把Notification传递给NotificationManager
	        notificationManager.notify(0, notification);
	    }

	// 取消通知
	public void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
}
