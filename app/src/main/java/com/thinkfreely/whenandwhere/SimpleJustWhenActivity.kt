package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.*
import kotlinx.coroutines.*

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

    private fun getRandomCard(cardfactory: GameCardFactory) : GameCard {
        val card = cardfactory.getRandomCard()
        return card
    }

    private fun populateGameBoard(factory: GameCardFactory) {
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)
        val listener = GamePanelDragListener(this)
        setContentView(R.layout.activity_simple_just_when)
        supportActionBar?.hide()


        scopeIO.launch {
            val ccard = getRandomCard(factory)
            val ncard = getRandomCard(factory)
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

        scopeIO.launch {
            val cardfactory = GameCardFactory(applicationContext)
            val newcard = getRandomCard(cardfactory)
            delay(1000)
            scopeMainThread.launch {
                val nyeartext = findViewById(R.id.NowYearText) as TextView
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