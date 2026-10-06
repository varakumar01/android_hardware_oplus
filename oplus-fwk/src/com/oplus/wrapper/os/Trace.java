package com.oplus.wrapper.os;

public class Trace {

    public static void traceBegin(long traceTag, String methodName) {
        android.os.Trace.traceBegin(traceTag, methodName);
    }

    public static void traceEnd(long traceTag) {
        android.os.Trace.traceEnd(traceTag);
    }
}
