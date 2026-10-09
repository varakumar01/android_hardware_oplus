package com.oplus.wrapper.os;

public class Trace {

    public static final long TRACE_TAG_VIEW = android.os.Trace.TRACE_TAG_VIEW;

    public static void traceBegin(long traceTag, String methodName) {
        android.os.Trace.traceBegin(traceTag, methodName);
    }

    public static void traceEnd(long traceTag) {
        android.os.Trace.traceEnd(traceTag);
    }

    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        android.os.Trace.asyncTraceBegin(traceTag, methodName, cookie);
    }

    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        android.os.Trace.asyncTraceEnd(traceTag, methodName, cookie);
    }

    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        android.os.Trace.traceCounter(traceTag, counterName, counterValue);
    }
}
