package com.thinkfreely.whenandwhere

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.*
import kotlinx.coroutines.*
import java.lang.IndexOutOfBoundsException
import java.util.*

private var Correct: Int = 0
private var Incorrect: Int = 0

private class GamePanelDragListener(parent: SimpleJustWhenActivity) : View.OnDragListener {

    val game : SimpleJustWhenActivity = parent

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        //println("PANEL")
        when (event?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    //println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    (v as FrameLayout).setBackgroundColor(Color.GRAY)
                    v.invalidate()
                    //println("ENTERED")
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    val currentcard = game.findViewById(R.id.currentCardLayout) as FrameLayout
                    currentcard.removeView(currentcard.getChildAt(1))
                    val card = currentcard.getTag(R.id.simpleGameCardImageView) as ImageView
                    (v as FrameLayout).addView(card)
                    v.setTag(R.id.simpleGameCard, currentcard.getTag(R.id.simpleGameCard))
                    v.setTag(R.id.simpleGameCardImageView, currentcard.getTag(R.id.simpleGameCardImageView))
                    v.invalidate()
                    currentcard.invalidate()
                    game.updateBoard(v)
                    //println("DROP in ")
                    return true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    //println("ACTION_LOCATION")
                    return true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    //println("ACTION_ENDED")
                    return true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    (v as FrameLayout).setBackgroundColor(Color.BLACK)
                    v.invalidate()
                    //println("EXITED")
                    return true
                }
                else -> {
                    println("UNKNOWN ACTION: " +event?.action.toString())
                    return true
                }
            }
    }
}

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SimpleJustWhenActivity : AppCompatActivity() {

    private var seconds : Int = 0
    lateinit var clocktimer : Timer
    lateinit var gamecards: MutableList<GameCard>
    lateinit var mediaplayer: MediaPlayer
    private var level : Int = 1
    private lateinit var mIntersitialAd: InterstitialAd

    private fun getRandomCard() : GameCard {
        val card = gamecards.get(0)
        gamecards.removeAt(0)
        return card
    }


    private fun populateGameBoard(factory: GameCardFactory) {
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)
        val listener = GamePanelDragListener(this)
        val levelcount = arrayOf<Int>(10, 10, 15, 20, 20)
        val leveleffort = arrayOf<Int>(1, 1, 2, 2, 3)
        var count: Int = 10
        var effort: Int = 3
        setContentView(R.layout.activity_simple_just_when)
        supportActionBar?.hide()
        Correct = 0
        Incorrect = 0


        mediaplayer = MediaPlayer.create(this, R.raw.epic)
        mediaplayer.setVolume(1.0f, 1.0f)
        mediaplayer.start()

        scopeIO.launch {
            try {
                count = levelcount[level]
                effort = leveleffort[level]
            } catch (e: Exception) {
                count = 20
                effort = 3
            }
            gamecards = factory.getRandomCardSet(count)
            val ccard = getRandomCard()
            val ncard = getRandomCard()
            scopeMainThread.launch {
                val currentcard = findViewById(R.id.currentCardLayout) as FrameLayout
                currentcard.setTag(R.id.simpleGameCardImageView,ccard.getCardView(applicationContext))
                currentcard.setTag(R.id.simpleGameCard,ccard)
                currentcard.addView(currentcard.getTag(R.id.simpleGameCardImageView) as ImageView)
                val nowcard = findViewById(R.id.nowLayout) as FrameLayout
                nowcard.setTag(R.id.simpleGameCardImageView,ncard.getCardView(applicationContext))
                nowcard.setTag(R.id.simpleGameCard,ncard)
                nowcard.addView(nowcard.getTag(R.id.simpleGameCardImageView) as ImageView)

                val after = findViewById(R.id.afterAreaLayout) as FrameLayout
                after.setOnDragListener(listener)
                val before = findViewById(R.id.beforeAreaLayout) as FrameLayout
                before.setOnDragListener(listener)
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_simple_just_when)
        val cardfactory = GameCardFactory(applicationContext)
        populateGameBoard(cardfactory)
        MobileAds.initialize(this)
        mIntersitialAd = InterstitialAd(this)
        mIntersitialAd.adUnitId = "ca-app-pub-1167846070848710/2102808219"
        //mIntersitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
    }


    override fun onPause() {
        super.onPause()
        mediaplayer.stop()
        clocktimer.cancel()
        clocktimer.purge()
    }

    override fun onResume() {
        super.onResume()
        mediaplayer.start()
        clocktimer = Timer()
        clocktimer.schedule(object: TimerTask() {
            override fun run() {
                val job = Job()
                val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
                scopeMainThread.launch {
                    updateTimeClock()
                }
            }
        }, 0, 1000)
    }

    fun updateTimeClock() {
        val timeView = findViewById(R.id.TimeText) as TextView
        seconds = seconds + 1
        timeView.setText("Time: " + seconds.toString())
        timeView.invalidate()
    }

    fun updateBoard(v: FrameLayout) {
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)

        val now = findViewById(R.id.nowLayout) as FrameLayout
        val ncard = now.getTag(R.id.simpleGameCard) as GameCard
        val acard = v.getTag(R.id.simpleGameCard) as GameCard
        val nyeartext = findViewById(R.id.NowYearText) as TextView
        nyeartext.setText(ncard.getYear())
        nyeartext.setTextColor(Color.WHITE)
        nyeartext.invalidate()
        if (v.id == R.id.afterAreaLayout) {
            val ayear = findViewById(R.id.AfterYearText) as TextView
            ayear.setText(acard.getYear())
            ayear.invalidate()
            //We decided that the current card came after the now card
            if (ncard.cameBefore(acard)) {
                v.setBackgroundColor(Color.GREEN)
                Correct = Correct + 1
            } else {
                v.setBackgroundColor(Color.RED)
                Incorrect = Incorrect + 1
            }
        } else {
            val byear = findViewById(R.id.BeforeYearText) as TextView
            byear.setText(acard.getYear())
            byear.invalidate()
            // We decided it came before the now card
            if (ncard.cameAfter(acard)) {
                v.setBackgroundColor(Color.GREEN)
                Correct = Correct + 1
            } else {
                v.setBackgroundColor(Color.RED)
                Incorrect = Incorrect + 1
            }
        }
        val correct = findViewById(R.id.CorrectScoreText) as TextView
        val incorrect = findViewById(R.id.IncorrectScoreText) as TextView
        correct.setText("Correct: " + Correct.toString())
        incorrect.setText("Incorrect: " + Incorrect.toString())

        if (gamecards.count() == 0) {
            endgame()
            return
        }
        val newcard = getRandomCard()
        scopeIO.launch {
            delay(1000)
            scopeMainThread.launch {
                val ayear = findViewById(R.id.AfterYearText) as TextView
                val byear = findViewById(R.id.BeforeYearText) as TextView
                nyeartext.setText("")
                ayear.setText("")
                byear.setText("")
                v.setBackgroundColor(Color.BLACK)
                v.removeView(v.getChildAt(2))
                now.removeView(now.getChildAt(2))
                now.addView(v.getTag(R.id.simpleGameCardImageView) as ImageView)
                now.setTag(R.id.simpleGameCardImageView, v.getTag(R.id.simpleGameCardImageView))
                now.setTag(R.id.simpleGameCard, v.getTag(R.id.simpleGameCard))
                val current = findViewById(R.id.currentCardLayout) as FrameLayout
                current.setTag(R.id.simpleGameCardImageView,newcard.getCardView(applicationContext))
                current.setTag(R.id.simpleGameCard,newcard)
                current.addView(current.getTag(R.id.simpleGameCardImageView) as ImageView)
            }
        }

    }

    private fun showEndResults(){
        val boardview = findViewById(R.id.HBoardLayout) as LinearLayout
        val scoreview = findViewById(R.id.ScoreBoardLayout) as LinearLayout
        scoreview.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate().alpha(0f).setDuration(2000.toLong()).setListener(null)
        }
        boardview.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate().alpha(0f).setDuration(2000.toLong()).setListener(null)
        }

        val resultview = findViewById(R.id.ResultScreenLayout) as ConstraintLayout
        resultview.animate().alpha(1f).setDuration(2000.toLong()).setListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                boardview.visibility = View.GONE
                scoreview.visibility = View.GONE
                resultview.visibility = View.VISIBLE
                val resulttext = findViewById(R.id.ResultScreenText) as TextView
                resulttext.setText("You got " + Correct.toString() + " right out of " + (Correct + Incorrect).toString() + " In " + seconds.toString() + " Seconds!")
                val nextbutton = findViewById(R.id.NextLevelButton) as ImageButton
                nextbutton.setOnClickListener {
                    level = level +1
                    resulttext.setText("Level " + level.toString())
                    scoreview.animate().alpha(0f).setDuration(2000.toLong()).setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            resultview.visibility = View.GONE
                            boardview.visibility = View.VISIBLE
                            seconds = 0
                            clocktimer = Timer()
                            clocktimer.schedule(object: TimerTask() {
                                    override fun run() {
                                        val job = Job()
                                        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
                                        scopeMainThread.launch {
                                            updateTimeClock()
                                        }
                                    }
                             }, 0, 1000)
                            populateGameBoard(GameCardFactory(applicationContext))
                        }
                    })
                }
                val endbutton = findViewById(R.id.EndGameButton) as ImageButton
                endbutton.setOnClickListener {
                    val adrequest = AdRequest.Builder().build()
                    if (adrequest.isTestDevice(applicationContext)) {
                        println("This is a test device")
                    }
                    mIntersitialAd.loadAd(adrequest)
                    if (mIntersitialAd.isLoaded) {
                        mIntersitialAd.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                println("Ad Failed to Load")
                                finish()
                            }
                            override fun onAdClosed() {
                                println("Ad Complete")
                                finish()
                            }
                        }
                        mIntersitialAd.show()
                    } else {
                        println("No Ad Available")
                        finish()
                    }
                }
            }
        })
    }

    private fun endgame() {
        mediaplayer.stop()
        clocktimer.cancel()
        clocktimer.purge()
        showEndResults()
        //finish()
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