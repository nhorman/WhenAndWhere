package com.thinkfreely.whenandwhere

import android.content.Context
import androidx.room.*
import java.io.File

@Entity
data class Card(
    @PrimaryKey(autoGenerate = true) var rowId: Int?,
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

    @Query("SELECT * from card ORDER BY RANDOM() LIMIT 1")
    fun getRandomCard(): Card

}

@Database(entities = arrayOf(Card::class), version = 1)
abstract class GameCardDb : RoomDatabase() {
    abstract fun cardDao(): CardDao
}


class GameCardFactory(val context: Context) {
    val db = Room.databaseBuilder(context, GameCardDb::class.java, "mycards.db").createFromAsset("database/cards.db").build()

    fun getRandomCard() : GameCard {
       val card = db.cardDao().getRandomCard()
       return GameCard(card)
    }

    fun getAllCards(): List<Card> {
        return db.cardDao().getAll()
    }
}