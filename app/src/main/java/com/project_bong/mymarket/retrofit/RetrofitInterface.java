package com.project_bong.mymarket.retrofit;

import androidx.annotation.Nullable;

import com.project_bong.mymarket.dto.Goods;
import com.project_bong.mymarket.dto.LoginUser;

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
    @POST("goods/save_goods.php")
    Call<Integer> callSaveGoods(@Part("goods") RequestBody bodyGoods, @Part List<MultipartBody.Part> goodsImages,@Part("info") RequestBody bodyInfo);

    @GET("goods/get_goods.php")
    Call<Goods> callGoods(@Query("goods_id") int goodsId);

    @GET("goods/get_goods_list.php")
    Call<ArrayList<Goods>> callGoodsList(@Query("page_idx") int pageIdx);

    @FormUrlEncoded
    @POST("goods/change_state.php")
    Call<String> callChangeState(@Field("goods_id") int goodsId,@Field("state") String state);

    @FormUrlEncoded
    @POST("goods/delete_goods.php")
    Call<String> callDeleteGoods(@Field("goods_id") int goodsId);
}
