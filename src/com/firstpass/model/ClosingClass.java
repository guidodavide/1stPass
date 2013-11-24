package com.firstpass.model;

import android.os.AsyncTask;
import android.util.Log;

import com.firstpass.activities.AuthenticationActivity;
import com.firstpass.activities.FirstPass;

public class ClosingClass extends AsyncTask<String, Void, String>{
	private FirstPass act;
	private AuthenticationActivity act2;
	private SocketProvider sp;
	
	private static final String TAG = "ClosingClass";

	protected static final int TIMER_RUNTIME = 10000;

	public ClosingClass(FirstPass firstPass) {
		this.act = firstPass;
		sp = (SocketProvider) this.act.getApplicationContext();
	}
	
	public ClosingClass(AuthenticationActivity firstPass) {
		this.act2 = firstPass;
		sp = (SocketProvider) this.act2.getApplicationContext();
	}



	@Override
	protected String doInBackground(String... arg0) {
		Log.d(TAG, "Chiudo");
		sp.closeSocket();
		
		return "OK";
	}

	@Override
	protected void 	onPreExecute(){
		super.onPreExecute();
	}


	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

}
