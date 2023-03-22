package com.project_bong.mymarket.home;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.project_bong.mymarket.databinding.DialogFragmentSearchFilterBinding;
import com.project_bong.mymarket.dto.SearchFilter;

public class SearchFilterDialogFragment extends BottomSheetDialogFragment {
    private DialogFragmentSearchFilterBinding binding;
    private Context mContext;
    private SearchFilter searchFilter;


    public static SearchFilterDialogFragment newInstance(String searchFilter){
        SearchFilterDialogFragment frag = new SearchFilterDialogFragment();
        Bundle args = new Bundle();
        args.putString("searchFilter",searchFilter);
        frag.setArguments(args);

        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogFragmentSearchFilterBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchFilter = new Gson().fromJson(getArguments().getString("searchFilter"),SearchFilter.class);
        Log.d("searchFilter","SearchFilterDialogFragment -> category : "+searchFilter.getCategory()+" keyword : "+searchFilter.getKeyword());

    }
}
