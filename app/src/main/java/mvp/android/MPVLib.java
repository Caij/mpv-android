package mvp.android;

// Wrapper for native library

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class MPVLib {

     static {
        String[] libs = { "mpv", "player" };
        for (String lib: libs) {
            System.loadLibrary(lib);
        }
     }

     public static native void create(Context appctx);
     public static native void init();
     public static native void destroy();
     public static native void attachSurface(Surface surface);
     public static native void detachSurface();

     public static native void command(@NonNull String[] cmd);

     public static native int setOptionString(@NonNull String name, @NonNull String value);

     public static native Bitmap grabThumbnail(int dimension);

     // FIXME: get methods are actually nullable
     public static native Integer getPropertyInt(@NonNull String property);
     public static native void setPropertyInt(@NonNull String property, @NonNull Integer value);
     public static native Double getPropertyDouble(@NonNull String property);
     public static native void setPropertyDouble(@NonNull String property, @NonNull Double value);
     public static native Boolean getPropertyBoolean(@NonNull String property);
     public static native void setPropertyBoolean(@NonNull String property, @NonNull Boolean value);
     public static native String getPropertyString(@NonNull String property);
     public static native void setPropertyString(@NonNull String property, @NonNull String value);

     public static native void observeProperty(@NonNull String property, int format);

     private static final List<EventObserver> observers = new ArrayList<>();

     public static void addObserver(EventObserver o) {
          synchronized (observers) {observers.add(o); }
     }
     public static void removeObserver(EventObserver o) {
          synchronized (observers) { observers.remove(o); }
     }

     public static void eventProperty(String property, long value) {
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.eventProperty(property, value);
          }
     }

     public static void eventProperty(String property, boolean value) {
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.eventProperty(property, value);
          }
     }

     public static void eventProperty(String property, double value) {
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.eventProperty(property, value);
          }
     }

     public static void eventProperty(String property, String value) {
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.eventProperty(property, value);
          }
     }

     public static void eventProperty(String property) {
          Log.i("MPVLib", "eventProperty(" + property + ")");
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.eventProperty(property);
          }
     }

     public static void event(int eventId) {
          Log.i("MPVLib", "event(" + eventId + ")");
          synchronized (observers) {
               for (EventObserver o : observers)
                    o.event(eventId);
          }
     }

     public static void endEvent(int reason, int error) {
        synchronized (observers) {
             for (EventObserver o : observers)
                  o.endEvent(reason, error);
        }
     }

     private static final List<LogObserver> log_observers = new ArrayList<>();

     public static void addLogObserver(LogObserver o) {
          synchronized (log_observers) { log_observers.add(o); }
     }
     public static void removeLogObserver(LogObserver o) {
          synchronized (log_observers) { log_observers.remove(o); }
     }

     public static void logMessage(String prefix, int level, String text) {
          synchronized (log_observers) {
               for (LogObserver o : log_observers)
                    o.logMessage(prefix, level, text);
          }
     }

     public interface EventObserver {
          void eventProperty(@NonNull String property);
          void eventProperty(@NonNull String property, long value);
          void eventProperty(@NonNull String property, boolean value);
          void eventProperty(@NonNull String property, @NonNull String value);
          void eventProperty(@NonNull String property, double value);
          void event(int eventId);

          void endEvent(int reason, int error);
     }

     public interface LogObserver {
          void logMessage(@NonNull String prefix, int level, @NonNull String text);
     }

     public static class mpvFormat {
          public static final int MPV_FORMAT_NONE=0;
          public static final int MPV_FORMAT_STRING=1;
          public static final int MPV_FORMAT_OSD_STRING=2;
          public static final int MPV_FORMAT_FLAG=3;
          public static final int MPV_FORMAT_INT64=4;
          public static final int MPV_FORMAT_DOUBLE=5;
          public static final int MPV_FORMAT_NODE=6;
          public static final int MPV_FORMAT_NODE_ARRAY=7;
          public static final int MPV_FORMAT_NODE_MAP=8;
          public static final int MPV_FORMAT_BYTE_ARRAY=9;
     }

     public static class mpvEventId {
          public static final int MPV_EVENT_NONE=0;
          public static final int MPV_EVENT_SHUTDOWN=1;
          public static final int MPV_EVENT_LOG_MESSAGE=2;
          public static final int MPV_EVENT_GET_PROPERTY_REPLY=3;
          public static final int MPV_EVENT_SET_PROPERTY_REPLY=4;
          public static final int MPV_EVENT_COMMAND_REPLY=5;
          public static final int MPV_EVENT_START_FILE=6;
          public static final int MPV_EVENT_END_FILE=7;
          public static final int MPV_EVENT_FILE_LOADED=8;
          public static final @Deprecated int MPV_EVENT_IDLE=11;
          public static final @Deprecated int MPV_EVENT_TICK=14;
          public static final int MPV_EVENT_CLIENT_MESSAGE=16;
          public static final int MPV_EVENT_VIDEO_RECONFIG=17;
          public static final int MPV_EVENT_AUDIO_RECONFIG=18;
          public static final int MPV_EVENT_SEEK=20;
          public static final int MPV_EVENT_PLAYBACK_RESTART=21;
          public static final int MPV_EVENT_PROPERTY_CHANGE=22;
          public static final int MPV_EVENT_QUEUE_OVERFLOW=24;
          public static final int MPV_EVENT_HOOK=25;
     }

     public static class mpvLogLevel {
          public static final int MPV_LOG_LEVEL_NONE=0;
          public static final int MPV_LOG_LEVEL_FATAL=10;
          public static final int MPV_LOG_LEVEL_ERROR=20;
          public static final int MPV_LOG_LEVEL_WARN=30;
          public static final int MPV_LOG_LEVEL_INFO=40;
          public static final int MPV_LOG_LEVEL_V=50;
          public static final int MPV_LOG_LEVEL_DEBUG=60;
          public static final int MPV_LOG_LEVEL_TRACE=70;
     }

     public static class MpvEndFileReason {
          /**
           * The end of file was reached. Sometimes this may also happen on
           * incomplete or corrupted files, or if the network connection was
           * interrupted when playing a remote file. It also happens if the
           * playback range was restricted with --end or --frames or similar.
           */
          public static final int MPV_END_FILE_REASON_EOF = 0;
          /**
           * Playback was stopped by an external action (e.g. playlist controls).
           */
          public static final int MPV_END_FILE_REASON_STOP = 2;
          /**
           * Playback was stopped by the quit command or player shutdown.
           */
          public static final int MPV_END_FILE_REASON_QUIT = 3;
          /**
           * Some kind of error happened that lead to playback abort. Does not
           * necessarily happen on incomplete or broken files (in these cases, both
           * MPV_END_FILE_REASON_ERROR or MPV_END_FILE_REASON_EOF are possible).
           *
           * mpv_event_end_file.error will be set.
           */
          public static final int MPV_END_FILE_REASON_ERROR = 4;
          /**
           * The file was a playlist or similar. When the playlist is read, its
           * entries will be appended to the playlist after the entry of the current
           * file, the entry of the current file is removed, and a MPV_EVENT_END_FILE
           * event is sent with reason set to MPV_END_FILE_REASON_REDIRECT. Then
           * playback continues with the playlist contents.
           * Since API version 1.18.
           */
          public static final int MPV_END_FILE_REASON_REDIRECT = 5;
     }
}
