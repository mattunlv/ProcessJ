package rewriters;

import ast.*;
import utilities.Log;
import utilities.Visitor;

public class IOCallsRewrite extends Visitor<AST> {

    public IOCallsRewrite() {
        Log.logHeader("****************************************");
        Log.logHeader("*   I O   C A L L S   R E W R I T E    *");
        Log.logHeader("****************************************");
    }

    @Override
    public AST visitInvocation(Invocation in) {
        Log.log(in, "Attempting to rewrite invocation of " + in.toString());

        if (in.targetProc != null) {
            Log.log(in,"targetProc is " + in.targetProc.name());
            if ( in.targetProc.filename != null                    &&
                 in.targetProc.filename.equals("io")               &&
                (in.targetProc.name().toString().equals("println") ||
                 in.targetProc.name().toString().equals("print"))) {
                Log.log("This is the function we're looking for");
            } else {
                return in;
            }
        }

        if (in.params() != null) {
            if(in.params().size() == 0) {
                Log.log(in, "Params are empty");
                return in;
            }

            boolean rewritten = false;
            Sequence<Expression> params = in.params();
            Sequence<Expression> newParams = new Sequence<Expression>();
            Log.log(in, "Invocation of " + in.toString() + " has argument(s):");
            for (int i = 0; i < params.size(); ++i) {
                if (params.child(i) instanceof BinaryExpr) {
                    Log.log(in, params.child(i).toString());
                    if (checkForString((BinaryExpr)params.child(i)) == true) {
                        rewritten = true;
                        Log.log(in, "string concatenation found, rewriting.");
                        newParams.merge(rebuildParams(extractParams((BinaryExpr)params.child(i))));
                    }
                }
            }

            Log.log(in, "Received params from extractParams():");
            for (int i = 0; i < newParams.size(); ++i) {
                Log.log(in, newParams.child(i).toString());
            }

            // TODO: is this appropriate? is there another way?
            if(rewritten == true) {
                in.children[2] = newParams;
            }
        }

        return in;
    }

    public Sequence<Expression> extractParams(BinaryExpr be) {
        Log.log(be, "Extracting Parameters from " + be.toString());

        Log.log(be, "Left is " + be.left().toString() + ", right is " + be.right().toString());

        Sequence<Expression> newParams = new Sequence<Expression>();

        if (be.left() instanceof BinaryExpr) {
            newParams.merge(extractParams((BinaryExpr)be.left()));
        } else if (be.left() instanceof PrimitiveLiteral || be.left() instanceof ArrayAccessExpr) {
            newParams.append(be.left());
        }

        newParams.append(be.right());

        return newParams;
    }

    public Sequence<Expression> rebuildParams(Sequence<Expression> params) {
        Log.log("rebuilding params from sequence:");
        for (int i = 0; i < params.size(); ++i) {
            Log.log(params.child(i).toString());
        }

        Sequence<Expression> rebuiltParams = new Sequence<Expression>();
        Expression left = null;
        BinaryExpr be = null;

        // parse the params and rebuilt our arguments the correct way
        for (int i = 0; i < params.size(); ++i) {
            Log.log("Checking " + params.child(i));
            if (params.child(i) instanceof PrimitiveLiteral &&
                ((PrimitiveLiteral)params.child(i)).getKind() == PrimitiveLiteral.StringKind) {
                Log.log(params.child(i).toString() + " is string PrimitiveLiteral, appending as argument");
                if (be != null) {
                    Log.log("Appending new BinaryExpr " + be.toString() + " to rebuiltParams");
                    rebuiltParams.append(be);
                    be = null;
                } else if (left != null) {
                    Log.log("Appending unfinished lhs of BinaryExpr " + left.toString() + " to rebuiltParams");
                    rebuiltParams.append(left);
                    left = null;
                }
                Log.log("Appending " + params.child(i).toString() + " to rebuiltParams");
                rebuiltParams.append(params.child(i));
            } else if (params.child(i) instanceof ArrayAccessExpr) {
                Type t = getArrayType(((ArrayAccessExpr)params.child(i)).target());
                if (t instanceof NamedType) {
                        Log.log("array of NamedTypes");
                    } else if (t instanceof PrimitiveType) {
                        Log.log("array of PrimitiveTypes");
                        if (((PrimitiveType)t).getKind() == PrimitiveType.StringKind) {
                            Log.log(params.child(i).toString() + " is string PrimitiveLiteral");
                            if (be != null) {
                                Log.log("Appending to new BinaryExpr " + be.toString() + " to rebuiltParams");
                                rebuiltParams.append(be);
                                be = null;
                            } else if (left != null) {
                                Log.log("Appending unfinished lhs of BinaryExpr " + left.toString() + " to rebuiltParams");
                                rebuiltParams.append(left);
                                left = null;
                            }
                            Log.log("Appending " + params.child(i).toString() + " to rebuiltParams");
                            rebuiltParams.append(params.child(i));
                        } else {
                            Log.log(params.child(i).toString() + " is not string PrimitiveLiteral");
                            if (be != null) {
                                Log.log("Appending " + params.child(i).toString() + " to existing binaryExpr");
                                be = new BinaryExpr(be, params.child(i), BinaryExpr.PLUS);
                            } else if (left != null) {
                                Log.log("Appending " + params.child(i).toString() + " as rhs of new binaryExpr");
                                be = new BinaryExpr(left, params.child(i), BinaryExpr.PLUS);
                            } else if (i == params.size() - 1) {
                                Log.log("Last item in params, adding as next argument");
                                rebuiltParams.append(params.child(i));
                            } else {
                                Log.log("Adding " + params.child(i).toString() + " as lhs of potential BinaryExpr");
                                left = params.child(i);
                            }
                        }
                    }
            } else {
                Log.log(params.child(i).toString() + " is not string PrimitiveLiteral");
                if (be != null) {
                    Log.log("Appending " + params.child(i).toString() + " to existing binaryExpr");
                    be = new BinaryExpr(be, params.child(i), BinaryExpr.PLUS);
                } else if (left != null) {
                    Log.log("Appending " + params.child(i).toString() + " as rhs of new binaryExpr");
                    be = new BinaryExpr(left, params.child(i), BinaryExpr.PLUS);
                } else if (i == params.size() - 1) {
                    Log.log("Last item in params, adding as next argument");
                    rebuiltParams.append(params.child(i));
                } else {
                    Log.log("Adding " + params.child(i).toString() + " as lhs of potential BinaryExpr");
                    left = params.child(i);
                }
            }
        }

        return rebuiltParams;
    }

    public boolean checkForString(BinaryExpr be) {
        Log.log(be, "Checking for string concatenation versus addition");

        // Check if the lhs is a string
        if (be.left() instanceof PrimitiveLiteral) {
            if(((PrimitiveLiteral)be.left()).getKind() == PrimitiveLiteral.StringKind) {
                return true;
            }
        }
        // Now the rhs
        if (be.right() instanceof PrimitiveLiteral) {
            if(((PrimitiveLiteral)be.right()).getKind() == PrimitiveLiteral.StringKind) {
                return true;
            }
        }

        // recursively check on left and right
        boolean left = false;
        boolean right = false;

        if (be.left() instanceof BinaryExpr) {
            left = checkForString((BinaryExpr)be.left());
        }

        if(be.right() instanceof BinaryExpr) {
            right = checkForString((BinaryExpr)be.right());
        }

        return false | left | right;
    }

    public Type getArrayType(Expression target) {
        Log.log("Finding base type of n-dimensional array");
        Log.log("target of element is " + target.toString());
        if (target instanceof ArrayAccessExpr) {
            return getArrayType(((ArrayAccessExpr)target).target());
        }
        Type t = null;
        if (target instanceof NameExpr) {
            Log.log("target of element is a NameExpr");
            AST md = ((NameExpr)target).myDecl;
            Log.log("myDecl is " + md.toString());
            if (md instanceof LocalDecl) {
                t = ((ArrayType)((LocalDecl)md).type()).getActualBaseType();
                Log.log("md instanceof LocalDecl: " + t.toString());
            } else if (md instanceof ParamDecl) {
                t = ((ArrayType)((ParamDecl)md).type()).getActualBaseType();
                Log.log("md instanceof ParamDecl: " + t.toString());
            }
        }

        return t;
    }
}
