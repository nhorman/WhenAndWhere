package com.thinkfreely.whenandwhere

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView


class GameCard(val carddata : Card) {
    val cardData = carddata

    fun getCardView(context : Context) : ImageView {

        val image = BitmapDrawable(BitmapFactory.decodeByteArray(cardData.cardLogo, 0,
            cardData.cardLogo?.size!!
        ))
        val cardimageview = ImageView(context)
        cardimageview.setImageDrawable(image)

        return cardimageview
    }
}