package com.example.chucknorrisjokeapp

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class JokeAdapter(private val onBottomReached : () -> Unit = {})
    :RecyclerView.Adapter<JokeAdapter.JokeViewHolder>()
{
    private val models : MutableList<JokeView.Model> = mutableListOf()

    class JokeViewHolder(val jokeView: JokeView) : RecyclerView.ViewHolder(jokeView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeViewHolder {
        val jokeView = JokeView(parent.context)
        return JokeViewHolder(jokeView)
    }

    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.jokeView.setupView(models[position])
        if(position== itemCount-1) onBottomReached()
    }

    override fun getItemCount() = models.size

    fun updateData(modelsInput : List<JokeView.Model>){
        models.clear()
        models.addAll(modelsInput)
    }
}