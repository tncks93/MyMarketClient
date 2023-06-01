package com.project_bong.mymarket.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.ChatRoomsAdapter;
import com.project_bong.mymarket.databinding.FragmentChatBinding;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ArrayList<ChatRoom> rooms;
    private ChatRoomsAdapter roomsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolbarFragmentChat.toolbarTitle.setText(getString(R.string.str_title_chat));
        initRoomsRecyclerView();
        getRooms(0);


    }

    private void initRoomsRecyclerView(){
        rooms = new ArrayList<>();
        roomsAdapter = new ChatRoomsAdapter(rooms);
        roomsAdapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerRoomChat.addItemDecoration(new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL));
        binding.recyclerRoomChat.setLayoutManager(linearLayoutManager);
        binding.recyclerRoomChat.setAdapter(roomsAdapter);

        roomsAdapter.setOnItemClickListener(new ChatRoomsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                int roomId = roomsAdapter.getItem(pos).getRoomId();
                Intent intent = new Intent(getContext(),ChatRoomActivity.class);
                intent.putExtra("roomId",roomId);
                startActivity(intent);
            }
        });

    }

    private void getRooms(int pageIdx){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getContext()).create(RetrofitInterface.class);
        Call<ArrayList<ChatRoom>> callChatRooms = retrofit.callChatRooms(pageIdx);
        callChatRooms.enqueue(new Callback<ArrayList<ChatRoom>>() {
            @Override
            public void onResponse(Call<ArrayList<ChatRoom>> call, Response<ArrayList<ChatRoom>> response) {
                if(response.body() != null){
                    roomsAdapter.addItems(response.body());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ChatRoom>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getContext(),t);
            }
        });
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
