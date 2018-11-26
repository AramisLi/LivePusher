package com.lsn.pusher.live.channel

import android.app.Activity
import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceHolder
import com.lsn.pusher.logE
import kotlin.math.abs

/**
 *Created by Aramis
 *Date:2018/11/22
 *Description:
 */
class CameraHelper(val activity: Activity, cameraId: Int, width: Int, height: Int) : SurfaceHolder.Callback,
        Camera.PreviewCallback {
    private var mCameraId = cameraId
    var onChangedSizeListener: ((w: Int, h: Int) -> Unit)? = null
    private var mPreviewCallback: Camera.PreviewCallback? = null
    private var mWidth = width
    private var mHeight = height
    private var mCamera: Camera? = null
    //相机旋转角度
    private var mRotation = 0

    private var buffer: ByteArray? = null
    private var bytes: ByteArray? = null
    private var mSurfaceHolder: SurfaceHolder? = null

    private fun startPreview() {
        logE("startPreview")
        try {
            mCamera = Camera.open(mCameraId)
            mCamera?.apply {
                val parameters = this.parameters
                //设置预览数据格式为NV21（YUV420的一种）
                parameters.previewFormat = ImageFormat.NV21
                setPreviewSize(parameters)
                setPreviewOrientation()
                this.parameters = parameters

                buffer = ByteArray(mWidth * mHeight * 3 / 2)
                bytes = ByteArray(buffer!!.size)
                //数据缓存区
                this.addCallbackBuffer(buffer)
                this.setPreviewCallbackWithBuffer(this@CameraHelper)
                this.setPreviewDisplay(mSurfaceHolder)
                onChangedSizeListener?.invoke(mWidth, mHeight)


                //自动对焦
                this.autoFocus { success, camera ->
                    logE("自动对焦 $success")
                    if (success) {

                        camera.cancelAutoFocus()
                    }
                }
                this.startPreview()
//                this.cancelAutoFocus()

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPreview() {
        mCamera?.setPreviewCallback(null)
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }


    private fun setPreviewOrientation() {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraId, info)
        mRotation = activity.windowManager.defaultDisplay.rotation
        val degrees = when (mRotation) {
            Surface.ROTATION_0 -> {
                onChangedSizeListener?.invoke(mHeight, mWidth)
                0
            }
            Surface.ROTATION_90 -> {
                onChangedSizeListener?.invoke(mWidth, mHeight)
                90
            }
            Surface.ROTATION_270 -> {
                onChangedSizeListener?.invoke(mWidth, mHeight)
                270
            }
            else -> 0
        }


        val result = if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            var a = (info.orientation + degrees) % 360
            a = (360 - a) % 360
            a
        } else {
            (info.orientation - degrees + 360) % 360
        }
        mCamera?.setDisplayOrientation(result)
    }

    /**
     * 获取摄像头可使用的宽高
     */
    private fun setPreviewSize(parameters: Camera.Parameters) {
        //获取摄像头可使用的宽高
        val supportedPreviewSizes = parameters.supportedPreviewSizes
        var size = supportedPreviewSizes[0]
//        logE("支持${size.width}x${size.height}")
        var m = abs(size.height * size.width - mWidth * mHeight)

        supportedPreviewSizes.removeAt(0)
        val iterator = supportedPreviewSizes.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
//            logE("支持${next.width}x${next.height}")
            val n = abs(next.height * next.width - mWidth * mHeight)
            if (n < m) {
                m = n
                size = next
            }
        }

        logE("最符合的宽高 ${size.width}x${size.height}")

        mWidth = size.width
        mHeight = size.height
        parameters.setPreviewSize(mWidth, mHeight)
    }


    fun setPreviewCallback(cb: Camera.PreviewCallback) {
        this.mPreviewCallback = cb
    }

    fun setPreviewDisplay(surfaceHolder: SurfaceHolder) {
        mSurfaceHolder = surfaceHolder
        mSurfaceHolder?.addCallback(this)
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        stopPreview()
        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stopPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        mPreviewCallback?.onPreviewFrame(data, camera)
        camera?.addCallbackBuffer(buffer)
    }

    fun switchCamera() {
        mCameraId = if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        stopPreview()
        startPreview()
    }

}