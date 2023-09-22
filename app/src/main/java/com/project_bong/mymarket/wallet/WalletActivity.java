package com.project_bong.mymarket.wallet;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.slider.Slider;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityWalletBinding;
import com.project_bong.mymarket.util.CredentialsGetter;

import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class WalletActivity extends AppCompatActivity {
    private ActivityWalletBinding binding;
    private Web3j web3j;
    private CredentialsGetter crGetter;
    private String fileName;
    private String password;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Log.d("wallet","scan 취소");
                } else {
                    binding.editRecipientAddressWallet.setText(result.getContents());
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarWallet.toolbarDefault);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbarWallet.toolbarTitle.setText("나의 지갑");

        fileName = getIntent().getStringExtra("fileName");
        password = getIntent().getStringExtra("password");
        binding.radioGroupPurposeWallet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_charge_token_wallet:
                        //토큰 충전
                        binding.layoutTransferTokenWallet.setVisibility(View.GONE);
                        binding.layoutChargeTokenWallet.setVisibility(View.VISIBLE);
                        break;
                    case R.id.radio_transfer_token_wallet:
                        //토큰 전송
                        binding.layoutChargeTokenWallet.setVisibility(View.GONE);
                        binding.layoutTransferTokenWallet.setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }
            }
        });
        binding.radioChargeTokenWallet.setChecked(true);
        setGasFee();
        binding.refreshWallet.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try{
                    getEthBalances();
                    getTokenBalances();
                    binding.refreshWallet.setRefreshing(false);
                }catch (Exception e){
                    e.printStackTrace();
                    binding.refreshWallet.setRefreshing(false);
                }
            }
        });
        binding.sliderGasLimitWallet.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {

                setGasFee();
            }
        });
        binding.sliderGasPriceWallet.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {

                setGasFee();
            }
        });
        try{
            crGetter = new CredentialsGetter(getBaseContext());
            crGetter.loadCredentials(fileName,password);
            web3j = Web3j.build(new HttpService(SCToken.END_POINT));

            ECKeyPair ecKeyPair = crGetter.getCredentials().getEcKeyPair();
            Log.d("wallet","public : "+ecKeyPair.getPublicKey().toString(16));
            Log.d("wallet","public : "+ecKeyPair.getPrivateKey().toString(16));

            binding.txtMyAddressWallet.setText(crGetter.getCredentials().getAddress());
            setQR();
            getEthBalances();
            getTokenBalances();
        }catch (Exception e){
            e.printStackTrace();
        }

        binding.btnChargeTokenWallet.setOnClickListener(v->{
            try{
                chargeToken();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        binding.btnTransferWallet.setOnClickListener(v->{
            try{
                transferToken();

            }catch (Exception e){
                e.printStackTrace();
            }
        });

        binding.btnCopyAddress.setOnClickListener(v->{
            ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("address",crGetter.getCredentials().getAddress());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getBaseContext(),"주소가 복사되었습니다",Toast.LENGTH_SHORT).show();
        });

        binding.btnScanQrCodeWallet.setOnClickListener(v->{
            ScanOptions options = new ScanOptions();
            options.setOrientationLocked(false);
            barcodeLauncher.launch(options);
        });


    }

    private void getEthBalances() throws Exception{
        EthGetBalance ethBalanceWei = web3j.ethGetBalance(crGetter.getCredentials().getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigDecimal ethBalance = Convert.fromWei(ethBalanceWei.getBalance().toString(),Convert.Unit.ETHER);
        Log.d("wallet","ethBalance : "+ethBalance);
        binding.txtBalancesEthWallet.setText(ethBalance+"ETH");
    }

    private void getTokenBalances() throws Exception{
        SCToken scToken = SCToken.load(SCToken.CONTRACT_ADDRESS,web3j,crGetter.getCredentials(),new DefaultGasProvider());
        BigInteger originalBalances = scToken.balanceOf(crGetter.getCredentials().getAddress()).sendAsync().get();
        BigInteger balances = new BigDecimal(originalBalances).divide(BigDecimal.TEN.pow(2)).toBigInteger();
        Log.d("wallet","SCTOriginalBalances : "+originalBalances);
        Log.d("wallet","SCTBalances : "+balances);

        binding.txtBalancesTokenWallet.setText(balances+"SCT");
    }

    private void chargeToken() throws Exception{
        String strAmount = binding.editAmountToChargeTokenWallet.getText().toString();
        if(strAmount.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),"충전할 토큰 개수를 입력해 주세요",Toast.LENGTH_SHORT).show();
            return;
        }

        int amount = Integer.valueOf(strAmount)/2;
        binding.progressWallet.setVisibility(View.VISIBLE);
        Thread buyTokenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    StaticGasProvider gasProvider = getGasProvider();
                    Transfer transfer = new Transfer(web3j,new RawTransactionManager(web3j,crGetter.getCredentials()));
                    RemoteCall<TransactionReceipt> callBuyToken = transfer.sendFunds(SCToken.CONTRACT_ADDRESS, BigDecimal.valueOf(amount), Convert.Unit.WEI,gasProvider.getGasPrice(),getGasProvider().getGasLimit());
                    callBuyToken.sendAsync().thenAccept(new Consumer<TransactionReceipt>() {
                        @Override
                        public void accept(TransactionReceipt transactionReceipt) {
                            Log.d("wallet","buyTokenAccepted : "+transactionReceipt.getTransactionHash());
                            Log.d("wallet","buyTokenAcceptedStatus : "+transactionReceipt.getStatus());


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        Toast.makeText(getBaseContext(),"토큰 충전 성공 : "+transactionReceipt.getTransactionHash(),Toast.LENGTH_SHORT).show();
                                        getEthBalances();
                                        getTokenBalances();
                                        binding.progressWallet.setVisibility(View.GONE);

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });


                        }
                    }).exceptionally(new Function<Throwable, Void>() {
                        @Override
                        public Void apply(Throwable throwable) {
                            Log.d("wallet","buyTokenException : "+throwable.getMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.progressWallet.setVisibility(View.GONE);
                                }
                            });
                            return null;
                        }
                    });


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        buyTokenThread.start();
    }

    private void transferToken() throws Exception{
        String recipient = binding.editRecipientAddressWallet.getText().toString();
        String strAmount = binding.editAmountToTransferWallet.getText().toString();
        if(recipient.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),"받는 계정의 주소를 입력해 주세요",Toast.LENGTH_SHORT).show();
            return;
        }
        if(strAmount.replaceAll(" ","").equals("")){
            Toast.makeText(getBaseContext(),"충전할 토큰 개수를 입력해 주세요",Toast.LENGTH_SHORT).show();
            return;
        }
        int amount = Integer.valueOf(strAmount)*100;
        binding.progressWallet.setVisibility(View.VISIBLE);
        FastRawTransactionManager manager = new FastRawTransactionManager(
                web3j,
                crGetter.getCredentials(),
                11155111,
                new PollingTransactionReceiptProcessor(web3j, 15000, 8));

        SCToken scToken = SCToken.load(SCToken.CONTRACT_ADDRESS,web3j,manager,getGasProvider());
        RemoteCall<TransactionReceipt> callbackTransfer = scToken.transfer(recipient, BigInteger.valueOf(amount));
        callbackTransfer.sendAsync().thenAccept(new Consumer<TransactionReceipt>() {
            @Override
            public void accept(TransactionReceipt transactionReceipt) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressWallet.setVisibility(View.GONE);
                        Log.d("wallet","txAccept : "+transactionReceipt.getTransactionHash());
                        Toast.makeText(getBaseContext(),"토큰 전송 성공 : "+transactionReceipt.getTransactionHash(),Toast.LENGTH_LONG).show();
                        try{
                            getEthBalances();
                            getTokenBalances();

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }
        }).exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressWallet.setVisibility(View.GONE);
                        Log.d("wallet","txException : "+throwable.getMessage());
                    }
                });

                return null;
            }
        });
    }

    private void setGasFee(){
        float gasLimit = binding.sliderGasLimitWallet.getValue();
        float gasPrice = binding.sliderGasPriceWallet.getValue();
        binding.txtGasLimitWallet.setText(String.valueOf((int)gasLimit));
        binding.txtGasPriceWallet.setText(String.valueOf((int)gasPrice));
        Log.d("wallet","gasLimit : "+gasLimit+" gasPrice : "+gasPrice+ " fee : "+gasLimit*gasPrice);

        BigDecimal gasFee = new BigDecimal(gasLimit*gasPrice).divide(BigDecimal.TEN.pow(9));
        Log.d("wallet","gasFee : "+gasFee);
        binding.txtGasFeeWallet.setText("트랜잭션 수수료 : "+gasFee+"ETH");
    }

    private StaticGasProvider getGasProvider(){
        BigInteger gasLimit = BigInteger.valueOf((long)binding.sliderGasLimitWallet.getValue());
        float gweiPrice = binding.sliderGasPriceWallet.getValue();
        BigInteger gasPrice = Convert.toWei(new BigDecimal(gweiPrice), Convert.Unit.GWEI).toBigInteger();
        return new StaticGasProvider(gasPrice,gasLimit);
    }

    private void setQR(){
        try{
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrCode = barcodeEncoder.encodeBitmap(crGetter.getCredentials().getAddress(), BarcodeFormat.QR_CODE,400,400);
            binding.imgQrPublicWallet.setImageBitmap(qrCode);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}