package com.example.chucknorrisjokeapp

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class JokeAdapter(
    private val onBottomReached : () -> Unit = {},
    private val onShareButtonClickListener : (value : String) -> Unit = {},
    private val onSaveButtonClickListener : (joke : Joke,saved : Boolean) -> Unit = {_,_->}
) :RecyclerView.Adapter<JokeAdapter.JokeViewHolder>()
{
    private val jokes : MutableList<Joke> = mutableListOf()
    private val savedJokes : MutableList<Boolean> = mutableListOf()

    class JokeViewHolder(val jokeView: JokeView) : RecyclerView.ViewHolder(jokeView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeAdapter.JokeViewHolder {
        val jokeView = JokeView(parent.context)
        return JokeViewHolder(jokeView)
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.jokeView.setupView(
            JokeView.Model(jokes[position],
            onShareButtonClickListener,
            onSaveButtonClickListener,
            savedJokes[position]))
        if(position== itemCount-1) onBottomReached()
    }

    override fun getItemCount() = jokes.size

    fun addJokes(jokeInput : MutableList<Joke>,savedJokesInput : MutableList<Boolean>){
        jokes.clear()
        savedJokes.clear()
        jokes.addAll(jokeInput)
        savedJokes.addAll(savedJokesInput)
        notifyDataSetChanged()
    }

    fun getJokes() : MutableList<Joke> = jokes
    fun getSaved() : MutableList<Boolean> = savedJokes

    fun onItemMoved(from : Int,to : Int){
        if(from>to){
            (from..to).forEach{
                Collections.swap(jokes,it,it+1)
                Collections.swap(savedJokes,it,it+1)
            }
        }else{
            (to..from).forEach{
                Collections.swap(jokes,it,it+1)
                Collections.swap(savedJokes,it,it+1)
            }
        }
        this.notifyItemMoved(from,to)
    }

    fun onJokeRemoved(i : Int){
        jokes.removeAt(i)
        savedJokes.removeAt(i)
        this.notifyItemRemoved(i)
    }
}