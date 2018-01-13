package me.snoty.demo
import java.io.*
import java.net.Socket
import java.security.KeyStore
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine




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

    private var scanner: Scanner? = null

    constructor(s: Socket, isReader: Boolean) {
        println("Client connected: " + s.remoteSocketAddress)
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
                    if(line != null && line.length > 2) {
                        pw.println(line)
                        println("- Sending data to [" + sock?.remoteSocketAddress + "] -")
                        pw.flush()
                    }
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
        return scanner?.nextLine() ?: ""
    }

}