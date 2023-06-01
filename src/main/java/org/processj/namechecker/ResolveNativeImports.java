package org.processj.namechecker;

import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.ast.Import;
import org.processj.ast.Pragma;
import org.processj.ast.ProcTypeDecl;
import java.util.HashMap;
import org.processj.utilities.Log;
import org.processj.utilities.Visitor;

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
        String str = pr.getValue()!=null? pr.getValue() : "";
        Log.log(pr, "Visiting a pragma " + pr.getName().getname() + " " + str);
        if ( str.isEmpty() )
            hm.put(pr.getName().getname(), pr.getName().getname());
        else
            hm.put(pr.getName().getname(), str.replace("\"", ""));
        return null;
    }
    
    @Override
    public AST visitImport(Import im) {
        Log.log(im, "Visiting an import (of file: " + im + ")");
        curImport = im;
        // For each top-level declaration, determine if this declaration
        // is part of a PJ native org.processj.library
        for (Compilation c : im.getCompilations()) {
            if ( c==null )
                continue;
            // For each pragma, check if they are part of a native org.processj.library
            // function
            for (Pragma p : c.getPragmas())
                p.visit(this);
            // Mark all top-level declarations 'native' if they are part
            // of a PJ native org.processj.library function
            for (AST decl : c.typeDecls())
                decl.visit(this);
            hm.clear();
        }
        return null;
    }
    
    @Override
    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd, "Visiting a ProcTypeDecl (" + pd + " " + pd.getSignature() + ")");
        if ( !hm.isEmpty() && curImport!=null ) {
            String path = ResolveImports.makeImportPath(curImport);
            Log.log(pd, "Package path: " + path);
            // TODO: maybe use variables from Library.java instead??
            if (hm.containsKey("LIBRARY") && hm.containsKey("NATIVE")) {
                Log.log(pd, "Package file name: " + curImport.file().getname());
                pd.isNative = true;
                pd.library = curImport.toString();
                pd.filename = hm.get("FILE");
                pd.nativeFunction = pd.toString();
            } else {
                // TODO: Non-native procedure found??
            }
        }
        return null;
    }
}
