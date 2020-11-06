package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class HistoricStoriesSelector : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_historic_stories_selector)
        supportActionBar?.hide()
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val storylist = findViewById(R.id.StoriesList) as LinearLayout
        val factory = GameCardFactory(this)
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)
        val mycontext = this
        scopeIO.launch {
            val stories = factory.getStoryList()
            scopeMainThread.launch {
                for (s in stories) {
                    val button = Button(mycontext)
                    button.setText(s.storyname)
                    button.setTag(R.id.storyname, s.storyname)
                    button.setPadding(3,0,0,3)
                    button.setOnClickListener {
                        val storyname = it.getTag(R.id.storyname) as String
                        val intent = Intent(mycontext, HistoricStoriesGameActivity::class.java).apply {
                            putExtra("STORYNAME", storyname)
                        }
                        startActivity(intent)
                    }
                    storylist.addView(button)
                }
            }
        }

    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}