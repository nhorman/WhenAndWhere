package com.thinkfreely.whenandwhere

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.NO_ACTION
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.sql.Blob

@Entity(tableName = "storypage")
data class StoryPage(
    @PrimaryKey(autoGenerate=true) var rowId: Int,
    @ColumnInfo(name = "pagenumber") var pagenumber: Int,
    @ColumnInfo(name ="storyname") var storyname: String,
    @ColumnInfo(name = "storytext", typeAffinity = ColumnInfo.BLOB) var storytext: ByteArray,
    @ColumnInfo(name = "correctcardname") var correctcardname: String,
    @ColumnInfo(name = "answerordermatters") var answerordermatters: Int,
    @ColumnInfo(name = "answertext", typeAffinity = ColumnInfo.BLOB) var answertext: ByteArray
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey(autoGenerate = true) var rowId: Int,
    @ColumnInfo(name="storyname") var storyname: String
)

@Entity(tableName = "locations")
data class Location(
    @PrimaryKey
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "longitude") var longitude: Float,
    @ColumnInfo(name = "latitude") var latitude: Float,
    @ColumnInfo(name = "longdir") var longdir: String?,
    @ColumnInfo(name = "latdir") var latdir: String?,
    @ColumnInfo(name = "width") var width: Int,
    @ColumnInfo(name = "height") var height: Int
)

@Entity(tableName = "card", foreignKeys = [ForeignKey(entity = Location::class, parentColumns = ["location"], childColumns = ["location"], onDelete = NO_ACTION )])
data class Card(
    @PrimaryKey(autoGenerate = true) var rowId: Int,
    @ColumnInfo(name = "level") var level: Long?,
    @ColumnInfo(name = "category") var category: String?,
    @ColumnInfo(name = "cardname") var cardname: String?,
    @ColumnInfo(name = "storyonly") var storyonly: Int?,
    @ColumnInfo(name = "cardlogo", typeAffinity = ColumnInfo.BLOB) var cardLogo : ByteArray?,
    @ColumnInfo(name = "year") var year : Long?,
    @ColumnInfo(name = "location") var location : String?,
    @ColumnInfo(name = "cardtext") var cardText : String?,
    @ColumnInfo(name = "credittext") var credittext: String?,
    @ColumnInfo(name = "crediturl") var crediturl: String?
)

@Dao
interface CardDao {
    @Query("SELECT * from card")
    fun getAll(): List<Card>

    @Query("SELECT * from card WHERE storyonly == 0 ORDER BY RANDOM() LIMIT 1")
    fun getRandomCard(): Card

    @Query("SELECT * from stories")
    fun getStoryList(): List<Story>

    @RawQuery
    fun getStoryPages(query: SupportSQLiteQuery): List<StoryPage>

    @RawQuery
    fun getStory(query: SupportSQLiteQuery): Story

    @RawQuery
    fun getStoryCards(query: SupportSQLiteQuery): List<Card>

    @RawQuery
    fun getCardLocation(query: SupportSQLiteQuery): Location

    @RawQuery
    fun getRandomCardList(query : SupportSQLiteQuery): List<Card>

    @RawQuery
    fun getSpecificCard(query: SupportSQLiteQuery): Card

}

@Database(entities = arrayOf(Card::class, Location::class, Story::class, StoryPage::class), version = 1)
abstract class GameCardDb : RoomDatabase() {
    abstract fun cardDao(): CardDao
}



private var gamedb : Any? = null

class GameCardFactory(val context: Context) {

    init {
        val path: File = context.getDatabasePath("mycards.db").absoluteFile
        path.delete()
    }
    init {
        if (gamedb == null) {
            try {
                gamedb = Room.databaseBuilder(context, GameCardDb::class.java, "mycards.db")
                    .createFromAsset("database/cards.db").build()
            } catch (e: Exception) {
                context.deleteDatabase("mycards.db")
                println("Database Corrupted, rebuilding")
                gamedb = Room.databaseBuilder(context, GameCardDb::class.java, "mycards.db")
                    .createFromAsset("database/cards.db").build()
            }
        }
    }
    private val db = gamedb as GameCardDb


    fun getStoryList(): List<Story> {
        return db.cardDao().getStoryList()
    }

    fun getStory(storyname: String): Story {
        val squery = SimpleSQLiteQuery("SELECT * FROM stories WHERE storyname = '" + storyname + "'")
        val story = db.cardDao().getStory(squery)
        return story
    }

    fun getStoryPages(story: Story): List<StoryPage> {
        val squery = SimpleSQLiteQuery("SELECT * FROM storypage WHERE storyname = '" + story.storyname + "'")
        val pages = db.cardDao().getStoryPages(squery)
        return pages
    }

    fun getStoryCards(pages: List<StoryPage>): List<GameCard> {
        val mycards: MutableList<GameCard> = mutableListOf()
        var allcardnames: List<String> = emptyList<String>()
        for (p in pages) {
            val cardsnames = p.correctcardname
            if (cardsnames.isBlank() == true)
                continue
            val cardlist = cardsnames.split(":")
            allcardnames = allcardnames + cardlist
        }
        //remove duplicates
        allcardnames = allcardnames.distinct()

        for (c in allcardnames) {
            val newcarddata = this.getSpecificCard(c)
            val lquery = SimpleSQLiteQuery("SELECT * FROM locations WHERE location = '" + newcarddata.location + "'")
            val ldata = db.cardDao().getCardLocation(lquery)
            mycards.add(GameCard(newcarddata, ldata))
        }

        return mycards as List<GameCard>
    }

    fun getRandomCard() : GameCard {
       val card = db.cardDao().getRandomCard()
        val lquery = SimpleSQLiteQuery("SELECT * FROM locations WHERE location = '" + card.location + "' AND storyonly = 0")
        val loc = db.cardDao().getCardLocation(lquery)
       return GameCard(card, loc)
    }

    fun getAllCards(): List<Card> {
        return db.cardDao().getAll()
    }

    fun getRandomCardSet(num: Int) : MutableList<GameCard> {
        val mycards : MutableList<GameCard> = mutableListOf()
        val query = SimpleSQLiteQuery("SELECT * FROM card WHERE storyonly == 0 ORDER BY RANDOM() LIMIT " + num.toString())
        val cardata = db.cardDao().getRandomCardList(query)
        for (c in cardata) {
            val lquery = SimpleSQLiteQuery("SELECT * FROM locations WHERE location = '" + c.location + "'")
            val ldata = db.cardDao().getCardLocation(lquery)
            mycards.add(GameCard(c, ldata))
        }
        return mycards
    }

    fun getSpecificCard(row: Int): Card {
        val query = SimpleSQLiteQuery("SELECT * FROM card WHERE rowId =" + row.toString())
        val carddata = db.cardDao().getSpecificCard(query)
        return carddata
    }

    fun getSpecificCard(name: String): Card {
        val query = SimpleSQLiteQuery("SELECT * FROM card WHERE cardname = '" + name + "'")
        val carddata = db.cardDao().getSpecificCard(query)
        return carddata
    }

    fun overrideDB(newdb: File, context: Context) {
        gamedb = Room.databaseBuilder(context, GameCardDb::class.java, "mycards.db")
            .createFromFile(newdb).build()
    }

    fun resetDBToInternal(context: Context) {
         gamedb = Room.databaseBuilder(context, GameCardDb::class.java, "mycards.db")
                    .createFromAsset("database/cards.db").build()
    }

    companion object {
        var custom_db : String = ""

    }

}