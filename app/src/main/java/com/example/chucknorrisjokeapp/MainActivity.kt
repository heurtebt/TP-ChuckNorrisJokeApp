package com.example.chucknorrisjokeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: JokeAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val compDisp: CompositeDisposable = CompositeDisposable()
    private val jokes : MutableList<Joke> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = JokeAdapter{getJokes()}

        getJokes()

        recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun getJokes() {
        val jokeService = JokeApiServiceFactory().createService()
        val jokeSingle : Single<Joke> = jokeService.giveMeAJoke()
        compDisp.add(jokeSingle.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeat(10)
            .doOnSubscribe {progressBar.visibility = View.VISIBLE}
            .doAfterTerminate {progressBar.visibility = View.GONE}
            .subscribeBy(
                onError = { e -> Log.wtf("Request failed", e) },
                onNext ={joke : Joke ->
                    jokes.add(joke)},
                onComplete = {viewAdapter.addJokes(jokes)}
            )
        )
    }

    override fun onStop(){
        super.onStop()
        compDisp.clear()
    }
}
