package cn.leizy.screen

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

/**
 * @author Created by wulei
 * @date 2021/3/15, 015
 * @description
 */
class SocketLive(
    private val context: Context,
    private val port: Int,
    private val callback: (ByteArray) -> Unit
) {
//    private val URL = "ws://172.16.17.65:"
    private val URL = "ws://172.16.16.80:"
    private lateinit var myWebSocketClient: MyWebSocketClient

    fun start() {
        try {
            val url = URI(URL + port)
            myWebSocketClient = MyWebSocketClient(url)
            myWebSocketClient.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            myWebSocketClient.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private inner class MyWebSocketClient(serverURI: URI) : WebSocketClient(serverURI) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            ToastUtil.show(context as Activity, "Screen Socket onOpen")
        }

        override fun onMessage(message: String?) {
            ToastUtil.show(context as Activity, "Screen Socket onMessage $message")
        }

        override fun onMessage(bytes: ByteBuffer?) {
            bytes?.run {
                val buf = ByteArray(remaining())
                bytes.get(buf)
                callback.invoke(buf)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            ToastUtil.show(context as Activity, "Screen Socket onClose")
        }

        override fun onError(ex: Exception?) {
            ToastUtil.show(context as Activity, "Screen Socket onError")
        }

    }
}