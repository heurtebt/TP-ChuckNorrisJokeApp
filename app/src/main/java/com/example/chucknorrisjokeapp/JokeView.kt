package com.example.chucknorrisjokeapp

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.joke_layout.view.save
import kotlinx.android.synthetic.main.joke_layout.view.share
import kotlinx.android.synthetic.main.joke_layout.view.jokeValue

class JokeView @JvmOverloads constructor (context : Context,
                                          attrs : AttributeSet? = null)
    : ConstraintLayout(context, attrs, 0){
    init {
        LayoutInflater.from(context)
            .inflate(R.layout.joke_layout, this, true)
        this.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun setupView(model: Model){
        jokeValue.text = model.joke.value
        save.setImageResource(if(model.saved) R.drawable.ic_star_primary_dark_24dp else R.drawable.ic_star_border_primary_24dp)
        share.setOnClickListener { model.onShareButtonClickListener(model.joke.value) }
        save.setOnClickListener { model.onSaveButtonClickListener(model) }
    }

    data class Model(val joke : Joke,
                     val onShareButtonClickListener: (value:String)->Unit={},
                     val onSaveButtonClickListener: (model:Model)->Unit={},
                     val saved : Boolean)
}