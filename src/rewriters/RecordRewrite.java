package rewriters;

import java.util.LinkedHashSet;
import java.util.Set;

import ast.*;
import utilities.Log;
import utilities.SymbolTable;
import utilities.Visitor;

/**
 * Visitor used for rewriting the body of records that inherit fields
 * from one or more than one record. Since multiple inheritance is not
 * supported in Java, this visitor adds _shallow_ copies of fields
 * to a record that extends multiple records.
 * 
 * @author ben
 */
public class RecordRewrite extends Visitor<AST> {
    
    /** Top level declarations */
    private SymbolTable sym;
    
    public RecordRewrite(SymbolTable sym) {
        this.sym = sym;
        Log.logHeader("****************************************");
        Log.logHeader("*     R E C O R D   R E W R I T E      *");
        Log.logHeader("****************************************");
    }
    
    public Set<RecordMember> addExtendedRecordName(AST a) {
        Log.log(a, "extends a RecordTypeDecl (" + ((RecordTypeDecl) a).name().getname() + ")");
        RecordTypeDecl rt = (RecordTypeDecl) a;
        Set<RecordMember> set = new LinkedHashSet<>();
        for (RecordMember rm : rt.body()) {
            Log.log(rt, "adding member " + rm.type() + " " + rm.name().getname());
            set.add(rm);
        }
        // Add member fields that belong to extended records
        for (Name parent : rt.extend()) {
            if (sym.get(parent.getname()) != null) {
                Set<RecordMember> setNames = addExtendedRecordName((RecordTypeDecl) sym.get(parent.getname()));
                for (RecordMember rm : setNames)
                    if (!set.add(rm))
                        Log.log(rt, String.format("Name '%s' already in (%s)", rm.name().getname(), rt.name().getname()));
            }
        }
        return (Set<RecordMember>) set;
    }
    
    // DONE
    @Override
    public AST visitRecordTypeDecl(RecordTypeDecl rt) {
        Log.log(rt, "Visiting a RecordTypeDecl (" + rt.name().getname() + ")");
        Set<RecordMember> set = new LinkedHashSet<>();
        // Merge the member fields of all extended records
        for (Name name : rt.extend())
            if (sym.get(name.getname()) != null)
                set.addAll(addExtendedRecordName((RecordTypeDecl) sym.get(name.getname())));
        for (RecordMember rm : rt.body())
            set.add(rm);
        rt.body().clear();
        // Rewrite the extend node
        for (RecordMember rm : set)
            rt.body().append(rm);
        Log.log(rt, String.format("record %s with %s member(s)", rt.name().getname(), rt.body().size()));
        for (RecordMember rm : rt.body())
            Log.log(rt, "> member " + rm.type() + " " + rm.name());
        return null;
    }
}
