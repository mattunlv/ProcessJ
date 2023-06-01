package org.processj.namechecker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;

import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.ast.Import;
import org.processj.ast.Name;
import org.processj.ast.Sequence;
import org.processj.parser.Parser;
import org.processj.lexer.Lexer;
import org.processj.utilities.PJMessage;
import org.processj.utilities.Log;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;
import org.processj.utilities.Settings;

import static org.processj.utilities.Files.IsDirectory;
import static org.processj.utilities.Files.IsFile;

public class ResolveImports<T extends AST> extends Visitor<T> {

    public static String currentFileName = PJBugManager.INSTANCE.getFileName();
    private SymbolTable currentScope = null;

    public ResolveImports(SymbolTable currentScope) {
        this.currentScope = currentScope;
        Log.logHeader("****************************************");
        Log.logHeader("*    R E S O L V E   I M P O R T S     *");
        Log.logHeader("****************************************");
        Log.logHeader("> File: " + PJBugManager.INSTANCE.getFileName());
    }

    public static String packageNameToString(Sequence<Name> packageName) {
        StringBuilder sb = new StringBuilder();
        Iterator<Name> it = packageName.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(".");
            }
        }

        return sb.toString();
    }

    /**
     * Imports (by scanning, parsing, and building a tree) one file, checks
     * its path format, and then validates the extension (path) of all the
     * *import* statements found in the given file.
     *
     * @param a
     *          An AST node - just used for line number information.
     * @param fileName
     *          The name of the file being imported.
     * @return Returns a Compilation representing the imported file.
     */
    public static Compilation importFile(AST a, String fileName) {
        Log.log(a.line + " Attempting to import: " + fileName);
        Compilation c = TopLevelDecls.alreadyImportedFiles.get(fileName);
        if (c != null) {
            Log.log(a.line + " Import of '" + fileName + "' already done before!");
            return c;
        }
        try {
            // Set the package name
            PJBugManager.INSTANCE.setPackageName(fileName);

            Log.log(a.line + " Starting import of file: '" + fileName + "'");
            Lexer s1 = new Lexer(new java.io.FileReader(fileName));
            Parser p1 = new Parser(s1);
            java_cup.runtime.Symbol r = p1.parse();

            TopLevelDecls.alreadyImportedFiles.put(fileName, (Compilation) r.value);
            return (Compilation) r.value;
        } catch (java.io.FileNotFoundException e) {
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(a)
                    .addError(VisitorMessageNumber.RESOLVE_IMPORTS_102)
                    .addArguments(fileName)
                    .build());
        } catch (Exception e) {
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(a)
                    .addError(VisitorMessageNumber.RESOLVE_IMPORTS_106)
                    .addArguments(fileName)
                    .build());
        }
        return null;
    }

    /**
     * Static class used for filtering files in imports (only the ones ending in
     * the proper extension will be considered) PJFiles takes a directory and a
     * filename and determines if it should be imported - importFileExtension is
     * ".pj" by default. This is used for importing files in an import statement
     * ending in *.
     */
    static class PJfiles implements FilenameFilter {
        public boolean accept(File dir, String name) {
            String[] result = name.split("\\.");
            return result[result.length - 1]
                    .equals(Settings.IMPORT_FILE_EXTENSSION);
        }
    }

    /**
     * Given a directory, makeFileList creates an array list of Strings representing
     * the absolute paths of all the files in the directory and its sub-directories
     * that satisfy the filter in the PJFiles class.
     *
     * @param list
     *            After execution list will contain the list of file names in the
     *            directory given by the 'directory' parameter.
     * @param directory
     *            The name of the directory from which to import files.
     */
    public static void makeFileList(ArrayList<String> list, String directory) {
        Log.log("makeFileList(): Called with : " + directory);
        // 'entries' will contain all the files in the directory 'directory'
        // that has the right file extension (typically .pj)
        String entries[] = new File(directory).list(new PJfiles());
        for (String s : entries) {
            File f = new File(directory + "/" + s);
            if (f.isFile()) {
                list.add(directory + "/" + s);
            }
        }
        // 'list' now contains all the appropriate files in 'directory' - now
        // handle the subdirectories in order.
        entries = new File(directory).list();
        for (String s : entries) {
            File f = new File(directory + "/" + s);
            if (f.isDirectory())
                makeFileList(list, directory + "/" + s);
        }
    }

    public static String makeImportPath(Import im) {
        String path = "";
        if (im.path() != null) {
            int i = 0;
            for (Name n : im.path()) {
                path = path + n.getname();
                if (i < im.path().size() - 1)
                    path = path + "/";
                i++;
            }
        }
        return path;
    }

    /**
     * VisitImport will read and parse an import statement. The chain of symbol tables
     * will be left in the 'symtab' field. The parentage of multiple files imported in
     * the same import is also through the parent link.
     */
    public T visitImport(final Import importStatement) {
        // TODO: Make sure absolute file paths are not necessary
        Log.log(importStatement.line + ": Visiting an import (of file: " + importStatement + ")");

        // An import is first tried in the local directory
        // then in the include directory - unless it is of the form 'f' then it must be local.
        // Make the path for this import
        // Try local first
        String path = /*new File("").getAbsolutePath() + "/" +*/ importStatement.getPath();

        Log.log("visitImport(): Package path is : " + path);
        Log.log("visitImport(): Package file name is : " + importStatement.getSymbol());

        // 'fileList' will hold a list of files found in wildcard imports (.*)
        ArrayList<String> fileList = new ArrayList<String>();

        if(importStatement.isWildcard()) { // a .* import

            // Is it a local directory?
            if(IsDirectory(path)) {

                // Yes, so add it's content to the fileList
                makeFileList(fileList, path);

            } else {

                // It was not a local directory, but see if it is a library directory
                path = new File(Settings.includeDir)
                        .getAbsolutePath() + "/" + Settings.language + "/" + importStatement.getPath();

                Log.log("visitImport(): Not a local, so try a library: " + path);

                if(IsDirectory(path)) {

                    // Yes, it was, so add it's content to the fileList
                    makeFileList(fileList, path);

                } else {

                    // Oh no, the directory wasn't found at all!
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                            .addAST(importStatement)
                            .addError(VisitorMessageNumber.RESOLVE_IMPORTS_103)
                            .addArguments(importStatement.getPackageName())
                            .build());

                }

            }

            Log.log("visitImport(): About to import '" + importStatement.file().getname() + ".pj'");

        } else { // Not a .* import

            path = path + "/" + importStatement.getSymbol() + ".pj";

            // Set package name
            PJBugManager.INSTANCE.setPackageName(importStatement.getPackageName() + "." + importStatement.getSymbol());

            // Is it a local file
            if(IsFile(path)) {

                // Yes, so add it to the fileList
                fileList.add(path);

            } else {

                // No, so look in the library
                path = new File(Settings.includeDir)
                        .getAbsolutePath() + "/" + Settings.language
                        + "/" + importStatement.getPath() + (importStatement.getPath().equals("") ? "" : "/")
                        + importStatement.file().getname() + ".pj";

                Log.log("visitImport(): Not a local so try a library: " + path);

                // But only if it isn't of the form 'import f' cause they can only be local!
                if(!importStatement.isEmpty() && IsFile(path)) {

                    fileList.add(path);

                } else {

                    // Nope, nothing found!
                    if(importStatement.isEmpty()) {
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(importStatement)
                                .addError(VisitorMessageNumber.RESOLVE_IMPORTS_102)
                                .addArguments(importStatement.file().getname())
                                .build());
                    } else {
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(importStatement)
                                .addError(VisitorMessageNumber.RESOLVE_IMPORTS_105)
                                .addArguments(importStatement.getSymbol(), importStatement.getPath())
                                .build());
                    }
                }
            }
        }



        // 'fileList' now contains the list of all the files that this import caused to be imported
        for (String fn : fileList) {
            // Scan, parse and build tree.
            String oldCurrentFileName = currentFileName;
            currentFileName = fn;
            // Set current filename
            PJBugManager.INSTANCE.setFileName(fn);
            Compilation c = ResolveImports.importFile(importStatement, fn);

            // Set absolute path, file and package name from where the Import is created
            c.fileName = fn.substring(fn.lastIndexOf(File.separator) + 1, fn.length());
            c.path = fn.substring(0, fn.lastIndexOf(File.separator));
            c.packageName = importStatement.getPackageName();

            // Add it to the list of compilations for this import
            importStatement.addCompilation(c);
            // Create a symboltable for it
            SymbolTable symtab = new SymbolTable("Import: " + fn);

            currentScope.setImportParent(symtab);
            // Point to whoever called you
            symtab.setParent(SymbolTable.hook);
            SymbolTable.hook = symtab;

            SymbolTable oldHook = SymbolTable.hook;
            SymbolTable.hook = null;

            // Visit imports in the current process file
            c.visit(new ResolveImports<AST>(symtab));
            // Declare types and constants for handling it's imports
            c.visit(new TopLevelDecls<AST>(symtab));
            SymbolTable.hook = oldHook;

            currentFileName = oldCurrentFileName;
            // Reset filename
            PJBugManager.INSTANCE.setFileName(oldCurrentFileName);
        }
        return null;
    }
}