package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.util.Log;
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

public class GoodsRecyclerAdapter extends RecyclerView.Adapter<GoodsRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Goods> goodsList;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemGoodsAdapterBinding binding;

        public ViewHolder(ItemGoodsAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemClick(v,getAdapterPosition());
                }
            });

            }

    }

    public GoodsRecyclerAdapter(ArrayList<Goods> items){
        goodsList = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ItemGoodsAdapterBinding binding = ItemGoodsAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GoodsRecyclerAdapter.ViewHolder holder, int position) {
        Goods goods = goodsList.get(position);
        Glide.with(mContext).load(goods.getMainImage()).transform(new CenterCrop(),new RoundedCorners(20)).into(holder.binding.imgItemGoods);
        holder.binding.txtNameItemGoods.setText(goods.getName());
        holder.binding.txtCategoryItemGoods.setText(goods.getCategory());
        holder.binding.txtPriceItemGoods.setText(goods.getFormattedPrice());
        holder.binding.txtCreatedAtItemGoods.setText(goods.getCreatedAtForUser());

        if(goods.getState().equals(Goods.STATE_ON_SAIL)){
            holder.binding.txtStateItemGoods.setVisibility(View.GONE);
        }else{
            holder.binding.txtStateItemGoods.setVisibility(View.VISIBLE);
            holder.binding.txtStateItemGoods.setText(goods.getState());
        }



    }

    public void resetList(ArrayList<Goods> newList){
        goodsList.clear();
        goodsList.addAll(newList);
        Log.d("searchFilter","newListSize : "+newList.size());
        notifyDataSetChanged();
    }

    public Goods getItem(int position){
        return goodsList.get(position);
    }



    public void addNewList(ArrayList<Goods> newList){
        int startPosition = getItemCount();
        int itemCount = newList.size();
        goodsList.addAll(newList);
        notifyItemRangeInserted(startPosition,itemCount);

    }

    @Override
    public long getItemId(int position) {
        int itemId = getItem(position).getId();
        return itemId;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return goodsList.size();
    }
}