package org.processj.compiler.utilities;

import org.processj.compiler.ast.AST;

import java.io.PrintStream;

public final class Log {

    private final static PrintStream InfoStream    = System.out    ;
    private final static PrintStream WarningStream = System.out    ;
    private final static PrintStream ErrorStream   = System.err    ;

    public static boolean LogInfo       = true ;
    public static boolean LogWarning    = true ;
    public static boolean LogError      = true ;

    private Log() { /* Empty */ }

    public static void log(final String message) {

        Log.InfoStream.println(message);

    }

    public static void log(AST a, String s) {
        //if ( doLog )
        System.out.println("[info] " + PJBugManager.INSTANCE.getFileName() + ":" + a.line + ": " + s);
    }

    public static void Info(final String message) {

        if(LogInfo && (message != null))
            Log.InfoStream.println("[info] " + message);

    }

    public static void Warn(final String message) {

        if(LogWarning && (message != null))
            Log.WarningStream.println("[warning] " + message);

    }

    public static void Error(final String message) {

        if(LogError && (message != null))
            Log.ErrorStream.println("[error] " + message);

    }

}