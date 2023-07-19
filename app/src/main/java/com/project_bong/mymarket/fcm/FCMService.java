package com.project_bong.mymarket.fcm;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.splash.SplashActivity;

import java.util.Collection;
import java.util.Map;

public class FCMService extends FirebaseMessagingService {
    private final String TAG = "fcm";
    private final String CHANNEL_CHAT_ID = "chat";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "onMessageReceived 호출");

        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload : " + message.getData());

        } else {
            Log.d(TAG, "Message data size is 0");
        }

        createNotificationChannel();
        sendNotification(message.getData());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken : " + token);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_CHAT_ID, "채팅", NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private void sendNotification(Map<String, String> data) {
        int roomId =Integer.parseInt(data.get("room_id"));
        String message;
        switch (data.get("msg_type")) {
            case ChatMessage.TYPE_TEXT:
                message = data.get("content");
                break;

            case ChatMessage.TYPE_IMAGE:
                message = getString(R.string.str_message_for_image);
                break;

            default:
                return;
        }

        //Activity 시작 PendingIntent
        PendingIntent notifyPendingIntent = getPendingIntent(roomId,data.get("op_name"));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), CHANNEL_CHAT_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(data.get("op_name"))
                .setContentIntent(notifyPendingIntent)
                .setContentText(message);



        int notificationId = roomId;

        Glide.with(getBaseContext()).asBitmap().load(data.get("op_image")).circleCrop().placeholder(R.drawable.user_default).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                builder.setLargeIcon(resource);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                notificationManager.notify(notificationId, builder.build());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                builder.setLargeIcon(((BitmapDrawable)placeholder).getBitmap());
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getBaseContext());
                notificationManager.notify(notificationId, builder.build());
            }
        });
    }

    private PendingIntent getPendingIntent(int roomId,String opName){
        Intent launchIntent = new Intent(this, SplashActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchIntent.putExtra("roomId",roomId);
        launchIntent.putExtra("opName",opName);
        launchIntent.putExtra("launchMode","CHAT");

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
            return PendingIntent.getActivity(this,roomId,launchIntent,PendingIntent.FLAG_MUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        }else{
            return PendingIntent.getActivity(this,roomId,launchIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        }

    }
}
