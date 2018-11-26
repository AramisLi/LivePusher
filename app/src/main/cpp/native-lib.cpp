#include <jni.h>
#include <inttypes.h>
#include <string>
#include "safe_queue.h"
#include "librtmp/rtmp.h"
#include "macro.h"
#include "VideoChannel.h"

SafeQueue<RTMPPacket *> packets;
VideoChannel *videoChannel = NULL;
int isStart = 0;
pthread_t pid;
//准备好推流标记位
int readyPushing = 0;
//开始时间
uint32_t startTime;

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_lsn_pusher_MainActivity_stringFromJNI(
//        JNIEnv *env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

void releasePacket(RTMPPacket *&packet) {
    if (packet) {
        RTMPPacket_Free(packet);
        DELETE(packet);
        packet = 0;
    }
}

void videoCallback(RTMPPacket *packet) {
    if (packet) {
        //设置时间戳
        packet->m_nTimeStamp = RTMP_GetTime() - startTime;
        packets.push(packet);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lsn_pusher_live_LivePusher_native_1init(JNIEnv *env, jobject instance) {
    //准备一个Video编码器的工具类，进行编码
    videoChannel = new VideoChannel;
    videoChannel->setVideoCallback(videoCallback);
    //准备一个线程安全的队列，打包好的数据放入队列，在线程中同意的取出数据再发送给服务器
    packets.setReleaseCallback(releasePacket);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_lsn_pusher_live_LivePusher_native_1stop(JNIEnv *env, jobject instance) {


}

void *start(void *args) {
    char *url = static_cast<char *>(args);
    RTMP *rtmp = 0;
    do {
        //创建rtmp对象
        rtmp = RTMP_Alloc();
        if (!rtmp) {
            LOGE("alloc rtmp失败");
            break;
        }
        RTMP_Init(rtmp);
        int ret = RTMP_SetupURL(rtmp, url);
        if (!ret) {
            LOGE("设置地址失败 %s", url);
            break;
        }
        //设置超时时间
        rtmp->Link.timeout = 5;
        RTMP_EnableWrite(rtmp);
        //连接，第二个参数可以传一个packet测试
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            LOGE("连接服务器失败 %s", url);
            break;
        }
        //创建流
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            LOGE("创建流失败 %s", url);
            break;
        }
        //记录开始时间
        startTime = RTMP_GetTime();
        //表示可以开始推流了
        readyPushing = 1;
        packets.setWork(1);

        RTMPPacket *packet = 0;
        while (isStart) {
            packets.pop(packet);
            if (!isStart) {
                break;
            }
            if (!packet) {
                continue;
            }
            //必须要设置
            packet->m_nInfoField2 = rtmp->m_stream_id;
            //发送数据 3:表示队列
            ret = RTMP_SendPacket(rtmp, packet, 1);
            releasePacket(packet);
            if (!ret) {
                LOGE("发送数据失败");
                break;
            }
        }
        releasePacket(packet);
    } while (0);

    //释放
    packets.setWork(0);
    packets.clear();
    if (rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }

    delete url;
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lsn_pusher_live_LivePusher_native_1start(JNIEnv *env, jobject instance, jstring path_) {
    if (isStart) {
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    char *url = new char[strlen(path) + 1];
    strcpy(url, path);
    isStart = 1;
    pthread_create(&pid, 0, start, url);
    env->ReleaseStringUTFChars(path_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lsn_pusher_live_LivePusher_setVideoEncInfo(JNIEnv *env, jobject instance, jint width, jint height, jint fps,
                                                    jint bitrate) {

    LOGE("setVideoEncInfo width:%d,height:%d,fps:%d,bitrate:%d",width,height,fps,bitrate);

    if (videoChannel) {
        videoChannel->setVideoEncInfo(width, height, fps, bitrate);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_lsn_pusher_live_LivePusher_native_1pushVideo(JNIEnv *env, jobject instance, jbyteArray data_) {
    if (!videoChannel || !readyPushing) {
        return;
    }

    jbyte *data = env->GetByteArrayElements(data_, NULL);
    videoChannel->encodeData(data);
    env->ReleaseByteArrayElements(data_, data, 0);
}