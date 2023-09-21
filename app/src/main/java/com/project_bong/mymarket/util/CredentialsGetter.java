package com.project_bong.mymarket.util;

import android.content.Context;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;

public class CredentialsGetter {
    private Credentials credentials;
    private File keyDir;

    public CredentialsGetter(Context context) throws Exception{
        keyDir = new File(context.getFilesDir(),"key");
        if(!keyDir.exists()){
            keyDir.mkdir();
        }
    }

    public boolean existWalletFile(String walletName) throws Exception{
        if(new File(keyDir,walletName).exists()){
            return true;
        }

        return false;

    }

    public Credentials getCredentials(String walletName,String password) throws Exception{
        return WalletUtils.loadCredentials(password,new File(keyDir,walletName));
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
