
#ifndef _ANDROID_LOG_PRINT_H_
#define _ANDROID_LOG_PRINT_H_

#include <android/log.h>


#define LOG_TAG ("KWS")

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO   , LOG_TAG, __VA_ARGS__))

#define KWS_ERROR(info) LOGI("%s at : %s: %d: %s", info, __FILE__, __LINE__, __func__)

#endif
