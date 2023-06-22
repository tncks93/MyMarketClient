package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.project_bong.mymarket.databinding.ItemChatMineAdapterBinding;
import com.project_bong.mymarket.databinding.ItemChatOpponentAdapterBinding;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.util.LoginUserGetter;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ChatMessage> listMessages;
    private Context mContext;
    private int userId;

    private final int VIEW_TYPE_MINE = 100;
    private final int VIEW_TYPE_OPPONENT = 200;

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }


    public ChatMessageAdapter(ArrayList<ChatMessage> items){
        listMessages = items;
        userId = LoginUserGetter.getLoginUser().getId();
        Log.d("chat","messageAdapter UserId : "+userId);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_MINE:
                ItemChatMineAdapterBinding bindingMine = ItemChatMineAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
                return new MineViewHolder(bindingMine);


            case VIEW_TYPE_OPPONENT:
                ItemChatOpponentAdapterBinding bindingOpponent = ItemChatOpponentAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
                return new OpponentViewHolder(bindingOpponent);

            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = listMessages.get(position);

        if(holder instanceof MineViewHolder){
            //switch 문으로 msg type별로 나눠야 함.
            ((MineViewHolder) holder).binding.txtMessageChatMine.setText(chatMessage.getContent());


        }else if(holder instanceof OpponentViewHolder){
            Glide.with(mContext).load(chatMessage.getOpImage()).circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((OpponentViewHolder) holder).binding.imgProfileChatOpponent);

            ((OpponentViewHolder) holder).binding.txtMessageChatOpponent.setText(chatMessage.getContent());
        }


    }

    public void addFirstItems(ArrayList<ChatMessage> items){
        if(items.size() > 0){
            Log.d("chat","addFirstItems Size : "+items.size());
            listMessages.addAll(items);
            notifyItemRangeInserted(0,items.size());

        }
    }

    public void addNewItem(ChatMessage chatMessage){
        int newPosition = getItemCount();
        listMessages.add(chatMessage);
        notifyItemInserted(newPosition);
    }

    @Override
    public int getItemViewType(int position) {
        int fromId = listMessages.get(position).getFromId();
        Log.d("chat","getItemViewType FromID : "+fromId);

        if(userId == fromId){
            return VIEW_TYPE_MINE;
        }else{
            return VIEW_TYPE_OPPONENT;
        }


    }

    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mContext = recyclerView.getContext();
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class MineViewHolder extends RecyclerView.ViewHolder{
        private ItemChatMineAdapterBinding binding;

        public MineViewHolder(ItemChatMineAdapterBinding binding){
            super(binding.getRoot());
            this.binding = binding;


        }
    }

    public class OpponentViewHolder extends RecyclerView.ViewHolder{
        private ItemChatOpponentAdapterBinding binding;

        public OpponentViewHolder(ItemChatOpponentAdapterBinding binding){
            super(binding.getRoot());
            this.binding = binding;


        }
    }
}
