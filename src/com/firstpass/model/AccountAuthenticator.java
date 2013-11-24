package com.firstpass.model;

import com.firstpass.activities.AuthenticationActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	private final Context myCtx;

	public AccountAuthenticator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.myCtx = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
			String authTokenType, String[] requiredFeatures, Bundle options)
					throws NetworkErrorException {
		// TODO Auto-generated method stub
		final Bundle result;  
		final Intent intent; 
		intent = new Intent(this.myCtx, AuthenticationActivity.class);  
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType); 
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

		result = new Bundle();  
		result.putParcelable(AccountManager.KEY_INTENT, intent);  

		return result;  
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse arg0,
			Account arg1, Bundle arg2) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse arg0, Account arg1,
			String arg2, Bundle arg3) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
					throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

}
