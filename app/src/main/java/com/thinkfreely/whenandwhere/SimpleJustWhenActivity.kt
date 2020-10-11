package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_simple_just_when.*
import kotlinx.coroutines.*
import android.view.View.OnLongClickListener;
import android.view.ViewGroup
import android.widget.ImageButton;
import androidx.core.view.children

private class GamePanelDragListener() : View.OnDragListener {

    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        //println("PANEL")
        when (event?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    //println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    (v as FrameLayout).setBackgroundColor(Color.WHITE)
                    v.invalidate()
                    //println("ENTERED")
                    return true
                }
                DragEvent.ACTION_DROP -> {
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
        println("NO EVENT")
        return false
    }
}

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SimpleJustWhenActivity : AppCompatActivity() {


    private fun getCurrentCard() : ImageView {
        val cardfactory = GameCardFactory(applicationContext)
        val card = cardfactory.getRandomCard()
        val cardview = card.getCardView(applicationContext)
        return cardview
    }

    private fun populateGameBoard() {
        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)

        setContentView(R.layout.activity_simple_just_when)
        supportActionBar?.hide()


        scopeIO.launch {
            val card = getCurrentCard()
            scopeMainThread.launch {
                val currentcard = findViewById(R.id.currentCardLayout) as FrameLayout
                currentcard.addView(card)
                val listener = GamePanelDragListener()
                val simple = findViewById(R.id.simpleBoard) as FrameLayout
                val root = simple.getRootView()
                //simple.setOnDragListener(listener)
                //root.setOnDragListener(listener)
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
        populateGameBoard()
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