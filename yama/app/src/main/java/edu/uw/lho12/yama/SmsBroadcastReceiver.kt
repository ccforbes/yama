package edu.uw.lho12.yama

import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.provider.Telephony
import android.provider.Telephony.Sms.Intents.getMessagesFromIntent
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.contentValuesOf
import java.lang.StringBuilder

private const val TAG = "SmsBroadcastReceiver"

private const val CHANNEL_ID = "Channel_ID"

private var NOTIFICATION_ID = 0

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val smsMessages = getMessagesFromIntent(intent)




/*            StringBuilder().apply {
                for (msg in smsMessages) {
                    append(msg.messageBody)
                }
            }*/

            val builder = buildNotification(context, smsMessages[0])
            showNotification(builder, context)
        }

    }

    private fun buildNotification(context: Context?, smsMessage: SmsMessage): NotificationCompat.Builder {

        // Intent for when the notification itself or "view" button is tapped, directs to the reading messages display
        val reading = Intent(context, MainActivity::class.java)
        val readingIntent = PendingIntent.getActivity(context, 0, reading, 0)

        val number = smsMessage.originatingAddress

        // Intent for when the "reply" button is tapped, directs to the compose message display
        val compose = Intent(context, SendActivity::class.java).apply {
            putExtra("START_CONTACTS_ID", false)
            putExtra("REPLY_PHONE_NUMBER", smsMessage.originatingAddress)
        }
        val composeIntent = PendingIntent.getActivity(context, 0, compose, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context as Context, "basic")
            .setSmallIcon(R.drawable.ic_message_black_24dp)
            .setContentTitle(smsMessage.originatingAddress)
            .setContentIntent(readingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_message_black_24dp, "View",readingIntent)
            .addAction(R.drawable.ic_message_black_24dp, "Reply", composeIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(smsMessage.messageBody))
            .setChannelId(CHANNEL_ID)

        return builder
    }

    private fun showNotification(builder: NotificationCompat.Builder, context: Context?) {
        val notificationManager =
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Only need to create notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID++, builder.build())
    }
}