package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.*
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
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
                    val cardview = game.findViewById(R.id.GameCardLayout) as ViewGroup
                    val card = event?.localState as GameCard
                    val currentposition = card.cardview.parent as ViewGroup
                    if (currentposition === cardview) {
                        game.answercount++
                    }
                    currentposition.removeView(card.cardview)
                    currentFrame.addView(card.cardview)
                    currentFrame.setTag(R.id.answer, card)
                    if (game.answercount == game.pageanswercount) {
                        //show the answertext, wait a bit, then prompt for the next page
                        game.showPageAnswers()
                    }

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
    var answercount = 0
    var pageanswercount = 0

    private fun populateNextPage() {
        val storyv = findViewById(R.id.StoryText) as WebView
        try {
            val storytext = story.getStoryText(pageno)
            storyv.loadData(storytext, "text/html", "utf-8")
        } catch (e: Exception) {
            //we hit the end of the story, display a finish page and leave
            //for now just end the activity
            finish()
        }
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

    private fun goToNextPage() {
        pageno++
        populateNextPage()
        populateAnswerArea()
    }

    private fun populateAnswerArea()  {
        val listener = AnswerFrameDragListener(this)
        val answerarea = findViewById(R.id.AnswerCardLayout) as LinearLayout
        answerarea.removeAllViews()
        val answers = story.getAnswers(pageno)
        if (answers.count() == 0) {
            val nextbutton = Button(this)
            nextbutton.setText("Next Page")
            nextbutton.setOnClickListener {
                goToNextPage()
            }
            answerarea.addView(nextbutton)
        } else {
            pageanswercount = answers.count()
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

    fun showPageAnswers() {
        var idx = 0
        var gotitright = true
        val answers = story.getAnswers(pageno)
        val anstext = story.getStoryAnswer(pageno)
        val webview = findViewById(R.id.StoryText) as WebView
        val answerview = findViewById(R.id.AnswerCardLayout) as LinearLayout
        for (f in answerview.children) {
            val card = f.getTag(R.id.answer) as GameCard
            if (story.answerOrderMatters(pageno) == true) {
                if (card.cardData.cardname.equals(answers[idx])) {
                    continue
                } else {
                    gotitright = false
                    break
                }
            }
            else {
                if (answers.contains(card.cardData.cardname)) {
                    continue
                }
                else {
                    gotitright = false
                    break
                }
            }
        }

        answerview.removeAllViews()
        val correctframe = FrameLayout(this)
        correctframe.layoutParams =
                    FrameLayout.LayoutParams(400, FrameLayout.LayoutParams.MATCH_PARENT)
        val result = TextView(this)
        result.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        if (gotitright == true) {
            result.setText("CORRECT")
            result.setTextColor(Color.GREEN)
        } else {
            result.setText("INCORRECT")
            result.setTextColor(Color.RED)
        }
        correctframe.addView(result)
        answerview.addView(correctframe)

        val nextframe = FrameLayout(this)
        nextframe.layoutParams =
                    FrameLayout.LayoutParams(400, FrameLayout.LayoutParams.MATCH_PARENT)

        val nextbutton = Button(this)
        nextbutton.setText("Next Page")
        nextbutton.setOnClickListener {
            goToNextPage()
        }
        nextframe.addView(nextbutton)
        answerview.addView(nextframe)
        webview.loadData(anstext, "text/html", "utf-8")


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