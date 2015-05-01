package com.weekend.wear;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.jikexueyuan.wear.R;
import com.jikexueyuan.wear.R.drawable;
import com.jikexueyuan.wear.R.id;
import com.jikexueyuan.wear.R.layout;

public class MainActivity extends Activity implements DataApi.DataListener ,ConnectionCallbacks,OnConnectionFailedListener {

 private TextView tv_content;
 private Button btn_voice;
GoogleApiClient mGoogleAppiClient ;
private static final String phone_path="only-phone";
private static final String wear_path="only-wear";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGoogleAppiClient= new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .build();	
		btn_voice=(Button)findViewById(R.id.btn_voice);
		btn_voice.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				displaySpeechRecognizer();
			}
		});
		tv_content=(TextView)findViewById(R.id.tv_content);
		Wearable.DataApi.addListener(mGoogleAppiClient, this);
	}
	
	@Override
	protected void onStart() {
		mGoogleAppiClient.connect();
		super.onStart();
	}
	@Override
	protected void onStop() {
	    if (null != mGoogleAppiClient && mGoogleAppiClient.isConnected()) {
	        Wearable.DataApi.removeListener(mGoogleAppiClient, this);
	        mGoogleAppiClient.disconnect();
	    }
	    super.onStop();
	}
	private void createNotification(String title,String content){
		int notificationId = 001;
		// Build intent for notification content
		Intent viewIntent = new Intent(this, MainActivity.class);
		PendingIntent viewPendingIntent =
		        PendingIntent.getActivity(this, 0, viewIntent, 0);

		NotificationCompat.Builder notificationBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(title)
		        .setContentText(content)
		        .setContentIntent(viewPendingIntent);

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
		        NotificationManagerCompat.from(this);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(notificationId, notificationBuilder.build());
	}
	private static final int SPEECH_REQUEST_CODE = 0;

	// Create an intent that can start the Speech Recognizer activity
	private void displaySpeechRecognizer() {
	    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	// Start the activity, the intent will be populated with the speech text
	    startActivityForResult(intent, SPEECH_REQUEST_CODE);
	}

	// This callback is invoked when the Speech Recognizer returns.
	// This is where you process the intent and extract the speech text from the intent.
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	        Intent data) {
	    if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
	        List<String> results = data.getStringArrayListExtra(
	                RecognizerIntent.EXTRA_RESULTS);
	        String spokenText = results.get(0);
	        sendTextToPhone(spokenText);
	        tv_content.setText(spokenText);
	        // Do something with spokenText
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
	private void sendTextToPhone(String content){
		PutDataMapRequest dataMap = PutDataMapRequest.create("/only-phone");
		dataMap.getDataMap().putString("content", content);
		PutDataRequest request = dataMap.asPutDataRequest();
		PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
		        .putDataItem(mGoogleAppiClient, request);
		pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
			
			@Override
			public void onResult(DataItemResult result) {
				if(result.getStatus().isSuccess())
					Log.e("tag", "success");
				
			}
		});
	}

	@Override
	public void onConnected(Bundle arg0) {
	
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataChanged(DataEventBuffer dataEvents) {
	    for (DataEvent event : dataEvents) {
	        if (event.getType() == DataEvent.TYPE_DELETED) {
	        } else if (event.getType() == DataEvent.TYPE_CHANGED) {
	        	DataMap dataMap=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
	        	if(event.getDataItem().getUri().getPath().equals("/only-wear")){
	        		String content=dataMap.get("content");
	        		createNotification("test", content);
	        	}
	        	
	        }
	    }
}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
}
