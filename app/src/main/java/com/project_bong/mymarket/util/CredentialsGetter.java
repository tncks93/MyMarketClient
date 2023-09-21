package com.project_bong.mymarket.util;

import android.content.Context;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletUtils;

import java.io.File;

public class CredentialsGetter {
    private Credentials credentials;
    private File keyDir;
    private String fileName;

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

    public String makeWalletFile(String password) throws Exception{
        return fileName = WalletUtils.generateNewWalletFile(password,keyDir,false);
    }

    public String makeWalletFile(String password,String privateKey) throws Exception{
        credentials = Credentials.create(privateKey);
        ECKeyPair ecKeyPair = credentials.getEcKeyPair();
        return WalletUtils.generateWalletFile(password,ecKeyPair,keyDir,false);
    }

    public void loadCredentials(String walletName,String password) throws Exception{
        credentials = WalletUtils.loadCredentials(password,new File(keyDir,walletName));
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
