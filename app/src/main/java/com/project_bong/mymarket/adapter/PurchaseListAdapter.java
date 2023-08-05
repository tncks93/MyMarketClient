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

public class PurchaseListAdapter extends RecyclerView.Adapter<PurchaseListAdapter.ViewHolder> {
    private ArrayList<Goods> listGoods;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
        void onMoreClick(View v, int pos);
    }
    private OnItemClickListener mViewClickListener = null;


    public void setOnItemClickListener(OnItemClickListener listener){
        this.mViewClickListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        final ItemGoodsAdapterBinding binding;

        public ViewHolder(ItemGoodsAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v->{
                if(mViewClickListener != null){
                    mViewClickListener.onItemClick(v,getAdapterPosition());
                }
            });

            binding.btnMoreItemGoods.setOnClickListener(v->{
                if(mViewClickListener != null){
                    mViewClickListener.onMoreClick(v,getAdapterPosition());
                }
            });


            }

    }

    public PurchaseListAdapter(ArrayList<Goods> items){
        listGoods = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        ItemGoodsAdapterBinding binding = ItemGoodsAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PurchaseListAdapter.ViewHolder holder, int position) {
        holder.binding.btnMoreItemGoods.setVisibility(View.VISIBLE);

        Goods goods = listGoods.get(position);

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

    public void addItems(ArrayList<Goods> newItems){
        int positionStart = getItemCount();
        int itemSize = newItems.size();;

        listGoods.addAll(newItems);
        notifyItemRangeInserted(positionStart,itemSize);
    }

    @Override
    public int getItemCount() {
        return listGoods.size();
    }

    public Goods getItem(int position){
        return listGoods.get(position);
    }

    public void removeItem(int position){
        listGoods.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public long getItemId(int position) {
        return listGoods.get(position).getId();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        super.onAttachedToRecyclerView(recyclerView);
        
    }
}
