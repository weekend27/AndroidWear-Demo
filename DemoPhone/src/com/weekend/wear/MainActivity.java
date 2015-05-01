package com.weekend.wear;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
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
import com.jikexueyuan.wear.R.layout;

public class MainActivity extends Activity implements DataApi.DataListener ,ConnectionCallbacks,OnConnectionFailedListener{

	private static final String app_key="a9d956f11e71e6eb92a5c169278891b4";
	private static final String wear_path="/only-wear";
	GoogleApiClient mGoogleAppiClient ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGoogleAppiClient= new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .build();		
		Wearable.DataApi.addListener(mGoogleAppiClient, this);
	}
	@Override
	protected void onStart() {
		mGoogleAppiClient.connect();
		super.onStart();
	}
	public class GetTulingThread extends Thread{
		String info;
		public GetTulingThread(String mInfo) {
			this.info=mInfo;
		}
		@Override
		public void run() {
		String disPlayResult=getTulingResult(info);
		sendTextToWear(disPlayResult);
		super.run();
		}
	}
	private void sendTextToWear(String content){
		PutDataMapRequest dataMap = PutDataMapRequest.create(wear_path);
		dataMap.getDataMap().putString("content", content);
		PutDataRequest request = dataMap.asPutDataRequest();
		Wearable.DataApi
		        .putDataItem(mGoogleAppiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
					
					@Override
					public void onResult(DataItemResult result) {
					if(result.getStatus().isSuccess()){
						Log.e("phone", "success");
					}
					}
				});;
	}
private String getTulingResult(String info){
	String tulingStr = "";
	    String requesturl = "http://www.tuling123.com/openapi/api?key="+app_key+"&info="+info; 
	    HttpGet request = new HttpGet(requesturl); 
	    HttpResponse response;
		try {
			response = new DefaultHttpClient().execute(request);

	    //200即正确的返回码 
	    if(response.getStatusLine().getStatusCode()==200){ 
	        String result = EntityUtils.toString(response.getEntity()); 
	        try {
				JSONObject jsObj=new JSONObject(result);
				tulingStr=jsObj.getString("text");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        System.out.println("返回结果："+result); 
	    } 
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return tulingStr;

}
@Override
public void onDataChanged(DataEventBuffer dataEvents) {
    for (DataEvent event : dataEvents) {
        if (event.getType() == DataEvent.TYPE_DELETED) {
        } else if (event.getType() == DataEvent.TYPE_CHANGED) {
        	DataMap dataMap=DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        	if(event.getDataItem().getUri().getPath().equals("only-phone")){
        		String content=dataMap.get("content");
        		new GetTulingThread(content).start();
        	}
        		
        }
    }
}
@Override
public void onConnectionFailed(ConnectionResult arg0) {
	// TODO Auto-generated method stub
	
}
@Override
public void onConnected(Bundle arg0) {
	
}
@Override
public void onConnectionSuspended(int arg0) {
	// TODO Auto-generated method stub
	
}
@Override
protected void onStop() {
    if (null != mGoogleAppiClient && mGoogleAppiClient.isConnected()) {
        Wearable.DataApi.removeListener(mGoogleAppiClient, this);
        mGoogleAppiClient.disconnect();
    }
    super.onStop();
}
}
