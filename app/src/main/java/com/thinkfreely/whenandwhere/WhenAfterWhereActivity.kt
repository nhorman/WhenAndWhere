package com.thinkfreely.whenandwhere

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children

private class WhenDragListener(parent: WhenAfterWhereActivity, mycards: ArrayList<GameCard>) : View.OnDragListener {

    val game : WhenAfterWhereActivity = parent
    val cards = mycards

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        //println("PANEL")
        when (event?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    //println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    //println("ENTERED")

                    return true
                }
                DragEvent.ACTION_DROP -> {
                    val cardlist = game.findViewById(R.id.currentCardLinearLayout) as LinearLayout
                    val currentFrame = (v as FrameLayout)
                    val card = event?.localState as GameCard
                    val currentposition = card.cardview.parent as ViewGroup
                    currentposition.removeView(card.cardview)
                    currentFrame.addView(card.cardview)
                    if (cardlist.childCount == 0) {
                        game.mediaplayer.stop()
                        // Give us the score
                        game.showResults()
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
                    //println("EXITED")
                }
                else -> {
                    println("UNKNOWN ACTION: " +event?.action.toString())
                    return true
                }
            }
        return false
    }
}

class WhenAfterWhereActivity : AppCompatActivity() {
    private lateinit var gamecards : ArrayList<GameCard>
    var WhereCorrect : Int = 0
    var WhereIncorrect: Int = 0
    private var level = 1
    lateinit var mediaplayer: MediaPlayer

    private fun setupPlacementArea() {
        val playout = findViewById(R.id.placementCardLinearLayout)  as LinearLayout
        val clayout = findViewById(R.id.currentCardLinearLayout) as LinearLayout
        val earliestFrame = FrameLayout(this)
        earliestFrame.layoutParams = FrameLayout.LayoutParams(300, FrameLayout.LayoutParams.MATCH_PARENT)
        val earlyText = TextView(this)
        earlyText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        earlyText.setTextColor(Color.GREEN)
        earlyText.textSize = 24f
        earlyText.setText("Earliest")
        earlyText.gravity = Gravity.CENTER
        playout.addView(earlyText)

        val dragListener = WhenDragListener(this, gamecards)

        for (i in gamecards) {
            val cardframe = FrameLayout(this)
            cardframe.background = applicationContext.getDrawable(R.drawable.frame)
            cardframe.layoutParams = FrameLayout.LayoutParams(300, FrameLayout.LayoutParams.MATCH_PARENT)
            playout.addView(cardframe)
            cardframe.setOnDragListener(dragListener)

            val gamecardImage = i.getCardView(this)
            gamecardImage.layoutParams = ViewGroup.LayoutParams(300, ViewGroup.LayoutParams.MATCH_PARENT)
            gamecardImage.scaleType = ImageView.ScaleType.FIT_XY
            clayout.addView(gamecardImage)
        }

        val lateText = TextView(this)
        lateText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        lateText.setTextColor(Color.GREEN)
        lateText.textSize = 24f
        lateText.setText("Latest")
        lateText.gravity = Gravity.CENTER
        playout.addView(lateText)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_when_after_where)
        supportActionBar?.hide()

        gamecards = intent.getParcelableArrayListExtra<GameCard>("GAMECARDS") as ArrayList<GameCard>
        WhereCorrect = intent.getIntExtra("CORRECT_COUNT", 0)
        WhereIncorrect = intent.getIntExtra("INCORRECT_COUNT", 0)
        level = intent.getIntExtra("GAME_LEVEL", 1)
        mediaplayer = MediaPlayer.create(this, R.raw.epic)
        mediaplayer.setVolume(1.0f, 1.0f)
        mediaplayer.start()
        setupPlacementArea()

    }

    override fun onPause() {
        super.onPause()
        mediaplayer.stop()
    }

    override fun onResume() {
        super.onResume()
        mediaplayer.start()
    }


    fun showResults() {
        val boardview = findViewById(R.id.WhenBoardLayout) as LinearLayout
        boardview.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate().alpha(0f).setDuration(2000.toLong()).setListener(null)
        }

        val resultview = findViewById(R.id.WhenAfterWhereScoreLayout) as ConstraintLayout
        resultview.animate().alpha(1f).setDuration(2000.toLong()).setListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                //compute the number of correct cards in the positional view
                val placementview = findViewById(R.id.placementCardLinearLayout) as LinearLayout
                var Correct = 0
                var Incorrect = 0
                gamecards.sort()
                val answercards = mutableListOf<GameCard>()
                for (v in placementview.children) {
                    try {
                        val fv = v as FrameLayout
                        for (vv in fv.children) {
                            if (vv is ImageView) {
                                val card = vv.getTag(R.id.simpleGameCard) as GameCard
                                answercards.add(card)
                            }
                        }
                    }
                    catch (e: Exception) {
                        continue
                    }
                }
                for (i in 0..(gamecards.count()-1)) {
                    val ac = answercards[i]
                    val gc = gamecards[i]
                    if (ac === gc)
                        Correct++
                    else
                        Incorrect++
                }
                boardview.visibility = View.GONE
                resultview.visibility = View.VISIBLE
                val wherecorrect = findViewById(R.id.WhereCorrectText) as TextView
                val whereincorrect = findViewById(R.id.WhereIncorrectText) as TextView
                val whencorrect = findViewById(R.id.WhenCorrectText) as TextView
                val whenincorrect = findViewById(R.id.WhenIncorrectText) as TextView
                wherecorrect.setText("Where Correct: " + WhereCorrect.toString())
                whereincorrect.setText("Where Incorrect: "  + WhereIncorrect.toString())
                whencorrect.setText("When Correct: " + Correct.toString())
                whenincorrect.setText("When Incorrect: " + Incorrect.toString())
                val endbutton = findViewById(R.id.EndWhenWhereGame) as ImageButton
                endbutton.setOnClickListener {
                    finish()
                }
                val nextbutton = findViewById(R.id.NextWhenWhereLevel) as ImageButton
                nextbutton.setOnClickListener {
                    val intent = Intent(applicationContext, WhenAndWhereActivity::class.java).apply {
                        putExtra("GAME_LEVEL", level+1)
                    }
                    startActivity(intent)
                    finish()
                }
                val answers = findViewById(R.id.AnswerButton) as ImageButton
                val tcards = arrayListOf<GameCard>()
                tcards.addAll(gamecards)
                answers.setOnClickListener {
                    val aintent = Intent(applicationContext, WhenAfterWhereAnswers::class.java).apply {
                        putParcelableArrayListExtra("GAMECARDS", tcards)
                    }
                    startActivity(aintent)
                }
            }
        })
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