package com.firstpass.activities;

import java.util.concurrent.ExecutionException;

import com.example.firstpass.R;
import com.firstpass.model.ClosingClass;
import com.firstpass.model.ConnectingClassForRegister;
import com.firstpass.model.HashChecker;
import com.firstpass.model.SocketProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

public class AuthenticationActivity extends AccountAuthenticatorActivity {
	//	public static final String PARAM_AUTHTOKEN_TYPE = "auth.token";  
	public static final String PARAM_CREATE = "create";  

	public static final int REQ_CODE_CREATE = 1;  

	public static final int REQ_CODE_UPDATE = 2;  

	public static final String EXTRA_REQUEST_CODE = "req.code";  

	public static final int RESP_CODE_SUCCESS = 0;  

	public static final int RESP_CODE_ERROR = 1;  

	public static final int RESP_CODE_CANCEL = 2;

	private EditText usrBox;
	private EditText pswBox;
	private EditText pswBox2;
	private String res;
	private String accountType;
	private final static String TAG = "RegisterActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		usrBox=(EditText) findViewById(R.id.editTextRegUsername);
		pswBox=(EditText) findViewById(R.id.editTextRegPassword);
		pswBox2=(EditText) findViewById(R.id.editTextRegPassword2);
		Log.d(TAG, "Avviata");
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.authentication, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onCancelClick(View v) {  
		this.finish();  

	}

	public void onClickConfirm(View arg0) throws InterruptedException, ExecutionException {
		ConnectivityManager connMgr = (ConnectivityManager)	getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		Log.d(TAG, "Controllo connettività");
		if (networkInfo != null && networkInfo.isConnected()){
			//CONTROLLARE STRINGHE VUOTE e CAMPI CORTI!!!!!
			if(this.usrBox.getText().toString().length() < 6){
				Toast.makeText(this, "Username too short", Toast.LENGTH_SHORT).show();
				this.usrBox.setBackgroundColor(Color.CYAN); 
			}
			else if(this.pswBox.getText().toString().length() < 8){
				Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
				this.pswBox.setBackgroundColor(Color.CYAN); 
			}
			else if(!this.pswBox.getText().toString().equals(this.pswBox2.getText().toString())){
				Toast.makeText(this, "Passwords MUST match", Toast.LENGTH_SHORT).show();
				this.pswBox.setBackgroundColor(Color.CYAN);
				this.pswBox2.setBackgroundColor(Color.CYAN); 
			}
			else{
				Log.d(TAG, "Connetto per Registrazione");
				accountType = this.getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
				new ConnectingClassForRegister(this, this.usrBox.getText().toString(), this.pswBox.getText().toString(),"#R#\n").execute("");
			}
		}	
		else{
			Log.d(TAG, "Connessione assente");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setPositiveButton(
					R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
						}
					});

			builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);

			AlertDialog dialog = builder.create();

			dialog.show();
		}


	}

	public Object getRes() {
		return res;
	}

	public void setRes(String res) {
		this.res = res;
	}

	public void onSuccessfulExecute(String returning) {
		if(returning.equals("#R#\n")){
			SocketProvider sp = (SocketProvider)this.getApplicationContext();
			Log.d(TAG, "Ricevuta risposta affermativa");
			if (sp.getLastResult().equals("SI")){
				Toast.makeText(this, "User added locally", Toast.LENGTH_SHORT).show();
				new HashChecker(this, AccountManager.get(this), this.usrBox.getText().toString(),this.pswBox.getText().toString()).execute("");
			}
		}
		else if (returning.equals("#S#\n")){
			this.finish();
			Toast.makeText(this, "User added remotely", Toast.LENGTH_SHORT).show();
//			SocketProvider sp = (SocketProvider) this.getApplicationContext();
//			sp.closeSocket();
		}
	}
		
	public void onSuccessfulHashCheck(){
		final Intent intent = new Intent();  
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, this.usrBox.getText().toString());  
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);  
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);  
		this.setAccountAuthenticatorResult(intent.getExtras());  
		this.setResult(RESULT_OK, intent);
		new ConnectingClassForRegister(this, this.usrBox.getText().toString(), this.pswBox.getText().toString(),"#S#\n").execute("");
	}

	public void onFailedExecute() {
		Log.d(TAG, "Ricevuta risposta negativa");
		Toast.makeText(this, "Username Already exists!", Toast.LENGTH_SHORT).show();

	}

	public void onException() {
		Log.d(TAG, "Eccezione durante la comunicazione");
		Toast.makeText(this, ((SocketProvider)this.getApplicationContext()).getError(), Toast.LENGTH_SHORT).show();

	}
	
	@Override
	protected void onDestroy(){
		Log.d(TAG, "Distruzione");
		super.onDestroy();
		new ClosingClass(this);
	}

}  


