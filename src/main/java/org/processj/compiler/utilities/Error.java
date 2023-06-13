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
//        System.out.println(fileName + ":" + e.line + ": " + msg);
        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg);
    }

    public static void error(String msg) {
//        System.out.println(fileName + ": " + msg);
        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ": " + msg);

    }

    public static void error(AST e, String msg, boolean terminate) {
//        System.out.println(fileName + ":" + e.line + ": " + msg);
        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg);

    }

    public static void error(AST e, String msg, boolean terminate, int errorno) {

        //if(terminate)
        PJBugManager.ReportMessageAndExit(PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg
                + "\nError number: " + errorno);

            errorCount++;
//            errors += "\n" + fileName + ":" + e.line + ": " + msg;
            errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg;
            errors += "\n" + "Error number: " + errorno;

    }

    public static void warning(AST e, String msg, int errorno) {
//        System.out.print(fileName + ":" + e.line + ": " + msg);
        System.out.println(PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg);
        System.out.print("Warning number: " + errorno);
//        errors += "\n" + fileName + ":" + e.line + ": " + msg;
        errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg;
        errors += "\n" + "Warning number: " + errorno;
    }

    public static Type addError(AST e, String msg, int errorno) {
//        System.out.print(fileName + ":" + e.line + ": " + msg);
        System.out.println(PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg);
        System.out.println("Error number: " + errorno);
        errorCount++;
//        errors += "\n" + fileName + ":" + e.line + ": " + msg;
        errors += "\n" + PJBugManager.INSTANCE.getFileName() + ":" + e.line + ": " + msg;
        errors += "\n" + "Error number: " + errorno;
        return new ErrorType();
    }

    public static void error(String msg, boolean terminate) {
//        System.out.println(fileName + ": " + msg);
        System.out.println(PJBugManager.INSTANCE.getFileName() + ": " + msg);
        if(terminate)
            PJBugManager.ReportMessageAndExit("");

    }
}