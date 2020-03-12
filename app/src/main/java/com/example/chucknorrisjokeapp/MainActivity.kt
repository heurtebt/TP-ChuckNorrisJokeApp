package com.example.chucknorrisjokeapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.wtf("Jokes",ChuckNorrisJokes.jokes.toString())
        RecylclerView.layoutManager=LinearLayoutManager(this)
    }
}
