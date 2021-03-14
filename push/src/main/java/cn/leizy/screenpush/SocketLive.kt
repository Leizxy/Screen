package cn.leizy.screenpush

import android.media.projection.MediaProjection
import android.util.Log
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
class SocketLive(private val port: Int) {
    private lateinit var webSocket: WebSocket

    private val webSocketServer: WebSocketServer =
        object : WebSocketServer(InetSocketAddress(13001)) {
            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                webSocket = conn!!
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                Log.i("SocketLive", "onClose: ")
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
            }

            override fun onError(conn: WebSocket?, ex: Exception?) {
                Log.i("SocketLive", "onError: ")
            }

            override fun onStart() {
            }

        }

    fun start(mediaProjection: MediaProjection) {
        webSocketServer.start()
    }

    fun close() {
        try {
            webSocket.close()
            webSocketServer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}