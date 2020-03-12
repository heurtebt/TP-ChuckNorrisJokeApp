package com.example.chucknorrisjokeapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.joke_layout.view.*

class JokeAdapter(private val jokes: List<String>) :
RecyclerView.Adapter<JokeAdapter.JokeViewHolder>() {

    class JokeViewHolder(val jokeView: LinearLayout) : RecyclerView.ViewHolder(jokeView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeAdapter.JokeViewHolder {
        val jokeView =   LayoutInflater.from(parent.context)
            .inflate(R.layout.joke_layout, parent, false) as LinearLayout
        return JokeViewHolder(jokeView)
    }
    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.jokeView.textView.text = jokes[position]
    }
    override fun getItemCount() = jokes.size
}