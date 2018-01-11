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
        //DemoServer(sock, false).start()
        Thread.sleep(500)
    }
}


class DemoServer : Thread {

    private var sock: Socket? = null
    private var isReader: Boolean = false

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
                println("READER [br=$br], [sock=$sock]")
                while(true) {
                    if(sock == null) break
                    if(sock?.isConnected != true) break
                    if(sock?.isClosed == true) break

                    println("\n- Receiving data from [" + sock?.remoteSocketAddress + "] -")
                    var string = br.readLine()
                    println(string)
                }
            }
            else {
                val pw = PrintWriter(sock?.getOutputStream())
                while(true) {
                    pw.println(getDataToSend())
                    println("- Sending data to [" + sock?.remoteSocketAddress + "] -")
                    pw.close()
                }
            }
            println("closing socket")
            sock?.close()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }

        println("Connection closed.\n")

    }

    private fun getDataToSend(): String {
        val scanner = Scanner(System.`in`)

        print("Data to send (1 line): ")
        val input = scanner.next()
        scanner.close()

        return input
    }

}