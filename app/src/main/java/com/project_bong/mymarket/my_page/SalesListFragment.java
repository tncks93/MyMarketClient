package com.project_bong.mymarket.my_page;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.SalesListAdapter;
import com.project_bong.mymarket.databinding.FragmentSalesListBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.GoodsActivity;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SalesListFragment extends Fragment {
    private FragmentSalesListBinding binding;
    private SalesListAdapter salesAdapter;
    private ArrayList<Goods> goodsList;
    public static final String ARG_STATE = "state";

    private String state;
    private boolean isLoading = true;

    public SalesListFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            state = getArguments().getString(ARG_STATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSalesListBinding.inflate(LayoutInflater.from(getContext()),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();

        salesAdapter.setOnItemClickListener(new SalesListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int goodsId = salesAdapter.getItem(pos).getId();
                Intent intent = new Intent(getContext(), GoodsActivity.class);
                intent.putExtra("goodsId",goodsId);
                startActivity(intent);
            }
        });

        binding.recyclerListSales.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerListSales.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = salesAdapter.getItemCount();
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && lastVisibleItemPosition != 0){
                    int pagingIdx = salesAdapter.getItem(itemTotalCount-1).getId();

                    getSales(state,pagingIdx);

                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getSales(state,0);
    }

    private void getSales(String state, int pagingIdx){
        isLoading = true;
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getContext()).create(RetrofitInterface.class);
        Call<ArrayList<Goods>> callGetSales = retrofit.callGetSales(state,pagingIdx);
        callGetSales.enqueue(new Callback<ArrayList<Goods>>() {
            @Override
            public void onResponse(Call<ArrayList<Goods>> call, Response<ArrayList<Goods>> response) {
                if(response.body() != null) {

                    salesAdapter.addItems(response.body(),pagingIdx);
                    if(salesAdapter.getItemCount()==0){
                        binding.recyclerListSales.setVisibility(View.GONE);
                        binding.txtHaveNoHistories.setVisibility(View.VISIBLE);
                    }else{
                        binding.recyclerListSales.setVisibility(View.VISIBLE);
                        binding.txtHaveNoHistories.setVisibility(View.GONE);
                    }
                }

                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<Goods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getContext(),t);
                isLoading = false;
            }
        });
    }
    private void initRecyclerView(){
        goodsList = new ArrayList<>();
        salesAdapter = new SalesListAdapter(goodsList);
        salesAdapter.setHasStableIds(true);
        binding.recyclerListSales.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        binding.recyclerListSales.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerListSales.setAdapter(salesAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}