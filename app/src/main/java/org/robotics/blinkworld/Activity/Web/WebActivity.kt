package org.robotics.blinkworld.Activity.Web

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*
import org.robotics.blinkworld.R

class WebActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        val link  = intent.getStringExtra("link")
        full_screan_close_web.setOnClickListener {
            finish()
        }
        web_activity.settings.javaScriptEnabled = true
        web_activity.webViewClient = WebViewClient()
        web_activity.loadUrl(link.toString())
    }
}