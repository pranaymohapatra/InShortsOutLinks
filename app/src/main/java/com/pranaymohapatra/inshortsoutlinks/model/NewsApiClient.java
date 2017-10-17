package com.pranaymohapatra.inshortsoutlinks.model;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface NewsApiClient {
    @GET("newsjson")
    Observable<List<NewsModel>> getNews();
}