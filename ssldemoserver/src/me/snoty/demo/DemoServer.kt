package me.snoty.demo
import java.io.*
import java.net.Socket
import java.security.KeyStore
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocketFactory


fun main(args: Array<String>) {

    val ssf = SSLContext.getDefault().serverSocketFactory as SSLServerSocketFactory
    val ss = ssf.createServerSocket(9096)

    println("Server address is "+ss.localSocketAddress)

    while (true) {
        println("Waiting for client to connect ...")
        val sock = ss.accept()
        DemoServer(sock, true).start()
        //DemoServer(sock, false).start()
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
                val br = BufferedReader(InputStreamReader(sock?.getInputStream()))
                while(true) {
                    val data = br.read()
                    if(data != null) {
                        println("\n- Receiving data from [" + sock?.remoteSocketAddress + "] -")
                        println(data)
                    }
                    Thread.sleep(100)
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
            sock?.close()
        } catch (ioe: IOException) {
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