package com.firstpass.model;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.firstpass.activities.AuthenticationActivity;
import com.firstpass.activities.Login;
import com.firstpass.utils.ByteObj;
import com.firstpass.utils.EncDecSocket;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class HashChecker extends AsyncTask<String, Void, String> {
	private AuthenticationActivity authActivity;
	private Login logActivity;
	private AccountManager acMan;
	private String user;
	private String password;
	private ProgressDialog dialog;
	private static final String accountType = "com.example.FirstPass";
	private Bundle bundle;

	public HashChecker(AuthenticationActivity authActivity, AccountManager acMan, String user, String password){
		this.authActivity = authActivity;
		this.logActivity = null;
		this.acMan = acMan;
		this.user = user;
		this.password = password;
		dialog = new ProgressDialog(authActivity);
	}
	
	public HashChecker(Login loginActivity, AccountManager acMan, String user, String password){
		this.logActivity = loginActivity;
		this.authActivity = null;
		this.acMan = acMan;
		this.user = user;
		this.password = password;
		dialog = new ProgressDialog(loginActivity);
	}

	@Override
	protected void 	onPreExecute(){
		if(this.logActivity == null){
			this.dialog.setMessage("Adding User...\n");
			this.dialog.show();
		}
		else{
			this.dialog.setMessage("Checking User...\n");
			this.dialog.show();
		}
	}

	@Override
	protected String doInBackground(String... params) {
		if(this.logActivity == null){
			final Account account = new Account(user, accountType);
			try {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ByteObj obj = EncDecSocket.hash(password);
				bundle = new Bundle();
				bundle.putString("#S41T", EncDecSocket.base64EncodetoString(obj.getIVParameterSpec()));
				acMan.addAccountExplicitly(account, EncDecSocket.base64EncodetoString(obj.getEncriptedData()), bundle);
				Log.d("HashChecker", "salt " + EncDecSocket.base64EncodetoString(obj.getIVParameterSpec()));
				Log.d("HashChecker", "hash " + EncDecSocket.base64EncodetoString(obj.getEncriptedData()));
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			boolean OK = false;
			Account[] accounts = acMan.getAccountsByType(accountType);
			Account result = null;
			for(Account a : accounts){
				if(a.name.equals(user)){
					OK = true;
					Log.d("HashChecker", "trovato " + a.name);
					result = a;
					break;
				}
			}
			if(OK){
				String storedHash = acMan.getPassword(result);
				String storedSalt = acMan.getUserData(result, "#S41T");
				
				Log.d("HashChecker", "check hash " + storedHash);
				Log.d("HashChecker", "check salt " + storedSalt);
				
				try {
					String calculatedHash = EncDecSocket.hash(password, EncDecSocket.base64DecodeFromString(storedSalt));
					if(!calculatedHash.equals(storedHash)){
						return "Username or Password wrong!";	
					}
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
				return "User doesn't exist!";
//			new ConnectingClassForLogin(this, this.usrBox.getText().toString(), this.pswBox.getText().toString(),"#L#\n").execute("");
		}
		return "Logged in";
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (this.dialog.isShowing()) {
			this.dialog.dismiss();
		}
		if(this.logActivity == null){
			authActivity.onSuccessfulHashCheck();
		}
		else{
			logActivity.onHashCheckResult(result);
		}
	}

}
