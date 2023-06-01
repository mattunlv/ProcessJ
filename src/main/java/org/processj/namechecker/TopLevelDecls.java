package org.processj.namechecker;

import java.util.Hashtable;

import org.processj.ast.AST;
import org.processj.ast.Compilation;
import org.processj.ast.ConstantDecl;
import org.processj.ast.Modifier;
import org.processj.ast.NamedType;
import org.processj.ast.ProcTypeDecl;
import org.processj.ast.ProtocolTypeDecl;
import org.processj.ast.RecordTypeDecl;
import org.processj.utilities.PJMessage;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.Log;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.Visitor;
import org.processj.utilities.VisitorMessageNumber;

/**
 * ToplevelDecls.java:
 *
 * Inserts all top-level declarations into a symbol table. When an import
 * statement is encountered.
 *
 */
public class TopLevelDecls<T extends AST> extends Visitor<T> {

    // Symbol table associated with this file. Set in the constructor.
    private SymbolTable symtab;

    public static String currentFileName = PJBugManager.INSTANCE.getFileName();

    // All imported files are kept in this table - indexed by absolute path and
    // name.
    public static Hashtable<String, Compilation> alreadyImportedFiles = new Hashtable<String, Compilation>();
    private Compilation currentCompilation;

    public TopLevelDecls(SymbolTable symtab) {
        Log.logHeader("*****************************************");
        Log.logHeader("*     T O P   L E V E L   D E C L S     *");
        Log.logHeader("*****************************************");
        Log.logHeader("> File: " + PJBugManager.INSTANCE.getFileName());
        Log.logHeader("");
        this.symtab = symtab;
    }

    // TODO: imported files MUST name checked BECAUSE names in 'extends' of protcols
    // and record and constants and procedures can be undefined.
    // What does that mean?

    // TODO: locally imported files should not be in packages .... what about 'this'
    // file ? what about its package ... this must be sorted out

    /**
     * Establishes a symbol table with the top-level declarations declared in the
     * file associated with this compilation inserted. Also causes the creation of a
     * symbol table chain for imported files that is available through the
     * 'importParent' field of the symbol table. This chain can be traversed through
     * its parent links.
     *
     * @param co
     *            a Compilation parse tree node.
     */
    public T visitCompilation(Compilation co) {

        Log.log(" Defining forward referencable names (" + PJBugManager.INSTANCE.getFileName() + ").");
        currentCompilation = co;

        // now visit all the type declarations and the constants in this compilation.
        Log.log(" Visiting type declarations for " + PJBugManager.INSTANCE.getFileName());
        co.typeDecls().visit(this);

        Log.logHeader("");
        Log.logHeader("> File: " + PJBugManager.INSTANCE.getFileName());
        Log.logHeader("*******************************************");
        Log.logHeader("* T O P   L E V E L   D E C L S   D O N E *");
        Log.logHeader("*******************************************");

        return null;

    }

    // **********************************************************************
    // * Top-Level declarations handled below.
    // *
    // * Top-level declarations are simply inserted into the current symbol
    // * table which is held in the variable symtab.
    // **********************************************************************

    // ConstantDecl
    public T visitConstantDecl(ConstantDecl cd) {

        Log.log(cd.line + ": Visiting a ConstantDecl " + cd);

        cd.myCompilation = currentCompilation;
        if(!symtab.put(cd.var().name().getname(), cd))
            PJBugManager.INSTANCE.reportMessage(
                    new PJMessage.Builder()
                    .addAST(cd)
                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_200)
                    .addArguments(cd.toString())
                    .build());

        return null;

    }

    // ProcTypeDecl
    public T visitProcTypeDecl(ProcTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProcTypeDecl " + pd);
        pd.myCompilation = currentCompilation;

        // Procedures can be overloaded, so an entry in the symbol table for a procedure
        // is another symbol table which is indexed by signature.
        if(Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE))
            if(!pd.returnType().isVoidType())
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(pd)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_205)
                        .addArguments(pd.toString())
                        .build());

        // Mobile procedure may NOT be overloaded.
        // If a symbol table contains a mobile the field isMobileProcedure is true.
        Object s = symtab.getShallow(pd.toString());
        if(s == null) {
            // this is the first time we see a procedure by this name in this scope
            SymbolTable st = new SymbolTable();
            if(Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE))
                st.isMobileProcedure = true;

            st.put(pd.getSignature(), pd);
            symtab.put(pd.toString(), st);
        } else {
            if (s instanceof SymbolTable) {
                SymbolTable st = (SymbolTable) s;
                if (Modifier.hasModifierSet(pd.modifiers(), Modifier.MOBILE)) {
                    if (st.isMobileProcedure)
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(pd)
                                .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_206)
                                .addArguments(pd.toString())
                                .build());
                    else
                        PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                                .addAST(pd)
                                .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_208)
                                .addArguments(pd.toString())
                                .build());
                } else
                    st.put(pd.getSignature(), pd);
            } else
                PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                        .addAST(pd)
                        .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_201)
                        .addArguments(pd.getname())
                        .build());
        }
        return null;
    }

    // RecordTypeDecl
    public T visitRecordTypeDecl(RecordTypeDecl rd) {
        Log.log(rd.line + ": Visiting a RecordTypeDecl " + rd);
        rd.myCompilation = currentCompilation;
        if(!symtab.put(rd.toString(), rd))
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(rd)
                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_202)
                    .addArguments(rd.toString())
                    .build());
        return null;
    }

    // ProtocolTypeDecl
    public T visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        Log.log(pd.line + ": Visiting a ProtocolTypeDecl " + pd);
        pd.myCompilation = currentCompilation;
        if (!symtab.put(pd.toString(), pd))
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(pd)
                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_203)
                    .addArguments(pd.toString())
                    .build());
        return null;
    }

    // NamedType
    public T visitNamedType(NamedType nt) {
        Log.log("Toplevel Named Type:" + nt);
        nt.myCompilation = currentCompilation;
        if(!symtab.put(nt.toString(), nt))
            PJBugManager.INSTANCE.reportMessage(new PJMessage.Builder()
                    .addAST(nt)
                    .addError(VisitorMessageNumber.TOP_LEVEL_DECLS_207)
                    .addArguments(nt.toString())
                    .build());
        return null;
    }
}