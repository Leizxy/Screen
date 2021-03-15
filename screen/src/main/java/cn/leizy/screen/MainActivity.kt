package cn.leizy.screen

import android.media.MediaCodec
import android.media.MediaFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.databinding.DataBindingUtil
import cn.leizy.screen.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var socketLive: SocketLive
    private lateinit var binding: ActivityMainBinding
    private lateinit var surface: Surface
    private lateinit var mediaCodec: MediaCodec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.surface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surface = holder.surface
                initSocket()
                initDecoder(surface)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })
    }

    private fun initDecoder(surface: Surface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 720, 1280)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 720 * 1280)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec.configure(format, surface, null, 0)
            mediaCodec.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSocket() {
        socketLive = SocketLive(this, 11000) {
            Log.i("MainActivity", "initSocket: ${it.contentToString()}")
            dealData(it)
        }
        socketLive.start()
    }

    private fun dealData(data: ByteArray) {
        val index = mediaCodec.dequeueInputBuffer(100000)
        if (index >= 0) {
            val inputBuffer = mediaCodec.getInputBuffer(index)
            inputBuffer?.run {
                clear()
                put(data, 0, data.size)
            }
            mediaCodec.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
        }
        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000)
        while (outputBufferIndex > 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::socketLive.isInitialized) {
            socketLive.close()
        }
    }

}