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

import com.firstpass.activities.AuthenticationActivity;

public class ConnectingClassForRegister extends AsyncTask<String, Void, String>{
	private AuthenticationActivity ctx;
	private String usr;
	private String psw;
	private Socket skt;
	private String code;
//	private String res;
	private ProgressDialog dialog;
	private SocketProvider sp;
	private static final String TAG = "ConnectingClass4Register";
//	private byte[] bytes;


	public ConnectingClassForRegister(AuthenticationActivity ctx, String usr, String psw,String code) {

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
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d(TAG, "Socket richiesta Registrazione");
			if(code.equals("#S#\n")){
				sp.writeOnSocket(code);
			}
			else{
				sp.writeOnSocket(code);
				sp.writeOnSocket(usr);
				sp.writeOnSocket(psw);
				String risposta = sp.readFromSocket();
				if (risposta.equals("SI")){
					sp.readFromSocket();
					sp.setError("");
//					double sfidaNum = Double.parseDouble(sp.readFromSocket());
//					bytes = EncDecSocket.toByteArray(sfidaNum);
				}
			}
			//TODO ricevere salt
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
		this.ctx.setRes("OK");
		return "OK";
	}

	@Override
	protected void 	onPreExecute(){
		Log.d(TAG, "pre Dialog");
		this.dialog.setMessage("Please Wait...\n");
		this.dialog.show();
	}

	@Override
	protected void onPostExecute(String result) {

//		this.res=result;
		//TODO settare salt e password
		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
		if(result.equals("OK")){
			Log.d(TAG, "Esito");
			if (sp.getLastResult().equals("SI")){
				ctx.onSuccessfulExecute(code);
			}
			else
				ctx.onFailedExecute();
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
