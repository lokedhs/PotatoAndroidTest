package com.dhsdevelopments.potato.initial

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.dhsdevelopments.potato.PotatoApplication
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.clientapi.ServerInfoResult
import com.dhsdevelopments.potato.common.Log
import kotlinx.android.synthetic.main.activity_read_server_name.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReadServerNameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_server_name)

        request_server_name_url.addTextChangedListener(MonitorUrlChange())
        updateButtonEnabled()
    }

    fun connectClicked(@Suppress("UNUSED_PARAMETER") view: View) {
        val url = request_server_name_url.text
        if (validUrlRegexp.matches(url)) {
            val normalisedUrl = normaliseUrl(url.toString())
            verifyServer(normalisedUrl)
        }
    }

    fun storeServerUrlAndFinish(url: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().apply {
            putString(getString(R.string.pref_servername), url)
            apply()
        }

        val intent = Intent(this, PotatoActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun normaliseUrl(s: String): String {
        val url = if (protocolRegexp.matches(s)) s else "https://$s"
        return if (url.endsWith("/")) url else "$url/"
    }

    fun updateButtonEnabled() {
        val valid = validUrlRegexp.matches(request_server_name_url.text)
        request_server_name_button.isEnabled = valid
    }

    fun verifyServer(url: String) {
        error_message_text_view.visibility = View.GONE
        updateBusyState(true)

        Log.i("Attempting to connecto to url: $url")
        val app = PotatoApplication.getInstance(this)
        val call = app.findApiProvider().makePotatoApi(urlPrefix = url).getServerInfo()
        call.enqueue(object : Callback<ServerInfoResult?> {
            override fun onFailure(call: Call<ServerInfoResult?>?, t: Throwable?) {
                showErrorMessage(getString(R.string.request_server_connect_failed))
                updateBusyState(false)
            }

            override fun onResponse(call: Call<ServerInfoResult?>, response: Response<ServerInfoResult?>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        showErrorMessage(getString(R.string.no_response_from_server))
                        updateBusyState(false)
                    } else {
                        storeServerUrlAndFinish(url)
                    }
                } else {
                    showErrorMessage(getString(R.string.request_server_connection_error, response.message()))
                    updateBusyState(false)
                }
            }
        })
    }

    fun showErrorMessage(msg: String) {
        error_message_text_view.text = msg
        error_message_text_view.visibility = View.VISIBLE
    }

    fun updateBusyState(busy: Boolean) {
        request_server_name_url.isEnabled = !busy
        request_server_name_button.isEnabled = !busy
        connect_progress.visibility = if (busy) View.VISIBLE else View.GONE
    }

    inner class MonitorUrlChange : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateButtonEnabled()
        }
    }

    companion object {
        private val validUrlRegexp = Regex("^(?:https?://)?[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*/?$")
        private val protocolRegexp = Regex("^https?://.*$")
    }
}
