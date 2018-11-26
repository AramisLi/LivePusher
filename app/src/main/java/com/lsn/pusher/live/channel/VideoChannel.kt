package com.lsn.pusher.live.channel

import android.app.Activity
import android.hardware.Camera
import android.view.SurfaceHolder
import com.lsn.pusher.live.LivePusher
import com.lsn.pusher.logE

/**
 *Created by Aramis
 *Date:2018/11/22
 *Description:
 */
class VideoChannel(
        private val livePusher: LivePusher, activity: Activity, width: Int, height: Int, bitrate: Int,
        fps: Int, cameraId: Int
) : Camera.PreviewCallback {

    private var isLiving = false
    private val cameraHelper = CameraHelper(activity, cameraId, width, height)

    init {
        cameraHelper.setPreviewCallback(this)
        cameraHelper.onChangedSizeListener = { w, h ->
            livePusher.setVideoEncInfo(w, h, fps, bitrate)
        }
    }

    fun setPreviewDisplay(surfaceHolder: SurfaceHolder) {
        cameraHelper.setPreviewDisplay(surfaceHolder)
    }

    fun switchCamera() {
        cameraHelper.switchCamera()
    }

    fun startLive() {
        isLiving = true
    }

    fun stopLive() {
        isLiving = false
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (data == null) {
            logE("相机数据为空")
            return
        }
        //获得到了相机的数据
        if (isLiving) {
            livePusher.native_pushVideo(data)
        }
    }


}