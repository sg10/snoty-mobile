package me.snoty.mobile.plugins

import android.service.notification.StatusBarNotification
import javax.net.SocketFactory
import javax.net.ssl.*
import android.system.Os.socket
import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/**
 * Created by Stefan on 02.01.2018.
 */
class ServerConnection : PluginInterface {

    private var socket : SSLSocket? = null

    override fun created(id: String, n: StatusBarNotification) {
        // send command
    }

    override fun removed(id: String, n: StatusBarNotification) {
        // send command
    }

    override fun updated(id: String, n: StatusBarNotification) {
        // send command
    }

    private fun establishConnection() {
        // Open SSLSocket directly to gmail.com
        val sf : SocketFactory = SSLSocketFactory.getDefault();
        socket = sf.createSocket("gmail.com", 443) as SSLSocket
        val hv : HostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
        val s : SSLSession = socket!!.session

        // Verify that the certicate hostname is for mail.google.com
        // This is due to lack of SNI support in the current SSLSocket.
        if (!hv.verify("mail.google.com", s)) {
            socket?.close()
            throw SSLHandshakeException("Expected mail.google.com, found " + s.peerPrincipal)
        }
    }

    private fun waitForCommand() {
        if(socket == null) {
            establishConnection()
        }

        val outw = BufferedWriter(OutputStreamWriter(socket?.outputStream))
        val inr = BufferedReader(InputStreamReader(socket?.inputStream))
        // read
        // - remove
        // - call intent
    }
}