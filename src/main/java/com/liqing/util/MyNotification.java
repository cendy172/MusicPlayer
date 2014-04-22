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

	// ��ʾNotification
	public void showNotification(String musicname,String singer) {
	        // ����һ��NotificationManager������
	        NotificationManager notificationManager = (
	                NotificationManager)context.getSystemService(
	                        android.content.Context.NOTIFICATION_SERVICE);
	        
	        // ����Notification�ĸ�������
	        Notification notification = new Notification(
	                R.drawable.ic_launcher,musicname+"-"+singer,
	                System.currentTimeMillis());
	        // ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
	        notification.flags |= Notification.FLAG_ONGOING_EVENT;
	        // �����ڵ����֪ͨ���е�"���֪ͨ"�󣬴�֪ͨ�Զ������
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        // ����֪ͨ���¼���Ϣ
	        CharSequence contentTitle = "MusicPlayer"; // ֪ͨ������
	        CharSequence contentText = musicname+"-"+singer; // ֪ͨ������
	        
	        Intent notificationIntent = new Intent(context,MainActivity.class);
	        notificationIntent.setAction(Intent.ACTION_MAIN);
	        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	        PendingIntent contentIntent = PendingIntent.getActivity(
	         context, 0, notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
	        notification.setLatestEventInfo(
	         context, contentTitle, contentText, contentIntent);
	        // ��Notification���ݸ�NotificationManager
	        notificationManager.notify(0, notification);
	    }

	// ȡ��֪ͨ
	public void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
}
