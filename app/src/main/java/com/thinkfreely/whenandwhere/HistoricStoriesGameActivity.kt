package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch



private class AnswerFrameDragListener(parent: HistoricStoriesGameActivity) : View.OnDragListener {

    val game : HistoricStoriesGameActivity = parent

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        //println("PANEL")
        when (event?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    //println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    println("ENTERED")
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    val currentFrame = (v as FrameLayout)
                    val card = event?.localState as GameCard
                    val currentposition = card.cardview.parent as ViewGroup
                    currentposition.removeView(card.cardview)
                    currentFrame.addView(card.cardview)
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
                    println("EXITED")
                }
                else -> {
                    println("UNKNOWN ACTION: " +event?.action.toString())
                    return true
                }
            }
        return false
    }
}

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class HistoricStoriesGameActivity : AppCompatActivity() {

    lateinit var story: GameStory
    var pageno = 0

    private fun populateNextPage() {
        val storyv = findViewById(R.id.StoryText) as WebView
        val storytext = story.getStoryText(pageno)
        storyv.loadData(storytext, "text/html", "utf-8")
    }

    private fun populateCardSet() {
        val gamecardlayout = findViewById(R.id.GameCardLayout) as LinearLayout
        val cards = story.getGameCards()
        for (c in cards) {
            val cardimage = c.getCardView(this)
            cardimage.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300 )
            cardimage.scaleType = ImageView.ScaleType.FIT_XY
            gamecardlayout.addView(cardimage)
        }
    }

    private fun populateAnswerArea() {
        val listener = AnswerFrameDragListener(this)
        val answers = story.getAnswers(pageno)
        val answerarea = findViewById(R.id.AnswerCardLayout) as LinearLayout
        answerarea.removeAllViews()
        if (answers.count() == 0) {
            val nextbutton = Button(this)
            nextbutton.setText("Next Page")
            nextbutton.setOnClickListener {
                pageno++
                populateNextPage()
                populateAnswerArea()
            }
            answerarea.addView(nextbutton)
        } else {
            for (a in answers) {
                val answerframe = FrameLayout(this)
                answerframe.background = applicationContext.getDrawable(R.drawable.frame)
                answerframe.layoutParams =
                    FrameLayout.LayoutParams(300, FrameLayout.LayoutParams.MATCH_PARENT)
                if (story.answerOrderMatters(pageno) == true)
                    answerframe.setTag(R.id.expectedAnswer, a)
                else
                    answerframe.setTag(R.id.expectedAnswer, "None")
                answerarea.addView(answerframe)
                answerframe.setOnDragListener(listener)
            }
        }
    }

    private fun populateGameBoard() {
        populateNextPage()
        populateCardSet()
        populateAnswerArea()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_historic_stories_game)
        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val storyname = intent.getStringExtra("STORYNAME") as String

        val factory = GameCardFactory(this)

        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)

        scopeIO.launch {
            story = GameStory(storyname, factory)
            scopeMainThread.launch {
                populateGameBoard()
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