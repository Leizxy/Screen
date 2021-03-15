package cn.leizy.screenpush

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Environment
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.and
import kotlin.text.*

/**
 * @author Created by wulei
 * @date 2021/3/15, 015
 * @description
 */
class CodecLiveH265(
    private val socketLive: SocketLive,
    private val mediaProjection: MediaProjection
) : Thread() {
    private val HEX_CHAR_TABLE: CharArray =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    private lateinit var mediaCodec: MediaCodec
    private lateinit var virtualDisplay: VirtualDisplay
    private val width = 720
    private val height = 1280

    fun startLive() {
        try {
            val format =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec = MediaCodec.createEncoderByType("video/hevc")
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val surface = mediaCodec.createInputSurface()
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "-display",
                width,
                height,
                1,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface,
                null,
                null
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
        start()
    }

    override fun run() {
        if (this::mediaCodec.isInitialized) {
            mediaCodec.start()
            val bufferInfo = MediaCodec.BufferInfo()
            while (true) {
                try {
                    val outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferId >= 0) {
                        val byteBuffer = mediaCodec.getOutputBuffer(outputBufferId)!!
//                        val outData = ByteArray(bufferInfo.size)
//                        byteBuffer.get(outData)
//                        writeContent(outData)
//                        writeBytes(outData)
                        dealFrame(byteBuffer, bufferInfo)
                        mediaCodec.releaseOutputBuffer(outputBufferId, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val NAL_VPS: Int = 32
    private val NAL_I: Int = 19
    private var vps_sps_pps_buf: ByteArray? = null

    private fun dealFrame(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        var offset = 4
        if (byteBuffer.get(2).toInt() == 0x01) {
            offset = 3
        }
        val type = ((byteBuffer.get(offset) and 0x7e).toInt() ushr 1)
        Log.i("CodecLiveH265", "dealFrame: $type")
        if (type == NAL_VPS) {
            vps_sps_pps_buf = ByteArray(bufferInfo.size)
            byteBuffer.get(vps_sps_pps_buf)
        } else if (type == NAL_I) {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            val newBuf = ByteArray(vps_sps_pps_buf!!.size + bytes.size)
            System.arraycopy(vps_sps_pps_buf, 0, newBuf, 0, vps_sps_pps_buf!!.size)
            System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf!!.size, bytes.size)
            socketLive.sendData(newBuf)
        } else {
            val bytes = ByteArray(bufferInfo.size)
            byteBuffer.get(bytes)
            socketLive.sendData(bytes)
        }
    }

    private fun writeBytes(array: ByteArray) {
        var writer: FileOutputStream? = null
        try {
            writer =
                FileOutputStream("${Environment.getExternalStorageDirectory()}/codec.h265", true)
            writer.write(array)
            writer.write('\n'.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                writer?.run {
                    close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun writeContent(array: ByteArray) {
        val sb = StringBuilder()
        array.forEach {
            val format = String.format("%02X", it)
//            Log.i("CodecLiveH265", "writeContent: byte $format")
//            Log.i("CodecLiveH265", "writeContent: ${(it and 0xF0.toByte()).toInt()}")
//            Log.i("CodecLiveH265", "writeContent: ${(it and 0xF0.toByte()).toInt() ushr 4}")
//            sb.append(HEX_CHAR_TABLE[(it and 0xF0.toByte()).toInt() shr 4])
//            sb.append(HEX_CHAR_TABLE[(it and 0X0F.toByte()).toInt()])
            sb.append(format)
        }
        Log.i("CodecLiveH265", "writeContent: $sb")
    }
}