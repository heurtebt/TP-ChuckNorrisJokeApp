package com.example.chucknorrisjokeapp
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chucknorrisjokeapp.JokesViewModel.LoadingStatus
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    /**
     * Our ViewModel instance, built with our Factory
     *
     * @see androidx.activity.viewModels
     */

    private val viewModel: JokesViewModel by viewModels {
        JokesViewModelFactory(
            this,
            getSharedPreferences("savedJokes",Context.MODE_PRIVATE))
    }

    private val jokeAdapter: JokeAdapter = JokeAdapter{viewModel.onNewJokesRequest()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipe.setColorSchemeColors(getColor(R.color.colorAccent))
        swipe.setSize(0)

        val viewManager : RecyclerView.LayoutManager = LinearLayoutManager(this)

        val recyclerView : RecyclerView= findViewById<RecyclerView>(R.id.myRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = jokeAdapter
        }

        val touchHelper = JokeTouchHelper(
            {i -> viewModel.onJokeRemovedAt(i)},
            {from,to-> viewModel.onJokePositionChanged(from,to)}
        )
        touchHelper.attachToRecyclerView(recyclerView)
        swipe.setOnRefreshListener { viewModel.onNewJokesRequest(reload = true) }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.jokeModels.observe(
            this,
            Observer { jokes: List<JokeView.Model> ->
                jokeAdapter.updateData(jokes)
            })

        viewModel.jokesSetChangedAction.observe(
            this,
            Observer { listAction: JokesViewModel.ListAction ->
                when(listAction){
                    is JokesViewModel.ListAction.ItemUpdatedAction ->
                        jokeAdapter.notifyItemChanged(listAction.position)
                    is JokesViewModel.ListAction.ItemInsertedAction ->
                        jokeAdapter.notifyItemInserted(listAction.position)
                    is JokesViewModel.ListAction.ItemRemovedAction ->
                        jokeAdapter.notifyItemRemoved(listAction.position)
                    is JokesViewModel.ListAction.ItemMovedAction ->
                        jokeAdapter.notifyItemMoved(listAction.fromPosition,listAction.toPosition)
                    is JokesViewModel.ListAction.DataSetChangedAction ->
                        jokeAdapter.notifyDataSetChanged()
                }
            })

        viewModel.jokesLoadingStatus.observe(
            this,
            Observer { loadingStatus: LoadingStatus ->
                swipe.isRefreshing = loadingStatus==LoadingStatus.LOADING
            })
    }


    /**
     * Convenient class used to build the instance of our JokeViewModel,
     * passing some params to its constructor.
     *
     * @see androidx.lifecycle.ViewModelProvider
     */
    private class JokesViewModelFactory(
        private val context: Context,
        private val sharedPrefs: SharedPreferences
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            JokesViewModel(context, sharedPrefs) as T
    }

}

