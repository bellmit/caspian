package com.emc.caspian.ccs.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by gulavb on 4/24/2015.
 */
public class ExceptionHelper {

    public static String printException(Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        String error = String.format("Exception: %s%s%s%s%s", e.getClass(), CRLF, e.getMessage(), CRLF,
                stackTrace.toString());
        return error;
    }

    public static String printExceptionCause(Exception e) {
        String error = String.format("Exception class=%s, message=%s", e.getClass(), e.getMessage());
        Throwable cause = e.getCause();
        while (cause != null) {
            error += String.format("%sCause=%s, message=%s", CRLF, cause.getClass(), cause.getMessage());
            cause = cause.getCause();
        }
        return error;
    }

    private static String CRLF = "\r\n";
}
