package com.firstpass.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.util.Base64;

public class EncDecSocket {
	
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    ByteBuffer.wrap(bytes).putDouble(value);
	    return bytes;
	}
	

	public static byte[] encrypt(byte[] property, String password, byte[] SALT) throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property));
	}

	public static byte[] decrypt(byte[] property, String password, byte[] SALT) throws GeneralSecurityException, IOException {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(password.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return pbeCipher.doFinal(base64Decode(property));
	}

	public static byte[] base64Decode(byte[] property) throws IOException {
		return org.apache.commons.codec.binary.Base64.decodeBase64(property);
	}
	public static byte[] base64DecodeFromString(String property) throws IOException {
		return Base64.decode(property, Base64.DEFAULT);
	}

	public static byte[] base64Encode(byte[] bytes) {
		return org.apache.commons.codec.binary.Base64.encodeBase64(bytes);
	}
	
	public static String base64EncodetoString(byte[] bytes){
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	
	public static byte[] ObjtoByte( Serializable o ) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream( baos );
		oos.writeObject( o );
		oos.close();
		return  org.apache.commons.codec.binary.Base64.encodeBase64( baos.toByteArray() );
	}

	public static Object BytetoObj( byte[] s ) throws IOException ,
	ClassNotFoundException {
		byte [] data = org.apache.commons.codec.binary.Base64.decodeBase64( s );
		ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream(  data ) );
		Object o  = ois.readObject();
		ois.close();
		return o;
	}
	
	public static ByteObj hash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException{
		byte[] salt = new byte[16];
		SecureRandom random = new SecureRandom(); 
		random.nextBytes(salt);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = f.generateSecret(spec).getEncoded();
		return new ByteObj(salt, hash);
	}
	
	public static String hash(String password, byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException{
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 128);
		SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = f.generateSecret(spec).getEncoded();
		return base64EncodetoString(hash);
	}
	

}