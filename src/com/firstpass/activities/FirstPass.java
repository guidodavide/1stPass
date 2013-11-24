package com.firstpass.activities;

import java.util.ArrayList;


import com.example.firstpass.R;
import com.firstpass.model.ClosingClass;
import com.firstpass.model.ConnectedClass;
import com.firstpass.model.Site;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FirstPass extends Activity implements OnItemClickListener{


	private ListView lst;
	private String curUsr;
	private TextView txtUsr;
	private ProgressBar bar;
	protected boolean mbActive;
	private ArrayList<Site> listSite;
	private Site tempSite;
	private Button buttonSync;
	private static final String TAG = "#1stPassActivity";
	private String password;
	private byte[] salt;
	private boolean copiablePas = false, visiblePass = false;

	public ArrayList<Site> getListSite() {
		return listSite;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.first_pass);

		Intent i =getIntent();
		this.curUsr = i.getStringExtra("usr");
		this.salt = i.getByteArrayExtra("salt");
		this.password = i.getStringExtra("pass");

		this.lst = (ListView) findViewById(R.id.listView);
		this.lst.setClickable(true);
		lst.setBackgroundColor(Color.LTGRAY);
		lst.setOnItemClickListener(this);

		this.txtUsr = (TextView) findViewById(R.id.textViewUsr);

		this.txtUsr.setText("Hi " + curUsr + " ! :)");
		this.txtUsr.setTextColor(Color.BLUE);

		this.bar = (ProgressBar) findViewById(R.id.progressBarFirstPass);
		this.bar.setVisibility(View.INVISIBLE);

		this.buttonSync = (Button) findViewById(R.id.buttonSync);
		this.buttonSync.setBackgroundColor(Color.WHITE);
		this.buttonSync.setVisibility(View.INVISIBLE);
		Log.d(TAG, "Avviata");

		Log.d(TAG, "Richiedo lista utente");
		new ConnectedClass(this, this.curUsr,"#LOAD_LIST#\n", salt, password).execute("");
		//settare salt e password da usare nel canale

	}

	@Override
	public void onBackPressed(){
		Log.d(TAG, "Chiudo sessione");
		new ConnectedClass(this, this.curUsr,"#CLOSE#\n", salt, password).execute("");
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_pass, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.visiblePassword:
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			visiblePassword();
			return true;
		case R.id.copiablePassword:
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			copiablePassword();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void copiablePassword() {
		copiablePas = !copiablePas;
	}

	private void visiblePassword() {

		visiblePass = !visiblePass;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Log.d(TAG, "Click su elemento lista");
		LayoutInflater factory = LayoutInflater.from(this);            
		View textEntryView = factory.inflate(R.layout.display_site, null);

		String pressedItem = (String)lst.getItemAtPosition(position);
		AlertDialog.Builder showSite = new AlertDialog.Builder(this);

		int i = 0;
		while(!listSite.get(i).getName().equals(pressedItem)){//trovo il Sito selezionato
			i++;
		}
		final Site sitozzo = listSite.get(i);

		showSite.setTitle("Site Details");
		showSite.setView(textEntryView);

		final TextView name = (TextView) textEntryView.findViewById(R.id.editTextShowName);
		final TextView url = (TextView) textEntryView.findViewById(R.id.editTextShowURL);
		final TextView usr = (TextView) textEntryView.findViewById(R.id.editTextShowUSR);
		final TextView psw = (TextView) textEntryView.findViewById(R.id.editTextShowPass);				

		name.setText(sitozzo.getName());
		name.setKeyListener(null);
		url.setText(sitozzo.getUrl());
		url.setKeyListener(null);
		usr.setText(sitozzo.getUsr());
		usr.setKeyListener(null);
		psw.setText(sitozzo.getPsw());
		psw.setKeyListener(null);
		if(visiblePass)
			psw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		else
			psw.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		if(copiablePas){
			psw.setTextIsSelectable(true);
			registerForContextMenu(psw);
		}
		else
			psw.setTextIsSelectable(false);

		final FirstPass fp = this;
		fp.updateFromServer(listSite);

		showSite.setTitle("Site Details");
		Log.d("alert spinta", sitozzo.toString());
		showSite.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		showSite.setCancelable(false);
		showSite.setNegativeButton("ELIMINA",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
				for(Site s : listSite){
					if(s.getName().equals(sitozzo.getName())){
						listSite.remove(s);
						break;
					}
				}
				fp.updateFromServer(listSite);
			}
		});
		showSite.setNeutralButton("WEB", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
				String web = "";
				for(Site s : listSite){
					if(s.getName().equals(sitozzo.getName())){
						web = s.getUrl();
						break;
					}
				}
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(web));
				startActivity(i);			}
		});

		AlertDialog alertAdd = showSite.create();

		alertAdd.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//user has long pressed your TextView
		//		text that you want to show in the context menu - I use simply Copy
		menu.add(0, v.getId(), 0, "Copy");

		//cast the received View to TextView so that you can get its text
		TextView myTextView = (TextView) v;

		//place your TextView's text in clipboard
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("password", myTextView.getText().toString());
		clipboard.setPrimaryClip(clip);
	}

	@Override
	protected void onDestroy(){
		Log.d(TAG, "Distruzione");
		super.onDestroy();
		new ClosingClass(this);
	}

	public void updateFromServer(ArrayList<Site> list){
		this.listSite = list;
		ArrayList<String> str = new ArrayList<String>();

		if(list!=null){
			for (Site n : list) {
				str.add(n.getName());
			}

			ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, str);
			lst.setAdapter(arrayAdapter); 
			this.bar.setVisibility(View.INVISIBLE);
			this.buttonSync.setVisibility(View.VISIBLE);
		}
		else
			Toast.makeText(this, "Error communicating! Server crashed?", Toast.LENGTH_SHORT).show();

	}


	public void addNewEntry(View view){
		Log.d(TAG, "Inserimento nuovo elemento");

		LayoutInflater factory = LayoutInflater.from(this);            
		final View textEntryView = factory.inflate(R.layout.alert_layout, null);

		AlertDialog.Builder editalert = new AlertDialog.Builder(this);

		editalert.setTitle("Add new Site");
		editalert.setView(textEntryView);		

		final EditText name = (EditText) textEntryView.findViewById(R.id.editTextName);
		final EditText url = (EditText) textEntryView.findViewById(R.id.editTextURL);
		final EditText usr = (EditText) textEntryView.findViewById(R.id.editTextUSR);
		final EditText psw = (EditText) textEntryView.findViewById(R.id.editTextPass);
		final FirstPass fp = this;

		url.setText("http://");

		//		editalert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
		//			public void onClick(DialogInterface dialog, int whichButton) {
		//				boolean found = false;
		//				ArrayList<Site> lista = fp.getListSite();
		//				for(Site t : lista){
		//					if(t.getName().equals(name.getText().toString())){
		//						found = true;
		//						break;
		//					}
		//				}
		//				if(name.getText().toString().equals("") || url.getText().toString().equals("") || usr.getText().toString().equals("") || psw.getText().toString().equals("")){
		//					Toast.makeText(fp, "One or more field are blank! Not Saved", Toast.LENGTH_SHORT).show();
		//				}
		//				else if(found){
		//					Toast.makeText(fp, "Service already inserted! Not Saved", Toast.LENGTH_SHORT).show();
		//				}
		//				else if(!URLUtil.isHttpUrl(url.getText().toString()) && !URLUtil.isValidUrl(url.getText().toString())){
		//					Toast.makeText(fp, "Not a valid URL", Toast.LENGTH_SHORT).show();
		//				}
		//				else{
		//					tempSite = new Site();
		//					tempSite.setName(name.getText().toString());
		//					tempSite.setUrl(url.getText().toString());
		//					tempSite.setUsr(usr.getText().toString());
		//					tempSite.setPsw(psw.getText().toString());
		//					listSite.add(tempSite);
		//					fp.updateFromServer(listSite);
		//				}
		//			}
		//		});
		editalert.setCancelable(false);
		
		editalert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				//				dialog.cancel();
			}
		});
		editalert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});


		final AlertDialog alertAdd = editalert.create();
		alertAdd.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				Button b = alertAdd.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						boolean found = false;
						ArrayList<Site> lista = fp.getListSite();
						for(Site t : lista){
							if(t.getName().equals(name.getText().toString())){
								found = true;
								break;
							}
						}
						if(name.getText().toString().equals("") || url.getText().toString().equals("") || usr.getText().toString().equals("") || psw.getText().toString().equals("")){
							Toast.makeText(fp, "One or more field are blank! Not Saved", Toast.LENGTH_SHORT).show();
						}
						else if(found){
							Toast.makeText(fp, "Service already inserted! Not Saved", Toast.LENGTH_SHORT).show();
						}
						else if(!URLUtil.isHttpUrl(url.getText().toString()) && !URLUtil.isValidUrl(url.getText().toString())){
							Toast.makeText(fp, "Not a valid URL", Toast.LENGTH_SHORT).show();
						}
						else{
							tempSite = new Site();
							tempSite.setName(name.getText().toString());
							tempSite.setUrl(url.getText().toString());
							tempSite.setUsr(usr.getText().toString());
							tempSite.setPsw(psw.getText().toString());
							listSite.add(tempSite);
							fp.updateFromServer(listSite);
							alertAdd.dismiss();
						}

					}
				}
						);
				b = alertAdd.getButton(AlertDialog.BUTTON_NEGATIVE);

				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						alertAdd.dismiss();
					}
				});
			}
		});
		alertAdd.show();
	}



	public ProgressBar getBar() {
		return this.bar;
	}

	public boolean isMbActive() {
		return mbActive;
	}

	public void setMbActive(boolean mbActive) {
		this.mbActive = mbActive;
	}

	public void setBar(ProgressBar bar) {
		this.bar = bar;
	}

	public void syncToServer(View v){
		Log.d(TAG, "Richiedo Sync");
		this.buttonSync.setVisibility(View.INVISIBLE);
		new ConnectedClass(this, this.curUsr,"#UPDATE#\n", salt, password).execute("");
	}


}

