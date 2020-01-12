package com.cse4100g10.taskmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class MyFirebaseMessagingService: FirebaseMessagingService() {
    private var TOKEN_API = "https://mcc-fall-2019-g10.appspot.com/api/user/"
    private val ADMIN_CHANNEL_ID = "admin_channel"
    // private var TOKEN_API = "ht"

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("TOKEN", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("TAG", "From: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            Log.d("TAG", "Message data payload: " + remoteMessage.data)

            /*if (true) {
                scheduleJob()
            } else {
                handleNow()
            }*/
        }

        remoteMessage.notification?.let {
            Log.d("TAG", "Message Notification Body: ${it.body}")
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        if (remoteMessage.notification != null) {
            Log.e("TAG", "MESSAGE ID => " + remoteMessage.messageId)
            Log.e("TAG", "ME => " + FirebaseInstanceId.getInstance().token)
            Log.e("TAG", "NOTIFICATION => " + remoteMessage.notification.toString())
            Log.e("TAG", "TO => " + remoteMessage.to)
            Log.e("TAG", "FROM => " + remoteMessage.from)
            showNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "xD"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, ProjectListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("0",
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, notificationBuilder.build())
    }


    private fun sendRegistrationToServer(token:String){
        val jsonobj = JSONObject()
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        jsonobj.put("msgToken",token)
        val que = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(
            Request.Method.PUT,TOKEN_API+auth.uid,jsonobj,
            Response.Listener {
                    response ->
                //println(response["message"].toString())
                println(response.toString())
            }, Response.ErrorListener {
                println("Error")
            }
        )
        que.add(req)
    }
}