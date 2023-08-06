package com.project_bong.mymarket.retrofit;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.project_bong.mymarket.dto.ChatMessage;
import com.project_bong.mymarket.dto.ChatRoom;
import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.dto.InterestingGoods;
import com.project_bong.mymarket.dto.LoginUser;
import com.project_bong.mymarket.dto.User;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitInterface {
    @FormUrlEncoded
    @POST("login/confirm_user.php")
    Call<String> callConfirmUser(@Field("phoneNum") String phoneNum);

    @FormUrlEncoded
    @POST("login/login.php")
    Call<LoginUser> callLoginUser(@Field("id")String id,@Field("isFirst") boolean isFirst);

    @Multipart
    @POST("login/sign_up.php")
    Call<String> callSignUp(@Part("user") RequestBody bodyUser, @Part @Nullable MultipartBody.Part image);

    @Multipart
    @POST("my_page/update_user.php")
    Call<String> callUpdateUser(@Part("user") RequestBody bodyUser, @Part @Nullable MultipartBody.Part image);

    @Multipart
    @POST("goods/save_goods.php")
    Call<Integer> callSaveGoods(@Part("goods") RequestBody bodyGoods, @Part List<MultipartBody.Part> goodsImages,@Part("info") RequestBody bodyInfo);

    @GET("goods/get_goods.php")
    Call<Goods> callGoods(@Query("goods_id") int goodsId);


    @GET("goods/get_goods_list.php")
    Call<ArrayList<Goods>> callGoodsList(@Query("page_idx") int pageIdx);

    @FormUrlEncoded
    @POST("goods/change_state.php")
    Call<String> callChangeState(@Field("goods_id") int goodsId,@Field("state") String state,@Field("is_sold_out") boolean isSoldOut);

    @FormUrlEncoded
    @POST("goods/delete_goods.php")
    Call<String> callDeleteGoods(@Field("goods_id") int goodsId);

    @Multipart
    @POST("goods/search_goods.php")
    Call<ArrayList<Goods>> callSearchedGoodsList(@Part("page_idx") int pageIdx,@Part("filter") RequestBody bodyFilter);

    @FormUrlEncoded
    @POST("chat/confirm_chat_room.php")
    Call<Integer> callConfirmChatRoom(@Field("goods_id") int goodsId,@Field("seller_id") int sellerId,@Field("created_at") String createdAt);

    @GET("chat/get_chat_rooms.php")
    Call<ArrayList<ChatRoom>> callChatRooms(@Query("page_idx") String pageIdx, @Query("mode") String mode);

    @GET("chat/get_room.php")
    Call<ChatRoom> callChatRoom(@Query("room_id") int roomId);
    @GET("chat/get_messages.php")
    Call<JsonObject> callChatMessages(@Query("room_id") int roomId,@Query("book_mark") @Nullable String bookMark);

    @GET("chat/get_all_unread.php")
    Call<Integer> callAllUnread(@Query("room_id") int roomId);

    @GET("chat/get_messages_paging.php")
    Call<JsonObject> callChatMessagesPaging(@Query("room_id") int roomId, @Query("page_idx") String pageIdx, @Query("paging_mode") String pagingMode);

    @GET("chat/get_goods_for_chat.php")
    Call<Goods> callGoodsForChat(@Query("room_id") int roomId);

    @Multipart
    @POST("chat/save_images.php")
    Call<ArrayList<ChatMessage>> callSaveImagesForChat(@Part("chats") RequestBody chats, @Part List<MultipartBody.Part> images);

    @FormUrlEncoded
    @POST("fcm/save_token.php")
    Call<String> callSaveFcmToken(@Field("token") String token);

    @GET("goods/get_buyer_list.php")
    Call<ArrayList<User>> callBuyerList(@Query("goods_id") int goodsId);

    @FormUrlEncoded
    @POST("goods/save_purchase.php")
    Call<String> callSavePurchase(@Field("buyer_id") int buyerId, @Field("goods_id") int goodsId);

    @GET("my_page/get_sales.php")
    Call<ArrayList<Goods>> callGetSales(@Query("state") String state,@Query("paging_idx") int pagingIdx);

    @GET("my_page/get_purchase.php")
    Call<ArrayList<Goods>> callGetPurchase(@Query("paging_idx") int paging_idx);

    @FormUrlEncoded
    @POST("my_page/remove_purchase.php")
    Call<Void> callRemovePurchase(@Field("goods_id") int goodsId);

    @GET("goods/get_is_interest.php")
    Call<Boolean> callIsInterest(@Query("goods_id") int goodsId);

    @FormUrlEncoded
    @POST("goods/set_is_interest.php")
    Call<Boolean> callSetIsInterest(@Field("goods_id") int goods_id);

    @GET("my_page/get_interests.php")
    Call<ArrayList<InterestingGoods>> callGetInterest(@Query("paging_idx") String pagingIdx);
}
