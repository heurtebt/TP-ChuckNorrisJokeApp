package com.example.chucknorrisjokeapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.util.Collections


/**
 * @param context, helpful for sharing joke
 * @param sharedPreferences, helpful for saving jokes
 *
 * @see androidx.lifecycle.ViewModel
 */
class JokesViewModel(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val service: JokeApiService = JokeApiServiceFactory().createService()
    private val composite: CompositeDisposable = CompositeDisposable()

    enum class LoadingStatus { LOADING, NOT_LOADING }

    /** Used as a "dynamic enum" to notify Adapter with correct action. */
    sealed class ListAction {
        data class ItemUpdatedAction(val position: Int) : ListAction()
        data class ItemInsertedAction(val position: Int) : ListAction()
        data class ItemRemovedAction(val position: Int) : ListAction()
        data class ItemMovedAction(val fromPosition: Int, val toPosition: Int) : ListAction()
        object DataSetChangedAction : ListAction()
    }

    /**
     * Private members of type MutableLiveData.
     * You can update a MutableLiveData value using setValue() (or postValue() if not main Thread).
     * Belong private because only the ViewModel should be able to update its liveData.
     *
     * @see androidx.lifecycle.MutableLiveData
     * @see androidx.lifecycle.LiveData#setValue()
     * @see androidx.lifecycle.LiveData#postValue()
     */
    private val _jokesLoadingStatus = MutableLiveData<LoadingStatus>()
    private val _jokesSetChangedAction = MutableLiveData<ListAction>()
    private val _jokes = MutableLiveData<List<Joke>>()


    /**
     * Public members of type LiveData.
     * This is what UI will observe and use to update views.
     * They are built with private MutableLiveData above.
     *
     * @see androidx.lifecycle.LiveData
     * @see androidx.lifecycle.Transformations
     */
    val jokesLoadingStatus: LiveData<LoadingStatus> = _jokesLoadingStatus
    val jokesSetChangedAction: LiveData<ListAction> = _jokesSetChangedAction
    val jokeModels: LiveData<List<JokeView.Model>> = Transformations.map(_jokes) {
        it.toJokesViewModel()
    }

    init {
        onSavedJokesRestored()
        onNewJokesRequest()
    }

    fun onNewJokesRequest(jokeCount: Int = 10,reload : Boolean = false) {
        if(reload){
            _jokes.value=listOf()
            onSavedJokesRestored()
        }

        val jokeSingle : Single<Joke> = service.giveMeAJoke()
        composite.add(jokeSingle.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .repeat(jokeCount.toLong())
            .doOnSubscribe {_jokesLoadingStatus.value = LoadingStatus.LOADING}
            .doFinally {_jokesLoadingStatus.value = LoadingStatus.NOT_LOADING}
            .subscribeBy(
                onError = { e -> Log.wtf("Request failed", e) },
                onNext ={joke : Joke ->
                    _jokes.value=_jokes.value?.plus(joke)
                    _jokesSetChangedAction.value = ListAction.ItemInsertedAction(_jokes.value?.lastIndex!!)
                }
            )
        )
    }


    fun onJokeRemovedAt(position: Int) {
        _jokes.value = _jokes.value?.filterIndexed{it,_->it!=position}
        _jokesSetChangedAction.value = ListAction.ItemRemovedAction(position)
    }

    fun onJokePositionChanged(previous: Int, target: Int) {
        _jokes.value = _jokes.value?.moveItem(previous,target)
        _jokesSetChangedAction.value = ListAction.ItemMovedAction(previous,target)
    }

    private fun onJokeStared(id: String) {
        val savedJokes : MutableList<Joke> = mutableListOf()

        if(sharedPreferences.contains("savedJokes")) {
            sharedPreferences.getString("savedJokes","")
                ?.let{ Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {it->
                    savedJokes.addAll(it)
                }
        }

        jokeModels.value?.forEachIndexed{index,model ->
            if(model.joke.id==id){
                savedJokes.add(model.joke)
                sharedPreferences.edit().putString(
                    "savedJokes",
                    Json(JsonConfiguration.Stable).stringify(
                        Joke.serializer().list,
                        savedJokes

                    )
                ).apply()
                _jokes.value=_jokes.value
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(index)
            }
        }
    }

    private fun onJokeUnStared(id: String) {
        val savedJokes : MutableList<Joke> = mutableListOf()

        if(sharedPreferences.contains("savedJokes")) {
            sharedPreferences.getString("savedJokes","")
                ?.let{ Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {it->
                    savedJokes.addAll(it)
                }
        }

        jokeModels.value?.forEachIndexed{index,model ->
            if(model.joke.id==id){
                savedJokes.remove(model.joke)
                sharedPreferences.edit().putString(
                    "savedJokes",
                    Json(JsonConfiguration.Stable).stringify(
                        Joke.serializer().list,
                        savedJokes

                    )
                ).apply()
                _jokes.value=_jokes.value
                _jokesSetChangedAction.value = ListAction.ItemUpdatedAction(index)
            }
        }
    }

    private fun onJokeShared(id: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,
                "Look at this joke ! : ${_jokes.value?.find{it.id==id}?.value}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(context,shareIntent,null)
    }

    private fun onSavedJokesRestored() {
        if(sharedPreferences.contains("savedJokes")) {
            sharedPreferences.getString("savedJokes","")
                ?.let{ Json(JsonConfiguration.Stable).parse(Joke.serializer().list,it)}
                ?.let {it->
                    _jokes.value=it
                    _jokesSetChangedAction.value = ListAction.DataSetChangedAction
                }
        }

    }

    override fun onCleared() {
        composite.clear()
    }

    private fun List<Joke>.toJokesViewModel(): List<JokeView.Model> = map { joke ->
        val saved : Boolean
        if (sharedPreferences.contains("savedJokes")) {
            if(sharedPreferences.getString("savedJokes", "")!=null){
                Json(JsonConfiguration.Stable).parse(Joke.serializer().list,
                    sharedPreferences.getString("savedJokes", "") as String).let{
                        saved=it.contains(joke)
                    }
            }else{saved=false}
        }else{saved=false}

        JokeView.Model(
            joke,
            {id -> onJokeShared(id)},
            onSaveButtonClickListener={if(!saved) onJokeStared(joke.id) else onJokeUnStared(joke.id)},
            saved=saved
        )
    }


    /** Convenient method to change an item position in a List */
    private inline fun <reified T> List<T>.moveItem(sourceIndex: Int, targetIndex: Int): List<T> =
        apply {
            if (sourceIndex <= targetIndex)
                Collections.rotate(subList(sourceIndex, targetIndex + 1), -1)
            else Collections.rotate(subList(targetIndex, sourceIndex + 1), 1)
        }

}