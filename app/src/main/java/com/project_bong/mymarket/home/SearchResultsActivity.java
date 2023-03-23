package com.project_bong.mymarket.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.GoodsRecyclerAdapter;
import com.project_bong.mymarket.databinding.ActivitySearchResultsBinding;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.dto.SearchFilter;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.sale.GoodsActivity;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import kr.co.prnd.slider.FlexibleStepRangeSlider;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultsActivity extends AppCompatActivity {
    private ActivitySearchResultsBinding binding;

    private SearchFilterDialogFragment fragmentSearchFilterDialog;
    private SearchFilter searchFilter;
    private GoodsRecyclerAdapter goodsAdapter;
    private ArrayList<Goods> goodsList;

    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbarSearchResults.setContentInsetsAbsolute(0,0);
        setSupportActionBar(binding.toolbarSearchResults);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSearchFilter();
        initGoodsAdapter();
        getGoodsSearched(0);

        binding.btnSetCategorySearchResults.setOnClickListener(v->{
            showCategorySelectDialog();

        });

        binding.btnSetPriceSearchResults.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultsActivity.this);
            View view = LayoutInflater.from(SearchResultsActivity.this).inflate(R.layout.dialog_select_price,
                    (LinearLayout)findViewById(R.id.layout_price_dialog));
            builder.setView(view);
            builder.setTitle("가격");

            EditText editMinPrice = ((EditText) view.findViewById(R.id.edit_min_price_price_dialog));
            EditText editMaxPrice = ((EditText)view.findViewById(R.id.edit_max_price_price_dialog));

            editMinPrice.setText(String.valueOf(searchFilter.getMinPrice()));
            editMaxPrice.setText(String.valueOf(searchFilter.getMaxPrice()));
            Button btnSavePriceFilter = ((Button)view.findViewById(R.id.btn_set_price_search_filter));

            AlertDialog dialog = builder.create();

            btnSavePriceFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int minPrice = Integer.parseInt(editMinPrice.getText().toString());
                    int maxPrice = Integer.parseInt(editMaxPrice.getText().toString());
                    DecimalFormat decimalFormat = new DecimalFormat("#,###");

                    if (minPrice >= maxPrice) {
                        Toast.makeText(getBaseContext(), getString(R.string.str_over_min_price_search), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (maxPrice > SearchFilter.DEFAULT_MAX_PRICE) {

                        String overMaxPrice = decimalFormat.format(SearchFilter.DEFAULT_MAX_PRICE);
                        Toast.makeText(getBaseContext(), getString(R.string.str_over_max_price_search, overMaxPrice), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    searchFilter.setMinPrice(minPrice);
                    searchFilter.setMaxPrice(maxPrice);

                    dialog.dismiss();

                    String txtPrice = "";
                    if (minPrice == SearchFilter.DEFAULT_MIN_PRICE && maxPrice == SearchFilter.DEFAULT_MAX_PRICE) {
                        txtPrice = "가격";
                    } else if (minPrice == SearchFilter.DEFAULT_MIN_PRICE) {
                        txtPrice = decimalFormat.format(maxPrice) + "원 이하";
                    } else if (maxPrice == SearchFilter.DEFAULT_MAX_PRICE) {
                        txtPrice = decimalFormat.format(minPrice) + "원 이상";
                    } else {
                        txtPrice = decimalFormat.format(minPrice) + "원 이상 " + decimalFormat.format(maxPrice) + "원 이하";
                    }

                    binding.btnSetPriceSearchResults.setText(txtPrice);

                    Log.d("searchFilter", "priceSlider-> min : " + minPrice + " max : " + maxPrice);

                    getGoodsSearched(0);
                }

            });

            dialog.show();

        });

        binding.editSearchSearchResult.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getText().toString().replaceAll(" ", "").equals("")) {
                    Toast.makeText(getBaseContext(), getString(R.string.str_hint_search), Toast.LENGTH_SHORT).show();

                } else {
                    String keyword = v.getText().toString();
                    searchFilter.setKeyword(keyword);
                    getGoodsSearched(0);

                }
                return true;
            }

        });

        binding.layoutRefreshSearchResults.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("searchFilter","isLoadingWhenRefresh : "+isLoading);
                if(!isLoading){
                    getGoodsSearched(0);
                }else{
                    finishRefreshing();
                }
            }
        });

        binding.recyclerSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerSearchResults.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = goodsAdapter.getItemCount();
                if(lastVisibleItemPosition == itemTotalCount && !isLoading && !isLastPage && lastVisibleItemPosition != 0){
                    int lastIdx = goodsAdapter.getItem(goodsAdapter.getItemCount()-1).getId();

                    getGoodsSearched(lastIdx);

                }
            }
        });
    }

    private void getGoodsSearched(int pageIdx){
        isLoading = true;

        if(pageIdx == 0){
            isLastPage = false;
        }

        RequestBody bodyFilter = RequestBody.create(new Gson().toJson(searchFilter), MediaType.parse("application/json; charset=utf-8"));
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<Goods>> callSearchedGoods = retrofit.callSearchedGoodsList(pageIdx,bodyFilter);
        callSearchedGoods.enqueue(new Callback<ArrayList<Goods>>() {
            @Override
            public void onResponse(Call<ArrayList<Goods>> call, Response<ArrayList<Goods>> response) {
                if(response.body() != null){
                    Log.d("searchFilter","result -> pageIdx : "+pageIdx+" size : "+response.body().size());

                    if(response.body().size() == 0){
                        isLastPage = true;

                    }

                    if(pageIdx == 0){
                        goodsAdapter.resetList(response.body());

                    }else{
                        goodsAdapter.addNewList(response.body());
                    }

                }
                finishRefreshing();
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<Goods>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finishRefreshing();
            }
        });

    }

    private void getSearchFilter(){
        searchFilter = new Gson().fromJson(getIntent().getStringExtra("searchFilter"),SearchFilter.class);
        if(!searchFilter.getKeyword().equals(SearchFilter.FILTER_NONE)){
            binding.editSearchSearchResult.setText(searchFilter.getKeyword());
        }

        if(!searchFilter.getCategory().equals(SearchFilter.FILTER_NONE)){
            binding.btnSetCategorySearchResults.setText(searchFilter.getCategory());
        }

        Log.d("searchFilter","SearchResultsActivity -> category : "+searchFilter.getCategory()+" keyword : "+searchFilter.getKeyword());
    }

    private void initGoodsAdapter(){
        goodsList = new ArrayList<>();
        goodsAdapter = new GoodsRecyclerAdapter(goodsList);
        goodsAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerSearchResults.addItemDecoration(new DividerItemDecoration(getBaseContext(),LinearLayoutManager.VERTICAL));
        binding.recyclerSearchResults.setLayoutManager(linearLayoutManager);
        binding.recyclerSearchResults.setAdapter(goodsAdapter);

        goodsAdapter.setOnItemClickListener(new GoodsRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int goodsId = goodsAdapter.getItem(pos).getId();
                Intent intent = new Intent(getBaseContext(), GoodsActivity.class);
                intent.putExtra("goodsId",goodsId);
                startActivity(intent);
            }
        });
    }


    private void finishRefreshing(){
        if(binding.layoutRefreshSearchResults.isRefreshing()){
            binding.layoutRefreshSearchResults.setRefreshing(false);
        }

    }


    private void showCategorySelectDialog(){
        String[] categories = getResources().getStringArray(R.array.array_list_goods_category);
        String allCategory = getString(R.string.str_all_category_search);

        String[] allCategories = new String[categories.length+1];
        allCategories[0] = allCategory;
        for(int i=0;i<categories.length;i++){
            allCategories[i+1] = categories[i];
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchResultsActivity.this)
                .setTitle(R.string.str_select_category)
                .setItems(allCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            searchFilter.setCategory(SearchFilter.FILTER_NONE);
                            binding.btnSetCategorySearchResults.setText(getString(R.string.str_category_goods));
                        }else{
                            String selectedCategory = allCategories[which];
                            searchFilter.setCategory(selectedCategory);
                            binding.btnSetCategorySearchResults.setText(selectedCategory);
                        }


                        getGoodsSearched(0);
                    }
                });
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return false;
    }
}