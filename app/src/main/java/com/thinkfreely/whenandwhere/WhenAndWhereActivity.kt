package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
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
                    println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    println("ENTERED")
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    println("DROP at " + event.x.toString() + " " + event.y.toString())
                    return true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    println("ACTION_LOCATION")
                    val loctext = game.findViewById(R.id.InfoText) as TextView

                    for (c in cards) {
                        if (c.inHotSpot(event.x, event.y)) {
                            println("Entered " + c.locationData.location + " at " +event.x.toString() + " " + event.y.toString())
                            loctext.setText(c.locationData.location)
                            loctext.invalidate()
                            return true
                        }
                    }
                    loctext.setText("")
                    loctext.invalidate()
                    return true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    println("ACTION_ENDED")
                    return true
                }
                DragEvent.ACTION_DRAG_EXITED -> {

                    println("EXITED")
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

    private fun getGameCards() {
        gamecards = gamecarddb.getRandomCardSet(5)
    }

    private fun populateGameBoard() {
        val pdensity = applicationContext.resources.displayMetrics.density
        val cardsview = findViewById(R.id.CardAreaLinearLayout) as LinearLayout
        val worldview = findViewById(R.id.WorldMapConstraintLayout) as ConstraintLayout
        val overlay = findViewById(R.id.mapOverlayLayout) as RelativeLayout
        val draglistener = WorldMapDragListener(this, gamecards)
        for (c in gamecards) {
            val cview = c.getCardView(this)
            cview.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (250 * pdensity).toInt())
            cardsview.addView(cview)
            val lhotspot = c.setLocationMarkerView(overlay, pdensity)
            lhotspot.setOnDragListener(draglistener)
        }
        worldview.setOnDragListener(draglistener)
        overlay.setOnDragListener(draglistener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_when_and_where)
        supportActionBar?.hide()
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