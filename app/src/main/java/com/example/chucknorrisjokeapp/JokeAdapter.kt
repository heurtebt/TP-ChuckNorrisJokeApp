package com.example.chucknorrisjokeapp

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class JokeAdapter(
    private val onBottomReached : () -> Unit = {},
    private val onShareButtonClickListener : (value : String) -> Unit = {},
    private val onSaveButtonClickListener : (id : String) -> Unit = {}
) :RecyclerView.Adapter<JokeAdapter.JokeViewHolder>()
{
    private val jokes : MutableList<Joke> = mutableListOf<Joke>()

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
            false))
        if(position== itemCount-1) onBottomReached()
    }

    override fun getItemCount() = jokes.size

    fun addJokes(jokeInput : MutableList<Joke>){
        jokes.addAll(jokeInput)
        notifyDataSetChanged()
    }

    fun getJokes() : MutableList<Joke> = jokes

    fun onItemMoved(from : Int,to : Int){
        if(from<to){
            (from..to).forEach{
                Collections.swap(jokes,it,it+1)
            }
        }else{
            (to..from).forEach{
                Collections.swap(jokes,it,it+1)
            }
        }
        this.notifyItemMoved(from,to)
    }

    fun onJokeRemoved(i : Int){
        jokes.removeAt(i)
        this.notifyItemRemoved(i)
    }
}