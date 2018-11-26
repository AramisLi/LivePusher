package com.lsn.pusher

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.lsn.pusher.live.LivePusher
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    private  var livePusher: LivePusher?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        button_start.setOnClickListener {
            toast("开始直播了")
            livePusher?.startLive("rtmp://14.192.48.27/myapp/lalala")
        }

        button_stop.setOnClickListener {
            toast("停止直播")
            livePusher?.stopLive()
        }

        button_switch.setOnClickListener {
            livePusher?.switchCamera()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 102)
        }else{
            start()
        }


    }

    private fun start(){
        livePusher = LivePusher(this, 480, 800, 800_000, 10, Camera.CameraInfo.CAMERA_FACING_BACK)
        livePusher?.setPreviewDisplay(surfaceView.holder)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        livePusher?.stopLive()
    }



}
