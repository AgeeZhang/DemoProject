package com.example.myapplication;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface UpdateService {

    //下载更新
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
