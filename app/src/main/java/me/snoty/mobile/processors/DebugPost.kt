package me.snoty.mobile.processors

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Stefan on 04.01.2018.
 */
class DebugPost : ProcessorInterface {

    private val POST_URL = "https://www.stefangruber.net/snoty-debug.php"
    private val TAG = "DebugPost"

    override fun created(id: String, n: StatusBarNotification) {
        sendPost(id, n, "created")
    }

    override fun removed(id: String, n: StatusBarNotification) {
        sendPost(id, n, "removed")
    }

    override fun updated(id: String, n: StatusBarNotification) {
        sendPost(id, n, "updated")
    }


    private fun sendPost(id: String, n: StatusBarNotification, action: String) {

        Log.d(TAG, "sending notification to debug server ...")

        val thread = Thread(Runnable {
            try {
                val url = URL(POST_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")


                val importance = "?"
                val noClear = checkFlag(n.notification, Notification.FLAG_NO_CLEAR)
                val localOnly = checkFlag(n.notification, Notification.FLAG_LOCAL_ONLY)

                val jsonParam = JSONObject()
                jsonParam.put("id", id)
                jsonParam.put("timestamp", dateFormat.format(Date()))
                jsonParam.put("operation", action)
                jsonParam.put("title", n.notification.extras[Notification.EXTRA_TITLE])
                jsonParam.put("text", n.notification.extras[Notification.EXTRA_TEXT])
                jsonParam.put("package", n.packageName)
                jsonParam.put("channel", n.notification.extras[Notification.EXTRA_CHANNEL_ID])
                jsonParam.put("noclear", ""+noClear)
                jsonParam.put("localonly", ""+localOnly)
                jsonParam.put("importance", importance)

                Log.i("JSON", jsonParam.toString())
                val os = DataOutputStream(conn.outputStream)
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString())

                os.flush()
                os.close()

                Log.d(TAG, "Sent (no exception)")

                Log.i("STATUS", ""+conn.responseCode)
                Log.i("MSG", conn.responseMessage)

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()
    }

    private fun checkFlag(notification: Notification?, flag: Int): Boolean {
        if(notification == null) {
            return false
        }
        return (notification.flags and flag) == flag
    }


}