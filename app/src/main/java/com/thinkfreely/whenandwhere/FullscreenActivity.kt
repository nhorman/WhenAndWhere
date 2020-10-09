package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }


    private fun showMainGameScreen() {
        val title = findViewById(R.id.titleView) as ImageView
        val start = findViewById(R.id.startButton) as ImageButton
        title.setVisibility(View.VISIBLE)
        start.setVisibility(View.VISIBLE)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)

        supportActionBar?.hide()

        val mediaplayer = MediaPlayer.create(this, R.raw.epic)
        mediaplayer.setVolume(1.0f, 1.0f)
        mediaplayer.start()

        val start = findViewById(R.id.startButton) as ImageButton
        start.setOnClickListener {
            mediaplayer.stop()
            val intent = Intent(this, GameSetupActivity::class.java)
            startActivity(intent)
            finish()
        }
        val credit = findViewById<ImageButton>(R.id.creditButton)
        credit.setOnClickListener {
            mediaplayer.stop()
            val intent = Intent(this, CreditActivity::class.java)
            startActivity(intent)
        }



        showMainGameScreen()
        //val vv = findViewById(R.id.videoView) as VideoView
        //vv.setOnCompletionListener {
        //    showMainGameScreen()
        //}
        //vv.setOnTouchListener(object: View.OnTouchListener{

          //  override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            //    vv.stopPlayback()
              //  showMainGameScreen()
                //return v?.onTouchEvent(event) ?: true
            //}

        //})
        //var uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.splash)
        //vv.setVideoURI(uri)
        //vv.start()
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