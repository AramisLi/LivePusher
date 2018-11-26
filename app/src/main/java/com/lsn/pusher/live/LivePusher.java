package com.lsn.pusher.live;

import android.app.Activity;
import android.view.SurfaceHolder;

import com.lsn.pusher.live.channel.AudioChannel;
import com.lsn.pusher.live.channel.VideoChannel;

/**
 * Created by Aramis
 * Date:2018/11/22
 * Description:
 */
public class LivePusher {
    static {
        System.loadLibrary("native-lib");
    }

    private AudioChannel audioChannel;
    private VideoChannel videoChannel;

    public LivePusher(Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        native_init();
        audioChannel = new AudioChannel();
        videoChannel = new VideoChannel(this, activity, width, height, bitrate, fps, cameraId);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        videoChannel.setPreviewDisplay(surfaceHolder);
    }

    public void switchCamera() {
        videoChannel.switchCamera();
    }

    public void startLive(String path) {
        native_start(path);
        audioChannel.startLive();
        videoChannel.startLive();
    }

    public void stopLive() {
        videoChannel.stopLive();
        audioChannel.stopLive();
        native_stop();
    }

    private native void native_init();

    private native void native_start(String path);

    private native void native_stop();

    //初始化编码器
    public native void setVideoEncInfo(int width, int height, int fps, int bitrate);

    //向native推送相机的数据
    public native void native_pushVideo(byte[] data);
}
