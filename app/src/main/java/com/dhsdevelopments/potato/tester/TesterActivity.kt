package com.dhsdevelopments.potato.tester

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.nlazy

class TesterActivity : AppCompatActivity() {
    val testNotificationButton: Button by nlazy { findViewById(R.id.test_notification_button) as Button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tester)

        testNotificationButton.setOnClickListener { testNotification() }
    }

    private fun testNotification() {

    }
}
