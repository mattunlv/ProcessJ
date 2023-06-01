
package org.processj.typechecker;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.processj.ast.*;
import org.processj.utilities.Error;
import org.processj.utilities.Log;
import org.processj.utilities.SymbolTable;
import org.processj.utilities.Visitor;
import org.processj.utilities.PJMessage;
import org.processj.utilities.PJBugManager;
import org.processj.utilities.MessageType;
import org.processj.utilities.VisitorMessageNumber;

/**
 * 
 * @author Matt Pedersen
 * @version 02/10/2019
 * @since 1.2
 */
public class TypeChecker extends Visitor<Type> {
    // The top level symbol table.
    private SymbolTable topLevelDecls = null;

    // The procedure currently being type checked.
    private ProcTypeDecl currentProcedure = null;

    // Contains the protocol name and the corresponding tags currently switched on.
    Hashtable<String, ProtocolCase> protocolTagsSwitchedOn = new Hashtable<String, ProtocolCase>();

    // Set of nested protocols in nested switch statements
    HashSet<String> protocolsSwitchedOn = new HashSet<String>();

    Set<DefineTopLevelDecl> visited = new HashSet<>();

    public TypeChecker(SymbolTable topLevelDecls) {
        debug = true; // TODO: WHAT DOES THIS DO?
        this.topLevelDecls = topLevelDecls;

        Log.log("======================================");
        Log.log("*       T Y P E   C H E C K E R      *");
        Log.log("======================================");
    }

    // ConstantDecl -- ??
    // LocalDecl
    // ParamDecl - nothing to do

    // Import - nothing to do
    // Pragma - nothing to do
    // AltStat - Nothing to do
    // ExprStat - nothing to do
    // ContinueStat - nothing to do here, but further checks are needed. TODO
    // ParBlock - nothing to do
    // SkipStat - nothing to do
    // StopStat - nothing to do

    // ProtocolCase - nothing to do.
    // SwitchGroup -- nothing to do - handled in SwitchStat
    // SwitchLabel -- nothing to do - handled in SwitchStat

    // Guard -- Nothing to do
    // Modifier - nothing to do
    // Name - nothing to do
    // Compilation -- Probably nothing to

    // Sequence - nothing to do


    // Alt Case
    //
    // Syntax: (Expr) && Guard : Statement
    //
    // Expr must be Boolean and Guard and Statement must be visited
    //
    // Boolean?(T(expr))
    @Override
    public Type visitAltCase(AltCase ac) {

        Log.log(ac.line + ": Visiting an alt case.");

        // Check the pre-condition if there is one.
        if(ac.precondition() != null) {
            Type t = ac.precondition().visit(this);
            // if (!t.isBooleanType())
            // CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
            // .addAST(ac)
            // .addError(VisitorMessageNumber.TYPE_CHECKER_660)
            // TODO REMOVE COMMENT
            // .addArguments(t.typeName())
            // .build(), MessageType.PRINT_CONTINUE);
        }

	    // guard is only null if the case is a nested alt.
	    if(ac.guard() != null) ac.guard().visit(this);

        ac.stat().visit(this);

        return null;

    }

    // Syntax: Expr.write(Expr)
    @Override
    public Type visitChannelWriteStat(ChannelWriteStat cw) {
        Log.log(cw.line + ": Visiting a channel write stat.");
        Type t = resolve(cw.channel().visit(this));

        // Check that the expression is of channel end type or channel type.
        if(!(t instanceof ChannelEndType || t instanceof ChannelType)) {
            // !!Error.error(cw, "Cannot write to a non-channel end.", false, 3023);
        }

        // Visit the expression being written.
        cw.expr().visit(this);
        return null;
    }

    // Syntax: do Stat while Expr
    @Override
    public Type visitDoStat(DoStat ds) {
        Log.log(ds.line + ": Visiting a do statement");

        Type eType = resolve(ds.expr().visit(this));

        // The expression must be of Boolean type.
        if (!eType.isBooleanType()) {
            // !! Error.addError(ds, "Non boolean Expression found as test in
            // do-statement.", 3024);
        }

        // Type check the statement of the do statement;
        if (ds.stat() != null) {
            ds.stat().visit(this);
        }

        return null;
    }

    @Override
    public Type visitForStat(ForStat fs) {
        Log.log(fs.line + ": Visiting a for statement");

        // A non-par for loop cannot enroll on anything.
        if (fs.barriers().size() > 0 || fs.isPar()) {
            // !! Error.error(..."Process already enrolled on barriers (a non-par for loop
            // cannot enroll on barriers)");
        }

        int i = 0;
        // Check that all the barrier expressions are of barrier type.
        for (Expression e : fs.barriers()) {
            Type t = resolve(e.visit(this));
            if (!t.isBarrierType()) {
                // !!Error.addError(fs.barriers().child(i), "Barrier type expected, found '" + t
                // + "'.", 3025);
            }
            i++;
        }

        if (fs.init() != null)
            fs.init().visit(this);
        if (fs.incr() != null)
            fs.incr().visit(this);
        if (fs.expr() != null) {
            Type eType = resolve(fs.expr().visit(this));

            if (!eType.isBooleanType()) {
                // !!Error.addError(fs, "Non-boolean expression found in for-statement.", 3026);
            }
        }
        if (fs.stats() != null)
            fs.stats().visit(this);

        return null;
    }

    // Syntax: if (Expr) Statement
    // if (Expr) Statement else Statement
    @Override
    public Type visitIfStat(IfStat is) {
        Log.log(is.line + ": Visiting a if statement");

        Type eType = resolve(is.expr().visit(this));

        if (!eType.isBooleanType()) {
            // !!Error.addError(is, "Non-boolean expression found as test in if-statement.",
            // 3027);
        }
        if (is.thenpart() != null)
            is.thenpart().visit(this);
        if (is.elsepart() != null)
            is.elsepart().visit(this);

        return null;
    }

    @Override
    public Type visitReturnStat(ReturnStat rs) {
        Log.log(rs.line + ": visiting a return statement");

        Type returnType = resolve(currentProcedure.returnType());

        // Check if the return type is void; if it is rs.expr() should be null.
        // Check if the return type is not voidl if it is not rs.expr() should not be
        // null.
        if (returnType instanceof PrimitiveType) {
            PrimitiveType pt = (PrimitiveType) returnType;
            if (pt.isVoidType() && rs.expr() != null)
                Error.addError(rs, "Procedure return type is void; return statement cannot return a value.", 3040);
            if (!pt.isVoidType() && rs.expr() == null)
                Error.addError(rs, "Procedure return type is '" + pt + "' but procedure return type is void.", 3041);
            if (pt.isVoidType() && rs.expr() == null)
                return null;
        }

        Type eType = resolve(rs.expr().visit(this));
        if (!returnType.typeAssignmentCompatible(eType))
            Error.addError(rs, "Incompatible type in return statement.", 3042);

        return null;
    }

    @Override
    public Type visitSuspendStat(SuspendStat ss) {
        Log.log(ss.line + ": Visiting a suspend stat.");
        if (!Modifier.hasModifierSet(currentProcedure.modifiers(), Modifier.MOBILE))
            Error.addError(ss, "Non-mobile procedure cannot suspend.", 3043);
        return null;
    }

    @Override
    public Type visitSwitchStat(SwitchStat ss) {
        Type exprType = resolve(ss.expr().visit(this));

        // The switch expression must be integral, string or a protocol type (we then
        // switch on the tag).
        if (!(exprType instanceof ProtocolTypeDecl || exprType.isIntegralType() || exprType.isStringType()))
            Error.addError(ss, "Illegal type '" + exprType + "' in expression in switch statement.", 0000);

        // String and Intergral types.
        if (exprType.isIntegralType() || exprType.isStringType()) {
            for (SwitchGroup sg : ss.switchBlocks()) {
                // For each Switch Group, cycle through the Switch Labels and check they are
                // assignment compatible with ...
                for (SwitchLabel sl : sg.labels()) {
                    if (!sl.isDefault()) {
                        // Get the type of the (constant expression)
                        if (exprType.isStringType() || exprType.isIntegralType()) {
                            // For string and Integral types, the constant expression must be assignable
                            // to the type of the switching expression.
                            Type labelType = resolve(sl.expr().visit(this));
                            // System.out.println("Type of tag " + sl.expr() + " is " + labelType);

                            if (!exprType.typeAssignmentCompatible(labelType))
                                Error.addError(ss,
                                        "Switch label '" + sl.expr() + "' of type '" + labelType
                                                + "' not compatible with switch expression's type '" + exprType + "'.",
                                        0000);
                            // sg.statements().visit(this);
                        }
                    }
                    sg.statements().visit(this);
                }
            }
        } else { // Protocol Type.
            // Get the name of the protocol.
            String protocolName = exprType.toString();

            if (protocolsSwitchedOn.contains(protocolName))
                Error.addError(ss, "Illegally nested switch on protocol '" + protocolName + "'.", 0000);
            else
                protocolsSwitchedOn.add(protocolName);

            // System.out.println("protocolsSwitchedOn before visiting body: " +
            // protocolsSwitchedOn.toString());

            // Cycle through the SwitchGroups one at a time.
            for (SwitchGroup sg : ss.switchBlocks()) {
                // For each Switch Group, cycle through the Switch Labels and check they are
                // assignment compatible with ...
                // System.out.println("-- New Group --");
                for (SwitchLabel sl : sg.labels()) {
                    if (!sl.isDefault()) {
                        // The label must ne a name.
                        if (!(sl.expr() instanceof NameExpr))
                            Error.addError(sl, "Switch label '" + sl.expr() + "' is not a protocol tag.", 0000);

                        // Get the name of the tag.
                        // TODO: We should probably use properly qualified names here - what if there
                        // are two different protocols named the same ?
                        String tag = ((NameExpr) sl.expr()).name().getname();
                        // System.out.println("Processing tag: " + tag);
                        // System.out.println(protocolTagsSwitchedOn.toString());

                        ProtocolTypeDecl ptd = (ProtocolTypeDecl) exprType;
                        ProtocolCase pc = ptd.getCase(tag);

                        if (pc == null)
                            Error.addError(sl, "Tag '" + tag + "' is not found in protocol '" + protocolName + "'.",
                                    0000);
                        else {
                            // System.out.println("pc is not null: " + pc.name().getname());
                            // System.out.println("Fields: ");
                            // for (RecordMember rm : pc.body()) {
                            // System.out.println(" " + rm.name().getname());
                            // }

                            // System.out.println(protocolTagsSwitchedOn.toString());

                            // insert into protocol Cases Swithced on
                            // visit the body.
                            protocolTagsSwitchedOn.put(protocolName, pc); // TODO: perhaps a hash table here isn't a
                            // good idea - something in reverse order may
                            // be what we need.
                        }
                        // System.out.println("ABOUT TO VISIT STATEMENTS");
                    }
                    sg.statements().visit(this);
                    if (!sl.isDefault())
                        protocolTagsSwitchedOn.remove(protocolName);
                }
            }
            // remove from protocol Cases Switched on
            protocolsSwitchedOn.remove(protocolName);
            // System.out.println("protocolsSwitchedOn after evaluating body: " +
            // protocolsSwitchedOn);
        }
        return null;
    }

    @Override
    public Type visitSyncStat(SyncStat ss) {
        Type t = ss.barrier().visit(this);
        if (!t.isBarrierType())
            Error.addError(ss, "Cannot sync on anything but a barrier type", 0000);

        return null;
    }

    @Override
    public Type visitTimeoutStat(TimeoutStat ts) {
        Log.log(ts.line + ": visiting a timeout statement.");
        Type dType = resolve(ts.delay().visit(this));
        if (!dType.isIntegralType())
            Error.error(ts, "Invalid type (" + dType + ") in timeout statement, integral type required.",
                    false, 3049);
        Type eType = resolve(ts.timer().visit(this));
        if (!eType.isTimerType())
            Error.error(ts, "Timer type required in timeout statement - found " + eType + ".", false, 3050);
        return null;
    }

    @Override
    public Type visitWhileStat(WhileStat ws) {
        Log.log(ws.line + ": Visiting a while statement");
        Type eType = resolve(ws.expr().visit(this));

        if (!eType.isBooleanType())
            Error.error(ws, "Non-Boolean Expression found as test in while-statement.", false, 3059);
        if (ws.stat() != null)
            ws.stat().visit(this);
        return null;
    }

    @Override
    public Type visitProcTypeDecl(ProcTypeDecl pd) {

        if(this.visited.add(pd)) {

            Log.log(pd.line + ": visiting a procedure type declaration (" + pd + ").");
            currentProcedure = pd;
            super.visitProcTypeDecl(pd);

        }

        return null;
    }

    @Override
    public Type visitProtocolTypeDecl(ProtocolTypeDecl pt) {

        if(this.visited.add(pt)) {

            Log.log(pt.line + ": Visiting a protocol type decl.");
            pt.visitChildren(this);
            Log.log(pt.line + ": Protocol type decl has type: " + pt);

        }

        return pt;
    }

    @Override
    public Type visitRecordTypeDecl(RecordTypeDecl rt) {

        if(this.visited.add(rt)) {

            Log.log(rt.line + ": Visiting a record type decl.");
            rt.visitChildren(this);
            Log.log(rt.line + ": Record type decl has type: " + rt);

        }
        return rt;
    }

    /// ------------------------------------------------------------------------------------------- ///
    /// Expressions
    /// ------------------------------------------------------------------------------------------- ///

    // ArrayAccessExpr
    //
    // Syntax: Expr1[Expr2]
    //
    // Expr1 must be array type and Expr2 my be integer.
    //
    // Array?(T(Expr1)) /\ Integer?(T(Expr2))
    //
    // If T(Expr1) = Array(BaseType) => T(Expr1[Expr2]) := BaseType
    @Override
    public Type visitArrayAccessExpr(final ArrayAccessExpr arrayAccessExpression) {

        Log.log(arrayAccessExpression.line + ": Visiting ArrayAccessExpr");

        Type targetType = resolve(arrayAccessExpression.targetExpression().visit(this));

        if(targetType instanceof ArrayType) {

            // Initialize a handle to the ArrayType
            final ArrayType arrayType = (ArrayType) targetType;

            // Initialize a handle to the resultant Type
            Type resultantType = arrayType.getComponentType();

            // Check for a depth greater than 1 in order to construct a new ArrayType
            if(arrayType.getDepth() > 1)
                resultantType = new ArrayType(arrayType.getComponentType(), arrayType.getDepth() - 1);

            // Set the Type
            arrayAccessExpression.setType(resultantType);

            Log.log(arrayAccessExpression.line + ": ArrayAccessExpr has type " + arrayAccessExpression.type);

            Type indexType = resolve(arrayAccessExpression.indexExpression().visit(this));

            // This error does not create an error type cause the baseType() is
            // still the array expression's type.
            if(!indexType.isIntegerType())
                PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                        .addAST(arrayAccessExpression)
                        .addError(VisitorMessageNumber.TYPE_CHECKER_655)
                        .addArguments(indexType.toString())
                        .build(), MessageType.PRINT_CONTINUE);

        } else {

            arrayAccessExpression.type = new ErrorType();
            // CompilerMessageManager.INSTANCE.reportMessage(new PJMessage.Builder()
            // .addAST(ae)
            // .addError(VisitorMessageNumber.TYPE_CHECKER_661)
            // TODO: REMOVE COMMENT
            // .addArguments(t.typeName())
            // .build(), MessageType.PRINT_CONTINUE);

        }

        Log.log(arrayAccessExpression.line + ": Array Expression has type: " + arrayAccessExpression.type);

        return arrayAccessExpression.type;

    }

    // Assignment
    //
    // Syntax: Name <op> Expr [ shoreted to v <op> e below ]
    // where op is one of =, +=, -=, *=, /=, %=, <<=, >>=, &=, |=, ^=
    //
    // = : (v := e)
    //
    // T(v) :=T T(e)
    // T(v = e) := T(v)
    // (Some variables are not assingable: channels, barriers, more ??)
    // TODO: RECORDS and PROTOCOLS
    //
    // +=, -=, *=, /=, %= : ( v ?= e) [ ? is one of {+,-,*,/,%} ]
    //
    // (op = + /\ String?(T(v)) /\
    // (Numeric?(T(e)) \/ Boolean?(T(e)) \/ String?(T(e) \/
    // Char?(T(e)))) \/
    // (op != + /\ (T(v) :=T T(e)))
    //
    //
    @Override
    public Type visitAssignment(Assignment as) {
        Log.log(as.line + ": Visiting an assignment");
        as.type = null; // gets set to ErrorType if an error happens.

        Type vType = resolve(as.left().visit(this));
        Type eType = resolve(as.right().visit(this));

        // Handle error types in operands
        if ((vType instanceof ErrorType) || (eType instanceof ErrorType)) {
            as.type = new ErrorType();
            Log.log(as.line + ": Array ExpressionAssignment has type: " + as.type);
            return as.type;
        }

        /**
         * Note: as.left() should be of NameExpr or RecordAccess or ArrayAccessExpr
         * class!
         */
        // TODO: Check the implementation of Assignable.
        if(!vType.assignable())
            PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                    .addAST(as)
                    .addError(VisitorMessageNumber.TYPE_CHECKER_630)
                    .build(), MessageType.PRINT_CONTINUE);

        // Now switch on the operators
        switch (as.op()) {
            case Assignment.EQ: {
                // =
                if (!vType.typeAssignmentCompatible(eType)) {
                    as.type = new ErrorType();
                    PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.TYPE_CHECKER_601)
                            .addArguments(eType.toString(), vType.toString())
                            .build(), MessageType.PRINT_CONTINUE);
                }
                break;
            }
            case Assignment.MULTEQ:
            case Assignment.DIVEQ:
            case Assignment.MODEQ:
            case Assignment.PLUSEQ:
            case Assignment.MINUSEQ:
                // *=, /=, %=, +=, -=

                // String += Primitive Type is OK
                if (as.op() == Assignment.PLUSEQ && vType.isStringType()
                        && (eType.isNumericType() || eType.isBooleanType() || eType.isStringType() || eType.isCharType()))
                    break; // type will be set below.
                else if (!vType.typeAssignmentCompatible(eType)) {
                    // Left-hand side is not assignment compatible with the right-hand side.
                    as.type = new ErrorType();
                    PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.TYPE_CHECKER_600)
                            .addArguments(eType.toString(), vType.toString())
                            .build(), MessageType.PRINT_CONTINUE);
                }
                break;
            case Assignment.LSHIFTEQ:
            case Assignment.RSHIFTEQ:
            case Assignment.RRSHIFTEQ:
                // <<=, >>=, >>>=
                if (!vType.isIntegralType()) {
                    as.type = new ErrorType();
                    PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.TYPE_CHECKER_604)
                            .addArguments(as.opString())
                            .build(), MessageType.PRINT_CONTINUE);
                }
                if (!eType.isIntegralType()) {
                    as.type = new ErrorType();
                    PJBugManager.INSTANCE.reportMessageAndExit(new PJMessage.Builder()
                            .addAST(as)
                            .addError(VisitorMessageNumber.TYPE_CHECKER_605)
                            .addArguments(as.opString())
                            .build(), MessageType.PRINT_CONTINUE);
                }
                break;
            case Assignment.ANDEQ:
            case Assignment.OREQ:
            case Assignment.XOREQ:
                // &=, |=, %=
                if (!((vType.isIntegralType() && eType.isIntegralType())
                        || (vType.isBooleanType() && eType.isBooleanType()))) {
                    as.type = new ErrorType();
                    // --Error.addError(as, "Both right and left-hand-side operands of operator '" +
                    // as.opString() + "' must be either of boolean or integral type.", 3009);
                }
                break;
        }

        // If we made it this far and as.type hasn't been set to be an error type, then
        // the type of the assignment expression is that of the left-hand side (vType)
        if (as.type == null)
            as.type = vType;

        Log.log(as.line + ": Assignment has type: " + as.type);
        return as.type;
    }

    // Syntax: Expr1 <op> Expr2
    @Override
    public Type visitBinaryExpr(BinaryExpr be) {
        Log.log(be.line + ": Visiting a Binary Expression");

        Type lType = resolve(be.left().visit(this));
        Type rType = resolve(be.right().visit(this));
        String op = be.opString();

        // Handle errors from type checking operands
        if (lType instanceof ErrorType || rType instanceof ErrorType) {
            be.type = new ErrorType();
            Log.log(be.line + ": Binary Expression has type: " + be.type);
            return be.type;
        }

        switch (be.op()) {
            // < > <= >= : Type can be Integer only.
            case BinaryExpr.LT:
            case BinaryExpr.GT:
            case BinaryExpr.LTEQ:
            case BinaryExpr.GTEQ: {
                if (lType.isNumericType() && rType.isNumericType()) {
                    be.type = new PrimitiveType(PrimitiveType.BooleanKind);
                } else {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires operands of numeric
                    // type.", 3010);
                }
                break;
            }
            // == != : Type can be anything but void.
            case BinaryExpr.EQEQ:
            case BinaryExpr.NOTEQ: {

                // TODO: barriers, timers, procs, records and protocols
                // Funny issues with inheritance for records and protocols.
                // should they then get a namedType as a type?
                // extern types cannot be compared at all!

                if (lType.typeEqual(rType))
                    if (lType.isVoidType()) {
                        be.type = new ErrorType();
                        // !!Error.addError(be, "Void type cannot be used here.", 3011);
                    } else
                        be.type = new PrimitiveType(PrimitiveType.BooleanKind);
                else if (lType.isNumericType() && rType.isNumericType())
                    // Any two numeric types can be compared.
                    be.type = new PrimitiveType(PrimitiveType.BooleanKind);
                else {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires operands of the same
                    // type.", 3012);
                }
                break;
            }
            // && || : Type can be Boolean only.
            case BinaryExpr.ANDAND:
            case BinaryExpr.OROR: {
                if (lType.isBooleanType() && rType.isBooleanType())
                    be.type = lType;
                else {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires operands of boolean
                    // type.", 3013);
                }
                break;
            }
            // & | ^ : Type can be Boolean or Integral.
            case BinaryExpr.AND:
            case BinaryExpr.OR:
            case BinaryExpr.XOR: {
                if (lType.isBooleanType() && rType.isBooleanType())
                    be.type = lType;
                else if (lType.isIntegralType() && rType.isIntegralType()) {
                    be.type = ((PrimitiveType) lType).typeCeiling((PrimitiveType) rType);

                    // Promote byte, short, and char to int.
                    if (be.type.isByteType() || be.type.isShortType() || be.type.isCharType())
                        be.type = new PrimitiveType(PrimitiveType.IntKind);

                } else {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires both operands of either
                    // integral or boolean type.", 3014);
                }
                break;
            }
            // + - * / % : Type must be numeric
            case BinaryExpr.PLUS:
            case BinaryExpr.MINUS:
            case BinaryExpr.MULT:
            case BinaryExpr.DIV:
            case BinaryExpr.MOD: {
                if (lType.isNumericType() && rType.isNumericType()) {
                    be.type = ((PrimitiveType) lType).typeCeiling((PrimitiveType) rType);

                    // Promote byte, short, and char to int.
                    if (be.type.isByteType() || be.type.isShortType() || be.type.isCharType())
                        be.type = new PrimitiveType(PrimitiveType.IntKind);
                } else if ((lType.isStringType()
                        && (rType.isNumericType() || rType.isBooleanType() || rType.isStringType()))
                        || (rType.isStringType()
                        && (lType.isNumericType() || lType.isBooleanType() || lType.isStringType())))
                    be.type = new PrimitiveType(PrimitiveType.StringKind);
                else {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires operands of numeric type
                    // or string/boolean, string/numeric, or string/string type.", 3015);
                }
                break;
            }
            // << >> >>>:
            case BinaryExpr.LSHIFT:
            case BinaryExpr.RSHIFT:
            case BinaryExpr.RRSHIFT: {
                if (!lType.isIntegralType()) {
                    be.type = new ErrorType();
                    // !!Error.addError(be, "Operator '" + op + "' requires left operand of integral
                    // type.", 3016);
                } else if (!rType.isIntegralType()) {
                    be.type = new ErrorType();
                    // !!be.type = Error.addError(be, "Operator '" + op + "' requires right operand
                    // of integral type.", 3017);
                } else {
                    be.type = lType;
                    if (be.type.isByteType() || be.type.isShortType() || be.type.isCharType())
                        be.type = new PrimitiveType(PrimitiveType.IntKind);
                }
                break;
            }
            default: {
                be.type = new ErrorType();
                // !!Error.addError(be, "Unknown operator '" + op + "'.", 3018);
            }
        }
        Log.log(be.line + ": Binary Expression has type: " + be.type);
        return be.type;
    }

    // Syntax: (Type)Expr
    @Override
    public Type visitCastExpr(CastExpr ce) {
        Log.log(ce.line + ": Visiting a cast expression");

        Type exprType = resolve(ce.expr().visit(this));
        Type castType = resolve(ce.type());

        // Handle errors here
        if (exprType instanceof ErrorType || castType instanceof ErrorType) {
            ce.type = new ErrorType();
            return ce.type;
        }

        if (exprType.isNumericType() && castType.isNumericType()) {
            ce.type = castType;
        } else if (exprType instanceof ProtocolTypeDecl && castType instanceof ProtocolTypeDecl) {

            // TODO: finish this
        } else if (exprType instanceof RecordTypeDecl && castType instanceof RecordTypeDecl) {
            // TODO: finish this
        } else {
            // Turns out that casts like this are illegal:
            // int a[][];
            // double b[][];
            // a = (int[][])b;
            // b = (double[][])a;
            ce.type = new ErrorType();
            // !!Error: Illegal cast of value of type exprType to castType.
        }

        Log.log(ce.line + ": Cast Expression has type: " + ce.type);
        return ce.type;
    }

    @Override
    public Type visitParamDecl(final ParamDecl parameterDeclaration) {

        System.out.println("Visiting ParamDecl: " + parameterDeclaration);

        return super.visitParamDecl(parameterDeclaration);

    }

    // Syntax: Expr.read or Expr.write
    @Override
    public Type visitChannelEndExpr(ChannelEndExpr ce) {
        Log.log(ce.line + ": Visiting a channel end expression.");

        Type t = resolve(ce.channel().visit(this));

        // Handle error types.
        if (t instanceof ErrorType) {
            ce.type = t;
            Log.log(ce.line + ": Channel End Expr has type: " + ce.type);
            return ce.type;
        }

        // Expression must be of ChannelType type.
        if (!(t instanceof ChannelType)) {
            ce.type = new ErrorType();
            // !!Error.addError(ce, "Channel end expression requires channel type.", 3019);
            Log.log(ce.line + ": Channel End Expr has type: " + ce.type);
            return ce.type;
        }

        // Now create a ChannelEndType based on the sharing attibutes of the channel
        // type.
        ChannelType ct = (ChannelType) t;
        int end = (ce.isRead() ? ChannelEndType.READ_END : ChannelEndType.WRITE_END);

        // Channel has no shared ends.
        if (ct.isShared() == ChannelType.NOT_SHARED)
            ce.type = new ChannelEndType(ChannelEndType.NOT_SHARED, ct.getComponentType(), end);

            // Channel has both ends shared, create a shared ChannelEndType.
        else if (ct.isShared() == ChannelType.SHARED_READ_WRITE)
            ce.type = new ChannelEndType(ChannelEndType.SHARED, ct.getComponentType(), end);

            // Channel has read end shared; if .read then create a shared channel end,
            // otherwise create a non-shared one.
        else if (ct.isShared() == ChannelType.SHARED_READ)
            ce.type = new ChannelEndType((ce.isRead() && ct.isShared() == ChannelType.SHARED_READ) ? ChannelEndType.SHARED
                    : ChannelType.NOT_SHARED, ct.getComponentType(), end);

            // Channel has write end shared; if .write then create a shared chanenl end,
            // otherwise create a non-shared one.
        else if (ct.isShared() == ChannelType.SHARED_WRITE)
            ce.type = new ChannelEndType(
                    (ce.isWrite() && ct.isShared() == ChannelType.SHARED_WRITE) ? ChannelEndType.SHARED
                            : ChannelType.NOT_SHARED,
                    ct.getComponentType(), end);

        else {
            // Techinically we should never be able to reach this code.
            ce.type = new ErrorType();
            // !!Error.addError(ce, "Unknown sharing status for channel end expression.",
            // 3020);
        }

        Log.log(ce.line + ": Channel End Expr has type: " + ce.type);
        return ce.type;

    }

    // Syntax: Expr.read()
    // Expr.read({...})
    @Override
    public Type visitChannelReadExpr(ChannelReadExpr cr) {
        Log.log(cr.line + ": Visiting a channel read expression.");

        // TODO: targetType MAY be a channelType:

        // For ease of use, the following code should be legal:
        // chan<int> c;
        // c.read();

        // Only channel ends, timers, and channels [see above] can be read.
        Type targetType = resolve(cr.channel().visit(this));
        if (!(targetType instanceof ChannelEndType || targetType.isTimerType() || targetType instanceof ChannelType)) {
            cr.type = new ErrorType();
            // Error.addError(cr, "Channel or Timer type required in channel/timer read.",
            // 3021);
            Log.log(cr.line + ": Channel read expression has type: " + cr.type);
            return cr.type;
        }

        // Construct the appropriate type depending on what is being read.
        if (targetType instanceof ChannelEndType) {
            ChannelEndType cet = (ChannelEndType) targetType;
            cr.type = cet.getComponentType();
        } else if (targetType instanceof ChannelType) {
            cr.type = ((ChannelType) targetType).getComponentType();
        } else {
            // Must be a time type, and timer read() returns values of type long.
            cr.type = new PrimitiveType(PrimitiveType.LongKind);
        }

        // Check that timer reads do not have an extended rendez-vous.
        if (targetType.isTimerType() && cr.extRV() != null) {
            // Don't generate an error type, just produce an error and keep going.
            // !!Error.addError(cr, "Timer read cannot have extended rendez-vous block.",
            // 3022);
        }

        // If there is an extended rendez-vous block and we are not reading from a
        // timer, check it.
        if (cr.extRV() != null && !targetType.isTimerType()) {
            cr.extRV().visit(this);
        }

        Log.log(cr.line + ": Channel read expression has type: " + cr.type);
        return cr.type;
    }

    @Override
    public Type visitInvocation(Invocation in) {

        Log.log(in.line + ": visiting invocation (" + in.procedureName() + ")");

        in.params().visit(this);

        // id::f(...)
        // id.id::f(...)
        // ...
        // id.id....id::f(...)
        // should be looked up directly in the appropriate symbol table and candidates
        // should only be taken from there.

        // TODO: this should be redone!!!
        boolean firstTable = true;
        SymbolTable st = topLevelDecls;
        Sequence<ProcTypeDecl> candidateProcs = new Sequence<ProcTypeDecl>();

        // Find all possible candidate procedures.
        // This should be procedures of the right name and the right number of
        // paremeters from:
        // - The top level symbol table (the one associated with the file passed to the
        // compiler.
        // - The symbol tables of any files imported by the top level file, but NOT what
        // they import.
        while(st != null) {
            SymbolTable procs = (SymbolTable) st.getShallow(in.procedureName().getname());
            if(procs != null) {
                for(Object pd: procs.entries.values().toArray()) {
                    ProcTypeDecl ptd = (ProcTypeDecl) pd;
                    if(ptd.formalParams().size() == in.params().size()) {
                        // TODO: this should store this somewhere
                        boolean candidate = true;
                        Log.log(" checking if Assignment Compatible proc: " + ptd + " ( " + ptd.getSignature()
                                + " ) ");
                        for (int i = 0; i < in.params().size(); i++) {

                            ptd.formalParams().child(i).visit(this);
                            in.params().child(i).visit(this);

                            candidate &= ptd.formalParams().child(i).type()
                                    .typeAssignmentCompatible(in.params().child(i).type);

                            //candidate = candidate && resolve(ptd.formalParams().child(i).type())
                                    //.typeAssignmentCompatible(resolve(in.params().child(i).type));
                        }
                        System.out.println("Here");
                        if (candidate) {
                            // System.out.println("Candidate kept");
                            candidateProcs.append(ptd);
                            Log.log("Possible proc: " + ptd + " " + ptd.formalParams());
                        } else
                            System.out.println("Candidate thrown away");
                    }

                }
            }

            if (firstTable)
                st = st.getImportParent();
            else
                st = st.getParent();
            firstTable = false;
        }

        Log.log("Found these candidates: ");
        Log.log("| " + candidateProcs.size() + " candidate(s) were found:");
        for (int i = 0; i < candidateProcs.size(); i++) {
            ProcTypeDecl pd = candidateProcs.child(i);
            Log.logNoNewline("|   " + in.procedureName().getname() + "(");
            Log.logNoNewline(pd.getSignature());
            Log.log(" )");
        }

        Log.log("" + candidateProcs.size());
        int noCandidates = candidateProcs.size();

        if (noCandidates == 0) {
            Error.error(in, "No suitable procedure found.", false, 3037);
            return null;
        } else if (noCandidates > 1) {
            // Iterate through the list of potential candidates
            for (int i = 0; i < candidateProcs.size(); i++) {
                // Take the i'th one out
                ProcTypeDecl ptd1 = candidateProcs.child(i);

                // Tf this proc has been removed - continue.
                if (ptd1 == null)
                    continue;
                // Temporarily remove ptd from candidateprocs so we
                // don't find it again in the next loop
                candidateProcs.set(i, null);
                // compare to all other candidates ptd2.
                for (int j = 0; j < candidateProcs.size(); j++) {
                    ProcTypeDecl ptd2 = candidateProcs.child(j);
                    // if the proc was already removed - continue on
                    if (ptd2 == null)
                        continue;
                    //
                    boolean candidate = true;
                    // grab all the parameters of ptd1 and ptd2
                    Sequence<ParamDecl> ptd1Params = ptd1.formalParams();
                    Sequence<ParamDecl> ptd2Params = ptd2.formalParams();

                    // now check is ptd2[k] :> ptd1[k] for all k. If it does remove ptd2.
                    // check each parameter in turn
                    for (int k = 0; k < ptd1Params.nchildren; k++) {
                        candidate = candidate && (resolve(((ParamDecl) ptd2Params.child(k)).type()))
                                .typeAssignmentCompatible(resolve(((ParamDecl) ptd1Params.child(k)).type()));

                        if (!candidate)
                            break;
                    }
                    if (candidate) {
                        // ptd1 is more specialized than ptd2, so throw ptd2 away.
                        Log.logNoNewline("|   " + in.procedureName().getname() + "(");
                        Log.logNoNewline(ptd2.getSignature());
                        Log.logNoNewline(" ) is less specialized than " + in.procedureName().getname() + "(");
                        Log.logNoNewline(ptd1.getSignature());
                        Log.log(" ) and is thus thrown away!");
                        // Remove ptd2
                        candidateProcs.set(j, null);
                        noCandidates--;
                    }
                }
                // now put ptd1 back in to candidateProcs
                candidateProcs.set(i, ptd1);
            }
        }
        if (noCandidates != 1) {
            // we found more than one!
            Log.log("| " + candidateProcs.size() + " candidate(s) were found:");
            for (int i = 0; i < candidateProcs.size(); i++) {
                ProcTypeDecl pd = candidateProcs.child(i);
                if (pd != null) {
                    Log.logNoNewline("|   " + in.procedureName().getname() + "(");
                    Log.logNoNewline(pd.getSignature());
                    Log.log(" )");
                }
            }
            Error.addError(in, "Found more than one candidate - cannot chose between them!", 3038);
            return null;
        } else {
            // we found just one!
            Log.log("| We were left with exactly one candidate to call!");
            Log.log("+------------- End of findMethod --------------");
            for (int i = 0; i < candidateProcs.size(); i++)
                if (candidateProcs.child(i) != null) {
                    in.targetProc = candidateProcs.child(i);
                    in.type = in.targetProc.returnType();
                }
        }
        Log.log("myPackage: " + in.targetProc.myPackage);
        Log.log(in.line + ": invocation has type: " + in.type);
        return in.type;
    }

    @Override
    public Type visitNameExpr(NameExpr ne) {

        Log.log(ne.line + ": Visiting a Name Expression (" + ne.name().getname() + ").");

        if (ne.myDecl instanceof LocalDecl || ne.myDecl instanceof ParamDecl || ne.myDecl instanceof ConstantDecl) {
            // TODO: what about ConstantDecls ???
            // TODO: don't think a resolve is needed here
            ne.type = resolve(((VarDecl) ne.myDecl).type());
        } else
            ne.type = Error.addError(ne, "Unknown name expression '" + ne.name().getname() + "'.", 3029);

        Log.log(ne.line + ": Name Expression (" + ne.name().getname() + ") has type: " + ne.type);
        return ne.type;

    }

    @Override
    public Type visitNewArray(NewArray ne) {
        Log.log(ne.line + ": Visiting a NewArray " + ne.dimsExpr().size() + " " + ne.dims().size());

        // check that each dimension is of integer type
        for (Expression exp : ne.dimsExpr()) {
            Type dimT = resolve(exp.visit(this));
            if (!dimT.isIntegralType())
                Error.addError(exp, "Array dimension must be of integral type.", 3031);
        }
        // if there is an initializer, then make sure it is of proper and equal depth.
        ne.type = new ArrayType(ne.baseType(), ne.dims().size() + ne.dimsExpr().size());
        if (ne.init() != null) {
            // The elements of ne.init() get visited in the last line of
            // arrayAssignmentCompatible.
            if (!arrayAssignmentCompatible(ne.type, ne.init()))
                Error.addError(ne, "Array Initializer is not compatible with type '" + ne.type + "'.", 3032);
            ne.init().type = ne.type;
        }
        Log.log(ne.line + ": NewArray type is " + ne.type);
        return ne.type;
    }

    @Override
    public Type visitRecordAccess(RecordAccess ra) {
        Log.log(ra.line + ": visiting a record access expression (" + ra.field().getname() + ")");
        Type tType = resolve(ra.record().visit(this));
        tType = tType.visit(this);

        // TODO: size of strings.... size()? size? or length? for now: size() -> see
        // visitInvocation

        // Array lengths can be accessed through a length 'field'.
        if ((tType instanceof ArrayType) && ra.field().getname().equals("size")) {
            ra.type = new PrimitiveType(PrimitiveType.IntKind);
            ra.isArraySize = true;
            Log.log(ra.line + ": Array size expression has type: " + ra.type);
            return ra.type;
        }

        if (tType.isStringType() && ra.field().getname().equals("length")) {
            ra.type = new PrimitiveType(PrimitiveType.LongKind); // TODO: should this be long ???
            ra.isStringLength = true;
            Log.log(ra.line + ": string length expression has type: " + ra.type);
            return ra.type;
        } else {
            if (!(tType instanceof RecordTypeDecl || tType instanceof ProtocolTypeDecl)) {
                ra.type = Error.addError(ra,
                        "Request for member '" + ra.field().getname() + "' in something not a record or protocol type.",
                        3061);
                return ra.type;
            }
            // tType can be a record and it can be a protocol:
            if (tType instanceof RecordTypeDecl) {
                // Find the field and make the type of the record access equal to the field.
                // TODO: test inheritence here
                RecordMember rm = ((RecordTypeDecl) tType).getMember(ra.field().getname());

                if (rm == null) {
                    ra.type = Error.addError(ra, "Record type '" + tType
                            + "' has no member '" + ra.field().getname() + "'.", 3062);
                    return ra.type;
                }
                // System.out.println("RM.type: " + rm.type());

                Type rmt = resolve(rm.type());
                ra.type = rmt;
            } else {
                // Must be a protocol type.
                //
                // switch statements cannot be nested on the same protocol!
                // Keep a hashtable of the protocols we have switched on - and their tags!
                //
                // | protocol Name | -> ProtocolCase
                //
                // We have <expr>.<field> that means a field <field> in the tag associated with
                // the type of <expr>.

                ProtocolTypeDecl pt = (ProtocolTypeDecl) tType;

                if (!protocolsSwitchedOn.contains(pt.toString())) {
                    Error.addError(pt, "Illegal access to non-switched protocol type '" + pt + "'.", 0000);
                    ra.type = new ErrorType();
                    Log.log(ra.line + ": record access expression has type: " + ra.type);
                    return ra.type;
                }
                // Lookup the appropriate ProtocolCase associated with the protocol's name in
                // protocolTagsSwitchedOn

                // System.out.println("HashSet: "+protocolsSwitchedOn);

                // System.out.println("[RecordAccess]: " + pt.name().getname());
                ProtocolCase pc = protocolTagsSwitchedOn.get(pt.toString());

                // System.out.println("[RecordAccess]: Found protocol tag: " +
                // pc.name().getname());

                String fieldName = ra.field().getname();
                // there better be a field in pc that has that name!
                boolean found = false;

                if (pc != null)
                    for (RecordMember rm : pc.body()) {
                        Log.log("Looking at field " + rm.name().getname());
                        if (rm.name().getname().equals(fieldName)) {
                            // yep we found it; now set the type
                            // System.out.println("FOUND IT - it is type " + rm.type());
                            Type rmt = resolve(rm.type());
                            ra.type = rmt;
                            found = true;
                            break;
                        }
                    }
                if (!found) {
                    Error.addError(ra, "Unknown field reference '" + fieldName + "' in protocol tag '"
                            + pt + "' in protocol '" + pt + "'.", 3073);
                    ra.type = new ErrorType();
                }
            }
        }
        Log.log(ra.line + ": record access expression has type: " + ra.type);
        return ra.type;
    }

    @Override
    public Type visitTernary(Ternary te) {
        Log.log(te.line + ": Visiting a ternary expression");

        Type eType = resolve(te.expr().visit(this));
        Type trueBranchType = te.trueBranch().visit(this);
        Type falseBranchType = te.falseBranch().visit(this);

        if (!eType.isBooleanType())
            Error.addError(te, "Non-boolean Expression (" + eType + ") found as test in ternary expression.",
                    3070);

        // e ? t : f
        // Primitive?(Type(t)) & Primitive?(Type(f)) & (Type(t) :=T Type(f) || Type(f)
        // :=T Type(t)) =>
        // Type(e ? t : f) = ceiling(Type(t), Type(f))
        if (trueBranchType instanceof PrimitiveType && falseBranchType instanceof PrimitiveType) {
            if (falseBranchType.typeAssignmentCompatible(trueBranchType)
                    || trueBranchType.typeAssignmentCompatible(falseBranchType))
                te.type = ((PrimitiveType) trueBranchType).typeCeiling((PrimitiveType) falseBranchType);
            else
                Error.addError(te, "Both branches of a ternary expression must be of assignment compatible types.",
                        3071);
        } else if (trueBranchType instanceof ProtocolTypeDecl && falseBranchType instanceof ProtocolTypeDecl) {
            te.type = null; // TODO
        } else if (trueBranchType instanceof RecordTypeDecl && falseBranchType instanceof RecordTypeDecl) {
            te.type = null; // TODO
        } else if ((trueBranchType instanceof ArrayType) && (falseBranchType instanceof ArrayType)) {
            te.type = null; // TODO
            // if both are of primitive type they must be the same. if they are of record
            // type or protocol type use the inheritance rules for those.
        } else
            Error.addError(te, "Both branches of a ternary expression must be of assignment compatible types.", 3072);

        Log.log(te.line + ": Ternary has type: " + te.type);
        return te.type;
    }

    @Override
    public Type visitUnaryPostExpr(UnaryPostExpr up) {
        Log.log(up.line + ": Visiting a unary post expression");
        up.type = null;
        Type eType = resolve(up.expr().visit(this));

        // TODO: what about protocol ?? Must be inside the appropriate case.
        if (up.expr() instanceof NameExpr || up.expr() instanceof RecordAccess
                || up.expr() instanceof ArrayAccessExpr) {
            if (!eType.isIntegralType() && !eType.isDoubleType() && !eType.isFloatType())
                up.type = Error.addError(up,
                        "Cannot apply operator '" + up.opString() + "' to something of type " + eType + ".",
                        3051);
        } else
            up.type = Error.addError(up, "Variable expected, found value.", 3055);

        // No errors found, set type.
        if (up.type == null)
            up.type = eType;

        Log.log(up.line + ": Unary Post Expression has type: " + up.type);
        return up.type;
    }

    @Override
    public Type visitUnaryPreExpr(UnaryPreExpr up) {
        Log.log(up.line + ": Visiting a unary pre expression");
        up.type = null;
        Type eType = resolve(up.expr().visit(this));

        switch (up.op()) {
            case UnaryPreExpr.PLUS:
            case UnaryPreExpr.MINUS:
                if (!eType.isNumericType())
                    up.type = Error.addError(up,
                            "Cannot apply operator '" + up.opString() + "' to something of type " + eType + ".",
                            3052);
                break;
            case UnaryPreExpr.NOT:
                if (!eType.isBooleanType())
                    up.type = Error.addError(up, "Cannot apply operator '!' to something of type " + eType + ".",
                            3053);
                break;
            case UnaryPreExpr.COMP:
                if (!eType.isIntegralType())
                    up.type = Error.addError(up, "Cannot apply operator '~' to something of type " + eType + ".",
                            3054);
                break;
            case UnaryPreExpr.PLUSPLUS:
            case UnaryPreExpr.MINUSMINUS:
                // TODO: protocol access
                if (!(up.expr() instanceof NameExpr) && !(up.expr() instanceof RecordAccess)
                        && !(up.expr() instanceof ArrayAccessExpr))
                    up.type = Error.addError(up, "Variable expected, found value.", 3057);

                if (!eType.isNumericType() && up.type == null)
                    up.type = Error.addError(up,
                            "Cannot apply operator '" + up.opString() + "' to something of type " + eType + ".",
                            3056);
                break;
        }
        // No error was found, set type.
        if (up.type == null)
            up.type = eType;
        Log.log(up.line + ": Unary Pre Expression has type: " + up.type);
        return up.type;
    }

    // ArrayLiteral
    //
    // Syntax: { ... }
    //
    @Override
    public Type visitArrayLiteral(ArrayLiteral al) {

        Log.log(al.line + ": visiting an array literal.");

        // Array Literals cannot appear without a 'new' keyword.
//        CompilerErrorManager.INSTANCE.reportMessage(
//                new ProcessJMessage.Builder().addAST(al).addError(VisitorMessageNumber.TYPE_CHECKER_656).build(),
//                MessageType.PRINT_CONTINUE);

        return null;

    }

    @Override
    public Type visitPrimitiveLiteral(PrimitiveLiteral pl) {
        Log.log(pl.line + ": Visiting a Primitive Literal (" + pl.getText() + ")");

        pl.type = new PrimitiveType(pl.getKind());

        Log.log(pl.line + ": Primitive Literal has type: " + pl.type);
        return pl.type;
    }

    @Override
    public Type visitProtocolLiteral(ProtocolLiteral pl) {
        Log.log(pl.line + ": Visiting a protocol literal");

        pl.type = pl.myTypeDecl;
        return pl.myTypeDecl;

        // Validity of the tag was already checked in NameChecker.

        // TODO: below code is incorrect as it does not take 'extends' into account

        // Name{ tag: exp_1, exp_2, ... ,exp_n }
        /*
         * ProtocolCase pc = pl.myChosenCase; ProtocolTypeDecl pd = pl.myTypeDecl; if
         * (pc.body().size() != pl.expressions().size()) Error.addError(pl,
         * "Incorrect number of expressions in protocol literal '" + pd.name().getname()
         * + "'.", 3033); for (int i = 0; i < pc.body().size(); i++) { Type eType =
         * resolve(pl.expressions().child(i).visit(this)); Type vType =
         * resolve(((RecordMember) pc.body().child(i)).type()); Name name =
         * ((RecordMember) pc.body().child(i)).name(); if
         * (!vType.typeAssignmentCompatible(eType)) Error.addError(pl,
         * "Cannot assign value of type '" + eType + "' to protocol field '" +
         * name.getname() + "' of type '" + vType + "'.", 3034); } Log.log(pl.line +
         * ": protocol literal has type: " + pl.myTypeDecl); return pl.myTypeDecl;
         */
    }

    @Override
    public Type visitRecordLiteral(RecordLiteral rl) {
        Log.log(rl.line + ": visiting a record literal (" + rl.name().getname() + ").");
        RecordTypeDecl rt = rl.myTypeDecl;

        // TODO: be careful here if a record type extends another record type, then the
        // record literal must contains
        // expressions for that part too!!!

        return rt;
    }

    /// ------------------------------------------------------------------------------------------- ///
    /// Types                                                                                       ///
    /// ------------------------------------------------------------------------------------------- ///

    // ArrayType
    //
    // An ArrayType's type is itself.
    @Override
    public Type visitArrayType(ArrayType at) {
        Log.log(at.line + ": Visiting an ArrayType");
        Log.log(at.line + ": ArrayType has type " + at);
        return at;
    }

    @Override
    public Type visitChannelType(ChannelType ct) {
        Log.log(ct.line + ": Visiting a channel type.");
        ct.getComponentType().visit(this);
        Log.log(ct.line + ": Channel type has type: " + ct);
        return ct;
    }

    @Override
    public Type visitChannelEndType(ChannelEndType ct) {
        Log.log(ct.line + ": Visiting a channel end type.");
        ct.getComponentType().visit(this);
        Log.log(ct.line + ": Channel end type " + ct);
        return ct;
    }

    @Override
    public Type visitExternType(ExternType et) {
        Log.log(et.line + ": Visiting an extern type");
        Log.log(et.line + ": Extern type has type: " + et);
        // The type of an external type is itself.
        return et;
    }

    @Override
    public Type visitPrimitiveType(PrimitiveType pt) {
        Log.log(pt.line + ": Visiting a Primitive type.");
        Log.log(pt.line + ": Primitive type has type: " + pt);
        return pt;
    }

    @Override
    public Type visitNamedType(NamedType namedType) {

        // TODO: NamedType should already have a Type bound to it by now. Instead of searching scopes, to bind the Type
        // TODO: The pointed Type's Compilation must already be checked or in progress; otherwise we will infinite loop


        return namedType.getType();

    }

    @Override
    public Type visitVar(Var va) {
        Log.log(va.line + ": Visiting a var (" + va.name().getname() + ").");

        if (va.init() != null) {

            Type vType = resolve(va.myDecl.type());
            Type iType = resolve(va.init().visit(this));

            if(vType instanceof ErrorType || iType instanceof ErrorType)
                return null;

            if(!vType.typeAssignmentCompatible(iType))
                Error.error(va, "Cannot assign value of type " + iType + " to variable of type "
                        + vType + ".", false, 3058);
        }
        return null;
    }

    public Type resolve(final Type type) {

        Log.log("  > Resolve: " + type);

        if(!(type instanceof NamedType))
            Log.log("  > Nothing to resolve - type remains: " + type);

        // A named type must resolve to an actual type; visit the NamedType to resolve it
        return (type instanceof NamedType) ? type.visit(this) : type;

    }

    public boolean arrayAssignmentCompatible(Type t, Expression e) {
        if (t instanceof ArrayType && e instanceof ArrayLiteral) {
            ArrayType at = (ArrayType) t;
            e.type = at; // we don't know that this is the type - but if we make it through it will be!
            ArrayLiteral al = (ArrayLiteral) e;

            // t is an array type i.e. XXXXXX[ ]
            // e is an array literal, i.e., { }
            if (al.elements().size() == 0) // the array literal is { }
                return true; // any array variable can hold an empty array
            // Now check that XXXXXX can hold value of the elements of al
            // we have to make a new type: either the base type if |dims| = 1
            boolean b = true;
            for(int i = 0; i < al.elements().size(); i++) {



                if (at.getDepth() == 1)
                    b = b && arrayAssignmentCompatible(at.getComponentType(), (Expression) al.elements().child(i));
                else {
                    ArrayType at1 = new ArrayType(at.getComponentType(), at.getDepth() - 1);
                    b = b && arrayAssignmentCompatible(at1, (Expression) al.elements().child(i));
                }
            }
            return b;
        } else if (t instanceof ArrayType && !(e instanceof ArrayLiteral))
            Error.addError(t, "Cannot assign non-array to array type '" + t + "'", 3039);
        else if (!(t instanceof ArrayType) && (e instanceof ArrayLiteral))
            Error.addError(t,
                    "Cannot assign value '" + e + "' to type '" + t + "'.",
                    3030);
        return t.typeAssignmentCompatible(e.visit(this));
    }

}
