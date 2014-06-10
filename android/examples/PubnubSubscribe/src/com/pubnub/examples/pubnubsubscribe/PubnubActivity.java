package com.pubnub.examples.pubnubsubscribe;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.examples.pubnubsubscribe.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PubnubActivity extends Activity {

	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "Pubnub";
	private NotificationManager mNotificationManager;
	public ArrayList<String> list = new ArrayList<String>();
	public CustomListAdapter adapter;

	volatile boolean sendNotification = false;

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, PubnubActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.icon)
				.setContentTitle("PubNub Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mBuilder.setAutoCancel(true);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		try {
			Uri notification = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(getApplicationContext(),
					notification);
			r.play();
		} catch (Exception e) {
			Log.i("PUBNUB", e.toString());
		}
	}

	public void notifyUser(JSONObject message) {
		String notification = null;
		Object data = null;
		try {
			data = message.get("data");
		} catch (JSONException e1) {
			Log.i("PUBNUB", e1.toString());
			return;
		}
		try {
			notification = message.getString("message");
		} catch (JSONException e) {
			notification = data.toString();
		}

		if (sendNotification) {
			sendNotification(notification);
		}
		list.add(0, data.toString());
		adapter.notifyDataSetChanged();

	}

	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				notifyUser(new JSONObject(intent.getStringExtra("data")));
			} catch (JSONException e) {
				Log.i("PUBNUB", e.toString());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_pubnub);

		ListView listview = (ListView) findViewById(R.id.message_list);
		/*
		 * adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,
		 * list);
		 * 
		 * listview.setAdapter(adapter);
		 */

		adapter = new CustomListAdapter(this, R.layout.custom_list, list);
		listview.setAdapter(adapter);

		IntentFilter myFilter = new IntentFilter("android.intent.action.MAIN");
		registerReceiver(receiver, myFilter);

		Intent serviceIntent = new Intent(this, PubnubService.class);
		startService(serviceIntent);

		Log.i("PubnubActivity", "PubNub Activity Started!");

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("PUBNUB", "Messages count : " + list.size());
		adapter.notifyDataSetChanged();
		sendNotification = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		sendNotification = true;
	}
}
