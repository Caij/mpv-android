package `is`.xyz.mpv

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface

// Wrapper for native library

@Suppress("unused")
object MPVLib {
    init {
        val libs = arrayOf("mpv", "player")
        for (lib in libs) {
            System.loadLibrary(lib)
        }
    }

    external fun create(appctx: Context)
    external fun init()
    external fun destroy()
    external fun attachSurface(surface: Surface)
    external fun detachSurface()

    external fun command(cmd: Array<out String>)

    external fun setOptionString(name: String, value: String): Int

    external fun grabThumbnail(dimension: Int): Bitmap?

    external fun getPropertyInt(property: String): Int?
    external fun setPropertyInt(property: String, value: Int)
    external fun getPropertyDouble(property: String): Double?
    external fun setPropertyDouble(property: String, value: Double)
    external fun getPropertyBoolean(property: String): Boolean?
    external fun setPropertyBoolean(property: String, value: Boolean)
    external fun getPropertyString(property: String): String?
    external fun setPropertyString(property: String, value: String)

    external fun observeProperty(property: String, format: Int)

    private val observers = mutableListOf<EventObserver>()

    @JvmStatic
    fun addObserver(o: EventObserver) {
        synchronized(observers) {
            observers.add(o)
        }
    }

    @JvmStatic
    fun removeObserver(o: EventObserver) {
        synchronized(observers) {
            observers.remove(o)
        }
    }

    @JvmStatic
    fun eventProperty(property: String, value: Long) {
        synchronized(observers) {
            for (o in observers)
                o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(property: String, value: Boolean) {
        synchronized(observers) {
            for (o in observers)
                o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(property: String, value: Double) {
        synchronized(observers) {
            for (o in observers)
                o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(property: String, value: String) {
        synchronized(observers) {
            for (o in observers)
                o.eventProperty(property, value)
        }
    }

    @JvmStatic
    fun eventProperty(property: String) {
        synchronized(observers) {
            for (o in observers)
                o.eventProperty(property)
        }
    }

    @JvmStatic
    fun eventNodeProperty(property: String, value: Any) {
          synchronized (observers) {
               for ( o in observers) {
                   o.eventNodeProperty(property, value)
               }
          }
     }

    @JvmStatic
    fun event(eventId: Int) {
        synchronized(observers) {
            for (o in observers)
                o.event(eventId)
        }
    }

    @JvmStatic
    fun endEvent(reason: Int, error: Int) {
        synchronized (observers) {
            for (o in observers) {
                o.endEvent(reason, error)
            }
        }
    }

    private val log_observers = mutableListOf<LogObserver>()

    @JvmStatic
    fun addLogObserver(o: LogObserver) {
        synchronized(log_observers) {
            log_observers.add(o)
        }
    }

    @JvmStatic
    fun removeLogObserver(o: LogObserver) {
        synchronized(log_observers) {
            log_observers.remove(o)
        }
    }

    @JvmStatic
    fun logMessage(prefix: String, level: Int, text: String) {
        synchronized(log_observers) {
            for (o in log_observers)
                o.logMessage(prefix, level, text)
        }
    }

    interface EventObserver {
        fun eventProperty(property: String)
        fun eventProperty(property: String, value: Long)
        fun eventProperty(property: String, value: Boolean)
        fun eventProperty(property: String, value: String)
        fun eventProperty(property: String, value: Double)
        fun eventNodeProperty(property: String, value: Any)
        fun endEvent(reason: Int, error: Int)
        fun event(eventId: Int)
    }

    interface LogObserver {
        fun logMessage(prefix: String, level: Int, text: String)
    }

    object MpvFormat {
        const val MPV_FORMAT_NONE: Int = 0
        const val MPV_FORMAT_STRING: Int = 1
        const val MPV_FORMAT_OSD_STRING: Int = 2
        const val MPV_FORMAT_FLAG: Int = 3
        const val MPV_FORMAT_INT64: Int = 4
        const val MPV_FORMAT_DOUBLE: Int = 5
        const val MPV_FORMAT_NODE: Int = 6
        const val MPV_FORMAT_NODE_ARRAY: Int = 7
        const val MPV_FORMAT_NODE_MAP: Int = 8
        const val MPV_FORMAT_BYTE_ARRAY: Int = 9
    }

    object MpvEvent {
        const val MPV_EVENT_NONE: Int = 0
        const val MPV_EVENT_SHUTDOWN: Int = 1
        const val MPV_EVENT_LOG_MESSAGE: Int = 2
        const val MPV_EVENT_GET_PROPERTY_REPLY: Int = 3
        const val MPV_EVENT_SET_PROPERTY_REPLY: Int = 4
        const val MPV_EVENT_COMMAND_REPLY: Int = 5
        const val MPV_EVENT_START_FILE: Int = 6
        const val MPV_EVENT_END_FILE: Int = 7
        const val MPV_EVENT_FILE_LOADED: Int = 8
        @Deprecated("")
        const val MPV_EVENT_IDLE: Int = 11
        @Deprecated("")
        const val MPV_EVENT_TICK: Int = 14
        const val MPV_EVENT_CLIENT_MESSAGE: Int = 16
        const val MPV_EVENT_VIDEO_RECONFIG: Int = 17
        const val MPV_EVENT_AUDIO_RECONFIG: Int = 18
        const val MPV_EVENT_SEEK: Int = 20
        const val MPV_EVENT_PLAYBACK_RESTART: Int = 21
        const val MPV_EVENT_PROPERTY_CHANGE: Int = 22
        const val MPV_EVENT_QUEUE_OVERFLOW: Int = 24
        const val MPV_EVENT_HOOK: Int = 25
    }

    object MpvLogLevel {
        const val MPV_LOG_LEVEL_NONE: Int = 0
        const val MPV_LOG_LEVEL_FATAL: Int = 10
        const val MPV_LOG_LEVEL_ERROR: Int = 20
        const val MPV_LOG_LEVEL_WARN: Int = 30
        const val MPV_LOG_LEVEL_INFO: Int = 40
        const val MPV_LOG_LEVEL_V: Int = 50
        const val MPV_LOG_LEVEL_DEBUG: Int = 60
        const val MPV_LOG_LEVEL_TRACE: Int = 70
    }

    object MpvEndFileReason {
          /**
           * The end of file was reached. Sometimes this may also happen on
           * incomplete or corrupted files, or if the network connection was
           * interrupted when playing a remote file. It also happens if the
           * playback range was restricted with --end or --frames or similar.
           */
         const val MPV_END_FILE_REASON_EOF = 0;
          /**
           * Playback was stopped by an external action (e.g. playlist controls).
           */
          const val MPV_END_FILE_REASON_STOP = 2;
          /**
           * Playback was stopped by the quit command or player shutdown.
           */
          const val MPV_END_FILE_REASON_QUIT = 3;
          /**
           * Some kind of error happened that lead to playback abort. Does not
           * necessarily happen on incomplete or broken files (in these cases, both
           * MPV_END_FILE_REASON_ERROR or MPV_END_FILE_REASON_EOF are possible).
           *
           * mpv_event_end_file.error will be set.
           */
          const val MPV_END_FILE_REASON_ERROR = 4;
          /**
           * The file was a playlist or similar. When the playlist is read, its
           * entries will be appended to the playlist after the entry of the current
           * file, the entry of the current file is removed, and a MPV_EVENT_END_FILE
           * event is sent with reason set to MPV_END_FILE_REASON_REDIRECT. Then
           * playback continues with the playlist contents.
           * Since API version 1.18.
           */
          const val MPV_END_FILE_REASON_REDIRECT = 5;
    }


     object MpvError {
          const val MPV_ERROR_SUCCESS = 0;         // 没有错误
          const val MPV_ERROR_EVENT_QUEUE_FULL = -1;   // 事件队列满
          const val MPV_ERROR_NOMEM = -2;              // 内存不足
          const val MPV_ERROR_UNSUPPORTED = -3;        // 功能不支持
          const val MPV_ERROR_INVALID_PARAMETER = -4;  // 参数无效
          const val MPV_ERROR_OPTION_NOT_FOUND = -5;   // 找不到选项
          const val MPV_ERROR_OPTION_FORMAT = -6;      // 选项格式错误
          const val MPV_ERROR_OPTION_ERROR = -7;       // 设置选项失败
          const val MPV_ERROR_PROPERTY_NOT_FOUND = -8; // 属性不存在
          const val MPV_ERROR_PROPERTY_FORMAT = -9;    // 属性格式错误
          const val MPV_ERROR_PROPERTY_UNAVAILABLE = -10; // 属性不可用
          const val MPV_ERROR_PROPERTY_ERROR = -11;    // 获取/设置属性失败
          const val MPV_ERROR_COMMAND = -12;           // 命令执行失败
          const val MPV_ERROR_LOADING_FAILED = -13;    // 加载文件失败
          const val MPV_ERROR_AO_INIT_FAILED = -14;    // 音频输出初始化失败
          const val MPV_ERROR_VO_INIT_FAILED = -15;    // 视频输出初始化失败
          const val MPV_ERROR_NOTHING_TO_PLAY = -16;   // 播放列表为空
          const val MPV_ERROR_UNKNOWN_FORMAT = -17;    // 不支持的格式
          const val MPV_ERROR_UNSUPPORTED_FORMAT = -18;// 文件格式不支持
          const val MPV_ERROR_DEMUXER_ERROR = -19;     // 解复用失败
          const val MPV_ERROR_EXIT = -20;           // 用户或脚本请求退出
          const val MPV_ERROR_WAITING = -21;           // 阻塞等待
          const val MPV_ERROR_GENERIC = -22;           // 通用错误
     }
}
