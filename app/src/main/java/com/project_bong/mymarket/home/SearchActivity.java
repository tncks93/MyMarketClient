package com.project_bong.mymarket.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.CategorySearchingAdapter;
import com.project_bong.mymarket.databinding.ActivitySaleBinding;
import com.project_bong.mymarket.databinding.ActivitySearchBinding;
import com.project_bong.mymarket.dto.Category;
import com.project_bong.mymarket.dto.SearchFilter;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private CategorySearchingAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSearch.toolbarDefault);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbarSearch.toolbarTitle.setText(getString(R.string.str_title_search));

        setCategoryList();

        //텍스트 검색
        binding.editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("searchAction","onEditorAction 호출됨");
                SearchFilter searchFilter = new SearchFilter(SearchFilter.FILTER_NONE,v.getText().toString());
                Intent intent = new Intent(getBaseContext(),SearchResultsActivity.class);
                intent.putExtra("searchFilter",new Gson().toJson(searchFilter));
                startActivity(intent);
                return true;
            }
        });

        //카테고리 클릭시
        categoryAdapter.setOnItemClickListener(new CategorySearchingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                String category = categoryAdapter.getCategory(pos);
                SearchFilter searchFilter = new SearchFilter(category,SearchFilter.FILTER_NONE);
                Log.d("searchFilter","min : "+searchFilter.getMinPrice());
                Intent intent = new Intent(getBaseContext(),SearchResultsActivity.class);
                intent.putExtra("searchFilter",new Gson().toJson(searchFilter));
                startActivity(intent);
            }
        });

    }

    private void setCategoryList(){
        ArrayList<Category> categories = new ArrayList<>();
        String[] categoryArray = getResources().getStringArray(R.array.array_list_goods_category);
        for(int i=0;i<categoryArray.length;i++){
            String stringResName = "str_category_"+i;
            String imgResName = "img_category_"+i;
            int categoryId = getResources().getIdentifier(stringResName,"string",getPackageName());
            int imageId = getResources().getIdentifier(imgResName,"drawable",getPackageName());

            categories.add(new Category(categoryId,imageId));
        }

        categoryAdapter = new CategorySearchingAdapter(categories);
        binding.recyclerCategorySearch.setHasFixedSize(true);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getBaseContext(), FlexDirection.ROW, FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        layoutManager.setAlignItems(AlignItems.FLEX_START);
        binding.recyclerCategorySearch.setLayoutManager(layoutManager);
        binding.recyclerCategorySearch.setAdapter(categoryAdapter);


    }
}