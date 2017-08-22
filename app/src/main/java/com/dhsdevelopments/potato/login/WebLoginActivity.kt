package com.dhsdevelopments.potato.login

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.initial.PotatoActivity

class WebLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_login)

        val webView = findViewById<WebView>(R.id.login_webview)
        webView.webViewClient = Client()

        val app = PotatoApplication.getInstance(this)
        webView.loadUrl(app.findApiProvider().serverUrlPrefix + "login?m=1")
    }

    private inner class Client : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.scheme == "potato") {
                when (request.url.host) {
                    "authenticated" -> {
                        val key = request.url.getQueryParameter("key")
                        if (key == null || key == "") {
                            throw RuntimeException("did not find key parameter")
                        }
                        val uid = request.url.getQueryParameter("user_id")
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
                        val email = request.url.getQueryParameter("email")
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
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }
}
