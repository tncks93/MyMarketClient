package com.project_bong.mymarket.wallet;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityNewAccountBinding;
import com.project_bong.mymarket.util.CredentialsGetter;
import com.project_bong.mymarket.util.Shared;

import org.web3j.crypto.WalletUtils;

public class NewAccountActivity extends AppCompatActivity {
    private ActivityNewAccountBinding binding;
    private ActivityResultLauncher<Intent> getPrivateKey = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK && result.getData() != null){
                String privateKey = result.getData().getStringExtra("privateKey");
                String password = result.getData().getStringExtra("password");

                if(privateKey != null && password != null){
                    Log.d("wallet","privateKey : "+privateKey+ " password : "+password);
                    try{
                        makeAndStartWallet(password,privateKey);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarNewAccount.toolbarDefault);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnCreateNewAccount.setOnClickListener(v->{
            //계정 생성하기 버튼 클릭
            WalletPasswordDialog dialog = WalletPasswordDialog.newInstance("계정 생성하기");
            dialog.setOnButtonClickListener(new WalletPasswordDialog.OnButtonClickListener() {
                @Override
                public void onButtonClick() {
                    String password = dialog.getPassword();
                    if(isEmptyPassword(password)){
                        Toast.makeText(getBaseContext(),"비밀번호를 입력해 주세요",Toast.LENGTH_SHORT).show();
                    }
                    try{
                        makeAndStartWallet(password);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            dialog.show(getSupportFragmentManager(),null);
        });

        binding.btnGetAccount.setOnClickListener(v->{
            //계정 가져오기 버튼 클릭
            Intent intent = new Intent(getBaseContext(), PrivateKeyActivity.class);
            intent.putExtra("executeMode",PrivateKeyActivity.WRITE_MODE);
            getPrivateKey.launch(intent);
        });
    }

    private boolean isEmptyPassword(String password){
        return password.replaceAll(" ", "").equals("");
    }

    private void makeAndStartWallet(String password) throws Exception{
        binding.progresNewAccount.setVisibility(View.VISIBLE);
        CredentialsGetter crGetter = new CredentialsGetter(getBaseContext());
        String fileName = crGetter.makeWalletFile(password);
        saveWalletName(fileName);
        Intent intent = new Intent(getBaseContext(), WalletActivity.class);
        intent.putExtra("password",password);
        intent.putExtra("fileName",fileName);

        startActivity(intent);
        finish();
    }

    private void makeAndStartWallet(String password,String privateKey) throws Exception{
        binding.progresNewAccount.setVisibility(View.VISIBLE);
        CredentialsGetter crGetter = new CredentialsGetter(getBaseContext());
        String fileName = crGetter.makeWalletFile(password,privateKey);
        saveWalletName(fileName);

        Intent intent = new Intent(getBaseContext(), WalletActivity.class);
        intent.putExtra("password",password);
        intent.putExtra("fileName",fileName);

        startActivity(intent);
        finish();

    }

    private void saveWalletName(String fileName){
        Shared shared = new Shared(getBaseContext(),Shared.WALLET_SOURCE_FILE_NAME);
        shared.setSharedString(Shared.WALLET_NAME_KEY,fileName);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}