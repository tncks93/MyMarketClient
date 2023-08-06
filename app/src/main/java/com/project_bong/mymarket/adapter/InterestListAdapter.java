package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ItemInterestsAdapterBinding;
import com.project_bong.mymarket.dto.InterestingGoods;

import java.util.ArrayList;

public class InterestListAdapter extends RecyclerView.Adapter<InterestListAdapter.ViewHolder> {
    private ArrayList<InterestingGoods> listInterest;
    private Context mContext;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
        void onLikeClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        final ItemInterestsAdapterBinding binding;
        public ViewHolder(ItemInterestsAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemClick(v,getAdapterPosition());
                }
            });

            binding.btnLikeInterests.setOnClickListener(v->{
                if(mListener != null){
                    mListener.onLikeClick(v,getAdapterPosition());
                }
            });


            }

    }

    public InterestListAdapter(ArrayList<InterestingGoods> items){
        listInterest = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ItemInterestsAdapterBinding binding = ItemInterestsAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
        
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(InterestListAdapter.ViewHolder holder, int position) {
        InterestingGoods interestingGoods = listInterest.get(position);

        Glide.with(mContext).load(interestingGoods.getMainImage()).transform(new CenterCrop(),new RoundedCorners(20))
                        .into(holder.binding.imgItemInterests);

        if(interestingGoods.isInterest()){
            holder.binding.btnLikeInterests.setImageDrawable(mContext.getDrawable(R.drawable.ic_favorite));
        }else{
            holder.binding.btnLikeInterests.setImageDrawable(mContext.getDrawable(R.drawable.ic_not_favorite));
        }

        holder.binding.txtCategoryItemInterests.setText(interestingGoods.getCategory());
        holder.binding.txtNameItemInterests.setText(interestingGoods.getName());
        holder.binding.txtPriceItemInterests.setText(interestingGoods.getFormattedPrice());
        holder.binding.txtStateItemInterests.setText(interestingGoods.getState());
        holder.binding.txtCreatedAtItemInterests.setText(interestingGoods.getCreatedAtForUser());

    }

    public void addNewItems(ArrayList<InterestingGoods> newItems){
        int startPosition = getItemCount();
        int itemSize = newItems.size();

        listInterest.addAll(newItems);
        notifyItemRangeInserted(startPosition,itemSize);
    }

    public void setNewItems(ArrayList<InterestingGoods> newItems){
        listInterest.clear();
        listInterest.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setInterest(int position,boolean isInterest){
        listInterest.get(position).setInterest(isInterest);
        notifyItemChanged(position);
        String message;
        if(isInterest){
            message = mContext.getString(R.string.str_is_interest);
        }else{
            message = mContext.getString(R.string.str_is_not_interest);
        }

        Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
    }

    public InterestingGoods getItem(int position){
        return listInterest.get(position);
    }

    @Override
    public int getItemCount() {
        return listInterest.size();
    }

    @Override
    public long getItemId(int position) {
        return listInterest.get(position).getId();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
    }
}
