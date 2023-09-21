package com.project_bong.mymarket.wallet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.project_bong.mymarket.adapter.ChatRoomsAdapter;
import com.project_bong.mymarket.databinding.DialogPasswordWalletBinding;

public class WalletPasswordDialog extends DialogFragment {
    private DialogPasswordWalletBinding binding;
    public interface OnButtonClickListener {
        void onButtonClick();
    }
    private OnButtonClickListener mListener = null;
    public void setOnButtonClickListener(OnButtonClickListener listener){
        this.mListener = listener;
    }
    public static WalletPasswordDialog newInstance(String title){
        WalletPasswordDialog dialog = new WalletPasswordDialog();
        Bundle args = new Bundle();
        args.putString("title",title);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogPasswordWalletBinding.inflate(LayoutInflater.from(getActivity()),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title");
        binding.txtTitlePasswordDialog.setText(title);
        binding.btnPasswordDialog.setOnClickListener(v->{
            if(mListener != null){
                mListener.onButtonClick();
            }
        });
    }

    public String getPassword(){
        return binding.editPasswordDialog.getText().toString();
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();

    }
}
