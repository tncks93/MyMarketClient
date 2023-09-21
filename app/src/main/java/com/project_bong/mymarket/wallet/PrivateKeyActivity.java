package com.project_bong.mymarket.wallet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityPrivateKeyBinding;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;

public class PrivateKeyActivity extends AppCompatActivity {
    private ActivityPrivateKeyBinding binding;
    private String executeMode;
    public static final String WRITE_MODE = "WRITE";
    public static final String READ_ONLY_MODE = "READ_ONLY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivateKeyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarPrivateKey.toolbarDefault);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarPrivateKey.toolbarTitle.setText("PrivateKey");

        executeMode = getIntent().getStringExtra("executeMode");

        switch(executeMode){
            case  WRITE_MODE:
                binding.btnPrivateKey.setText("계정 가져오기");
                binding.editPrivateKey.setEnabled(true);
                break;

            case READ_ONLY_MODE:
                binding.btnPrivateKey.setText("복사하기");
                binding.editPrivateKey.setEnabled(false);
                break;
          default:
              finish();
            break;
        }

        binding.btnPrivateKey.setOnClickListener(v->{
            switch(executeMode){
                case  WRITE_MODE:
                    exportPrivateKey();
                    break;
                case READ_ONLY_MODE:
                    break;

                default:
                    break;
            }
        });
    }

    private void exportPrivateKey(){
        String privateKey = binding.editPrivateKey.getText().toString();
        if(privateKey.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),"privateKey를 입력해 주세요",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!WalletUtils.isValidPrivateKey(privateKey)){
            Toast.makeText(getBaseContext(),"타당하지 않은 privateKey 입니다",Toast.LENGTH_SHORT).show();
            return;
        }
        WalletPasswordDialog dialog = WalletPasswordDialog.newInstance("계정 가져오기");
        dialog.setOnButtonClickListener(new WalletPasswordDialog.OnButtonClickListener() {
            @Override
            public void onButtonClick() {
                String password = dialog.getPassword();
                if(password.replaceAll(" ","").equals("")){
                    Toast.makeText(getBaseContext(),"비밀번호를 입력해 주세요",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent result = new Intent();
                result.putExtra("privateKey",privateKey);
                result.putExtra("password",password);
                setResult(RESULT_OK,result);
                finish();
            }
        });
        dialog.show(getSupportFragmentManager(),null);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}