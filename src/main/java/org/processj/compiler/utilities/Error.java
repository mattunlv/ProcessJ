package org.processj.compiler.utilities;

import java.io.File;

import org.processj.compiler.ast.AST;
import org.processj.compiler.ast.type.ErrorType;
import org.processj.compiler.ast.type.Type;

public class Error {
    public static String fileName = "";
    public static int errorCount = 0;
    public static String errors = "";
    public static String packageName = "";

    public static void setPackageName(String packageName) {
        // First strip the '.pj' part
        String str = packageName.replaceAll("\\.pj$", "");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath, "");
        // replace all '/' with .
        str = str.replaceAll("/", "\\.");
        Error.packageName = str;
    }

    public static void setFileName(String name) {
        // remove all double '//:'
        String str = name.replaceAll("//","/");
        // Now remove the absolute path
        String absPath = new File("").getAbsolutePath() + "/";
        str = str.replaceAll(absPath,"");
        fileName = str;
    }

    public static void error(AST e, String msg) {

        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg);
    }

    public static void error(String msg) {

        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ": " + msg);

    }

    public static void error(AST e, String msg, boolean terminate) {

        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg);

    }

    public static void error(AST e, String msg, boolean terminate, int errorno) {

        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg
                + "\nError number: " + errorno);

            errorCount++;
            errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg;
            errors += "\n" + "Error number: " + errorno;

    }

    public static void warning(AST e, String msg, int errorno) {

        System.out.println(PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg);
        System.out.print("Warning number: " + errorno);

        errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg;
        errors += "\n" + "Warning number: " + errorno;
    }

    public static Type addError(AST e, String msg, int errorno) {

        System.out.println(PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg);
        System.out.println("Error number: " + errorno);
        errorCount++;

        errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e + ": " + msg;
        errors += "\n" + "Error number: " + errorno;
        return new ErrorType();
    }

    public static void error(String msg, boolean terminate) {

        System.out.println(PJBugManager.INSTANCE.getFileName() + ": " + msg);
        if(terminate)
            PJBugManager.ReportMessageAndExit("");

    }
}