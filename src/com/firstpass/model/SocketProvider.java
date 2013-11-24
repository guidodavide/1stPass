package com.firstpass.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.example.firstpass.R;

import android.app.Application;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

public class SocketProvider extends Application {

	private static final String HOST = "jigen90.asuscomm.com";
	private static final int PORT = 8080;
	private static SocketProvider singleton;
	private static Socket skt;
	private String lastResult;
	private String error;

	public String getLastResult() {
		return lastResult;
	}

	public SocketProvider getInstance(){
		return singleton;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		error = "";
	}

	public void writeOnSocket(String s) throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NotFoundException{
		startSocket();
		OutputStream outputstream = skt.getOutputStream();
		OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
		BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

		bufferedwriter.write(s + "\n");
		bufferedwriter.flush();
	}

	public String readFromSocket() throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, NotFoundException{
		startSocket();
		InputStream inputstream = skt.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

		String result=bufferedreader.readLine();
		if(result.equals("SI") || result.equals("NO"))
			this.lastResult = result;
		return result;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		try {
			SocketProvider.skt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startSocket() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, NotFoundException, IOException, UnrecoverableKeyException, KeyManagementException{

		if(SocketProvider.skt  == null ){
			char[] passphrase = "myComplexPass1".toCharArray();
							
				KeyStore keystore = KeyStore.getInstance("BKS");
				if(android.os.Build.VERSION.RELEASE.equals("4.2.2")){
					keystore.load(this.getApplicationContext().getResources().openRawResource(R.raw.jb), passphrase);
					Log.d("TrustStore JB ",android.os.Build.VERSION.RELEASE);
				}
				else{
					keystore.load(this.getApplicationContext().getResources().openRawResource(R.raw.ics), passphrase);
					Log.d("TrustStore ICS ",android.os.Build.VERSION.RELEASE);
				}

				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keystore, passphrase);
				
				SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); //comunque restituisce la versione 1 in android
				
				KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
				
				TrustManager[] trustManagers = new TrustManager[]{
						new X509TrustManager() {
							public java.security.cert.X509Certificate[] getAcceptedIssuers()
							{
								return null;
							}
							public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
							{

							}
							public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
							{

							}
						}
				};
				
				sslContext.init(keyManagers, trustManagers, new SecureRandom());
				
				SSLSocketFactory sslSocketFactory = (SSLSocketFactory) sslContext.getSocketFactory();
				
				skt = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);
				
				skt.setKeepAlive(true);
				Log.d("Socket Provider", "Socket class: "+skt.getClass());
				Log.d("Socket Provider", "Remote address: "+skt.getInetAddress().toString());
				Log.d("Socket Provider", "Local socket address: "+skt.getLocalSocketAddress().toString());
				Log.d("Socket Provider", "Local port: "+skt.getLocalPort());
			
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Site> loadList(String usr, String code, byte[] salt, String pass) throws IOException, ClassNotFoundException, GeneralSecurityException {
		ArrayList<Site> res = new ArrayList<Site>();
		writeOnSocket(code);
		DataInputStream dis = new DataInputStream(skt.getInputStream());
		int size = dis.readInt();
		byte[] bytes = new byte[size];
		dis.readFully(bytes);
		
		res = (ArrayList<Site>)com.firstpass.utils.EncDecSocket.BytetoObj((com.firstpass.utils.EncDecSocket.decrypt(bytes,pass, salt)));
		
		return res;
	}

	public void upload(ArrayList<Site> listSite, String usr, String code, byte[] salt, String pass) throws IOException, GeneralSecurityException {
		writeOnSocket(code);
		DataOutputStream dos = new DataOutputStream(skt.getOutputStream());
		byte[] writable = com.firstpass.utils.EncDecSocket.encrypt((com.firstpass.utils.EncDecSocket.ObjtoByte(listSite)), pass, salt);
		dos.writeInt(writable.length);
//		dos.flush();
		dos.write(writable);
//		dos.flush();
	}

//	private void sendObjectOnSocket(ArrayList<Site> listSite) throws IOException {
//		ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
//		out.writeObject(listSite);
//		out.close();
//	}
	
	public void setError(String s){
		this.error = s;
	}
	
	public void closeSocket(){
		if(skt  != null ){
			try {
				skt.getOutputStream().close();
				skt.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public String getError(){
		return this.error;
	}
}
