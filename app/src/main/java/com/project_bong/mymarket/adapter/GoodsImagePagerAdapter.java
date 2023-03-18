package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project_bong.mymarket.databinding.ItemPagerImageGoodsBinding;

import java.util.ArrayList;

public class GoodsImagePagerAdapter extends RecyclerView.Adapter<GoodsImagePagerAdapter.ViewHolder> {
    private ArrayList<String> imagePathList;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPagerImageGoodsBinding binding;

        public ViewHolder(ItemPagerImageGoodsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            }

            public void bindImage(String path){
                Glide.with(mContext).load(path).centerCrop().into(binding.imgPagerGoodsAdapter);
            }

    }

    public GoodsImagePagerAdapter(ArrayList<String> items){
        imagePathList = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        ItemPagerImageGoodsBinding binding = ItemPagerImageGoodsBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GoodsImagePagerAdapter.ViewHolder holder, int position) {
        String imagePath = imagePathList.get(position);
        holder.bindImage(imagePath);


    }

    public void setList(ArrayList<String> imagePathList){
        this.imagePathList = imagePathList;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }
}