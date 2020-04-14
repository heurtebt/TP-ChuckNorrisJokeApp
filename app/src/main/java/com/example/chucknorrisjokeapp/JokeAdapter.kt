package com.example.chucknorrisjokeapp

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class JokeAdapter(
    private val onBottomReached : () -> Unit = {},
    private val onShareButtonClickListener : (id : String) -> Unit = {},
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
        jokes.clear()
        jokes.addAll(jokeInput)
        notifyDataSetChanged()
    }
}