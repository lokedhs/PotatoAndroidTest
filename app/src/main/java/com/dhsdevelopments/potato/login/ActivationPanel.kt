package com.dhsdevelopments.potato.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.initial.PotatoActivity
import com.dhsdevelopments.potato.nlazy
import java.text.MessageFormat

class ActivationPanel : AppCompatActivity() {
    companion object {
        val EXTRA_EMAIL_ADDRESS = "com.dhsdevelopments.potato.email"
    }

    val registeredMessage by nlazy { findViewById(R.id.registered_message) as TextView }
    val loginButton by nlazy { findViewById(R.id.login_button) as Button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation_panel)

        val email = intent.getStringExtra(EXTRA_EMAIL_ADDRESS)

        val fmt = MessageFormat(getString(R.string.registration_text))
        registeredMessage.text = fmt.format(arrayOf(email))

        loginButton.setOnClickListener({
            val intent = Intent(ActivationPanel@this, PotatoActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}
