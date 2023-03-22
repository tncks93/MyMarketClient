package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project_bong.mymarket.databinding.ItemCategorySearchAdapterBinding;
import com.project_bong.mymarket.dto.Category;

import java.util.ArrayList;

public class CategorySearchingAdapter extends RecyclerView.Adapter<CategorySearchingAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        final ItemCategorySearchAdapterBinding binding;
        public ViewHolder(ItemCategorySearchAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemClick(v,getAdapterPosition());
                }
            });


            }

    }

    public CategorySearchingAdapter(ArrayList<Category> items){
        categoryList = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        return new ViewHolder(ItemCategorySearchAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false));
    }

    @Override
    public void onBindViewHolder(CategorySearchingAdapter.ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.binding.txtItemCategorySearch.setText(mContext.getString(category.getCategoryId()));
        Glide.with(mContext).load(ContextCompat.getDrawable(mContext,category.getDrawableId())).into(holder.binding.imgItemCategorySearch);


    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mContext = recyclerView.getContext();
    }

    public String getCategory(int pos){
        return mContext.getString(categoryList.get(pos).getCategoryId());
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}