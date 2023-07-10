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
import com.project_bong.mymarket.databinding.ItemChatRoomsAdapterBinding;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.TimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomsAdapter extends RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder> {
    private ArrayList<ChatRoom> roomsList;
    private Context mContext;


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

        getAllUnread(holder,chatRoom.getRoomId());

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
            case  ChatMessage.TYPE_TEXT:
                holder.binding.txtLastMessageChat.setText(chatRoom.getLastMessage());
            break;

            case ChatMessage.TYPE_IMAGE:
                holder.binding.txtLastMessageChat.setText(mContext.getString(R.string.str_message_for_image));
                break;

          default:
            break;
        }

        setLocalTime(holder,chatRoom.getUpdatedAt());




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

    public void addFirstItems(ArrayList<ChatRoom> items){
        roomsList.clear();
        roomsList.addAll(items);
        notifyItemRangeInserted(0,items.size());
//        notifyDataSetChanged();

    }

    public void addNewItems(ArrayList<ChatRoom> items){
        if(items.size() > 0){
            for(int i=0; i<items.size();i++){
                int newRoomId = items.get(i).getRoomId();

                roomsList.removeIf(chatRoom -> newRoomId == chatRoom.getRoomId());
            }



            roomsList.addAll(0,items);
        }
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

    private void getAllUnread(ChatRoomsAdapter.ViewHolder holder,int roomId){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(mContext).create(RetrofitInterface.class);
        Call<Integer> callAllUnread = retrofit.callAllUnread(roomId);

        callAllUnread.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.body() != null){
                    int count = response.body();
                    String strCount = Integer.toString(count);
                    Log.d("chat","unreadCount : "+count+ " room : "+roomId);
                    if(count == 0){
                        holder.binding.textUnreadChat.setText(strCount);
                        holder.binding.textUnreadChat.setVisibility(View.GONE);
                    }else if(count < 100){
                        holder.binding.textUnreadChat.setText(strCount);
                        holder.binding.textUnreadChat.setVisibility(View.VISIBLE);
                    }else{
                        String over100 = count + "+";
                        holder.binding.textUnreadChat.setText(over100);
                        holder.binding.textUnreadChat.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                holder.binding.textUnreadChat.setVisibility(View.GONE);
                Log.d("chat","getUnreadCount error");
            }
        });
    }

    private void setLocalTime(ChatRoomsAdapter.ViewHolder holder, String UTCTime){
        TimeConverter timeConverter = new TimeConverter();
        LocalDateTime localDateTime = timeConverter.convertUTCtoLocal(UTCTime);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate thisYearStart = LocalDate.of(today.getYear(),1,1);

        Log.d("chat","local : "+localDateTime.toString()+" today : "+today.toString()
        +" yesterday : "+yesterday.toString()+" thisYearStart : "+thisYearStart.toString());

        String timeForDisplay;
        if(today.isEqual(localDateTime.toLocalDate())){
            //오늘
            String pattern = "a hh:mm";
            timeForDisplay = timeConverter.formatLocal(localDateTime,pattern);

        }else if(yesterday.isEqual(localDateTime.toLocalDate())){
            //어제
            timeForDisplay = "어제";
        }else if(localDateTime.toLocalDate().compareTo(thisYearStart)>=0){
            //올 해
            String pattern = "M월 d일";
            timeForDisplay = timeConverter.formatLocal(localDateTime,pattern);
        }else{
            //그 외
            String pattern = "yyyy.MM.dd";
            timeForDisplay = timeConverter.formatLocal(localDateTime,pattern);

        }

        if(timeForDisplay != null){
            holder.binding.txtTimeLastMessageChat.setText(timeForDisplay);
        }
    }
}
