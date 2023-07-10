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
import com.project_bong.mymarket.databinding.ItemChatCenterAdapterBinding;
import com.project_bong.mymarket.databinding.ItemChatMineAdapterBinding;
import com.project_bong.mymarket.databinding.ItemChatOpponentAdapterBinding;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.TimeConverter;

import java.util.ArrayList;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ChatMessage> listMessages;
    private Context mContext;
    private int userId;

    public final String PAGING_MODE_FRONT = "front";
    public final String PAGING_MODE_BACK = "back";
    private final int VIEW_TYPE_MINE = 100;
    private final int VIEW_TYPE_OPPONENT = 200;
    private final int VIEW_TYPE_CENTER = 300;

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

            case VIEW_TYPE_CENTER:
                ItemChatCenterAdapterBinding bindingCenter = ItemChatCenterAdapterBinding.inflate(LayoutInflater.from(mContext),viewGroup,false);
                return new CenterViewHolder(bindingCenter);

            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = listMessages.get(position);

        if(holder instanceof MineViewHolder){
            //switch 문으로 msg type별로 나눠야 함.
            switch (chatMessage.getMsgType()) {
                case ChatMessage.TYPE_TEXT:
                    Glide.with(mContext).clear(((MineViewHolder) holder).binding.imgMessageChatMine);
                    ((MineViewHolder) holder).binding.imgMessageChatMine.setVisibility(View.GONE);
                    ((MineViewHolder) holder).binding.txtMessageChatMine.setVisibility(View.VISIBLE);
                    ((MineViewHolder) holder).binding.txtMessageChatMine.setText(chatMessage.getContent());
                    break;

                case ChatMessage.TYPE_IMAGE:
                    Glide.with(mContext).load(chatMessage.getContent()).into(((MineViewHolder) holder).binding.imgMessageChatMine);
                    ((MineViewHolder) holder).binding.txtMessageChatMine.setVisibility(View.GONE);
                    ((MineViewHolder) holder).binding.imgMessageChatMine.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }

            if(chatMessage.getIsRead() == 0){
                ((MineViewHolder) holder).binding.txtIsReadChatMine.setVisibility(View.VISIBLE);
            }else if(chatMessage.getIsRead() == 1){
                ((MineViewHolder) holder).binding.txtIsReadChatMine.setVisibility(View.GONE);
            }

            ((MineViewHolder) holder).binding.txtCreatedAtChatMine.setText(getMessageTime(chatMessage.getSentAt()));


        }else if(holder instanceof OpponentViewHolder){
            Glide.with(mContext).load(chatMessage.getOpImage()).circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((OpponentViewHolder) holder).binding.imgProfileChatOpponent);

            switch (chatMessage.getMsgType()) {
                case ChatMessage.TYPE_TEXT:
                    Glide.with(mContext).clear(((OpponentViewHolder) holder).binding.imgMessageChatOpponent);
                    ((OpponentViewHolder) holder).binding.imgMessageChatOpponent.setVisibility(View.GONE);
                    ((OpponentViewHolder) holder).binding.txtMessageChatOpponent.setVisibility(View.VISIBLE);
                    ((OpponentViewHolder) holder).binding.txtMessageChatOpponent.setText(chatMessage.getContent());
                    break;

                case ChatMessage.TYPE_IMAGE:
                    Glide.with(mContext).load(chatMessage.getContent()).into(((OpponentViewHolder) holder).binding.imgMessageChatOpponent);
                    ((OpponentViewHolder) holder).binding.txtMessageChatOpponent.setVisibility(View.GONE);
                    ((OpponentViewHolder) holder).binding.imgMessageChatOpponent.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }

            ((OpponentViewHolder) holder).binding.txtCreatedAtChatOpponent.setText(getMessageTime(chatMessage.getSentAt()));
        }else if(holder instanceof CenterViewHolder){
            ((CenterViewHolder) holder).binding.txtDividerDate.setText(chatMessage.getContent());
        }


    }

    public void addFirstItems(ArrayList<ChatMessage> items){
        if(items.size() > 0){
            Log.d("chat","addFirstItems Size : "+items.size());
            listMessages.addAll(items);
            int insertedCount = setDateDividerAndGetCount(0,items.size()-1);
            notifyItemRangeInserted(0,insertedCount);

        }
    }

    public void updatePagedMessages(ArrayList<ChatMessage> items, String pagingMode,boolean isFirstPage){
        if(pagingMode.equals(PAGING_MODE_FRONT)){
            listMessages.addAll(0,items);
            int insertedCount = setDateDividerAndGetCount(0,items.size()-1);
            if(isFirstPage){
                TimeConverter timeConverter = new TimeConverter();
                String firstTime = timeConverter.getDateForChatDivider(listMessages.get(0).getSentAt());
                listMessages.add(0,makeDateDivider(firstTime));
                notifyItemRangeInserted(0,insertedCount+1);
            }else{
                notifyItemRangeInserted(0,insertedCount);
            }


        }else if(pagingMode.equals(PAGING_MODE_BACK)){
            int positionStart = listMessages.size();
            listMessages.addAll(items);
            int insertedCount = setDateDividerAndGetCount(positionStart-1,listMessages.size()-1);
            notifyItemRangeInserted(positionStart,insertedCount);
        }
    }

    public int setDateDividerAndGetCount(int firstPosition, int lastPosition){
        Log.d("chat","firstPosition : "+firstPosition+" lastPosition : "+lastPosition);
        if(firstPosition>=lastPosition){
            return 0;
        }

        TimeConverter timeConverter = new TimeConverter();
        int count = 0;
        for(int i = lastPosition;i>=firstPosition;i--){
            if(i+1 <= listMessages.size()-1){
                String before = listMessages.get(i).getSentAt();
                String after = listMessages.get(i+1).getSentAt();
                if(timeConverter.isDifferentDay(before,after)){
                    Log.d("chat","before : "+before+" after : "+after);
                    String dateDivider = timeConverter.getDateForChatDivider(after);
                    ChatMessage chatMessage = makeDateDivider(dateDivider);
                    listMessages.add(i+1,chatMessage);
                    count++;
                }

            }
        }
        Log.d("chat","last : "+lastPosition+" first : "+firstPosition+ " count : "+count);

        return lastPosition - firstPosition + count + 1;
    }

    private ChatMessage makeDateDivider(String content){
        return new ChatMessage(ChatMessage.TYPE_CENTER,content);
    }


    public void addNewItem(ChatMessage chatMessage){
        int newPosition = getItemCount();
        listMessages.add(chatMessage);
        notifyItemInserted(newPosition);
    }

    public void updateMyItem(ChatMessage chatMessage){
        String chatId = chatMessage.getSentAt();

        for(int i = listMessages.size()-1;i>=0;i--){
            Log.d("chat","chatId : "+listMessages.get(i).getSentAt());
            if(listMessages.get(i).getSentAt() != null && listMessages.get(i).getSentAt().equals(chatId)){
                listMessages.set(i,chatMessage);
                notifyItemChanged(i);
                break;
            }
        }
    }



    public void updateIsRead(){
        int positionStart = 0;
        for(int i = listMessages.size()-1; i>=0;i--){
            int isRead = listMessages.get(i).getIsRead();
            if(isRead == 1 || listMessages.get(i).getFromId() != userId){
                positionStart = i+1;
                break;
            }

            if(isRead == 0 && listMessages.get(i).getFromId() == userId){
                ChatMessage chatMessage = listMessages.get(i);
                chatMessage.setIsRead(1);
                listMessages.set(i,chatMessage);
            }
        }
        Log.d("chat","updateIsRead -> posStart : "+positionStart);

        int count = listMessages.size() - positionStart;
        notifyItemRangeChanged(positionStart,count);
    }

    @Override
    public int getItemViewType(int position) {
        int fromId = listMessages.get(position).getFromId();

        if(listMessages.get(position).getMsgType().equals(ChatMessage.TYPE_CENTER)){
            return VIEW_TYPE_CENTER;
        }

        if(userId == fromId){
            return VIEW_TYPE_MINE;
        }else{
            return VIEW_TYPE_OPPONENT;
        }
    }

    public ChatMessage getItem(int position) {
        return listMessages.get(position);
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

    private String getMessageTime(String utcTime){
        TimeConverter timeConverter = new TimeConverter();
        return timeConverter.getTime(utcTime);
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

    public class CenterViewHolder extends RecyclerView.ViewHolder{
        private ItemChatCenterAdapterBinding binding;

        public CenterViewHolder(ItemChatCenterAdapterBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
