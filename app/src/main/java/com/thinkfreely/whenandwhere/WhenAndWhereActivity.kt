package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


private class WorldMapDragListener(parent: WhenAndWhereActivity, mycards: MutableList<GameCard>) : View.OnDragListener {

    val game : WhenAndWhereActivity = parent
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
                    val card = v?.getTag(R.id.simpleGameCard) as GameCard
                    val loctext = game.findViewById(R.id.InfoText) as TextView
                    loctext.setText(card.locationData.location)
                    loctext.invalidate()
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    val cardlistview = game.findViewById(R.id.CardAreaLinearLayout) as LinearLayout
                    val hotspotcard = v?.getTag(R.id.simpleGameCard) as GameCard
                    val dragcard = hotspotcard.getDragCard()
                    val correcttext = game.findViewById(R.id.CorrectTextView) as TextView
                    val incorrecttext = game.findViewById(R.id.IncorrectTextView) as TextView

                    if (dragcard.isMyHotspot(v)) {
                        game.Correct++
                        correcttext.setText("CORRECT: " + game.Correct.toString())
                        correcttext.invalidate()
                    } else {
                        game.Incorrect++
                        incorrecttext.setText("INCORRECT: " + game.Incorrect.toString())
                        incorrecttext.invalidate()
                    }
                    cardlistview.removeView(dragcard.cardview)
                    cardlistview.invalidate()
                    if ((game.Correct + game.Incorrect) == game.gamecards.count()) {
                        val intent = Intent(game, WhenAfterWhereActivity::class.java).apply {
                            val tcards = arrayListOf<GameCard>()
                            tcards.addAll(game.gamecards)
                            putParcelableArrayListExtra("GAMECARDS", tcards)
                            putExtra("CORRECT_COUNT", game.Correct)
                            putExtra("INCORRECT_COUNT", game.Incorrect)
                            putExtra("GAME_LEVEL", game.level)
                        }
                        game.startActivity(intent)
                        game.finish()
                        // If we dropped all the cards, its time to move on to the when portion of our game

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
                    val loctext = game.findViewById(R.id.InfoText) as TextView
                    loctext.setText("")
                    loctext.invalidate()
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
class WhenAndWhereActivity : AppCompatActivity() {

    lateinit var gamecards: MutableList<GameCard>
    lateinit var gamecarddb : GameCardFactory
    var level = 1
    var Correct: Int = 0
    var Incorrect: Int = 0

    var levelmap = arrayOf<Int>(5, 10, 10, 20, 20)

    private fun getGameCards() {
        var cardnum = 20
        try {
            cardnum = levelmap[level]
        } catch (e: Exception) {
            cardnum = 20
        }
        gamecards = gamecarddb.getRandomCardSet(cardnum)
    }

    private fun populateGameBoard() {
        val pdensity = applicationContext.resources.displayMetrics.density
        val cardsview = findViewById(R.id.CardAreaLinearLayout) as LinearLayout
        val overlay = findViewById(R.id.WorldMapOverlay) as ConstraintLayout
        val draglistener = WorldMapDragListener(this, gamecards)
        for (c in gamecards) {
            val cview = c.getCardView(this)
            cview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (250 * pdensity).toInt())
            cardsview.addView(cview)
            val lhotspot = c.setLocationMarkerView(overlay, pdensity)
            lhotspot.setOnDragListener(draglistener)
        }
        //worldview.setOnDragListener(draglistener)
        //overlay.setOnDragListener(draglistener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_when_and_where)
        supportActionBar?.hide()
        level = intent.getIntExtra("GAME_LEVEL", 1)
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)
        gamecarddb = GameCardFactory(this)

        scopeIO.launch {
            getGameCards()
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