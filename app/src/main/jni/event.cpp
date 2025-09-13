#include <jni.h>

#include <mpv/client.h>

#include "globals.h"
#include "jni_utils.h"
#include "log.h"

// 辅助函数，递归地将 mpv_node 转换为 Java 对象
static jobject convert_node_to_java_object(JNIEnv *env, mpv_node *node) {
    if (!node) return NULL;

    switch (node->format) {
        case MPV_FORMAT_STRING:
            return env->NewStringUTF(node->u.string);
        case MPV_FORMAT_FLAG: {
            jclass booleanClass = env->FindClass("java/lang/Boolean");
            jmethodID method = env->GetStaticMethodID(booleanClass, "valueOf", "(Z)Ljava/lang/Boolean;");
            return env->CallStaticObjectMethod(booleanClass, method, (jboolean)(node->u.flag != 0));
        }
        case MPV_FORMAT_INT64: {
            jclass longClass = env->FindClass("java/lang/Long");
            jmethodID method = env->GetStaticMethodID(longClass, "valueOf", "(J)Ljava/lang/Long;");
            return env->CallStaticObjectMethod(longClass, method, (jlong)node->u.int64);
        }
        case MPV_FORMAT_DOUBLE: {
            jclass doubleClass = env->FindClass("java/lang/Double");
            jmethodID method = env->GetStaticMethodID(doubleClass, "valueOf", "(D)Ljava/lang/Double;");
            return env->CallStaticObjectMethod(doubleClass, method, (jdouble)node->u.double_);
        }
        case MPV_FORMAT_NODE_MAP: {
            jclass mapClass = env->FindClass("java/util/HashMap");
            jmethodID mapConstructor = env->GetMethodID(mapClass, "<init>", "()V");
            jobject mapObj = env->NewObject(mapClass, mapConstructor);
            jmethodID putMethod = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

            mpv_node_list *list = node->u.list;
            for (int i = 0; i < list->num; i++) {
                jstring key = env->NewStringUTF(list->keys[i]);
                jobject value = convert_node_to_java_object(env, &list->values[i]);
                if (value) {
                    env->CallObjectMethod(mapObj, putMethod, key, value);
                    env->DeleteLocalRef(value);
                }
                env->DeleteLocalRef(key);
            }
            return mapObj;
        }
        case MPV_FORMAT_NODE_ARRAY: {
            jclass listClass = env->FindClass("java/util/ArrayList");
            jmethodID listConstructor = env->GetMethodID(listClass, "<init>", "()V");
            jobject listObj = env->NewObject(listClass, listConstructor);
            jmethodID addMethod = env->GetMethodID(listClass, "add", "(Ljava/lang/Object;)Z");

            mpv_node_list *list = node->u.list;
            for (int i = 0; i < list->num; i++) {
                jobject element = convert_node_to_java_object(env, &list->values[i]);
                if (element) {
                    env->CallBooleanMethod(listObj, addMethod, element);
                    env->DeleteLocalRef(element);
                }
            }
            return listObj;
        }
        case MPV_FORMAT_BYTE_ARRAY: {
            mpv_byte_array *b = node->u.ba;
            if (!b) return NULL;
            jbyteArray byteArray = env->NewByteArray(b->size);
            env->SetByteArrayRegion(byteArray, 0, b->size, (const jbyte*)b->data);
            return byteArray;
        }
        default:
            return NULL;
    }
}

static void sendPropertyUpdateToJava(JNIEnv *env, mpv_event_property *prop)
{
    jstring jprop = env->NewStringUTF(prop->name);
    jstring jvalue = NULL;
    switch (prop->format) {
    case MPV_FORMAT_NONE:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_S, jprop);
        break;
    case MPV_FORMAT_FLAG:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sb, jprop,
            (jboolean) (*(int*)prop->data != 0));
        break;
    case MPV_FORMAT_INT64:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sl, jprop,
            (jlong) *(int64_t*)prop->data);
        break;
    case MPV_FORMAT_DOUBLE:
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_Sd, jprop,
            (jdouble) *(double*)prop->data);
        break;
    case MPV_FORMAT_STRING:
        jvalue = env->NewStringUTF(*(const char**)prop->data);
        env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_SS, jprop, jvalue);
        break;
    case MPV_FORMAT_NODE:{
        mpv_node *node = *(mpv_node**)prop->data;
        jobject jobj = convert_node_to_java_object(env, node);
        if (jobj) {
            env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_eventProperty_SN, jprop, jobj);
            env->DeleteLocalRef(jobj);
        } else {
            ALOGV("sendPropertyUpdateToJava: MPV_FORMAT_NODE value null: %s %d !", jprop, node->format);
        }
        break;
    }
    default:
        ALOGV("sendPropertyUpdateToJava: Unknown property update format received in callback: %d!", prop->format);
        break;
    }
    if (jprop)
        env->DeleteLocalRef(jprop);
    if (jvalue)
        env->DeleteLocalRef(jvalue);
}


static void sendEventToJava(JNIEnv *env, int event)
{
    env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_event, event);
}

static void sendEndEventToJava(JNIEnv *env, int reason, int error)
{
    env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_endEvent, reason, error);
}

static void sendLogMessageToJava(JNIEnv *env, mpv_event_log_message *msg)
{
    // filter the most obvious cases of invalid utf-8, since Java would choke on it
    const auto invalid_utf8 = [] (unsigned char c) {
        return c == 0xc0 || c == 0xc1 || c >= 0xf5;
    };
    for (int i = 0; msg->text[i]; i++) {
        if (invalid_utf8(static_cast<unsigned char>(msg->text[i])))
            return;
    }

    jstring jprefix = env->NewStringUTF(msg->prefix);
    jstring jtext = env->NewStringUTF(msg->text);

    env->CallStaticVoidMethod(mpv_MPVLib, mpv_MPVLib_logMessage_SiS,
        jprefix, (jint) msg->log_level, jtext);

    if (jprefix)
        env->DeleteLocalRef(jprefix);
    if (jtext)
        env->DeleteLocalRef(jtext);
}

void *event_thread(void *arg)
{
    JNIEnv *env = NULL;
    acquire_jni_env(g_vm, &env);
    if (!env)
        die("failed to acquire java env");

    while (1) {
        mpv_event *mp_event;
        mpv_event_property *mp_property = NULL;
        mpv_event_log_message *msg = NULL;
        mpv_event_end_file *info = NULL;

        mp_event = mpv_wait_event(g_mpv, -1.0);

        if (g_event_thread_request_exit)
            break;

        if (mp_event->event_id == MPV_EVENT_NONE)
            continue;

        switch (mp_event->event_id) {
            case MPV_EVENT_LOG_MESSAGE:
                msg = (mpv_event_log_message*)mp_event->data;
                ALOGV("[%s:%s] %s", msg->prefix, msg->level, msg->text);
                sendLogMessageToJava(env, msg);
                break;
            case MPV_EVENT_PROPERTY_CHANGE:
                mp_property = (mpv_event_property*)mp_event->data;
                sendPropertyUpdateToJava(env, mp_property);
                break;
            case MPV_EVENT_END_FILE:
                info = (mpv_event_end_file*)mp_event->data;
                sendEndEventToJava(env, info->reason, info->error);
                break;
            default:
                ALOGV("event: %s %d\n", mpv_event_name(mp_event->event_id), mp_event->error);
                sendEventToJava(env, mp_event->event_id);
            break;
        }
    }

    g_vm->DetachCurrentThread();

    return NULL;
}
