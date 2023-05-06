package namechecker;

import ast.AST;
import ast.Compilation;
import ast.Import;
import ast.Pragma;
import ast.ProcTypeDecl;
import java.util.HashMap;
import utilities.Log;
import utilities.Visitor;

/**
 * Visitors used for marking top-level declarations as 'native'.
 *
 * @param <T>
 *          The visitor interface used to traverse and resolve
 *          the type in an import statement.
 * 
 * @author ben
 * @version 01/31/2019
 * @since 1.2
 */
public class ResolveNativeImports extends Visitor<AST> {
    
    // The import currently being resolved
    private Import curImport = null;
    private HashMap<String, String> hm = new HashMap<>();
    
    public ResolveNativeImports() {
        Log.logHeader("***********************************************");
        Log.logHeader("* R E S O L V E   N A T I V E   I M P O R T S *");
        Log.logHeader("***********************************************");
    }
    
    @Override
    public AST visitCompilation(Compilation co) {
        Log.log("Finding native top type declarations");
        Log.log("Visiting type declarations");
        co.imports().visit(this);
        return null;
    }
    
    @Override
    public AST visitPragma(Pragma pr) {
        String str = pr.value()!=null? pr.value() : "";
        Log.log(pr, "Visiting a pragma " + pr.pname().getname() + " " + str);
        if ( str.isEmpty() )
            hm.put(pr.pname().getname(), pr.pname().getname());
        else
            hm.put(pr.pname().getname(), str.replace("\"", ""));
        return null;
    }
    
    @Override
    public AST visitImport(Import im) {
        Log.log(im, "Visiting an import (of file: " + im + ")");
        curImport = im;
        // For each top-level declaration, determine if this declaration
        // is part of a PJ native library
        for (Compilation c : im.getCompilations()) {
            if ( c==null )
                continue;
            // For each pragma, check if they are part of a native library
            // function
            for (Pragma p : c.pragmas())
                p.visit(this);
            // Mark all top-level declarations 'native' if they are part
            // of a PJ native library function
            for (AST decl : c.typeDecls())
                decl.visit(this);
            hm.clear();
        }
        return null;
    }
    
    @Override
    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd.name().getname() + " " + pd.signature() + ")");
        if ( !hm.isEmpty() && curImport!=null ) {
            String path = ResolveImports.makeImportPath(curImport);
            Log.log(pd, "Package path: " + path);
            // TODO: maybe use variables from Library.java instead??
            if (hm.containsKey("LIBRARY") && hm.containsKey("NATIVE")) {
                Log.log(pd, "Package file name: " + curImport.file().getname());
                pd.isNative = true;
                pd.library = curImport.toString();
                pd.filename = hm.get("FILE");
                pd.nativeFunction = pd.name().getname();
            } else {
                // TODO: Non-native procedure found??
            }
        }
        return null;
    }
}
