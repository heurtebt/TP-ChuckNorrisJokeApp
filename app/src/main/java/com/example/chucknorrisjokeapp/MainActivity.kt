package com.example.chucknorrisjokeapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.exmaple.chucknorrisjokeapp.JokeTouchHelper
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: JokeAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val jokeService = JokeApiServiceFactory().createService()
    private val compDisp: CompositeDisposable = CompositeDisposable()

    private val jokes : MutableList<Joke> = mutableListOf()
    private val savedJokes : MutableList<Boolean> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = JokeAdapter(
            {getJokes()},
            {value->onShareButtonClick(value)},
            {joke,saved->onSaveButtonClick(joke,saved)}
        )

        recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }

        val sharedPreferences = getSharedPreferences("savedJokes",Context.MODE_PRIVATE)
        if(sharedPreferences.contains("savedJokes")) {
            sharedPreferences.getString("savedJokes","")
                ?.let{Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {
                    jokes.addAll(it)
                    it.forEach { _ -> savedJokes.add(true) }
                    viewAdapter.addJokes(jokes,savedJokes)
                }
        }

        if (savedInstanceState != null) {
            savedInstanceState.getString("jokes")
                ?.let{Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {
                    jokes.addAll(it)
                    it.forEach{_->savedJokes.add(false)}
                    viewAdapter.addJokes(jokes,savedJokes)
                }
        }else{getJokes()}

        val touchHelper = JokeTouchHelper(
            {i -> viewAdapter.onJokeRemoved(i)},
            {from,to->viewAdapter.onItemMoved(from,to)}
        )
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onStop(){
        super.onStop()
        compDisp.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        jokes.clear()
        jokes.addAll(viewAdapter.getJokes())
        savedJokes.clear()
        savedJokes.addAll(viewAdapter.getSaved())
        outState.putString("jokes",Json(JsonConfiguration.Stable).stringify(Joke.serializer().list,
            jokes.filterIndexed { index, _ -> !savedJokes[index] }))
        super.onSaveInstanceState(outState)
    }

    private fun onShareButtonClick(value:String){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Look at this joke ! : "+value)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun onSaveButtonClick(joke:Joke,saved : Boolean){
        jokes.clear()
        jokes.addAll(viewAdapter.getJokes())
        savedJokes.clear()
        savedJokes.addAll(viewAdapter.getSaved())
        val sharedPreferences = getSharedPreferences("savedJokes",Context.MODE_PRIVATE)
        savedJokes[jokes.indexOf(joke)]=saved
        sharedPreferences.edit()
            .putString("savedJokes",
                Json(JsonConfiguration.Stable)
                    .stringify(Joke.serializer().list,
                        jokes.filterIndexed { index, _ -> savedJokes[index] }))
            .apply()
        viewAdapter.addJokes(jokes,savedJokes)
    }

    private fun getJokes() {
        jokes.clear()
        jokes.addAll(viewAdapter.getJokes())
        savedJokes.clear()
        savedJokes.addAll(viewAdapter.getSaved())
        val jokeSingle : Single<Joke> = jokeService.giveMeAJoke()
        compDisp.add(jokeSingle.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeat(10)
            .doOnSubscribe {progressBar.visibility = View.VISIBLE}
            .doAfterTerminate {progressBar.visibility = View.GONE}
            .subscribeBy(
                onError = { e -> Log.wtf("Request failed", e) },
                onNext ={joke : Joke -> jokes.add(joke)
                    savedJokes.add(false)
                },
                onComplete = {
                    viewAdapter.addJokes(jokes, savedJokes)
                }
            )
        )
    }
}
