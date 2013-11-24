package com.firstpass.model;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import android.app.ProgressDialog;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.firstpass.activities.Login;
import com.firstpass.utils.EncDecSocket;

public class ConnectingClassForLogin extends AsyncTask<String, Void, String>{
	private Login ctx;
	private String usr;
	private String psw;
	private Socket skt;
	private String code;
//	private String res;
	private ProgressDialog dialog;
	private SocketProvider sp;
	private byte[] bytes;
	private static final String TAG = "ConnectingClass4Login";


	public ConnectingClassForLogin(Login ctx, String usr, String psw,String code) {

		this.code = code;
		this.usr = usr;
		this.psw = psw;
		this.ctx=ctx;
		dialog = new ProgressDialog(ctx);
		this.sp = (SocketProvider)this.ctx.getApplicationContext();
	}

	@Override
	protected String doInBackground(String... arg0) {
		try{
			Log.d(TAG, "Socket richiesta Login");
			sp.writeOnSocket(code);
			double sfidaNum = Double.parseDouble(sp.readFromSocket());
			Log.d(TAG, "Rispondo a sfida");
			sp.writeOnSocket("" + (double)(sfidaNum+1)+"\t"+usr+"\t"+psw);
			sp.readFromSocket();
			bytes = EncDecSocket.toByteArray(sfidaNum);
		}catch ( IOException e) {
			e.printStackTrace();
			sp.setError("IOException on socket, Server is UP?");
			return "Error";
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("UnrecoverableKeyException on socket");
			return "Error";
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("KeyManagementException on socket");
			return "Error";
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("KeyStoreException on socket");
			return "Error";
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("NoSuchAlgorithmException on socket");
			return "Error";
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("CertificateException on socket");
			return "Error";
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sp.setError("NotFoundException on socket");
			return "Error";
		}
		return "OK";
	}

	@Override
	protected void 	onPreExecute(){
		Log.d(TAG, "pre Dialog");
		this.dialog.setMessage("Please Wait...");
		this.dialog.show();
	}

	@Override
	protected void onPostExecute(String result) {
		
//		this.res=result;
		//settare salt e password

		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
		if(result.equals("OK")){
			Log.d(TAG, "Esito");
			if (sp.getLastResult().equals("SI"))
				ctx.onSuccessfulExecute(bytes);
			else
				ctx.onFailedResult();
		}
		else{
			ctx.onException();
		}
	}

//	public String getRes() {
//		return res;
//	}

	public Socket getSocket(){
		return skt;

	}

}
