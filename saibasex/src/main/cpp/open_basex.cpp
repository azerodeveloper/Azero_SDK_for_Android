#include <jni.h>
#include <stdio.h>
#include <string>
#include <android/log.h>
#include <inttypes.h>
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <algorithm>
#include <chrono>
#include <fstream>
#include <memory>
#include <sstream>
#include <thread>
#include <vector>
#include <iostream>

#include "sai_micbasex_interface.h"
//#include "basex_jni.h"
using namespace std;

#define LOG_TAG "SoundAI"
#define LOG_D(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOG_E(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

//==============================================================
static JavaVM *jvm;
static JNIEnv *env;
static jobject basex_data_callback;
static jmethodID on_basex_data_mid;
static jmethodID on_basex_start_mid;
static jmethodID on_basex_end_mid;
static int flag = 0;                    // is always record
static void *handle;

int initJinEnv() {
    JavaVMAttachArgs jvmArgs;
    jvmArgs.version = JNI_VERSION_1_6;

    int attachedHere = 0; // know if detaching at the end is necessary
    jint res = jvm->GetEnv((void **) &env,
                           JNI_VERSION_1_6); // checks if current env needs attaching or it is already attached
    if (JNI_EDETACHED == res) {
        // Supported but not attached yet, needs to call AttachCurrentThread
        res = jvm->AttachCurrentThread(&env, NULL);
        if (JNI_OK == res) {
            attachedHere = 1;
        } else {
        }
    } else if (JNI_OK == res) {
    } else {
        // JNI_EVERSION, specified version is not supported cancel this..
    }
    return attachedHere;
}

void invokeRecordStart() {
    if(basex_data_callback) {
        env->CallVoidMethod(basex_data_callback, on_basex_start_mid);
    }
}

void invokeRecordEnd() {
    if(basex_data_callback){
        env->CallVoidMethod(basex_data_callback, on_basex_end_mid);
    }
}

void invokeJavaBasexCallback(int attachedHere, char *data, size_t size) {

    // 回调 Java 的方法，打印当前线程 id ，发现不是主线程就对了
    jbyteArray array = env->NewByteArray(size);
    jbyte *pArray;
    if (array == NULL) {
        LOG_E("receive_callback: NewCharArray error.");
        return;
    }

    //pArray = (jchar*)calloc(size, sizeof(jchar));//开辟jchar类型内存空间  
    pArray = (jbyte *) calloc(size, sizeof(jbyte));
    if (pArray == NULL) {
        LOG_E("receive_callback: calloc error.");
        return;
    }

    //copy buffer to jchar array  
    for (int i = 0; i < size; i++) {
        *(pArray + i) = *(data + i);//复制buf数据元素到pArray内存空间  
    }
    //copy buffer to jcharArray  
    env->SetByteArrayRegion(array, 0, size, pArray);//复制pArray的jchar数据元素到jcharArray  
    //invoke java callback method  
    if(basex_data_callback) {
        env->CallVoidMethod(basex_data_callback, on_basex_data_mid, array, (jint) size);
    }
    //release resource  
    env->DeleteLocalRef(array);
    free(pArray);
    pArray = NULL;

    if (attachedHere) { // Key check
        jvm->DetachCurrentThread(); // Done only when attachment was done here
    }
}


void cstrsplit(char* str,int* res)
{
    const char *delim = ",";
    const char *token = strtok(str, delim);
    if(token)
    {
        int c = 0;
        do
        {
            res[c++] = atoi(token);
        } while ((token = strtok(NULL, delim)));
    }
}

static int vFileWrite(const std::string fname,const char* data,int size)
{
    std::fstream fs(fname.data(),std::fstream::out|std::fstream::app);
    if(fs.good())
    {
        fs.write(data,size);
        fs.close();
        return size;
    }
    else
    {
        return -1;
    }
}

int releaseBasex() {
    flag = 0;
    return 0;
}

int runBasex(int runtime, const char *hw, int mic, int ch, const char *chmap, int bit, int rate,
             int micshift, int refshift, int mode, int peroidsize, int peroidcount_buffersize) {

    LOG_D("[open_basex]runtime: %d\n", runtime);
    LOG_D("[open_basex]hw: %s\n", hw);
    LOG_D("[open_basex]chmap: %s\n", chmap);

//	atoi (表示 ascii to integer)是把字符串转换成整型数的一个函数

    int32_t frame = 256;

    LOG_D("[open_basex]SaiMicBaseX_Init:before\n");
    handle = SaiMicBaseX_Init(ch, mic, frame, hw);
    if (!handle) {
        LOG_E("[open_basex]SaiMicBaseX_Init:error\n");
        return 1;
    }

    SaiMicBaseX_RegisterErrorCb(handle, NULL, [](
            void *usserdata,
            int code,
            const char *msg) {
        LOG_E("[open_basex][error_cb]code:%d msg:%s\n", code, msg);
    });

    LOG_D("[open_basex]SaiMicBaseX_Init:ok\n");

    const char *delim = ",";
    const char *token = strtok(const_cast<char *>(chmap), delim);
    if (token) {
        int ch_map[20];
        int c = 0;
        do {
            LOG_D("[open_basex] token = %s\n", token);
            ch_map[c++] = atoi(token);
        } while ((token = strtok(NULL, delim)));
        SaiMicBaseXSetChannelMap(handle, ch_map, ch);
        LOG_D("[open_basex]SaiMicBaseXSetChannelMap ok!~");
    }

    SaiMicBaseX_SetBit(handle, bit);
    SaiMicBaseX_SetSampleRate(handle, rate);
    SaiMicBaseX_SetMicShiftBits(handle, micshift);
    SaiMicBaseX_SetRefShiftBits(handle, refshift);
    SaiMicBaseX_SetDecodeMode(handle, mode);
    SaiMicBaseX_SetPeroidSize(handle, peroidsize);
    SaiMicBaseX_SetBufferSize(handle, peroidcount_buffersize);

    int attachedHere = initJinEnv();
    invokeRecordStart();

    if (SaiMicBaseX_Start(handle) == 0) {
        LOG_D("[open_basex]start basex success\n");
    } else {
        LOG_E("[open_basex]start basex failed!\n");
        return 1;
    }
    ///////////////////////////////
    int res;
    int16_t *data;

//    FILE *fp = fopen("/sdcard/record_open_basex.pcm", "wb+");
    int loop = runtime;

    if (runtime == -1) {
        flag = 1;
//        loop = 0;
    }

    while (flag || loop-- > 0) {
        if(handle){
            res = SaiMicBaseX_ReadData(handle, &data);
        }
//        LOG_D("[open_basex]loop:%d res:%d\n", loop, res);

        if (res <= 0) {
            LOG_E("[open_basex]SaiMicBaseX_ReadData:error %d\n", res);
            return 1;
        }

        invokeJavaBasexCallback(attachedHere, (char *) data, res * sizeof(int16_t));

//        if (fp) {
//            fwrite((char *) data, sizeof(char), res * sizeof(int16_t), fp);
//            LOG_D("[open_basex]write data to file:record_open_basex.pcm %d",res * sizeof(int16_t));
//        }
    }
//    fclose(fp);

    /////////////////////////
    LOG_D("[open_basex]release before\n");
    SaiMicBaseX_Release(handle);
    LOG_D("[open_basex]release after\n");
    invokeRecordEnd();

    return 0;
}

//==============================================================

//public native int initBaseX(int time, String hw, int micNum, int channelNum, String channelMap,
//                            int bit, int sampleRate, int micshift, int refshift, int decode,
//                            int periodSize, int bufferSize, SaveDataListener listener);

extern "C" JNIEXPORT jint JNICALL Java_com_soundai_basex_lib_BasexJni_initBaseX
        (JNIEnv *env, jobject javaObj, jint runtime, jstring hw, jint micnum, jint chnum,
         jstring chmap, jint bit, jint rate, jint micshift, jint refshift, jint mode,
         jint pSize, jint pCount, jobject callback) {
    LOG_D("[open_basex]Java_com_soundai_basex_lib_BasexJni_initBaseX! ");
//    runBasex(500, "hw:0,1", 6, 8, "7,6,3,4,1,2,5,0", 16, 16000, 16, 16, 0, 512, 4);

    env->GetJavaVM(&jvm);
    basex_data_callback = env->NewGlobalRef(callback);
    if (basex_data_callback != NULL) {
        jclass basex_cb_cls = env->GetObjectClass(basex_data_callback);
        on_basex_data_mid = env->GetMethodID(basex_cb_cls, "onSaveData", "([BI)V");
        on_basex_start_mid = env->GetMethodID(basex_cb_cls, "onRecordStart", "()V");
        on_basex_end_mid = env->GetMethodID(basex_cb_cls, "onRecordEnd", "()V");
    }
    const char *charHw = env->GetStringUTFChars(hw, 0);
    const char *charChmap = env->GetStringUTFChars(chmap, 0);
    runBasex(runtime, charHw, micnum, chnum, charChmap, bit, rate, micshift, refshift, mode, pSize,
             pCount);
    return 0;
}

extern "C" JNIEXPORT jint JNICALL Java_com_soundai_basex_lib_BasexJni_releaseBaseX
        (JNIEnv *env, jobject javaObj) {
    LOG_D("[open_basex]Java_com_soundai_basex_lib_BasexJni_releaseBaseX! ");
    releaseBasex();
    return 0;
}
