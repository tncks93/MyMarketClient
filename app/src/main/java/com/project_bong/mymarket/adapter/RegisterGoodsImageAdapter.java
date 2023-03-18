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
import com.project_bong.mymarket.databinding.ItemHeaderRegisterImageAdapterBinding;
import com.project_bong.mymarket.databinding.ItemRegisterImageAdpaterBinding;
import com.project_bong.mymarket.util.ItemMoveCallback;

import java.util.ArrayList;
import java.util.Collections;

public class RegisterGoodsImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_IMAGE = 1;

    private Context mContext;
    private ArrayList<String> imagePathList;

    public interface OnItemClickListener {
        void onItemAddClick(View v);
        void onItemRemoveClick(View v,int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder {

        ItemRegisterImageAdpaterBinding binding;
        public ImageViewHolder(ItemRegisterImageAdpaterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.btnClearImageGoodsAdapter.setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemRemoveClick(v,getAdapterPosition()-1);
                }
            });


            }

    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        ItemHeaderRegisterImageAdapterBinding binding;
        public HeaderViewHolder(ItemHeaderRegisterImageAdapterBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            binding.btnAddImageRegisterSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null){
                        mListener.onItemAddClick(view);
                    }
                }
            });
        }
    }

    public RegisterGoodsImageAdapter(ArrayList<String> items){
        imagePathList = items;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == VIEW_TYPE_HEADER){
            ItemHeaderRegisterImageAdapterBinding binding = ItemHeaderRegisterImageAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
            return new HeaderViewHolder(binding);

        }else{
            ItemRegisterImageAdpaterBinding binding = ItemRegisterImageAdpaterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
            return new ImageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ImageViewHolder){
            int realPos = position-1;
            String path = imagePathList.get(realPos);
            Glide.with(mContext).load(path).transform(new CenterCrop(),new RoundedCorners(20)).into(((ImageViewHolder) holder).binding.imgRegisterGoodsAdapter);

            if(realPos == 0){
                ((ImageViewHolder) holder).binding.txtForMainImageGoodsAdapter.setVisibility(View.VISIBLE);
            }else {
                ((ImageViewHolder) holder).binding.txtForMainImageGoodsAdapter.setVisibility(View.GONE);
            }
        }else if(holder instanceof HeaderViewHolder){
            ((HeaderViewHolder) holder).binding.btnAddImageRegisterSale.setText(imagePathList.size()+"/10");
        }


    }

    public void addImage(String path){
        Log.d("image","path : "+path);
        int position = getItemCount();
        imagePathList.add(path);
        notifyItemInserted(position);
        notifyItemChanged(0);
        Log.d("image","size: "+imagePathList.size());
    }

    public void addImages(ArrayList<String> paths){
        int firstPosition = getItemCount();
        imagePathList.addAll(paths);
        notifyItemRangeInserted(firstPosition,paths.size());
        notifyItemChanged(0);
        Log.d("image","size: "+imagePathList.size());

    }

    public void removeItem(int realPos){
        imagePathList.remove(realPos);
        notifyItemRemoved(realPos+1);

        if(realPos == 0){
            notifyItemRangeChanged(0,2);
        }else{
            notifyItemChanged(0);
        }
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        Log.d("ItemMoveHelper","from : "+fromPosition+" to : "+toPosition);
        if(toPosition != 0){
            int realFromPosition = fromPosition-1;
            int realToPosition = toPosition-1;
            if(fromPosition < toPosition){
                for(int  i = realFromPosition;i<realToPosition;i++){
                    Collections.swap(imagePathList,i,i+1);
                }
            }else{
                for (int i = realFromPosition; i > realToPosition; i--){
                    Collections.swap(imagePathList,i,i-1);
                }
            }

            notifyItemMoved(fromPosition,toPosition);

            if(toPosition == 1){
                notifyItemRangeChanged(1,2);
            }

        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){
            return VIEW_TYPE_HEADER;
        }else{
            return VIEW_TYPE_IMAGE;
        }

    }

    @Override
    public int getItemCount() {
        return imagePathList.size() + 1;
    }
}
