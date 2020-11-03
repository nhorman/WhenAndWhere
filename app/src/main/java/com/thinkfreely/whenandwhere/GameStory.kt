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

    fun getStoryText(page: Int) : String {
        val text = String(pages[page].storytext)
        return text
    }

    fun getStoryAnswer(page: Int) : String {
        val text = String(pages[page].answertext)
        return text
    }

    fun getGameCards(): List<GameCard> {
        return cards
    }

    fun getAnswers(page: Int) : List<String>{
        val answerstring = pages[page].correctcardname
        if (answerstring.isEmpty() == true)
            return emptyList<String>()
        val answerlist = answerstring.split(":")
        return answerlist
    }

    fun answerOrderMatters(page: Int) : Boolean {
        if (pages[page].answerordermatters == 0)
            return false
        return true
    }
}