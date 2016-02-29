package com.dhsdevelopments.potato.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.initial.PotatoActivity

class WebLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)

        val webView = findViewById(R.id.login_webview) as WebView
        webView.setWebViewClient(Client())

        val app = PotatoApplication.getInstance(this)
        webView.loadUrl(app.serverUrlPrefix + "login?m=1")
    }

    private inner class Client : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri = Uri.parse(url)
            if (uri.scheme == "potato") {
                when (uri.host) {
                    "authenticated" -> {
                        val key = uri.getQueryParameter("key")
                        if (key == null || key == "") {
                            throw RuntimeException("did not find key parameter")
                        }
                        val uid = uri.getQueryParameter("user_id")
                        if (uid == null || key == "") {
                            throw RuntimeException("response did not include user id")
                        }

                        val prefs = PreferenceManager.getDefaultSharedPreferences(this@WebLoginActivity)
                        val editor = prefs.edit()
                        editor.putString(getString(R.string.pref_apikey), key)
                        editor.putString(getString(R.string.pref_user_id), uid)
                        editor.apply()

                        val intent = Intent(this@WebLoginActivity, PotatoActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    "sent-registration" -> {
                        val email = uri.getQueryParameter("email")
                        if (email == null || email == "") {
                            throw RuntimeException("did not find email parameter")
                        }

                        val intent = Intent(this@WebLoginActivity, ActivationPanel::class.java)
                        intent.putExtra(ActivationPanel.EXTRA_EMAIL_ADDRESS, email)
                        startActivity(intent)
                        finish()
                    }
                }

                return true
            }
            else {
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
    }
}
