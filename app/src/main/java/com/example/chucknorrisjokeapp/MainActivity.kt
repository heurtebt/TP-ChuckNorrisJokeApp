package com.example.chucknorrisjokeapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: JokeAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val jokeService = JokeApiServiceFactory().createService()
    private val compDisp: CompositeDisposable = CompositeDisposable()

    private val models : MutableList<JokeView.Model> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        swipe.setColorSchemeColors(getColor(R.color.colorAccent))
        swipe.setSize(0)

        viewManager = LinearLayoutManager(this)
        viewAdapter = JokeAdapter{getJokes()}

        recyclerView = findViewById<RecyclerView>(R.id.myRecyclerView).apply {
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }

        val sharedPreferences = getSharedPreferences("savedJokes",Context.MODE_PRIVATE)
        if(sharedPreferences.contains("savedJokes")) {
            sharedPreferences.getString("savedJokes","")
                ?.let{Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {it->
                    models.addAll(it.map {
                        JokeView.Model(
                            it,
                            {value -> onShareButtonClick(value)},
                            {model -> onSaveButtonClick(model)},
                            true)
                    })
                    viewAdapter.updateData(models)
                }
        }

        if (savedInstanceState != null) {
            savedInstanceState.getString("jokes")
                ?.let{Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {it->
                    models.addAll(it.map {
                        JokeView.Model(
                            it,
                            {value -> onShareButtonClick(value)},
                            {model -> onSaveButtonClick(model)},
                            false)
                    })
                    viewAdapter.updateData(models)
                }
        }else{getJokes()}

        val touchHelper = JokeTouchHelper(
            {i -> onJokeRemoved(i)},
            {from,to-> onItemMoved(from,to)}
        )
        touchHelper.attachToRecyclerView(recyclerView)

        swipe.setOnRefreshListener { getJokes(true) }
    }

    override fun onStop(){
        super.onStop()
        compDisp.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("jokes",Json(JsonConfiguration.Stable).stringify(Joke.serializer().list,
            models.filter{!it.saved }.map{it.joke}))
        super.onSaveInstanceState(outState)
    }

    private fun onShareButtonClick(value:String){
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Look at this joke ! : $value")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun onSaveButtonClick(model:JokeView.Model){
        val sharedPreferences = getSharedPreferences("savedJokes",Context.MODE_PRIVATE)
        models[models.indexOf(model)] = model.copy(saved=!model.saved)
        sharedPreferences.edit().putString(
            "savedJokes",
            Json(JsonConfiguration.Stable).stringify(
                Joke.serializer().list,
                models.filter{ it.saved }.map{it.joke}
            )
        ).apply()
        viewAdapter.updateData(models)
    }

    private fun getJokes(reloading : Boolean = false) {
        if(reloading){
            val modelsSaved : MutableList<JokeView.Model> =
                models.filter{it.saved} as MutableList<JokeView.Model>
            models.clear()
            models.addAll(modelsSaved)
        }
        val jokeSingle : Single<Joke> = jokeService.giveMeAJoke()
        compDisp.add(jokeSingle.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeat(10)
            .doOnSubscribe {swipe.isRefreshing = true}
            .doAfterTerminate {swipe.isRefreshing = false}
            .subscribeBy(
                onError = { e -> Log.wtf("Request failed", e) },
                onNext ={joke : Joke -> models.add(
                    JokeView.Model(
                        joke,
                        {value -> onShareButtonClick(value)},
                        {model -> onSaveButtonClick(model)},
                        false
                    )
                )},
                onComplete = {
                    viewAdapter.updateData(models)
                }
            )
        )
    }

    private fun onItemMoved(from : Int,to : Int){
        if(from>to){
            (from..to).forEach{
                Collections.swap(models,it,it+1)
            }
        }else{
            (to..from).forEach{
                Collections.swap(models,it,it+1)
            }
        }
        viewAdapter.updateData(models)
        viewAdapter.notifyItemMoved(from,to)
    }

    private fun onJokeRemoved(i : Int){
        models.removeAt(i)
        viewAdapter.updateData(models)
        viewAdapter.notifyItemRemoved(i)
    }
}
