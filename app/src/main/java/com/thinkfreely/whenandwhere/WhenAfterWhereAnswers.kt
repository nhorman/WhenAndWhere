package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.children

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class WhenAfterWhereAnswers : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_when_after_where_answers)
        supportActionBar?.hide()
        val table = findViewById(R.id.AnswersView) as LinearLayout
        for (i in 2..(table.children.count()-1)) {
            table.removeViewAt(i)
        }
        val gamecards = intent.getParcelableArrayListExtra<GameCard>("GAMECARDS") as ArrayList<GameCard>
        for (c in gamecards) {
            val row = LinearLayout(this)
            row.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            val event = TextView(this)
            event.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val location = TextView(this)
            location.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val year = TextView(this)
            year.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            event.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            event.setTextColor(Color.GREEN)
            event.setText(c.cardData.cardText)
            location.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            location.setTextColor(Color.GREEN)
            location.setText(c.locationData.location)
            year.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            year.setTextColor(Color.GREEN)
            year.setText(c.getYear())
            row.addView(event)
            row.addView(location)
            row.addView(year)
            table.addView(row)
            table.invalidate()

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