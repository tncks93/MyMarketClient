package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ItemSelectBuyerAdapterBinding;
import com.project_bong.mymarket.dto.User;

import java.util.ArrayList;

public class SelectBuyerAdapter extends RecyclerView.Adapter<SelectBuyerAdapter.ViewHolder> {
    private ArrayList<User> buyerList;
    private Context mContext;
    private int selectedPosition = -1;
    private int previousSelectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSelectBuyerAdapterBinding binding;

        public ViewHolder(ItemSelectBuyerAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemClick(v,getAdapterPosition());
                }
            });


        }


    }



    public SelectBuyerAdapter(ArrayList<User> items){
        buyerList = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        return new ViewHolder(ItemSelectBuyerAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false));
    }

    @Override
    public void onBindViewHolder(SelectBuyerAdapter.ViewHolder holder, int position) {
        User user = buyerList.get(position);

        Glide.with(mContext).load(user.getUserImage()).circleCrop().into(holder.binding.imgBuyerItemBuyer);
        holder.binding.txtNameItemBuyer.setText(user.getName());

        if(selectedPosition != -1){
            if(position == selectedPosition){
                holder.binding.btnRadioItemBuyer.setChecked(true);
                holder.binding.getRoot().setBackgroundColor(mContext.getColor(R.color.light_gray));
            }
        }

        if(previousSelectedPosition != -1){
            if(position == previousSelectedPosition){
                holder.binding.btnRadioItemBuyer.setChecked(false);
                holder.binding.getRoot().setBackgroundColor(mContext.getColor(R.color.white));
            }
        }
    }

    public void setItems(ArrayList<User> items){
        buyerList.clear();
        buyerList.addAll(items);
        notifyDataSetChanged();
    }

    public void setClick(int selectedPosition){
        if(this.selectedPosition == selectedPosition){
            return;
        }
        previousSelectedPosition = this.selectedPosition;
        this.selectedPosition = selectedPosition;
        if(previousSelectedPosition != -1){
            notifyItemChanged(previousSelectedPosition);
        }

        if(selectedPosition != -1){
            notifyItemChanged(selectedPosition);
        }

//        Log.d("selectBuyer","previous : "+previousSelectedPosition+" selected : "+selectedPosition);
    }

    public int getSelectedBuyerId(){
        if(selectedPosition != -1){
            return buyerList.get(selectedPosition).getId();
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return buyerList.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
    }
}
