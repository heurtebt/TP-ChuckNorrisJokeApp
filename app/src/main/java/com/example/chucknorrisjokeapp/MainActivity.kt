package com.example.chucknorrisjokeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val compDisp: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = JokeAdapter(ChuckNorrisJokes.jokes)

        recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }


        val jokeService = JokeApiServiceFactory().createService()
        val jokeSingle : Single<Joke> = jokeService.giveMeAJoke()
        compDisp.add(jokeSingle.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribeBy(
                onError = { e  -> Log.wtf("Request failed", e) },
                onSuccess = { joke: Joke -> Log.wtf("Joke", joke.value) }
            )
        )
    }

    override fun onStop(){
        super.onStop()
        compDisp.dispose()
    }
}
