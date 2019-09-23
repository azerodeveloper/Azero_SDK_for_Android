//
// Created by luantianxiang on 10/24/18.
//

#ifndef SAI_UTIL_SAI_MICBASEX_INTERFACE_H
#define SAI_UTIL_SAI_MICBASEX_INTERFACE_H

#ifndef SAI_API_EXPORT
# if defined(_MSC_VER)
#  if defined(DLL_EXPORT)
#   define SAI_API_EXPORT extern "C" __declspec(dllexport)
#  else
#   define SAI_API_EXPORT
#  endif
# elif defined(__GNUC__)
#  define SAI_API_EXPORT extern "C" __attribute__ ((visibility ("default")))
# else
#  define SAI_API_EXPORT
# endif
#endif

SAI_API_EXPORT void *SaiMicBaseX_Init(int16_t ch, int16_t mic, int32_t frame, const char *hw);

SAI_API_EXPORT int32_t SaiMicBaseX_Start(void *handle);

SAI_API_EXPORT int32_t SaiMicBaseX_ReadData(void *handle, int16_t** data);

SAI_API_EXPORT void SaiMicBaseX_Reset(void *handle);

SAI_API_EXPORT void SaiMicBaseX_Release(void *handle);

SAI_API_EXPORT void SaiMicBaseX_SetSampleRate(void *handle, int sample_rate);

SAI_API_EXPORT void SaiMicBaseX_SetBit(void *handle, int bit);

SAI_API_EXPORT void SaiMicBaseXSetChannelMap(void *handle, const int * true_ch, int num_ch);

SAI_API_EXPORT void SaiMicBaseX_SetMicShiftBits(void *handle, int bits);

SAI_API_EXPORT void SaiMicBaseX_SetRefShiftBits(void *handle, int bits);

typedef void (*error_cb_func)(void *usrdata,int num,const char *msg);
SAI_API_EXPORT void SaiMicBaseX_RegisterErrorCb(void *handle,void *usrdata,error_cb_func error_cb);

SAI_API_EXPORT void SaiMicBaseX_SetDecodeMode(void *handle,int mode);

SAI_API_EXPORT void SaiMicBaseX_SetBufferSize(void *handle, int buf_size);

SAI_API_EXPORT void SaiMicBaseX_SetPeroidSize(void *handle, int period_size);

SAI_API_EXPORT const char* SaiMicBaseX_GetVersion();

SAI_API_EXPORT void SaiMicBaseX_SetDelayChannel(void *handle,const int* delay_channel,const int* delay_len,int size);

#endif //SAI_UTIL_SAI_MICBASEX_INTERFACE_H