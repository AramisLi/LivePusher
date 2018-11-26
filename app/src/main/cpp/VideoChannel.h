//
// Created by 李志丹 on 2018/11/22.
//

#ifndef PUSHER_VIDEOCHANNEL_H
#define PUSHER_VIDEOCHANNEL_H

#include <inttypes.h>
#include <x264.h>
#include <pthread.h>
#include "librtmp/rtmp.h"
#include "macro.h"

class VideoChannel {
    typedef void (*VideoCallback)(RTMPPacket *packet);

public:
    VideoChannel();

    ~VideoChannel();

    void setVideoCallback(VideoCallback callback);

    //创建x264编码器
    void setVideoEncInfo(int width, int height, int fps, int bitrate);

    void encodeData(int8_t *data);

private:
    pthread_mutex_t mutex;

    int mWidth;
    int mHeight;
    int mFps;
    int mBitrate;

    x264_t *videoCodec=0;
    x264_picture_t *pic_in = 0;
    int ySize;
    int uvSize;
    VideoCallback videoCallback = 0;
    int index=0;

    void sendSpsPps(uint8_t *sps, uint8_t *pps, int sps_len, int pps_len);

    void sendFrame(int type, int i_payload, uint8_t *p_payload);
};


#endif //PUSHER_VIDEOCHANNEL_H
