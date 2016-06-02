package com.demo.retroboot;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by morbo on 6/2/16.
 */
public interface DemoServiceApi {

    @GET("echo")
    Observable<Event> getEcho();

}
