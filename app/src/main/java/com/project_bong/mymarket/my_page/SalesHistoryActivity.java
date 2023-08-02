package com.project_bong.mymarket.my_page;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivitySalesHistoryBinding;
import com.project_bong.mymarket.dto.Goods;

public class SalesHistoryActivity extends AppCompatActivity {
    private ActivitySalesHistoryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySalesHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarSalesHistory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarTitleSalesHistory.setText(getString(R.string.str_sales_history));

//        setTab();
        ChangeFragmentAdapter fragmentAdapter = new ChangeFragmentAdapter(this);
        binding.pagerSalesHistory.setAdapter(fragmentAdapter);


        new TabLayoutMediator(binding.layoutTabSalesHistory, binding.pagerSalesHistory, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        //판매중
                        tab.setText(Goods.STATE_ON_SAIL);
                        break;

                    case 1:
                        //예약중
                        tab.setText(Goods.STATE_RESERVED);
                        break;

                    case 2:
                        //거래완료
                        tab.setText(Goods.STATE_SOLD_OUT);
                        break;

                    default:
                        break;
                }
            }
        }).attach();
    }

    private void setTab(){
        //tabOnSail,tabReserved,tabSoldOut
        TabLayout.Tab tabOnSail = binding.layoutTabSalesHistory.newTab();
        TabLayout.Tab tabReserved = binding.layoutTabSalesHistory.newTab();
        TabLayout.Tab tabSoldOut = binding.layoutTabSalesHistory.newTab();

        tabOnSail.setText(Goods.STATE_ON_SAIL);
        tabReserved.setText(Goods.STATE_RESERVED);
        tabSoldOut.setText(Goods.STATE_SOLD_OUT);


        binding.layoutTabSalesHistory.addTab(tabOnSail);
        binding.layoutTabSalesHistory.addTab(tabReserved);
        binding.layoutTabSalesHistory.addTab(tabSoldOut);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class ChangeFragmentAdapter extends FragmentStateAdapter {


        public ChangeFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String state = null;
            switch (position) {
                case 0:
                    //판매중
                    state = Goods.STATE_ON_SAIL;
                    break;

                case 1:
                    //예약중
                    state = Goods.STATE_RESERVED;
                    break;

                case 2:
                    //거래완료
                    state = Goods.STATE_SOLD_OUT;
                    break;

                default:
                    break;
            }

            Fragment fragment = new SalesListFragment();
            Bundle args = new Bundle();
            args.putString(SalesListFragment.ARG_STATE,state);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}