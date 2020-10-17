package com.thinkfreely.whenandwhere

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.DragEvent
import android.view.DragEvent.ACTION_DRAG_LOCATION
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.absoluteValue

private var hotspotmap: MutableMap<String, FrameLayout> = mutableMapOf()

private class GameCardDragListener : View.OnDragListener {
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        //println("CARD")
        when (event?.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    //println("ACTION_DRAG_STARTED")
                    return true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    //println("ACTION_DRAG_ENTERED")
                    return true
                }
                DragEvent.ACTION_DROP -> {
                    //println("ACTION_DROP")
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
                    return true
                }
                else -> {
                    //println("UNKNOWN ACTION: " +event?.action.toString())
                    return true
                }
            }
        //println("NO EVENT")
    }
}

private class CardShadowBuilder(v: View) : View.DragShadowBuilder(v) {

    private val iv = (v as ImageView)
    private val shadow = BitmapDrawable(iv.drawable.toBitmap())
    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        // Sets the width of the shadow to half the width of the original View
        val width: Int = view.width / 2

        // Sets the height of the shadow to half the height of the original View
        val height: Int = view.height / 4

        // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
        // Canvas that the system will provide. As a result, the drag shadow will fill the
        // Canvas.
        shadow.setBounds(0, 0, width, height)

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set(width, height)

        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set(width / 2, height / 4)
    }

    // Defines a callback that draws the drag shadow in a Canvas that the system constructs
    // from the dimensions passed in onProvideShadowMetrics().
    override fun onDrawShadow(canvas: Canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        shadow.draw(canvas)
    }
}
class GameCard(carddata : Card, location: Location) {
    val cardData = carddata
    val locationData = location
    lateinit var hotspotview: FrameLayout
    var top : Int = 0
    var bottom: Int = 0
    var right: Int = 0
    var left: Int = 0

    fun getCardView(context : Context) : ImageView {
        val image = BitmapDrawable(BitmapFactory.decodeByteArray(cardData.cardLogo, 0,
            cardData.cardLogo?.size!!
        ))
        val cardimageview = ImageView(context).apply {
            setImageBitmap(image.bitmap)
            val tag = "image bitmap"
            setOnDragListener(GameCardDragListener())
            setOnLongClickListener {v: View ->
                val item = ClipData.Item(v.tag as? CharSequence)
                val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )
                val myShadow = CardShadowBuilder(v)
                v.startDrag(dragData, myShadow, null, 0)
            }
        }
        cardimageview.setImageDrawable(image)
        return cardimageview
    }

    fun setLocationMarkerView(p: ViewGroup, density: Float) : FrameLayout {
        println("Layout for " + this.locationData.location)
        lateinit var flayout : FrameLayout

        var fromtoptmp: Float = locationData.latitude
        var fromlefttmp: Float = locationData.longitude

        //percentage of the way down the screen
        if (locationData.latdir.equals("North")) {
            fromtoptmp = 1 - (fromtoptmp / 90.toFloat())
        }
        else {
            0.5 + fromtoptmp / 180.toFloat()
        }
        fromtoptmp = (fromtoptmp - ((locationData.height/2).toFloat() / 90.toFloat()))
        top = (fromtoptmp * p.height.toFloat()).toInt()
        bottom = top + (locationData.height)

        if (locationData.longdir.equals("West")) {
            fromlefttmp = 1 - (fromlefttmp / 180.toFloat())
        } else {
            fromlefttmp = (0.5 + fromlefttmp / 360.toFloat()).toFloat()
        }
        fromlefttmp = fromlefttmp - ((locationData.width/2).toFloat() / 180.toFloat())
        left = (fromlefttmp * (p.width.toFloat())).toInt()
        right = left + (locationData.width)

        if (hotspotmap.containsKey(this.locationData.location)) {
            flayout = hotspotmap.get(this.locationData.location) as FrameLayout
        } else {
            flayout = FrameLayout(p.context)
            hotspotmap.set(this.locationData.location, flayout)
            val layoutp = FrameLayout.LayoutParams(this.locationData.width, this.locationData.height)
            layoutp.setMargins(left, top, right, bottom)
            flayout.layoutParams = layoutp
            flayout.setBackgroundColor(Color.RED)
            flayout.setTag(R.id.simpleGameCard, this)
            p.addView(flayout)
        }

        println("top =" + top.toString())
        println("bottom =" + bottom.toString())
        println("left =" + left.toString())
        println("right =" + right.toString())
        println("view =" + p.height.toString() + " " + p.width.toString())
        hotspotview = flayout
        return flayout
    }

    fun inHotSpot(x: Float, y: Float) : Boolean {
        if (x.toInt() >= left && x.toInt() <= right && y.toInt() >= top && y.toInt() <= bottom)
            return true
        return false
    }

    fun cameBefore(othercard: GameCard) : Boolean {
        val thisyear = this.cardData.year?.toInt()
        val otheryear = othercard.cardData.year?.toInt()
        if (thisyear == null)
            return false
        if (otheryear == null)
            return false

        if (thisyear <= otheryear) {
            return true
        }
        return false
    }

    fun cameAfter(othercard: GameCard) : Boolean {
        val thisyear = this.cardData.year?.toInt()
        val otheryear = othercard.cardData.year?.toInt()
        if (thisyear == null)
            return false
        if (otheryear == null)
            return false

        if (thisyear >= otheryear) {
            return true
        }
        return false
    }

    fun getYear() : String {
        val year = this.cardData.year
        if (year == null)
            return "Unknown"
        val absyear = year?.absoluteValue
        if (year < 0) {
            return absyear.toString() + " BC"
        }
        return year.toString()
    }
}