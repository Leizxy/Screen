package cn.leizy.screenpush

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjection
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

/**
 * @author wulei
 * @date 3/14/21
 * @description
 */
class SocketLive(private val context: Context, private val port: Int) {
    private lateinit var webSocket: WebSocket

    private val webSocketServer: WebSocketServer =
        object : WebSocketServer(InetSocketAddress(port)) {
            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                ToastUtil.show(context as Activity, "Socket onOpen")
                webSocket = conn!!
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                ToastUtil.show(context as Activity, "Socket onClose")
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
                ToastUtil.show(context as Activity, "Socket onMessage")
            }

            override fun onError(conn: WebSocket?, ex: Exception?) {
                ToastUtil.show(context as Activity, "Socket onError $ex")
            }

            override fun onStart() {
                ToastUtil.show(context as Activity, "Socket onStart")
            }

        }

    fun start(mediaProjection: MediaProjection) {
        webSocketServer.start()
        val codecLiveH265 = CodecLiveH265(this, mediaProjection)
        codecLiveH265.startLive()
    }

    fun close() {
        try {
            webSocket.close()
            webSocketServer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendData(data: ByteArray) {
        if (this::webSocket.isInitialized && webSocket.isOpen) {
            webSocket.send(data)
        }
    }
}