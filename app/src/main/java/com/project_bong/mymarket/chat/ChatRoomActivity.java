package com.project_bong.mymarket.chat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.adapter.ChatMessageAdapter;
import com.project_bong.mymarket.databinding.ActivityChatRoomBinding;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.retrofit.RetrofitClientInstance;
import com.project_bong.mymarket.retrofit.RetrofitInterface;
import com.project_bong.mymarket.util.ImageProcessor;
import com.project_bong.mymarket.util.LoginUserGetter;
import com.project_bong.mymarket.util.PermissionsGetter;
import com.project_bong.mymarket.util.Shared;
import com.project_bong.mymarket.util.TimeConverter;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private Goods goods;
    private boolean isLoading = false;
    private boolean isLastPage = true;
    private boolean isFirstPage = false;
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> listMessages;
    private WebSocket webSocket = null;

    private final String CONTENT_MESSAGE = "message";
    private final String CONTENT_READ_EVENT = "read_event";

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.CAMERA};
    private ActivityResultLauncher<String[]> requestStoragePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            boolean isAllGranted = true;
            for(boolean isGranted: result.values()){
                if(!isGranted){
                    isAllGranted = false;
                    break;
                }
            }

            if(isAllGranted){
                //이미지(카메라,앨범) 선택 dialog
                startDialogForImage();
            }
        }
    });

    private ActivityResultLauncher<Intent> getPictures = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            //다중 이미지 처리
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                try{
                    ArrayList<Uri> photoUriList = new ArrayList<>();
                    if(result.getData().getClipData() != null){
                        ClipData clipData = result.getData().getClipData();
                        for(int i=0;i<clipData.getItemCount();i++){
                            photoUriList.add(clipData.getItemAt(i).getUri());
                        }
                    }else{
                        photoUriList.add(result.getData().getData());
                    }

                    ArrayList<ChatMessage> tmpChatList = getImagesToSend(photoUriList);

                    if(isLastPage){
                        sendMultiImages(tmpChatList);
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    });

    private Uri photoUri = null;

    private ActivityResultLauncher<Intent> getCapture = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            //카메라 이미지 처리
            if(result.getResultCode() == RESULT_OK && photoUri != null){
                ArrayList<Uri> photoUriList = new ArrayList<>();
                photoUriList.add(photoUri);
                try{
                    ArrayList<ChatMessage> tmpChatList = getImagesToSend(photoUriList);
                    if(isLastPage){
                        sendMultiImages(tmpChatList);

                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getIntExtra("roomId",-1);
        String opName = getIntent().getStringExtra("opName");

        setSupportActionBar(binding.toolbarChatRoom);
        getSupportActionBar().setTitle(opName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initMessageAdapter();
//        getGoodsInfo();
//        getRoomInfo();
        getFirstMessages();

        initiateWebSocket();

        binding.btnSendMessageChatRoom.setOnClickListener(v->{
            String message = binding.editMessageChatRoom.getText().toString();
            if(!message.replaceAll(" ","").equals("")){
                JsonObject msgJson = getMsgJsonFromStr(message);
                ChatMessage myMessage = new ChatMessage(LoginUserGetter.getLoginUser().getId(),ChatMessage.TYPE_TEXT,message,msgJson.get("sent_at").getAsString(),0);
                if(isLastPage){
                    messageAdapter.addNewItem(myMessage);
                    binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);

                }else{

                }
                webSocket.send(msgJson.toString());
                binding.editMessageChatRoom.setText("");
                Log.d("chat","sendMessage : "+msgJson);
            }else{
                Toast.makeText(getBaseContext(),getString(R.string.str_hint_send_chat),Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSendImageChatRoom.setOnClickListener(v->{
            //이미지 전송 버튼
            if(!isNeedPermissions()){
                //dialog
                startDialogForImage();
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

    private void getGoodsInfo(){
        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<Goods> callGoods = retrofit.callGoodsForChat(roomId);
        callGoods.enqueue(new Callback<Goods>() {
            @Override
            public void onResponse(Call<Goods> call, Response<Goods> response) {
                if(response.body() != null){
                    goods = response.body();
                    Glide.with(getBaseContext()).load(goods.getMainImage()).circleCrop().into(binding.imgGoodsChatRoom);
                    binding.txtGoodsNameChatRoom.setText(goods.getName());
                    binding.txtGoodsStateChatRoom.setText(goods.getState());
                }
            }

            @Override
            public void onFailure(Call<Goods> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
            }
        });
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
                    if(bookMark == null){
                        binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);
                    }else{
                        //bookMark 위치로 스크롤
                    }
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
        isLoading = true;
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
                    messageAdapter.updatePagedMessages(pagedMessages,pagingMode,isFirstPage);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000,null);
        webSocket = null;

        saveBookMark();
    }

    private JsonObject getMsgJsonFromStr(String message){
        TimeConverter timeConverter = new TimeConverter();

        JsonObject jsonMessage = new JsonObject();
        jsonMessage.addProperty("type","MESSAGE");
        jsonMessage.addProperty("content",message);
        jsonMessage.addProperty("msg_type","text");
        String timeUTC = timeConverter.getUTC();
        jsonMessage.addProperty("sent_at",timeUTC);


        return jsonMessage;
    }

    private void saveBookMark(){
        Shared sharedBookMark = new Shared(getBaseContext(),Shared.BOOK_MARK_FILE_NAME);
        TimeConverter timeConverter = new TimeConverter();
        sharedBookMark.setSharedString(String.valueOf(roomId),timeConverter.getUTC());
    }

    private boolean isNeedPermissions(){
        PermissionsGetter permissionsGetter = new PermissionsGetter(getBaseContext());
        if(permissionsGetter.haveDeniedPermissions(permissions)){
            String[] deniedPermissions = permissionsGetter.getPermissions();
            if(deniedPermissions != null){
                requestStoragePermissions.launch(deniedPermissions);
                return true;
            }
        }
        return false;
    }

    private void startDialogForImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomActivity.this)
                .setItems(getResources().getStringArray(R.array.array_image_send_chat), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //카메라
                                try{
                                    photoUri = null;
                                    String fileName = "chat_"+ System.currentTimeMillis();
                                    ImageProcessor ip = new ImageProcessor(getBaseContext());
                                    photoUri = FileProvider.getUriForFile(getBaseContext(),ImageProcessor.getAUTHORITY(),ip.createTempFile(fileName));
                                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                                    getCapture.launch(captureIntent);

                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                break;

                            case 1:
                                //갤러리
                                Intent takePicturesIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                takePicturesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                                takePicturesIntent.setType("image/jpeg");

                                getPictures.launch(takePicturesIntent);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.show();
    }

    private ArrayList<ChatMessage> getImagesToSend(ArrayList<Uri> uriList) throws Exception{
        if(!checkImgCount(uriList)){

        }
        ArrayList<ChatMessage> tmpImageChatList = new ArrayList<>();
        TimeConverter timeConverter = new TimeConverter();
        for(int i=0;i<uriList.size();i++){
            Uri uri = uriList.get(i);
            ImageProcessor ip = new ImageProcessor(getBaseContext());
            Bitmap bitmap = ip.getBitmapFromContentUri(uri);
            String fileName = "chat_"+System.currentTimeMillis()+i;
            String path = ip.saveJPEGFromSingleBitmap(bitmap,fileName);
            ChatMessage chatMessage = new ChatMessage(LoginUserGetter.getLoginUser().getId(),ChatMessage.TYPE_IMAGE,path,timeConverter.getUTC(),0);
            tmpImageChatList.add(chatMessage);
            Log.d("chat","imageSentAt : "+chatMessage.getSentAt());
        }

        return tmpImageChatList;
    }

    private void sendMultiImages(ArrayList<ChatMessage> photoMessageList){
        //내 채팅 추가
        for(int i=0;i<photoMessageList.size();i++){
            messageAdapter.addNewItem(photoMessageList.get(i));
        }
        binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);

        //이미지 서버 저장
        RequestBody bodyChats = RequestBody.create(new Gson().toJson(photoMessageList), MediaType.parse("application/json; charset=utf-8"));

        ArrayList<MultipartBody.Part> partImages = new ArrayList<>();

        for(int i=0;i<photoMessageList.size();i++) {
            File image = new File(photoMessageList.get(i).getContent());
            RequestBody imageBody = RequestBody.create(image, MediaType.parse("image/jpeg"));
            String fileName = "chat_" + roomId + System.currentTimeMillis() + i + ".jpeg";
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image[]", fileName, imageBody);
            partImages.add(imagePart);
        }

        RetrofitInterface retrofit = RetrofitClientInstance.getRetrofitInstance(getBaseContext()).create(RetrofitInterface.class);
        Call<ArrayList<ChatMessage>> callSaveChatImages = retrofit.callSaveImagesForChat(bodyChats,partImages);
        callSaveChatImages.enqueue(new Callback<ArrayList<ChatMessage>>() {
            @Override
            public void onResponse(Call<ArrayList<ChatMessage>> call, Response<ArrayList<ChatMessage>> response) {
                if(response.body() != null){
                    ArrayList<ChatMessage> chatMessages = response.body();

                    for(int i=0;i<chatMessages.size();i++){
                        ChatMessage chatMessage = chatMessages.get(i);


                        JsonObject jsonMessage = (JsonObject) new Gson().toJsonTree(chatMessage);
                        jsonMessage.addProperty("type","MESSAGE");

                        webSocket.send(jsonMessage.toString());

                        Log.d("chat","imageJson : "+jsonMessage.toString());
                    }


                }
            }

            @Override
            public void onFailure(Call<ArrayList<ChatMessage>> call, Throwable t) {
                RetrofitClientInstance.setOnFailure(getBaseContext(),t);
            }
        });





    }

    private boolean checkImgCount(ArrayList<Uri> uriList){
        if(uriList.size()<=10){
            return true;
        }else{
            Toast.makeText(getBaseContext(),getString(R.string.str_too_many_images),Toast.LENGTH_SHORT).show();
            return false;
        }
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(newMessage.getFromId() == LoginUserGetter.getLoginUser().getId()){
                                messageAdapter.updateMyItem(newMessage);
                            }else{
                                messageAdapter.addNewItem(newMessage);
                                binding.recyclerMessageChatRoom.scrollToPosition(messageAdapter.getItemCount()-1);
                            }

                        }
                    });


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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}