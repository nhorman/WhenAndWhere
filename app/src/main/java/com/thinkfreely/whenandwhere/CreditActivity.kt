package com.thinkfreely.whenandwhere

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CreditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)
        supportActionBar?.hide()

        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)

        val carddb = GameCardFactory(this)
        scopeIO.launch {
            val cards = carddb.getAllCards()
            scopeMainThread.launch {
                val creditlayout = findViewById(R.id.creditsVLayout) as LinearLayout
                for (card in cards) {
                    val hlayout = LinearLayout(baseContext)
                    hlayout.orientation = LinearLayout.HORIZONTAL
                    val credittext = TextView(baseContext)
                    credittext.setTextColor(Color.WHITE)
                    credittext.gravity = Gravity.CENTER
                    credittext.setText(card.credittext)
                    hlayout.addView(credittext)
                    val crediturl = TextView(baseContext)
                    crediturl.setTextColor(Color.WHITE)
                    crediturl.gravity = Gravity.CENTER
                    crediturl.setText(card.crediturl)
                    Linkify.addLinks(crediturl, Linkify.WEB_URLS)
                    crediturl.setLinksClickable(true)
                    hlayout.addView(crediturl)
                    creditlayout.addView(hlayout)
                }
            }


        }


    }
}