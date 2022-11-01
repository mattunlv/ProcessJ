package rewriters;

import ast.*;
import utilities.Visitor;
import printers.*;
import utilities.PJMessage;
import utilities.PJBugManager;
import utilities.MessageType;
import utilities.VisitorMessageNumber;
import utilities.Error;

/**
 * The purpose of this rewriter is to lift yielding Channel Read Expressions out
 * of other expressions. For example:
 *
 * x = c.read() + 2;
 *
 * causes a problem because the code generated for the 'c.read' expression
 * yields. This is solved by turning all channel read expressions into simple
 * assignments with simple (NameExpr) target channels (the channel expression
 * from which the read happens. The above example generates:
 *
 * temp_0 = c.read(); x = temp_0 + 2;
 *
 * Errors generated in this file:
 *
 * 1000 ->
 */

public class ChannelReadRewrite {
    private int tempCounter = 0;

    private String nextTemp() {
        return "temp_" + tempCounter++;
    }

    private LocalDecl makeLocalDecl(Type t, String name) {
        return new LocalDecl(t, new Var(new Name(name), null), false);
    }

    private Assignment makeAssignment(String v, Expression e) {
        return new Assignment(new NameExpr(new Name(v)), e, Assignment.EQ);
    }

    public class Pair {
        private Sequence<Statement> statements;
        private Expression expression;

        public Pair(Sequence<Statement> statements, Expression expression) {
            this.statements = statements;
            this.expression = expression;
        }

        public Pair(Sequence<Statement> statements) {
            this.statements = statements;
            this.expression = null;
        }

        public Pair(Expression expression) {
            this.statements = new Sequence();
            this.expression = expression;
        }

        public Pair appendStatement(Sequence<Statement> sequence) {
            statements.merge(sequence);
            return this;
        }

        public Pair appendStatement(Statement statement) {
            statements.append(statement);
            return this;
        }

        public Pair setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        public Sequence<Statement> getStatements() {
            return statements;
        }

        public Expression getExpression() {
            return expression;
        }
    }

    // this rewriter returns a Sequence of two elements: [ Sequence<Statement>,
    // Expression ]
    // @0 is the extra generated statements
    // @1 is the expression that the rewrite resulted in, if any.
    //
    // if @0 is empty, then @1 is the entire result.
    // if @1 is null, then no expression was returned.

    private void print(String msg, AST node) {
        System.out.println(msg + ":");
        node.visit(new printers.ParseTreePrinter());
        node.visit(new printers.PrettyPrinter());
        System.out.println();
        System.out.println(msg + "--- DONE ---");
        System.out.println();
        System.out.println();
    }

    public AST go(AST a) {
        if (a instanceof AltCase) {
            return a;
        } else if (a instanceof AltStat) { // alt { ... }
            // TODO: handle the replicated alt
            // visit the childre
            AltStat as = (AltStat) a;
            for (int i = 0; i < as.body().size(); i++)
                go(as.body().child(i));
            return as;
        } else if (a instanceof Annotation) { // ??
            return a;
        } else if (a instanceof Annotations) { // ??
            return a;
        } else if (a instanceof ArrayAccessExpr) { // e1[e2]
            ArrayAccessExpr aae = (ArrayAccessExpr) a;
            if (aae.target().doesYield() && !aae.index().doesYield()) {
                System.out.println("ArrayAccessExpr (Case 1)");
                // e1[e2], if e1 yields
                // return [ [T t; <t = e1>;], t1[e2] ], <...> means rewritten
                String t = nextTemp();
                // T t; (T is the type of e1)
                LocalDecl ld = makeLocalDecl(aae.target().type, t);
                // t = e1;
                // rewrite 't = e1' -- e1 may be complex => [ {...}, null ]
                Sequence re = (Sequence) go(new ExprStat(makeAssignment(t, aae.target())));
                Sequence bl = (Sequence) re.child(0); // t = ...; will be the last in this sequence
                // make t[e2]
                ArrayAccessExpr aae2 = new ArrayAccessExpr(new NameExpr(new Name(t)), aae.index());

                // now make [ [ld; stuff from bl;], aae2 ]
                Sequence s = new Sequence(ld);
                // merge the stats from the block
                s.merge(bl);
                Sequence retSeq = new Sequence(s);
                retSeq.append(aae2);
                return retSeq;
            } else if (aae.index().doesYield()) {
                System.out.println("ArrayAccessExpr (Case 2)");
                // e1[e2], if e1 yields
                // return [ [T1 t1; T2 t2; <t1 = e1>; <t2 = e2>], t1[t2] ], <...> means
                // rewritten
                String t1 = nextTemp();
                String t2 = nextTemp();
                // T1 t1; (T1 is the type of e1)
                LocalDecl ld1 = makeLocalDecl(aae.target().type, t1);
                // T2 t2; (T2 is the type of e2)
                LocalDecl ld2 = makeLocalDecl(aae.index().type, t2);
                // t1 = e1;
                // t2 = e2;
                // rewrite 't1 = e1' -- e1 may be complex => [ {...}, null ]
                // rewrite 't2 = e2' -- e2 may be comples => [ {...}, null ]
                Sequence re1 = (Sequence) go(new ExprStat(makeAssignment(t1, aae.target())));
                Sequence re2 = (Sequence) go(new ExprStat(makeAssignment(t2, aae.index())));
                Sequence bl1 = (Sequence) re1.child(0); // t1 = ...; will be the last in this block
                Sequence bl2 = (Sequence) re2.child(0); // t2 = ...; will be the last in this block
                // make t1[t2]
                ArrayAccessExpr aae2 = new ArrayAccessExpr(new NameExpr(new Name(t1)), new NameExpr(new Name(t2)));

                // now make [ [ld1; ld2; stuff from bl1; stuff from bl2], aae2 ]
                Sequence s = new Sequence(ld1); // [ ld1 ]
                s.append(ld2); // [ ld1; ld2 ]
                // merge the stats from the block
                s.merge(bl1); // [ ld1; ld2; stuff from bl1 ]
                s.merge(bl2); // [ ld1; ld2; stuff from bl1; stuff from bl2 ]
                Sequence retSeq = new Sequence(s); // [ [ ld1; ld2; stuff from bl1; stuff from bl2 ] ]
                retSeq.append(aae2); // [ [ ld1; ld2; stuff from bl1; stuff from bl2 ], aae2 ]
                return retSeq;
            } else {
                System.out.println("ArrayAccessExpr (Case 3)");
                Sequence s = new Sequence(new Sequence()); // [ [ ] ]
                s.append(aae); // [ [ ], aae2 ]
                return s;
            }
        } else if (a instanceof ArrayLiteral) {
            // Todo: Check that no entries have yields.
            return a;
        } else if (a instanceof ArrayType) { // T[][][]
            return a;
        } else if (a instanceof Assignment) { // v = e
            Assignment as = (Assignment) a;
            if (as.right().doesYield()) {
                // v = e, e yields
                Sequence seq2 = (Sequence) go(as.right()); // [ [ ... ] <e> ]
                // I DON"T THINK THIS CAN HAPPEN!!!
                // if seq2 == [ null, e ] => return v = e
                if (seq2.child(0) == null) {
                    System.out.println("Assignment (Case 1A)");
                    return a;
                } else {
                    System.out.println("Assignment (Case 1B)");
                    // seq2 == [ [ ... ], <e> ]
                    Sequence se = (Sequence) (seq2.child(0)); // [ ... ]
                    Assignment as2 = new Assignment(as.left(), (Expression) seq2.child(1), as.op()); // v = <e>
                    Sequence retSeq = new Sequence(se); // [ [ ... ] ]
                    retSeq.append(as2); // [ [ ... ], v = <e> ]
                    return retSeq;
                }
                // TODO: <op>= should probably be handled in a special manner???
            } else {
                System.out.println("Assignment (Case 2)");
                // v = e, e does not yield
                // returns [ [ ], v = e ]
                Sequence retSeq = new Sequence(new Sequence()); // [ [ ] ]
                retSeq.append(as); // [ [ ], as ]
                return retSeq;
            }
        } else if (a instanceof BinaryExpr) { // e1 <op> e2
            BinaryExpr be = (BinaryExpr) a;
            if (be.left().doesYield() && !be.right().doesYield()) {
                System.out.println("BinaryExpr (Case 1)");
                String t = nextTemp();
                // T t; (T is the type of e1)
                LocalDecl ld = makeLocalDecl(be.left().type, t);
                // t = e1;
                // rewrite 't = e1' -- e may be complex itself.
                Sequence re = (Sequence) go(new ExprStat(makeAssignment(t, be.left()))); // re = [ [ ... ] , null ]
                Sequence se = (Sequence) re.child(0); // se = [ ... ]
                // make t <op> e2
                BinaryExpr be2 = new BinaryExpr(new NameExpr(new Name(t)), be.right(), be.op());
                // now make [ [ ld; stuff from se], be2 ]
                Sequence s = new Sequence(ld); // [ ld ]
                s.merge(se); // [ ld; stuff from se; ]
                // return [ [ld; stuff from se], be2 ]
                Sequence retSeq = new Sequence(s); // [ [ ld; stuff from se; ] ]
                retSeq.append(be2); // [ [ ld; stuff from se], be2 ]
                return retSeq;
            } else if (be.right().doesYield()) {
                System.out.println("BinaryExpr (Case 2)");
                String t1 = nextTemp();
                String t2 = nextTemp();
                // T1 t1;
                // T2 t2;
                LocalDecl ld1 = makeLocalDecl(be.left().type, t1);
                LocalDecl ld2 = makeLocalDecl(be.right().type, t2);
                // t1 = e1;
                // t2 = e2
                // rewrite 't1 = e1' and 't2 = e2' -- e1 and e2 may be complex itself.
                Sequence re1 = (Sequence) go(new ExprStat(makeAssignment(t1, be.left())));
                Sequence re2 = (Sequence) go(new ExprStat(makeAssignment(t2, be.right())));
                Sequence bl1 = (Sequence) re1.child(0);
                Sequence bl2 = (Sequence) re2.child(0);

                // make t1 <op> t2
                BinaryExpr be2 = new BinaryExpr(new NameExpr(new Name(t1)), new NameExpr(new Name(t2)), be.op());
                // now make s = [ [ ld1, ld2, stuff from bl1; stuff from bl2; ], be2 ]
                Sequence s = new Sequence(ld1); // [ ld1; ]
                s.append(ld2); // [ ld1; ld2 ]
                s.merge(bl1); // [ ld1; ld2; stuff from bl1; ]
                s.merge(bl2); // [ ld1; ld2; stuff from bl1; stuff from bl2; ]
                // return a Sequence with a new new block with [ [ ld1; ld2; stuff from bl1;
                // stuff from bl2; ], be2 ]
                Sequence retSeq = new Sequence(s); // [ ld1; ld2; stuff from bl1; stuff from bl2; ]
                retSeq.append(be2); // [ [ ld1; ld2; stuff from bl1; stuff from bl2; ], be2 ]
                return retSeq;
            } else {
                System.out.println("BinaryExpr (Case 3)");
                // return [ {} , v = e ]
                Sequence retSeq = new Sequence(new Sequence()); // [ [ ] ]
                retSeq.append(be); // [ [ ], be ]
                return retSeq;
            }
        } else if (a instanceof Block) { // { S1; S2; ... ; Sn }
            System.out.println("Block");
            Block bl = (Block) a; // { [S1; S2; ..., Sn] }
            Sequence s = (Sequence) go(bl.stats()); // [<S1>; <S2>; ..., <Sn>]
            Sequence retSeq = new Sequence(new Sequence(new Block(s))); // [ [ { [<S1>; <S2>; ..., <Sn>] } ] ]
            retSeq.append(null); // [ [ { [<S1>; <S2>; ..., <Sn>] } ], null ]
            return retSeq;
        } else if (a instanceof BreakStat) { // break
            Sequence s = new Sequence(a);
            Sequence ret = new Sequence(s);
            ret.append(null);
            return ret;
        } else if (a instanceof CastExpr) { // (T)e
            System.out.println("CastExpr");
            CastExpr ce = (CastExpr) a;
            Sequence s = new Sequence();
            if (ce.expr().doesYield()) { // rewrite the expression
                String t = nextTemp();
                // T t;
                LocalDecl ld = makeLocalDecl(ce.expr().type, t);
                // t = e;
                Sequence se = (Sequence) go(new ExprStat(makeAssignment(t, ce.expr())));
                s.append(ld);
                s.merge(se.child(0));
                Sequence re = new Sequence(s);
                ce.children[1] = new NameExpr(new Name(t));
                re.append(ce);
                return re;
            } else {
                Sequence re = new Sequence(s);
                re.append(ce);
                return re;
            }
        } else if (a instanceof ChannelEndExpr) { // e.read or e.write
            return a;
        } else if (a instanceof ChannelEndType) { //
            return a;
        } else if (a instanceof ChannelReadExpr) { //
            System.out.println("ChannelReadExpr");
            // TODO: this fails in codegen for c.read().read() -- problem is in the type
            // checker
            // e.read()
            ChannelReadExpr cre = (ChannelReadExpr) a;
            if (cre.channel().doesYield()) {
                // the channel expression is another channel read.
                String t = nextTemp();
                // T t;
                LocalDecl ld = makeLocalDecl(cre.channel().type, t);
                // t = e;
                Sequence se = (Sequence) go(new ExprStat(makeAssignment(t, cre.channel())));
                Sequence se2 = new Sequence(ld);
                se2.merge(se.child(0));
                Sequence ret = new Sequence(se2);
                Block extRev = null;
                if (cre.extRV() != null) {
                    Sequence extRevSeq = (Sequence) go(cre.extRV());
                    extRev = (Block) ((Sequence) ((Sequence) extRevSeq.child(0))).child(0);
                }
                ret.append(new ChannelReadExpr(new NameExpr(new Name(t)), extRev));
                return ret;
            } else {
                Sequence ret = new Sequence(new Sequence()); // [ [ ] ]
                ret.append(cre); // [ [ ] c.read() ]
                return ret;
            }
        } else if (a instanceof ChannelWriteStat) { // e1.write(e2)
            System.out.println("ChannelWriteStat");
            ChannelWriteStat cws = (ChannelWriteStat) a;
            // e1 yields, e2 does not
            Sequence ret;
            if (cws.channel().doesYield() && !cws.expr().doesYield()) {
                // make [ [ T t; <t = e1>; t.write(e2); ], null ]
                String t = nextTemp();
                // T t;
                LocalDecl ld = makeLocalDecl(cws.channel().type, t);
                // t = e;
                Sequence se = (Sequence) go(new ExprStat(makeAssignment(t, cws.channel())));
                Sequence s = new Sequence(ld);
                s.merge(se.child(0));
                ret = new Sequence(s);
            } else if (cws.expr().doesYield()) {
                // make [ [ T1 t1; <t1 = e1>; T2 t2; <t2 = e2>; t1.write(t2); ], null ]
                String t1 = nextTemp();
                // T1 t1;
                LocalDecl ld1 = makeLocalDecl(cws.channel().type, t1);
                // t1 = e1;
                Sequence se = (Sequence) go(new ExprStat(makeAssignment(t1, cws.channel())));
                Sequence s = new Sequence(ld1);
                s.merge(se.child(0));
                String t2 = nextTemp();
                // T2 t2;
                LocalDecl ld2 = makeLocalDecl(cws.expr().type, t2);
                // t2 = e2;
                Sequence se2 = (Sequence) go(new ExprStat(makeAssignment(t2, cws.expr())));
                s.append(ld2);
                s.merge(se2.child(0));
                s.append(new ChannelWriteStat(new NameExpr(new Name(t1)), new NameExpr(new Name(t2))));
                ret = new Sequence(s);
            } else {
                ret = new Sequence(new Sequence(cws));
            }
            ret.append(null);
            return ret;
        } else if (a instanceof Compilation) {
            System.out.println("Compilation");
            Compilation c = (Compilation) a;
            for (int i = 0; i < c.typeDecls().size(); i++) {
                go(c.typeDecls().child(i));
            }
            return a;
        } else if (a instanceof ConstantDecl) { // const ???
            return a;
        } else if (a instanceof ContinueStat) { // continue;
            Sequence s = new Sequence(a);
            Sequence ret = new Sequence(s);
            ret.append(null);
            return ret;
        } else if (a instanceof DoStat) { // do => if
            // These do no longer exist in the parse tree by this time
            return a;
        } else if (a instanceof ExprStat) { // ...;
            System.out.println("ExprStat");
            Sequence s = (Sequence) go(((ExprStat) a).expr());
            // should give us back [ [ ... ] , e ]. Just turn that into [ ...; e ]
            Sequence se = (Sequence) s.child(0); // grab the statements
            se.append((Statement) (new ExprStat((Expression) s.child(1))));
            Sequence retSeq = new Sequence(se);
            retSeq.append(null);
            return retSeq;
        } else if (a instanceof ExternType) { // ??
            return a;
        } else if (a instanceof ForStat) { // for => if
            // These do no longer exist in the parse tree by this time
            return a;
        } else if (a instanceof Guard) { // ( e ) && v = e.read()
            return a;
        } else if (a instanceof IfStat) {
            System.out.println("IfStat");
            IfStat is = (IfStat) a;
            Sequence seq2 = new Sequence();
            if (is.expr().doesYield()) { // the expression on the if yield.
                String t = nextTemp();
                // T t; (T is the type of the expression)
                LocalDecl ld = makeLocalDecl(is.expr().type, t);
                seq2.append(ld);
                // t = e;
                Sequence s = (Sequence) go(new ExprStat(makeAssignment(t, is.expr())));
                seq2.merge(s.child(0)); // [ T t; <t = e>; ]
                is.children[0] = new NameExpr(new Name(t));
            }
            tempCounter--;
            Sequence s = (Sequence) go(is.thenpart());
            if (((Sequence) s.child(0)).size() > 1) {
                Sequence ss = (Sequence) s.child(0);
                is.children[1] = new Block(ss);
            } else
                is.children[1] = ((Sequence) ((Sequence) s.child(0))).child(0);
            if (is.elsepart() != null) {
                s = (Sequence) go(is.elsepart());
                if (((Sequence) s.child(0)).size() > 1) {
                    Sequence ss = (Sequence) s.child(0);
                    is.children[2] = new Block(ss);
                } else
                    is.children[2] = ((Sequence) ((Sequence) s.child(0))).child(0);
            }
            seq2.append(is);
            Sequence ret = new Sequence(seq2);
            ret.append(null);
            return ret;
        } else if (a instanceof ImplicitImport) {
            return a;
        } else if (a instanceof Import) {
            return a;
        } else if (a instanceof Invocation) {
            System.out.println("Invocation");
            Invocation in = (Invocation) a;
            boolean yields = false; // once set to true a parameter that does not yield must be lifted out
            // because it may have side effects.
            // iterate the parameters backwards:
            Sequence ret = new Sequence();
            for (int i = in.params().size() - 1; i >= 0; i--) {
                System.out.println("Parameter #" + i);
                Expression e = in.params().child(i);
                if (yields || e.doesYield()) {
                    yields = true;
                    if (e.doesYield()) { // e has a channel read in it
                        Sequence s2 = new Sequence();
                        String t = nextTemp();
                        // T t; (T is the type of the i'th parameter)
                        LocalDecl ld = makeLocalDecl(e.type, t);
                        // t = e;
                        s2.append(ld);
                        Sequence s = (Sequence) go(new ExprStat(makeAssignment(t, e))); // [ [ T t; <t = e>; ], null ]
                        s2.merge(s.child(0));
                        ret = s2.merge(ret);
                        in.params().set(i, new NameExpr(new Name(t)));
                    } else { // e doesn't yield so just make t = e;
                        Sequence s2 = new Sequence();
                        String t = nextTemp();
                        // T t; (T is the type of the i'th parameter)
                        LocalDecl ld = makeLocalDecl(e.type, t);
                        s2.append(ld);
                        // t = e;
                        // No need to rewrite it as it doen't yield.
                        Sequence s = new Sequence(new ExprStat(makeAssignment(t, e))); // [ t = e; ]
                        s2.merge(s.child(0));
                        ret = s2.merge(ret);
                        in.params().set(i, new NameExpr(new Name(t)));
                    }
                }
            }
            Sequence seq = new Sequence(ret);
            seq.append(in); // [ [ ... ], in ];
            return seq;
        } else if (a instanceof Literal) {
            return a;
        } else if (a instanceof LocalDecl) {
            // T t or T t = e
            // if a local declaration has an init that yields rewrite like this:
            // T t = e => T t; <t = e>; < > means rewritten;
            // TODO: not finished.
            LocalDecl ld = (LocalDecl) a;
            if (ld.var().init() == null || (ld.var().init() != null && !ld.var().init().doesYield())) {
                System.out.println("LocalDecl (Case 1)");
                // T t; or T t = e where e does not yield.
                Sequence retSeq = new Sequence(new Sequence(a)); // [ [ T t ] ] or [ [ T t = e ] ]
                retSeq.append(null); // [ [ T t ], null ] or [ [ T t = e ], null ]
                return retSeq;
            } else {
                // T t = e where e yields.
                System.out.println("LocalDecl (Case 2)");
                Sequence s = new Sequence(makeLocalDecl(ld.type(), ld.var().name().getname())); // [ T t; ]
                Sequence seq1 = (Sequence) go(new ExprStat(makeAssignment(ld.var().name().getname(), ld.var().init()))); // [
                                                                                                                            // [
                                                                                                                            // <t
                                                                                                                            // =
                                                                                                                            // e>;
                                                                                                                            // ],
                                                                                                                            // null]
                s.merge((Sequence) seq1.child(0)); // [ T t; <t = e>; ]
                Sequence retSeq = new Sequence(s); // [ [ T t; <t = e>; ] ]
                retSeq.append(null); // [ [ T t; <t = e>; ], null ]
                return retSeq;
            }
        } else if (a instanceof Name) {
            return a;
        } else if (a instanceof NameExpr) {
            return a;
        } else if (a instanceof NamedType) {
            return a;
        } else if (a instanceof NewArray) {
            return a;
        } else if (a instanceof NewMobile) {
            return a;
        } else if (a instanceof ParBlock) {
            ParBlock pb = (ParBlock) a;
            Sequence s = pb.stats();
            for (int i = 0; i < s.size(); i++) {
                Sequence res = (Sequence) go(s.child(i));
                s.set(i, ((Sequence) res.child(0)).child(0));
            }
            Sequence result = new Sequence(new Sequence(new ParBlock(s, pb.barriers())));
            result.append(null);
            return result;
        } else if (a instanceof ParamDecl) {
            return a;
        } else if (a instanceof PrimitiveLiteral) {
            return a;
        } else if (a instanceof PrimitiveType) {
            return a;
        } else if (a instanceof Pragma) {
            return a;
        } else if (a instanceof PrimitiveType) {
            return a;
        } else if (a instanceof ProcTypeDecl) {
            System.out.println("ProcTypeDecl");
            // For a procedure just handle the body (it is a sequence)
            ProcTypeDecl pd = (ProcTypeDecl) a;
            Sequence s = (Sequence) go(pd.body());
            System.out.println(s);
            pd.children[6] = ((Sequence) s.child(0)).child(0);
            return pd;
        } else if (a instanceof ProtocolCase) {
            return a;
        } else if (a instanceof ProtocolTypeDecl) {
            return a;
        } else if (a instanceof QualifiedName) {
            return a;
        } else if (a instanceof RecordAccess) {
            return a;
        } else if (a instanceof RecordMember) {
            return a;
        } else if (a instanceof RecordMemberLiteral) {
            return a;
        } else if (a instanceof RecordTypeDecl) {
            return a;
        } else if (a instanceof ReturnStat) {
            return a;
        } else if (a instanceof Sequence) {
            System.out.println("Sequence");
            // [ s1; s2; ...; sn ] returns [ <s1>; <s2>; ...; <sn> ]
            // Note, a Sequence is the only node that doesn't return something of the form [
            // [ ... ], ... ], but just of the form [ .... ]
            Sequence seq = (Sequence) a;
            Sequence retSeq = new Sequence(); // [ ]
            for (int i = 0; i < seq.size(); i++) {
                System.out.println("Sequence (#" + i + ")");
                // call recursively on each child; update the entry as a new type of node may
                // have been returned.
                Sequence seq2 = (Sequence) go(seq.child(i));
                // if (seq2.child(0) == null) {
                // System.out.println("THIS SHOULD NEVER HAPPEN");
                // System.exit(1);
                // seq.set(i, seq2.child(1));
                // } else {
                // seq2 = [ [...] , c2 ] - just append c2 to the sequence in the first position.
                // NOTE: this ONLY works if c2 is a _Statement_, so do NOT call go() with a
                // sequence of
                // expressions or anything else. They should be handled manually in the
                // appropriate node.
                Sequence s = (Sequence) seq2.child(0); // [ <Si>; ]
                retSeq.merge(s); // [ <S0>; ... <Si>; ]
                retSeq.visit(new PrettyPrinter());
                // }
            }
            return retSeq;
        } else if (a instanceof SkipStat) {
            return a;
        } else if (a instanceof StopStat) {
            return a;
        } else if (a instanceof SuspendStat) {
            return a;
        } else if (a instanceof SwitchGroup) {
            return a;
        } else if (a instanceof SwitchLabel) {
            return a;
        } else if (a instanceof SwitchStat) { // switch (e) { ... }
            SwitchStat ss = (SwitchStat) a;
            // handle the children:
            for (int i = 0; i < ss.switchBlocks().size(); i++) {
                go(ss.switchBlocks().child(i));
            }
            if (ss.expr().doesYield()) {
                // make [ [ T t; <t = e> ], switch(t) { <...> } ]
                Sequence s2 = new Sequence(); // [ ]
                String t = nextTemp();
                // T t;
                LocalDecl ld = makeLocalDecl(ss.expr().type, t); // T t;
                s2.append(ld); // [ T t; ]
                // t = e;
                Sequence s = (Sequence) go(new ExprStat(makeAssignment(t, ss.expr()))); // [ [<t = e>; ], null ]
                s2.merge(s.child(0)); // [ T t; <t = e>; ]
                Sequence ret = new Sequence(s2);
                ss.children[0] = new NameExpr(new Name(t));
                ret.append(s);
                return ret;
            } else {
                // make [ [ switch (e) { ... } ], null ]
                Sequence ret = new Sequence(new Sequence(a));
                ret.append(null);
                return ret;
            }
        } else if (a instanceof SyncStat) { // e.sync()
            SyncStat ss = (SyncStat) a;
            if (ss.doesYield()) {
                ;// CompilerErrorManager.INSTANCE.reportMessage(new
                    // ProcessJMessage.Builder().addError(VisitorMessageNumber.REWRITE_1000));
            }
            // make [ [ ss ], null ]
            Sequence ret = new Sequence(new Sequence(ss));
            ret.append(null);
            return ret;
        } else if (a instanceof Ternary) { // ???
            return a;
        } else if (a instanceof TimeoutStat) {
            TimeoutStat ts = (TimeoutStat) a;
            if (ts.timer().doesYield()) {
                ;// CompilerErrorManager.INSTANCE.reportMessage(new
                    // ProcessJMessage.Builder().addError(VisitorMessageNumber.REWRITE_1001));
            }
            if (!ts.delay().doesYield()) {
                // make [ [ ts ], null ]
                Sequence ret = new Sequence(new Sequence(ts));
                ret.append(null);
                return ret;
            } else {
                // e1.timeout(e2)
                // make [ [T t; <t = e2>; e1.timeout(t);], null]

                // ...
                return null;

            }
        } else if (a instanceof UnaryPostExpr) {
            UnaryPostExpr upe = (UnaryPostExpr) a;
            if (upe.expr().doesYield()) {
                ;// Error.addError(upe, "Unary Post-Expressions cannot have channel reads",
                    // 0000);
            }
            // make [ [ ], e ]
            Sequence ret = new Sequence(new Sequence()); // [ [ ] ]
            ret.append(upe); // [ [ ], e ]
            return ret;
        } else if (a instanceof UnaryPreExpr) {
            UnaryPreExpr upe = (UnaryPreExpr) a;
            if (upe.expr().doesYield()) {
                ;// Error.addError(upe, "Unary Pre-Expressions cannot have channel reads", 0000);
            }
            // make [ [ ], e ]
            Sequence ret = new Sequence(new Sequence()); // [ [ ] ]
            ret.append(upe); // [ [ ], e ]
            return ret;
        } else if (a instanceof Var) {
            return a;
        } else if (a instanceof WhileStat) { // No longer exists.
            return a;
        } else {
            System.out.println("Default");

            return a;
        }
    }
}
