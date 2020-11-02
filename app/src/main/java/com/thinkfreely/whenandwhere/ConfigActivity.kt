package com.thinkfreely.whenandwhere

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        val db = GameCardFactory(this)

        val job = Job()
        val scopeMainThread = CoroutineScope(job + Dispatchers.Main)
        val scopeIO = CoroutineScope(job + Dispatchers.IO)

        val customtoggle = findViewById(R.id.EnableCustomDBToggle) as ToggleButton
        customtoggle.setOnCheckedChangeListener { buttonView, isChecked ->
            val dburltext = findViewById(R.id.CustomDBEditText) as EditText
            if (isChecked)
                dburltext.isEnabled = true
            else
                dburltext.isEnabled = false
        }

        val donebutton = findViewById(R.id.ConfigDoneButton) as Button
        donebutton.setOnClickListener {
            if (customtoggle.isChecked) {
                scopeIO.launch {
                    var length: Int
                    val buffer: ByteArray = ByteArray(1024)
                    val urltextview = findViewById(R.id.CustomDBEditText) as EditText
                    val u = URL(urltextview.text.toString())
                    try {
                        val dis = DataInputStream(u.openStream())
                        val f = File("newcards.db")
                        val fos = FileOutputStream(f)
                        length = dis.read(buffer)
                        while (length > 0) {
                            fos.write(buffer, 0, length)
                            length = dis.read(buffer)
                        }
                        db.overrideDB(f, applicationContext)
                        f.delete()

                    } catch (e: Exception) {
                        scopeMainThread.launch {
                        val toast =
                            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT)
                            toast.show()
                        }
                    }
                    scopeMainThread.launch {
                        finish()
                    }
                }
            } else {
                db.resetDBToInternal(applicationContext)
                finish()
            }
        }
    }
}