package org.processj.namechecker;

import java.io.File;

import org.processj.ast.*;
import org.processj.utilities.PJMessage;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.Log;
import org.processj.utilities.Settings;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

public class ResolvePackageTypes extends Visitor<AST> {

    public ResolvePackageTypes() {
        Log.logHeader("***************************************************");
        Log.logHeader("* P A C K A G E D   T Y P E   R E S O L U T I O N *");
        Log.logHeader("***************************************************");
        Log.logHeader("> File: " + PJBugManager.INSTANCE.getFileName());
    }

    // X.Y.Z::f, pa is X.Y.Z and we get that turned into X/Y/Z.pj
    private String makeImportFileName(Sequence<Name> pa) {
        String path = "";
        int i = 0;
        for (Name n : pa) {
            path = path + n.getname();
            if (i < pa.size() - 1)
                path = path + "/";
            i++;
        }
        return path + ".pj";
    }

    public void resolveTypeOrConstant(Name na) {
        Log.log("ResolvePackagedTypes: Resolving '" + na + "'");
        Sequence<Name> pa = na.packageAccess();
        String fileName = "";
        String oldCurrentFileName = "";
        Compilation comp = null;
        // pa is the sequence of names before the :: (if any)
        // if there is no package access then the name must be a
        // name declared locally or in an imported file - both will
        // be correctly resolved at name checking time.
        if (pa.size() > 0) {
            oldCurrentFileName = PJBugManager.INSTANCE.getFileName();
            // Turn X.Y.Z::f into X/Y/Z.pj
            fileName = Settings.absolutePath + makeImportFileName(pa);
            // Does X/Y/Z.pj exist?
            if(new File(fileName).isFile()) // Yes it did - so it is a non-libtary file.
                ; // don't do anything just continue after the if.
            else { // No, it was not a local file so now try the org.processj.library directory
                fileName = new File(org.processj.utilities.Settings.includeDir)
                        .getAbsolutePath()
                        + "/"
                        + org.processj.utilities.Settings.language
                        + "/"
                        + makeImportFileName(pa);
                if (new File(fileName).isFile()) { // Yes it is a org.processj.library file.
                    // don't do anything just continue after the if.
                } else {
                    // It was neither a local nor a org.processj.library file - throw an error...
                    PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(pa)
                                .addError(VisitorMessageNumber.RESOLVE_IMPORTS_101)
                                .addArguments(makeImportFileName(pa))
                                .build());
                }
            }
            PJBugManager.INSTANCE.setFileName(fileName);
            // Now import it
            comp = ResolveImports.importFile(pa.child(0), fileName);

            SymbolTable st = new SymbolTable();
            if (comp.visited == false) {
                comp.visited = true;
                comp.visit(new TopLevelDecls<AST>(st));
                comp.visit(new ResolvePackageTypes());
                comp.visit(new NameChecker<AST>(st));
                // TODO: should we type check here?
            }
            PJBugManager.INSTANCE.setFileName(oldCurrentFileName);
            st = SymbolTable.hook;
            // TODO: this should do a proper find if its a symbol table that comes back
            // but we probably need Type checking for that !
            // so for now - SymbolTable implements TopLevelDecl as well!
            DefineTopLevelDecl td = (DefineTopLevelDecl) st.getShallow(na.simplename());
            if (td != null) { // Yes, we found something
                na.c = comp;
                na.resolvedPackageAccess = td;
                Log.log(na.line + " Resolved '" + na + "' to '" + td + "'");
            } else {
                ;// TODO: don't error out now - the NameChecker will do that!
                //Error.error(na,"Constant or Type '" + na + "' not declared.", false, 0000);
            }
        }
    }

    public AST visitName(final Name name) {

        Log.log(name.line + " Resolving Name '" + name.getname() + "'");

        if(name.specifiesPackage())
            resolveTypeOrConstant(name);

        return null;

    }

    public AST visitNamedType(final NamedType namedType) {

        Log.log(namedType.line + " Resolving NamedType '" + namedType + "'");

        // If the NamedType specifies a Package
        if(namedType.specifiesPackage()) {

            // Initialize a handle to the Name
            final Name name = namedType.name();

            // Bind the resolved Type to the Name
            resolveTypeOrConstant(name);

            // Bind the resolved Type to the NamedType if possible
            if(name.resolvedPackageAccess instanceof Type)
                namedType.setType((Type) name.resolvedPackageAccess);

        }

        return null;

    }

}