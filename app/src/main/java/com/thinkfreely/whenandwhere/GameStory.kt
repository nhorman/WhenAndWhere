package com.thinkfreely.whenandwhere

import android.content.Context

class GameStory(storydata: Story, context: Context) {
    val sdata = storydata
    lateinit var cards : List<GameCard>
    lateinit var pages : List<StoryPage>

    init {
        //Get all the story data we need from the databsae
        val factory = GameCardFactory(context)
    }
}