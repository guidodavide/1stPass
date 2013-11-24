package com.firstpass.model;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.firstpass.activities.FirstPass;

public class ConnectedClass extends AsyncTask<String, Void, String>{
	private FirstPass act;
	private String usr;
	private String code;
	private boolean mb;

	private ArrayList<Site> list;
	private SocketProvider sp;
	private String pass;
	private byte[] salt;
	private Thread timerThread;
	
	private static final String TAG = "ConnectedClass";

	protected static final int TIMER_RUNTIME = 10000;

	public ConnectedClass(FirstPass firstPass, String curUsr, String code, byte[] salt, String password) {
		this.usr=curUsr;
		this.code=code;
		this.act = firstPass;
		this.mb  = this.act.isMbActive();
		sp = (SocketProvider) this.act.getApplicationContext();
		this.salt = salt;
		this.pass = password;
	}



	@Override
	protected String doInBackground(String... arg0) {
		
		//TODO deve già essere sicura avendo salt e password
		if(this.code.equals("#UPDATE#\n")){
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(TAG, "Socket invio lista");
			try {
				sp.upload(this.act.getListSite(), this.usr, code, salt, pass);
				this.list = this.act.getListSite();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else if (this.code.equals("#LOAD_LIST#\n")) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(TAG, "Socket richiesta lista");
			try {
				this.list = sp.loadList(this.usr, code, salt, pass);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else if(this.code.equals("#CLOSE#\n")){
			try {
				Log.d(TAG, "Socket richiesta chiusura sessione");
				sp.writeOnSocket("#CLOSE#");
			}catch ( IOException e) {
				e.printStackTrace();
				sp.setError("IOException on socket");
			} catch (UnrecoverableKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("UnrecoverableKeyException on socket");
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("KeyManagementException on socket");
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("KeyStoreException on socket");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("NoSuchAlgorithmException on socket");
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("CertificateException on socket");
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				sp.setError("NotFoundException on socket");
			}
		}
		else{
			return "NO";
		}
		return code;
	}

	@Override
	protected void 	onPreExecute(){	

		this.act.getBar().setVisibility(View.VISIBLE);
		this.act.getBar().setProgress(0);

		timerThread = new Thread() {

			@Override
			public void run() {

				mb=true;

				try {
					int waited = 0;
					while(mb && (waited < TIMER_RUNTIME)) {
						sleep(200);
						if(mb) {
							waited += 200;
							updateProgress(waited);
						}
					}
				} catch(InterruptedException e) {
					// do nothing
				} finally {
					onContinue();
				}
			}
		};

		timerThread.start();
	}

	public void onContinue() {

	}


	public void updateProgress(final int timePassed) {
		if(null != this.act.getBar()) {
			// Ignore rounding error here
			final int progress = this.act.getBar().getMax() * timePassed / TIMER_RUNTIME;
			this.act.getBar().setProgress(progress);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if(!this.code.equals("#CLOSE#\n")){
			timerThread.interrupt();
			this.act.getBar().setProgress(this.act.getBar().getMax());

			this.act.updateFromServer(list);}
	}

}
