package com.dhsdevelopments.potato.tester

import android.app.Activity
import android.app.DialogFragment
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import com.dhsdevelopments.potato.Log
import com.dhsdevelopments.potato.R
import com.dhsdevelopments.potato.nlazy

class TesterActivity : Activity() {
    val testNotificationButton by nlazy { findViewById(R.id.test_notification_button) as Button }
    val openDialogButton by nlazy { findViewById(R.id.test_dialog_button) as Button }
    val dialogStylePicker by nlazy { findViewById(R.id.dialog_style_number_picker) as NumberPicker }
    val themePicker by nlazy { findViewById(R.id.theme_picker) as NumberPicker }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tester)

        testNotificationButton.setOnClickListener { testNotification() }
        openDialogButton.setOnClickListener { openDialog() }

        dialogStylePicker.minValue = 0
        dialogStylePicker.maxValue = 7
        themePicker.minValue = 0
        themePicker.maxValue = 4
    }

    private fun testNotification() {
        val unread = 1
        val prefs = getSharedPreferences("com.dhsdevelopments.potato_preferences", MODE_PRIVATE)
        val builder = Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("New Potato messages")
                .setContentText("You have new messages in ${unread} channel" + if (unread == 1) "" else "s")
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)


        if (prefs.getBoolean(getString(R.string.pref_notifications_private_message_vibrate), true)) {
            builder.setVibrate(longArrayOf(0, 500, 0, 500))
        }

        val ringtone = prefs.getString(getString(R.string.pref_notifications_private_message_ringtone), null)
        val v = prefs.getBoolean(getString(R.string.pref_notifications_private_message_vibrate), true)
        Log.d("r=${ringtone}, v=${v}")
        if (ringtone != null) {
            builder.setSound(Uri.parse(ringtone))
        }

        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify("foo", 1, builder.build())
    }

    private fun openDialog() {
        val ft = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        val dialogFragment = TestDialogFragment()
        val args = Bundle()
        args.putInt("style", makeStyleFromInt(dialogStylePicker.value))
        args.putInt("theme", makeThemeFromInt(themePicker.value))
        Log.d("Setting arguments to: $args")
        dialogFragment.arguments = args
        dialogFragment.show(ft, "dialog")
    }

    private fun makeStyleFromInt(style: Int): Int {
        return when (style) {
            0 -> DialogFragment.STYLE_NO_TITLE
            1 -> DialogFragment.STYLE_NO_FRAME
            2 -> DialogFragment.STYLE_NO_INPUT
            3 -> DialogFragment.STYLE_NORMAL
            4 -> DialogFragment.STYLE_NORMAL
            5 -> DialogFragment.STYLE_NO_TITLE
            6 -> DialogFragment.STYLE_NO_FRAME
            7 -> DialogFragment.STYLE_NORMAL
            else -> throw IllegalArgumentException("Illegal style id: $style")
        }
    }

    private fun makeThemeFromInt(theme: Int): Int {
        return when (theme) {
            0 -> android.R.style.Theme_Material
            1 -> android.R.style.Theme_Material_Dialog
            2 -> android.R.style.Theme_Material_Dialog_Presentation
            3 -> android.R.style.Theme_Material_Panel
            4 -> android.R.style.Theme_Material_Light_Panel
            else -> throw IllegalArgumentException("Illegal theme id: $theme")
        }
    }
}

class TestDialogFragment : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val styleId = arguments.getInt("style")
        val themeId = arguments.getInt("theme")
        setStyle(styleId, themeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_dialog, container, false)
        dialog.setTitle("Test title")
        return view;
    }
}
