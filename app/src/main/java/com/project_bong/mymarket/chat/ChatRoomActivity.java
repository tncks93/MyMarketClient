package com.project_bong.mymarket.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.databinding.ActivityChatRoomBinding;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.TimeConverter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class ChatRoomActivity extends AppCompatActivity {
    private ActivityChatRoomBinding binding;
    private int roomId;
    private WebSocket webSocket = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getIntExtra("roomId",-1);

        initiateWebSocket();

        binding.btnSendMessageChatRoom.setOnClickListener(v->{
            String message = binding.editMessageChatRoom.getText().toString();
            if(!message.replaceAll(" ","").equals("")){
                String msgJson = getMsgJsonFromStr(message);

                webSocket.send(msgJson);
                Log.d("chat","sendMessage : "+msgJson);
            }else{
                Toast.makeText(getBaseContext(),getString(R.string.str_hint_send_chat),Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000,null);
        webSocket = null;
    }

    private String getMsgJsonFromStr(String message){
        TimeConverter timeConverter = new TimeConverter();
        String timeUTC = timeConverter.getUTC();
        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type","MESSAGE");
        jsonMessage.addProperty("message",message);
        jsonMessage.addProperty("msg_type","text");
        jsonMessage.addProperty("sent_at",timeUTC);

        return jsonMessage.toString();
    }

    public class SocketListener extends WebSocketListener {
        private final String TAG = "socket";
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


        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
        }
    }

}