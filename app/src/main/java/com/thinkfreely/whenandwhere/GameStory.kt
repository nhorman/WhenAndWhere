package com.thinkfreely.whenandwhere

import android.content.Context

class GameStory(story: String, gamefactory: GameCardFactory) {
    val storyname = story
    val factory  = gamefactory
    lateinit var cards : List<GameCard>
    lateinit var pages : List<StoryPage>
    lateinit var storydata: Story

    init {
        //Get all the story data we need from the databsae
        storydata = factory.getStory(storyname)
        pages = factory.getStoryPages(storydata)
        cards = factory.getStoryCards(pages)
    }
}