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
import android.os.Parcel
import android.os.Parcelable
import android.view.DragEvent
import android.view.DragEvent.ACTION_DRAG_LOCATION
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.parcel.Parcelize
import kotlin.math.absoluteValue

private var hotspotmap: MutableMap<String, ImageView> = mutableMapOf()

private class GameCardDragListener() : View.OnDragListener {


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
                    GameCard.dragcard == null
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

class HotSpotImageView(context: Context, card: GameCard, density: Float) : ImageView(context) {

    val locationData = card.locationData
    val density : Float = density
    var firstpass = true

    override fun onMeasure(wspec: Int, hspec: Int) {
        super.onMeasure(wspec, hspec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val parent = this.parent as ConstraintLayout
        var fromtoptmp: Float = locationData.latitude
        var fromlefttmp: Float = locationData.longitude
        val pheight = parent.measuredHeight
        val pwidth = parent.measuredWidth

        //percentage of the way down the screen
        if (locationData.latdir.equals("North")) {
            fromtoptmp = fromtoptmp / 90.toFloat()
            fromtoptmp = (pheight/2) * (1 - fromtoptmp)
        }
        else {
            fromtoptmp = fromtoptmp / 90.toFloat()
            fromtoptmp = (pheight/2)  * (1 + fromtoptmp)
        }
        val top = (fromtoptmp).toInt()
        val bottom = pheight - (top + locationData.height / density).toInt()

        if (locationData.longdir.equals("West")) {
            fromlefttmp = fromlefttmp / 180.toFloat()
            fromlefttmp = (pwidth/2) * (1 - fromlefttmp)
        } else {
            fromlefttmp = fromlefttmp / 180.toFloat()
            fromlefttmp = (pwidth/2) * (1 + fromlefttmp)
        }
        val left = (fromlefttmp).toInt()
        val right = pwidth - (left + locationData.width / density).toInt()
        val constraints = ConstraintSet()
        constraints.clone(parent)
        constraints.clear(id)
        constraints.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, top)
        constraints.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (bottom))
        constraints.connect(id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, left)
        constraints.connect(id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, (right))
        constraints.constrainHeight(id, (locationData.height * density).toInt())
        constraints.constrainWidth(id, (locationData.width * density).toInt())
        constraints.applyTo(parent)
    }
}


class GameCard(val carddata : Card, val location: Location) : Parcelable {
    val cardData = carddata
    val locationData = location
    lateinit var hotspotview: ImageView
    lateinit var cardview: ImageView

    private val draglistener = GameCardDragListener()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(cardData.rowId)
        dest?.writeLong(cardData.level as Long)
        dest?.writeString(cardData.category)
        dest?.writeInt((carddata.cardLogo as ByteArray).size)
        dest?.writeByteArray(carddata.cardLogo)
        dest?.writeLong(carddata.year as Long)
        dest?.writeString(carddata.location)
        dest?.writeString(carddata.cardText)
        dest?.writeString(carddata.credittext)
        dest?.writeString(carddata.crediturl)

        dest?.writeString(locationData.location)
        dest?.writeFloat(locationData.longitude)
        dest?.writeFloat(locationData.latitude)
        dest?.writeString(locationData.longdir)
        dest?.writeString(locationData.latdir)
        dest?.writeInt(locationData.width)
        dest?.writeInt(locationData.height)
    }

    fun getCardView(context : Context) : ImageView {
        val myself = this
        val image = BitmapDrawable(BitmapFactory.decodeByteArray(cardData.cardLogo, 0,
            cardData.cardLogo?.size!!
        ))
        val cardimageview = ImageView(context).apply {
            id = View.generateViewId()
            setImageBitmap(image.bitmap)
            val tag = "image bitmap"
            setOnDragListener(draglistener)
            setOnLongClickListener {v: View ->
                val item = ClipData.Item(v.tag as? CharSequence)
                val dragData = ClipData(
                    v.tag as? CharSequence,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )
                val myShadow = CardShadowBuilder(v)
                dragcard = myself
                v.startDrag(dragData, myShadow, null, 0)
            }
        }
        cardimageview.setImageDrawable(image)
        this.cardview = cardimageview
        return cardimageview
    }

    fun setLocationMarkerView(parent: ConstraintLayout, density: Float) : ImageView {
        println("Layout for " + this.locationData.location)
        lateinit var flayout :ImageView


        if (hotspotmap.containsKey(this.locationData.location)) {
            flayout = hotspotmap.get(this.locationData.location) as ImageView
        } else {
            flayout = HotSpotImageView(parent.context, this, density)
            flayout.id = View.generateViewId()
            parent.addView(flayout)

            hotspotmap.set(this.locationData.location, flayout)
            //constraints.clone(p)
            flayout.setBackgroundColor(Color.RED)
            flayout.setTag(R.id.simpleGameCard, this)
            //flayout.layoutParams = FrameLayout.LayoutParams(right-left, top-bottom)
        }
        hotspotview = flayout
        return flayout
    }

    fun isMyHotspot(v: View) : Boolean
    {
        if (v == hotspotview)
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

    fun cameBefore(year: Int) : Boolean {
        val thisyear = this.cardData.year?.toInt()
        if (thisyear == null)
            return false
        if (thisyear <= year)
            return true
        return false
    }

    fun cameAfter(year: Int) : Boolean {
        val thisyear = this.cardData.year?.toInt()
        if (thisyear == null)
            return false
        if (thisyear >= year)
            return true
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

    fun getDragCard(): GameCard {
        return dragcard
    }

    companion object {
        lateinit var dragcard: GameCard
        @JvmField val CREATOR = object : Parcelable.Creator<GameCard> {
            override fun createFromParcel(parcel: Parcel): GameCard {
                val row = parcel.readInt()
                val level = parcel.readLong()
                val category = parcel.readString()
                val logolen = parcel.readInt()
                val logo = ByteArray(logolen)
                parcel.readByteArray(logo)
                val carddata = Card (
                    row,
                    level,
                    category,
                    logo,
                    parcel.readLong(),
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString(),
                )
                val location = Location(
                    parcel.readString() as String,
                    parcel.readFloat(),
                    parcel.readFloat(),
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readInt(),
                    parcel.readInt()
                )

                return GameCard(carddata, location)
            }

            override fun newArray(size: Int): Array<GameCard?> {
                return arrayOfNulls(size)
            }
        }

    }

}