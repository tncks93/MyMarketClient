package com.project_bong.mymarket.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.ChatRoomsAdapter;
import com.project_bong.mymarket.databinding.FragmentChatBinding;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.LoginUserGetter;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private ArrayList<ChatRoom> rooms;
    private ChatRoomsAdapter roomsAdapter;
    private WebSocket webSocket = null;

    //paging 변수
    private boolean isLoading = false;
    private final String MODE_FIRST = "first";
    private final String MODE_LATER = "later";
    private final String MODE_EARLIER = "earlier";

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
//        getRooms("0",MODE_FIRST);

    }

    @Override
    public void onStart() {
        super.onStart();
        getNewRooms();
        initiateWebSocket();
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
                ChatRoom chatRoom = roomsAdapter.getItem(pos);
                Log.d("chat","roomInfo -> "+new Gson().toJson(chatRoom).toString());
                int roomId = chatRoom.getRoomId();
                Intent intent = new Intent(getContext(),ChatRoomActivity.class);
                intent.putExtra("roomId",roomId);
                intent.putExtra("opName",chatRoom.getOpName());
                startActivity(intent);
            }
        });

        binding.recyclerRoomChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //paging
                int lastVisibleItemPosition = ((LinearLayoutManager)binding.recyclerRoomChat.getLayoutManager()).findLastCompletelyVisibleItemPosition() +1;
                int itemTotalCount = roomsAdapter.getItemCount();
                if(itemTotalCount > 0 && lastVisibleItemPosition == itemTotalCount && !isLoading){
                    int lastIdx = roomsAdapter.getItemCount()-1;
                    Log.d("chat","preLastIdx : "+roomsAdapter.getItem(lastIdx).getUpdatedAt());
                    getRooms(roomsAdapter.getItem(lastIdx).getUpdatedAt(),MODE_EARLIER);
                }

            }
        });

    }

    private void getNewRooms(){
        if(roomsAdapter.getItemCount() == 0){
            getRooms("0",MODE_FIRST);
        }else{
            getRooms(roomsAdapter.getItem(0).getUpdatedAt(),MODE_LATER);
        }
    }

    private void getRooms(String pageIdx,String mode) {

        isLoading = true;
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getContext()).create(RetrofitInterface.class);
        Call<ArrayList<ChatRoom>> callChatRooms = retrofit.callChatRooms(pageIdx,mode);
        callChatRooms.enqueue(new Callback<ArrayList<ChatRoom>>() {
            @Override
            public void onResponse(Call<ArrayList<ChatRoom>> call, Response<ArrayList<ChatRoom>> response) {
                if (response.body() != null) {
                    if(response.body().size()>=1){
                        Log.d("chat","resSize : "+response.body().size());
                        Log.d("chat","resFirstUpdatedAt : "+response.body().get(0).getUpdatedAt());

                    }

                    switch (mode) {
                        case MODE_FIRST:
                            roomsAdapter.addFirstItems(response.body());
                            break;

                        case MODE_EARLIER:
                            roomsAdapter.addItems(response.body());
                            break;

                        case MODE_LATER:
                            roomsAdapter.addNewItems(response.body());
                            break;

                        default:
                            break;
                    }
//                    if (pageIdx.equals("0")) {
//                        roomsAdapter.newItems(response.body());
//                    } else {
//                        roomsAdapter.addItems(response.body());
//                    }
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<ArrayList<ChatRoom>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getContext(), t);
                isLoading = false;
            }
        });
    }

    private void initiateWebSocket(){
        if(webSocket == null){
            Log.d("chat","LobbyWebSocket 호출");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("ws://3.39.40.4:8080").build();

            ChatFragment.LobbySocketListener socketListener = new ChatFragment.LobbySocketListener();

            webSocket = client.newWebSocket(request, socketListener);
            client.dispatcher().executorService().shutdown();
        }
    }

    public class LobbySocketListener extends WebSocketListener {
        private final String TAG = "chat";
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
            super.onOpen(webSocket,response);
            JsonObject jsonEntry = new JsonObject();
            jsonEntry.addProperty("type","LOBBY");
            jsonEntry.addProperty("user_id", LoginUserGetter.getLoginUser().getId());

            webSocket.send(jsonEntry.toString());

        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            Log.d(TAG,"socket closed");
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable okhttp3.Response response) {
            super.onFailure(webSocket, t, response);
            Log.d(TAG,"error : "+t);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket,text);
            Log.d(TAG,"message : "+text);

            //로비 업데이트
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatRoom room = new Gson().fromJson(text, ChatRoom.class);
                    roomsAdapter.updateRoom(room);
                }
            });



        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        webSocket.close(1000,null);
        webSocket = null;
    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
