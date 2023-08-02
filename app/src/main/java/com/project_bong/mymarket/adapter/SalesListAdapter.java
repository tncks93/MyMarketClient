package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.project_bong.mymarket.databinding.ItemGoodsAdapterBinding;
import com.project_bong.mymarket.dto.Goods;

import java.util.ArrayList;

public class SalesListAdapter extends RecyclerView.Adapter<SalesListAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Goods> listSales;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        final ItemGoodsAdapterBinding binding;
        public ViewHolder(ItemGoodsAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


            }

    }

    public SalesListAdapter(ArrayList<Goods> items){
        listSales = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        ItemGoodsAdapterBinding binding = ItemGoodsAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SalesListAdapter.ViewHolder holder, int position) {
        Goods goods = listSales.get(position);

        Glide.with(mContext).load(goods.getMainImage()).transform(new CenterCrop(),new RoundedCorners(20))
                        .into(holder.binding.imgItemGoods);

        holder.binding.txtCategoryItemGoods.setText(goods.getCategory());
        holder.binding.txtNameItemGoods.setText(goods.getName());
        holder.binding.txtCreatedAtItemGoods.setText(goods.getCreatedAtForUser());
        holder.binding.txtPriceItemGoods.setText(goods.getFormattedPrice());

        if(goods.getState().equals(Goods.STATE_ON_SAIL)){
            holder.binding.txtStateItemGoods.setVisibility(View.GONE);
        }else{
            holder.binding.txtStateItemGoods.setVisibility(View.VISIBLE);
            holder.binding.txtStateItemGoods.setText(goods.getState());
        }


    }

    @Override
    public int getItemCount() {
        return listSales.size();
    }

    public void addItems(ArrayList<Goods> newItems){
        listSales.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return listSales.get(position).getId();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        this.mContext = recyclerView.getContext();
        super.onAttachedToRecyclerView(recyclerView);
    }
}