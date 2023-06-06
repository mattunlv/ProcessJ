package org.processj.compiler.utilities;

import org.processj.compiler.ast.AST;

import java.io.PrintStream;

public class Log {

    private final static PrintStream InfoStream    = System.out    ;
    private final static PrintStream WarningStream = System.out    ;
    private final static PrintStream ErrorStream   = System.err    ;

    public static boolean doLog         = true ;
    public static boolean LogInfo       = true ;
    public static boolean LogWarning    = true ;
    public static boolean LogError      = true ;

    public static void startLogging() {
        doLog = true;
    }

    public static void stopLogging() {
        doLog = false;
    }

    private Log() {
        // Avoid creating instances
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

    public static void log(String s) {
        //if ( doLog )
            System.out.println("[info] " + PJBugManager.INSTANCE.getFileName() + ": " + s);
    }

    public static void log(AST a, String s) {
        //if ( doLog )
            System.out.println("[info] " + PJBugManager.INSTANCE.getFileName() + ":" + a.line + ": " + s);
    }
    
    public static void logHeader(String s) {
        //if ( doLog )
            System.out.println("[info] " + s);
    }

    public static void logNoNewline(String s) {
        //if ( doLog )
            System.out.print("[info] " + PJBugManager.INSTANCE.getFileName() + ": " + s);
    }

    public static void logNoNewline(AST a, String s) {
        //if ( doLog )
            System.out.print("[info] " + PJBugManager.INSTANCE.getFileName() + ":" + a.line + ": " + s);
    }
}