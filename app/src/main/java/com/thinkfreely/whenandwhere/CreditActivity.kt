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
                    val hlayout = LinearLayout(creditlayout.context)
                    hlayout.orientation = LinearLayout.HORIZONTAL
                    hlayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
                    val credittext = TextView(baseContext)
                    credittext.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    credittext.setTextColor(Color.WHITE)
                    credittext.gravity = Gravity.CENTER_HORIZONTAL
                    credittext.setText(card.credittext)
                    hlayout.addView(credittext)
                    val crediturl = TextView(baseContext)
                    crediturl.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    crediturl.setTextColor(Color.WHITE)
                    crediturl.gravity = Gravity.CENTER_HORIZONTAL
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