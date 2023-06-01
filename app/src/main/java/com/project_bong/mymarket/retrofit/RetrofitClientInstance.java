package com.project_bong.mymarket.retrofit;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.project_bong.mymarket.R;
import com.project_bong.mymarket.util.Shared;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {
    private static Retrofit retrofit;
    private static OkHttpClient.Builder httpClient;
    private static final String BASE_URL = "http://3.39.40.4/";
    private static final String FAILURE = "FAILURE";
    private static Gson gson = new GsonBuilder()
            .setLenient().create();



    public static Retrofit getRetrofitInstance(Context mContext) {
        if(retrofit == null) {
            if(httpClient == null){
                httpClient = new OkHttpClient.Builder();
                Shared shared = new Shared(mContext,Shared.LOGIN_FILE_NAME);
                String sessId = shared.getSharedString(Shared.LOGIN_KEY);
                if(sessId != null){
                    String cookie = "PHPSESSID="+sessId;
                    httpClient.addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request().newBuilder().addHeader("Cookie", cookie).build();
                            return chain.proceed(request);
                        }
                    });
                }

            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }


    public static void setOnFailure(Context context, Throwable t){
        String msg = context.getString(R.string.failure_on_network);
        Log.d(FAILURE,t.getMessage());
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();

    }

    public static void initiate(){
        retrofit = null;
        httpClient = null;
    }
}
