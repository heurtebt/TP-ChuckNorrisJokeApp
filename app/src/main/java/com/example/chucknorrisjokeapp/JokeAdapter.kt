package com.example.chucknorrisjokeapp

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JokeAdapter(private val jokes: List<String>) :
RecyclerView.Adapter<JokeAdapter.JokeViewHolder>() {

    class JokeViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JokeAdapter.JokeViewHolder {
        val textView =  TextView(parent.context)
        return JokeViewHolder(textView)
    }
    override fun onBindViewHolder(holder: JokeViewHolder, position: Int) {
        holder.textView.text = jokes[position]
    }
    override fun getItemCount() = jokes.size
}