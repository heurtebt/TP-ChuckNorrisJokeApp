package com.example.chucknorrisjokeapp

import io.reactivex.Single
import retrofit2.http.GET


interface JokeApiService {
    @GET("random/")
    fun giveMeAJoke(): Single<Joke>
}