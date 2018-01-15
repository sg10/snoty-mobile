package me.snoty.demo
import java.io.*
import java.net.Socket
import java.security.KeyStore
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import javax.swing.JOptionPane
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Alert




fun main(args: Array<String>) {

    val ssf = SSLContext.getDefault().serverSocketFactory as SSLServerSocketFactory
    val ss = ssf.createServerSocket(9096)

    println("Server address is "+ss.localSocketAddress)

    while (true) {
        val sock = ss.accept()
        DemoServer(sock, true).start()
        DemoServer(sock, false).start()
        Thread.sleep(500)
    }
}


class DemoServer : Thread {

    private var sock: Socket? = null
    private var isReader: Boolean = false
    private var inputPromptShown = false

    private var scanner: Scanner? = null

    constructor(s: Socket, isReader: Boolean) {
        val t = if(isReader) "reader" else "writer"
        println("Client connected: ${s.remoteSocketAddress} as $t")
        sock = s
        this.isReader = isReader
    }

    override fun run() {
        try {
            if(isReader) {
                val inputStream = sock?.getInputStream()
                val br = BufferedReader(InputStreamReader(inputStream))
                while(true) {
                    if(sock == null) break
                    if(sock?.isConnected != true) break
                    if(sock?.isClosed == true) break

                    println("\n- Waiting for data from [" + sock?.remoteSocketAddress + "] -")
                    var string = br.readLine()
                    println(string)

                    if(string == null) break
                }
            }
            else {
                scanner = Scanner(System.`in`)

                val pw = PrintWriter(sock?.getOutputStream())
                while(true) {
                    val line = getDataToSend()
                    if(line.length > 2) {
                        pw.println(line)
                        println("- Sending data to [" + sock?.remoteSocketAddress + "] -")
                        pw.flush()
                    }//{"type":"NotificationOperation","id":"me.snoty.mobile#0","actionId":2,"operation":"action","inputValue":"blaaaaaa"}
                }
            }
            println("closing socket")
            sock?.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        scanner?.close()

        println("Connection closed.\n")

    }

    private fun getDataToSend(): String {
        val input = scanner?.nextLine()
        if(input != null && !input.isEmpty() && !inputPromptShown) {
            inputPromptShown = true
            println("Showing input")
            val json = JOptionPane.showInputDialog(null, "Enter JSON") ?: ""
            inputPromptShown = false
            return json
        }
        return ""
    }

}