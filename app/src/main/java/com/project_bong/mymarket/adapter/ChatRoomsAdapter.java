package com.project_bong.mymarket.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project_bong.mymarket.databinding.ItemChatRoomsAdapterBinding;
import com.project_bong.mymarket.dto.ChatRoom;

import java.util.ArrayList;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder> {
    private ArrayList<ChatRoom> roomsList;
    private Context mContext;

    private final String MSG_TYPE_TEXT = "text";

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }
    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        final ItemChatRoomsAdapterBinding binding;
        public ViewHolder(ItemChatRoomsAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v->{
                if(mListener != null){
                    mListener.onItemClick(v,getAdapterPosition());
                }
            });


            }

    }

    public ChatRoomsAdapter(ArrayList<ChatRoom> items){
        roomsList = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        return new ViewHolder(ItemChatRoomsAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false));
    }

    @Override
    public void onBindViewHolder(ChatRoomsAdapter.ViewHolder holder, int position) {
        ChatRoom chatRoom = roomsList.get(position);

        Glide.with(mContext).load(chatRoom.getOpImage()).centerCrop().circleCrop().into(holder.binding.imgOpponentProfile);
        if(chatRoom.getGoodsImage() == null){
            holder.binding.imgGoodsChat.setVisibility(View.GONE);
            Glide.with(mContext).clear(holder.binding.imgGoodsChat);
        }else{
            holder.binding.imgGoodsChat.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(chatRoom.getGoodsImage()).centerCrop().circleCrop().into(holder.binding.imgGoodsChat);
        }

        holder.binding.txtOpponentNameChat.setText(chatRoom.getOpName());

        switch(chatRoom.getLastMessageType()){
            case  MSG_TYPE_TEXT:
                holder.binding.txtLastMessageChat.setText(chatRoom.getLastMessage());
            break;

          default:
            break;
        }


    }

    public void updateRoom(ChatRoom chatRoom){
        int roomId = chatRoom.getRoomId();
        boolean doesExist = false;
        int existPosition = -1;

        for(int i=0;i<roomsList.size();i++){
            if(roomsList.get(i).getRoomId() == roomId){
                doesExist = true;
                existPosition = i;
                break;
            }
        }

        if(doesExist && existPosition != -1){
            roomsList.remove(existPosition);
            notifyItemRemoved(existPosition);
        }

        roomsList.add(0,chatRoom);
        notifyItemInserted(0);

    }

    public void newItems(ArrayList<ChatRoom> items){
        roomsList.clear();
        roomsList.addAll(items);
        notifyDataSetChanged();

    }
    public void addItems(ArrayList<ChatRoom> items){
        int firstPos = getItemCount();
        int itemSize = items.size();
        roomsList.addAll(items);
        notifyItemRangeInserted(firstPos,itemSize);
    }

    public ChatRoom getItem(int pos){
        return roomsList.get(pos);
    }

    @Override
    public int getItemCount() {
        return roomsList.size();
    }

    @Override
    public long getItemId(int position) {
        return roomsList.get(position).getRoomId();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
    }
}
