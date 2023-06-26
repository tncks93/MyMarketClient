package com.project_bong.mymarket.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.ChatMessageAdapter;
import com.project_bong.mymarket.databinding.ActivityChatRoomBinding;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.Shared;
import com.project_bong.mymarket.util.TimeConverter;

import java.lang.reflect.Type;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {
    private ActivityChatRoomBinding binding;
    private int roomId;
    private ChatRoom chatRoom;
    private boolean isLoading = false;
    private boolean isLastPage = true;
    private boolean isFirstPage = false;
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> listMessages;
    private WebSocket webSocket = null;

    private final String CONTENT_MESSAGE = "message";
    private final String CONTENT_READ_EVENT = "read_event";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getIntExtra("roomId",-1);

        initMessageAdapter();
//        getRoomInfo();
        getFirstMessages();

        initiateWebSocket();

        binding.btnSendMessageChatRoom.setOnClickListener(v->{
            String message = binding.editMessageChatRoom.getText().toString();
            if(!message.replaceAll(" ","").equals("")){
                String msgJson = getMsgJsonFromStr(message);
                ChatMessage myMessage = new ChatMessage(LoginUserGetter.getLoginUser().getId(),ChatMessage.TYPE_TEXT,message,0);
                messageAdapter.addNewItem(myMessage);
                binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);
                webSocket.send(msgJson);
                binding.editMessageChatRoom.setText("");
                Log.d("chat","sendMessage : "+msgJson);
            }else{
                Toast.makeText(getBaseContext(),getString(R.string.str_hint_send_chat),Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerMessageChatRoom.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(messageAdapter.getItemCount() == 0 || isLoading){
                    return;
                }
                int firstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int lastVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();

                if(firstVisiblePosition == 0 && !isFirstPage){
                    //이전 채팅 paging
                    getMessagesWithPaging(messageAdapter.getItem(0).getSentAt(), messageAdapter.PAGING_MODE_FRONT);
                    return;
                }

                if(lastVisiblePosition == messageAdapter.getItemCount()-1 && !isLastPage){
                    //나중 채팅 paging
                    getMessagesWithPaging(messageAdapter.getItem(messageAdapter.getItemCount()-1).getSentAt(), messageAdapter.PAGING_MODE_BACK);
                }


            }
        });

    }

    private void initiateWebSocket(){
        if(webSocket == null){
            Log.d("chat","testWebSocket 호출");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("ws://3.39.40.4:8080").build();

            ChatRoomActivity.SocketListener socketListener = new ChatRoomActivity.SocketListener();

            webSocket = client.newWebSocket(request, socketListener);
            client.dispatcher().executorService().shutdown();
        }

    }

    private void initMessageAdapter(){
        listMessages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(listMessages);
        binding.recyclerMessageChatRoom.setLayoutManager(new LinearLayoutManager(getBaseContext(),LinearLayoutManager.VERTICAL,false));
        binding.recyclerMessageChatRoom.setAdapter(messageAdapter);
        binding.recyclerMessageChatRoom.setItemAnimator(null);
    }
    private void getRoomInfo(){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ChatRoom> callChatRoom = retrofit.callChatRoom(roomId);
        callChatRoom.enqueue(new Callback<ChatRoom>() {
            @Override
            public void onResponse(Call<ChatRoom> call, Response<ChatRoom> response) {
                if(response.body() != null){
                    chatRoom = response.body();
                }else{
                    Toast.makeText(getBaseContext(),getString(R.string.failure_on_network),Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ChatRoom> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                finish();
            }
        });
    }
    private void getFirstMessages(){
        isLoading = true;
        String bookMark = new Shared(getBaseContext(),Shared.BOOK_MARK_FILE_NAME).getSharedString(String.valueOf(roomId));
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<JsonObject> callChatMessages = retrofit.callChatMessages(roomId,bookMark);
        callChatMessages.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.body() != null){
                    JsonObject jsonResponse = response.body();
                    isLastPage = jsonResponse.get("is_last").getAsBoolean();
                    Log.d("chat","firstMessages-> isLastPage : "+isLastPage);

                    //JsonArray 를 ArrayList<ChatMessage> 로 변환
                    Type typeList = new TypeToken<ArrayList<ChatMessage>>(){}.getType();
                    ArrayList<ChatMessage> firstMessages = new Gson().fromJson(jsonResponse.get("messages").getAsJsonArray().toString(),typeList);
                    messageAdapter.addFirstItems(firstMessages);
                }

                isLoading = false;
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
                isLoading = false;
            }
        });
     }

     private void getMessagesWithPaging(String pagingIdx, String pagingMode){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<JsonObject> callPagedChatMessages = retrofit.callChatMessagesPaging(roomId,pagingIdx,pagingMode);
        callPagedChatMessages.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.body() != null){
                    JsonObject jsonResponse = response.body();
                    if(pagingMode.equals(messageAdapter.PAGING_MODE_FRONT)){
                        isFirstPage = jsonResponse.get("is_first").getAsBoolean();
                        Log.d("chat","pagingResponse-> isFirst : "+isFirstPage);
                    }else if(pagingMode.equals(messageAdapter.PAGING_MODE_BACK)){
                        isLastPage = jsonResponse.get("is_last").getAsBoolean();
                        Log.d("chat","pagingResponse-> isLast : "+isLastPage);
                    }
                    Type typeList = new TypeToken<ArrayList<ChatMessage>>(){}.getType();
                    ArrayList<ChatMessage> pagedMessages = new Gson().fromJson(jsonResponse.get("messages").getAsJsonArray().toString(),typeList);
                    messageAdapter.updatePagedMessages(pagedMessages,pagingMode);

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
            }
        });
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000,null);
        webSocket = null;

        saveBookMark();
    }

    private String getMsgJsonFromStr(String message){
        TimeConverter timeConverter = new TimeConverter();
        String timeUTC = timeConverter.getUTC();
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type","MESSAGE");
        jsonMessage.addProperty("content",message);
        jsonMessage.addProperty("msg_type","text");
        jsonMessage.addProperty("sent_at",timeUTC);

        return jsonMessage.toString();
    }

    private void saveBookMark(){
        Shared sharedBookMark = new Shared(getBaseContext(),Shared.BOOK_MARK_FILE_NAME);
        TimeConverter timeConverter = new TimeConverter();
        sharedBookMark.setSharedString(String.valueOf(roomId),timeConverter.getUTC());
    }

    public class SocketListener extends WebSocketListener {
        private final String TAG = "chat";
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
            Log.d(TAG,"socket is open");
            JsonObject jsonEntry = new JsonObject();
            jsonEntry.addProperty("type","ENTRY");
            jsonEntry.addProperty("room_id",roomId);
            jsonEntry.addProperty("user_id", LoginUserGetter.getLoginUser().getId());

            webSocket.send(jsonEntry.toString());
            Log.d("chat","sendEntry : "+jsonEntry.toString());


        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
            Log.d("chat","socket closed");
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosing(webSocket, code, reason);
            Log.d("chat","socket closing");
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable okhttp3.Response response) {
            super.onFailure(webSocket, t, response);
            Log.d(TAG,"error : "+t);
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket,text);
            Log.d("chat","message : "+text);
            JsonObject jsonMessage = new JsonParser().parse(text).getAsJsonObject();
            String contentType = jsonMessage.get("content_type").getAsString();

            switch (contentType) {
                case CONTENT_MESSAGE:
                    ChatMessage newMessage = new Gson().fromJson(jsonMessage.get("content"),ChatMessage.class);
                    Log.d("chat","newMessage : "+newMessage.getContent());
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            messageAdapter.addNewItem(newMessage);
//                            binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);
//                        }
//                    });


                    break;

                case CONTENT_READ_EVENT:
                    //read_event
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageAdapter.updateIsRead();
                        }
                    });

                    break;

                default:
                    break;
            }




        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }
    }

}