package com.firstpass.activities;

import com.example.firstpass.R;
import com.firstpass.model.ConnectingClassForLogin;
import com.firstpass.model.HashChecker;
import com.firstpass.model.SocketProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	private EditText usrBox;
	private EditText pswBox;
	private static final String TAG = "LoginActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		this.usrBox = (EditText) findViewById(R.id.editTextLogUser);
		this.pswBox = (EditText) findViewById(R.id.editTextLogPass);
		Log.d(TAG, "Avviata");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void onClickLogin(View view){
		ConnectivityManager connMgr = (ConnectivityManager)	getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		Log.d(TAG, "Controllo connettività");
		if (networkInfo != null && networkInfo.isConnected()) {
			Log.d(TAG, "Trovata connessione");
			if(this.usrBox.getText().toString().equals(""))
				Toast.makeText(this, "Please insert a valid username", Toast.LENGTH_SHORT).show();
			else if(this.usrBox.getText().toString().equals(""))
				Toast.makeText(this, "Field password cannot be left blank", Toast.LENGTH_SHORT).show();
			else{
				new HashChecker(this, AccountManager.get(this), this.usrBox.getText().toString(),this.pswBox.getText().toString()).execute("");
			}

		}
		else{
			Log.d(TAG, "Connessione assente");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});

			builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

			AlertDialog dialog = builder.create();

			dialog.show();
		}


	}

//	public void onRegisterClick(View view){
//		Log.d(TAG, "Avvio RegisterActivity");
//		this.startActivity(new Intent(this,Register.class));
//	}

	public void onSuccessfulExecute(byte[] bytes) {
		Log.d(TAG, "Ricevuta risposta affermativa");
		SocketProvider sp = (SocketProvider)this.getApplicationContext();

		if (sp.getLastResult().equals("SI")){//check se null!!! => no connessione o errore socket
			
			Intent i = new Intent(this,FirstPass.class);
			i.putExtra("usr", this.usrBox.getText().toString());
			i.putExtra("salt", bytes);
			i.putExtra("pass", this.pswBox.getText().toString());
			//TODO inserire password e salt del canale
			
			Log.d(TAG, "avvio activity FirstPass");
			this.startActivity(i);
		}


	}

	public void onFailedResult() {
		Log.d(TAG, "Ricevuta risposta negativa");
		Toast.makeText(this, "Wrong User or/and Password", Toast.LENGTH_SHORT).show();
	}

	public void onException() {
		Log.d(TAG, "Eccezione durante la comunicazione");
		Toast.makeText(this, ((SocketProvider)this.getApplicationContext()).getError(), Toast.LENGTH_SHORT).show();
	}

	public void onHashCheckResult(String result) {
		Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
		if(result.equals("Logged in")){
			new ConnectingClassForLogin(this, this.usrBox.getText().toString(), this.pswBox.getText().toString(),"#L#\n").execute("");
		}
	}

}



