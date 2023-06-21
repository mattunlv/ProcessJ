package org.processj.compiler.phase;

import org.processj.compiler.ast.expression.binary.AssignmentExpression;
import org.processj.compiler.ast.expression.literal.PrimitiveLiteralExpression;
import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.expression.result.TernaryExpression;
import org.processj.compiler.ast.expression.yielding.ChannelReadExpression;
import org.processj.compiler.ast.statement.declarative.LocalDeclaration;
import org.processj.compiler.ast.type.ParameterDeclaration;
import org.processj.compiler.ast.type.ProcedureTypeDeclaration;
import org.processj.compiler.phase.jvm.ClassFile;
import org.processj.compiler.phase.jvm.instruction.mnemonic.*;

public class CodeGenerationJVM extends Phase {
    /**
     * <p>Initializes the {@link Phase} to its' default state with the specified {@link Listener}.</p>
     *
     * @param listener The {@link Listener} to bind to the {@link Phase}.
     * @since 0.1.0
     */
    public CodeGenerationJVM(Listener listener) {
        super(listener);
    }

    /**
     * ClassFile {
     *     4 bytes        Java Magic Number
     *     2 bytes        Minor Version
     *     2 bytes        Major Version
     *     2 bytes        Size of the constant pool
     *     * bytes        Numerous bytes making up the constant pool
     *     2 bytes        This class' access modifiers (Ie. public)
     *     2 bytes        Index of this class in constant pool
     *     2 bytes        Index of this class' super class in constant pool
     *     2 bytes        Number of interfaces
     *     * bytes        Numerous bytes making up interface definitions
     *     2 bytes        Number of fields in this class
     *     * bytes        Numerous bytes making up field definitions
     *     2 bytes        Number of methods in this class
     *     * bytes        Numerous bytes making up method definitions
     *     2 bytes        Attributes count ( meta data for class file )
     *     * bytes        Numerous bytes making up attribute definitions
     * }
     */

    byte[] ProcessClassBytes = new byte[] {
            (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,     // Magic Number
            0x00, 0x00,                                             // Minor Version
            0x00, 0x34,                                             // Major Version (Version 8)

            /// -------------
            /// Constant Pool

                0x00, 0x05,                                                 // Constant Pool Size + 1

                /* Attribute Names */
        /* 1 */ 0x01,                                                       // Tag: Constant_Utf8_info
                0x00, 0x03,                                                 // String Length: 4
                0x43, 0x6f, 0x64, 0x65,                                     // 'Code'

                /* Class Info */
        /* 2 */ 0x07,                                                       // Tag: CONSTANT_class_info
                0x00, 0x03,                                                 // NameIndex: 3

        /* 3 */ 0x01,                                                       // Tag: Constant_Utf8_info
                0x00, 0x10,                                                 // String Length: 16
                0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e,
                0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65, 0x63, 0x74,             // 'java/lang/Object'

                /* Class: Process */
        /* 4 */ 0x07,                                                       // Tag: CONSTANT_class_info
                0x00, 0x05,                                                 // NameIndex: 5

        /* 5 */ 0x01,                                                       // Tag: Constant_Utf8_info
                0x00, 0x07,                                                 // String Length: 7
                0x50, 0x72, 0x6f, 0x63, 0x65, 0x73, 0x73,                   // 'Process'

                    /* Class: Process - Method Info */
            /* 6 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x03,                                             // String Length: 3
                    0x72, 0x75, 0x6e,                                       // '<init>'

            /* 7 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x03,                                             // String Length: 3
                    0x28, 0x29, 0x56,                                       // '()V'

            /* 8 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x03,                                             // String Length: 3
                    0x72, 0x75, 0x6e,                                       // 'run'

            /* 9 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x03,                                             // String Length: 3
                    0x28, 0x29, 0x56,                                       // '()V'

                    /* Class: Process - Field Info */
           /* 10 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x0c,                                             // String Length: 12
                    0x69, 0x73, 0x4e, 0x6f, 0x74, 0x54, 0x65, 0x72,
                    0x6d, 0x69, 0x6e, 0x61, 0x74, 0x65, 0x64,               // 'isNotTerminated'

           /* 11 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x01,                                             // String Length: 1
                    0x5a,                                                   // 'Z'

           /* 12 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x0c,                                             // String Length: 7
                    0x69, 0x73, 0x52, 0x65, 0x61, 0x64, 0x79,               // 'isReady'

           /* 13 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x01,                                             // String Length: 1
                    0x5a,                                                   // 'Z'

           /* 14 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x0c,                                             // String Length: 7
                    0x72, 0x65, 0x73, 0x75, 0x6d, 0x65,                     // 'resume'

           /* 15 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x01,                                             // String Length: 1
                    0x5a,                                                   // 'I'

           /* 16 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x0c,                                             // String Length: 9
                    0x6d, 0x61, 0x78, 0x4f, 0x66, 0x66, 0x73, 0x65, 0x74,   // 'maxOffset'

           /* 17 */ 0x01,                                                   // Tag: Constant_Utf8_info
                    0x00, 0x01,                                             // String Length: 1
                    0x5a,                                                   // 'I'

            0x10, 0x21,                                                 // Modifier: public class
            0x00, 0x04,                                                 // This Class Index: 4
            0x00, 0x02,                                                 // Super Class Index: 2
            0x00, 0x00,                                                 // Interface Count: 0

            /// ------
            /// Fields

            0x00, 0x04,                                                 // Field Count: 4

        /* 1 */ 0x10, 0x04,                                             // Access Modifiers: synthetic protected
                0x00, 0x0a,                                             // Name Index: 10
                0x00, 0x0b,                                             // Descriptor Index: 11
                0x00, 0x00,                                             // Attributes Count: 0

        /* 2 */ 0x10, 0x04,                                             // Access Modifiers: synthetic protected
                0x00, 0x0c,                                             // Name Index: 12
                0x00, 0x0d,                                             // Descriptor Index: 13
                0x00, 0x00,                                             // Attributes Count: 0

        /* 3 */ 0x10, 0x04,                                             // Access Modifiers: synthetic protected
                0x00, 0x0e,                                             // Name Index: 14
                0x00, 0x0f,                                             // Descriptor Index: 15
                0x00, 0x00,                                             // Attributes Count: 0

        /* 4 */ 0x10, 0x04,                                             // Access Modifiers: synthetic protected
                0x00, 0x10,                                             // Name Index: 16
                0x00, 0x11,                                             // Descriptor Index: 17
                0x00, 0x00,                                             // Attributes Count: 0

            /* Dynamically Generated Fields go here */

            /// -------
            /// Methods

            0x00, 0x02,                                                  // Method Count: 1

            /* Process#<init>() */
        /* 1 */ 0x10, 0x01,                                              // Access Modifiers: synthetic final public
                0x00, 0x06,                                              // Name Index
                0x00, 0x01,                                              // Attributes Count: 1
                0x00, 0x01,                                              // Attribute Name Index: 1 -> 'Code'
                0x00, 0x00,                                              // Code Attribute Size
                0x04,                                                    // Max Stack Size: 0
                0x00,                                                    // Max Local Size: 0
                0x0F,                                                    // Code Length (In Bytes) 15 + Frame Field Set

                    (byte)  0x2a,                                        // aload_0                         ; Push 'this'
                    (byte)  0x59,                                        // dup                             ; Duplicate;
                    (byte)  0x59,                                        // dup                             ; Duplicate;
                    (byte)  0x59,                                        // dup                             ; Duplicate;
                    (byte)  0x59,                                        // dup                             ; Duplicate;
                    (byte)  0x04,                                        // iconst_1                        ; Push constant 1
                    (byte)  0xb5, 0x00, 0x01,                            // putfield #1                     ; Set 'isNotTerminated'
                    (byte)  0x04,                                        // iconst_1                        ; Push constant 1
                    (byte)  0xb5, 0x00, 0x02,                            // putfield #2                     ; Set 'isReady'
                    (byte)  0x03,                                        // iconst_0                        ; Push constant 0
                    (byte)  0xb5, 0x00, 0x03,                            // putfield #3                     ; Set 'resume'
                    // TODO: MaxOffset bipush <byte>
                    // TODO: <init> on BarrierSet invokespecial
                    // TODO: putfield
                    (byte)  0xb1,                                        // return                          ; End: return

                /* Process#run Exception Table */
                0x00,                                                    // Exception Table Size: 0
                0x00,                                                    // Attribute Count: 0

                /* Process#run() */
        /* 2 */ 0x10, 0x01,                                              // Access Modifiers: synthetic final public
                0x00, 0x06,                                              // Name Index
                0x00, 0x01,                                              // Attributes Count: 1
                0x00, 0x01,                                              // Attribute Name Index: 1 -> 'Code'
                0x00, 0x00,                                              // Code Attribute Size
                0x04,                                                    // Max Stack Size: 0
                0x00,                                                    // Max Local Size: 0
                0x00,                                                    // Code Length (In Bytes)

                    /* Process#run() Code */
                    /* Prologue */
                    (byte)  0x2a,                                                // aload_0                         ; Push 'this'
                    (byte)  0xb4, 0x00, 0x01,                                    // getfield #1                     ; 'isNotTerminated'
                    (byte)  0x9a, 0x00, 0x00, // add end/return offset           // ifeq                            ; Branch to end if isNotTerminated == 0
                    (byte)  0x2a,                                                // aload_0                         ; Push 'this'
                    (byte)  0xb4, 0x00, 0x02,                                    // getfield #2                     ; 'isReady'
                    (byte)  0x99, 0x00, 0x00, // Add end/return offset           // ifeq                            ; Branch to end if isReady == 0
                    // TODO: Wind stack here
                    (byte)  0x2a,                                                // aload_0                         ; Push 'this'
                    (byte)  0xb4, 0x00, 0x03,                                    // getfield #3                     ; 'resume' (32-bit int)
                    (byte)  0xaa,                                                // tableswitch                     ; Start Lookup Switch
                    (byte)  0x00, 0x00, 0x00, 0x22,                              //                                 ; Default: Address of Epilogue -> offsets + executions + 34
                    (byte)  0x00, 0x00, 0x00, 0x00,                              //                                 ; Low Address <addressof tableswitch + 12>
                    (byte)  0x00, 0x00, 0x00, 0x00,                              //                                 ; High Address
                    // TODO: Offsets
                    // <Executions>
                    /* Epilogue */
                    (byte)  0x2a,                                                // aload_0                         ; Push 'this'
                    (byte)  0xb4, 0x00, 0x04,                                    // getfield #4                     ; 'maxOffset' (32-bit int)
                    (byte)  0x2a,                                                // aload_0                         ; Push 'this'
                    (byte)  0x59,                                                // dup                             ; Duplicate; Max Stack Size: 4
                    (byte)  0x59,                                                // dup                             ; Duplicate; Max Stack Size: 4
                    (byte)  0xb4, 0x00, 0x03,                                    // getfield #3                     ; 'resume' (32-bit int)
                    (byte)  0x04,                                                // iconst_1                        ; Push constant 1
                    (byte)  0x60,                                                // iadd                            ; Add the Values
                    (byte)  0x5a,                                                // dup_x1                          ; Duplicate back
                    (byte)  0xb5, 0x00, 0x03,                                    // putfield #3                     ; Set the result 'resume'
                    (byte)  0x85,                                                // i2l                             ; Convert to long
                    (byte)  0x94,                                                // lcmp                            ; Compare, Pushes 1 if 'maxOffset' is greater than 'resume'
                    (byte)  0xb5, 0x00, 0x01,                                    // putfield #1                     ; Set the result 'isNotTerminated'
                    // TODO: Load Barriers Set Reference
                    // TODO: Retrieve Size
                    // TODO: Compare With 0
                    // TODO: Set isReady
                    // TODO: Unwind stack here
                    // TODO: <Optional> Set return value
                    (byte)  0xb1,                                                // return                          ; End: return

                /* Process#run Exception Table */
                0x00,                                                            // Exception Table Size: 0
                0x00,                                                            // Attribute Count: 0

            /// ----------
            /// Attributes

            0x00, 0x00,                                                         // Attributes: 0

    };


    /**
     * For Processes, Fields:
     * Jump/Offset Index FieldRef: Integer -> Where the prologue should jump to
     * Ready FieldRef: boolean -> If the Process is ready
     * Terminated FieldRef: boolean -> If the Process is terminated
     * <fields>: Parameters -> The Procedure's Parameters
     * <barriers>: Barriers -> The Procedure's Barriers
     * 1. getfield terminated
     * 2. ifcmp...              # If we're terminated, jump to the end
     * 3. getfield isReady      # Retrieve the isReady flag/check barriers
     * 4. ifcmp...              # If we're not ready, jump to the end
     * 5. aload_0 ...           # Set the operand stack with jump offset at top
     *    dup
     * 6. goto                  # Jump to that section
     * 7. <execute>             # Execute Instructions
     * 8. goto epilogue         # Jump to epilogue
     * 9. ...
     * 10.                      # Unwind Stack
     * 12.                      # Check the Jump index; if > jumps.length, set terminated to true
     * 13.                      # Set the result field if the process returns a result
     * 13. return               # Leave
     * When invoking:
     *      1. Create a parameters instance
     *      2. Create the process instance
     *      3. Enroll the process as a barrier
     *      4. Enqueue Process into Scheduler
     *          a. If the Scheduler was locked, Go to epilogue
     *          b. Otherwise, increment jump index, Go to Epilogue
     * Note: Epilogue is always in the Jump Offsets
     * @param procedureTypeDeclaration
     * @return
     */
    @Override
    public void visitProcedureTypeDeclaration(final ProcedureTypeDeclaration procedureTypeDeclaration) {


    }

    private ClassFile.Code getCode() {

        return new ClassFile.Code();

    }

    private void setCode(final ClassFile.Code code) {



    }

    @Override
    public void visitParameterDeclaration(final ParameterDeclaration parameterDeclaration) {

        this.getCode().addParameter(new ClassFile.Parameter(parameterDeclaration.getName().toString()));

    }

    /**
     * The initialization expression can go to:
     *      1. ChannelReadExpression
     * @param localDeclaration
     * @return
     * @throws Phase.Error
     */
    @Override
    public void visitLocalDeclaration(final LocalDeclaration localDeclaration) throws Phase.Error {

        this.getCode().addLocal(new ClassFile.Local(localDeclaration.getName().toString()));

        // Assert the LocalDeclaration is initialized
        if(localDeclaration.isInitialized())
            localDeclaration.getInitializationExpression().accept(this);

    }

    @Override
    public void visitAssignmentExpression(final AssignmentExpression assignmentExpression) throws Phase.Error {

        // expression1 = expression2 = expression3 = expression4 = ... = expressionn

        // Resolve the right-hand side
        assignmentExpression.getRightExpression().accept(this);

    }

    // Target Can be:
    //      Ternary
    //      Record Access
    //      NameExpression
    //      InvocationExpression
    //      ImplicitImport?
    //      ChannelRead?
    //      ChannelEndExpression?
    //      CastExpression
    //      AssignmentExpression
    //      ArrayAccessExpression
    @Override
    public void visitChannelReadExpression(final ChannelReadExpression channelReadExpression) throws Phase.Error {
        // TODO: Generate
        // dup              ; Duplicate Reference
        // invokevirtual    ; isReadyToRead
        // ifne 0x06        ; Jump over
        // aload_0          ; Push this
        // getfield #3      ; Get resume
        // iconst_1         ; Push 1
        // isub             ; subtract
        // putfield #3      ; Put field
        // goto <offset>    ; Go to prologue
        // invokevirtual    ; read()
        // aload_0          ; Push 'this'
        // putfield #x      ; Put in read result



        // Resolve the Target
        channelReadExpression.getTargetExpression().accept(this);

        // Initialize a handle to enclosing Context & the current Code
        final ClassFile.Code code = this.getCode()  ;

        // Add the ChannelReadInstruction
        code.addInstruction(new ChannelRead());

        // Resolve the Extended Rendezvous
        channelReadExpression.getExtendedRendezvous().accept(this);

    }

    @Override
    public final void visitNameExpression(final NameExpression nameExpression) throws Phase.Error {

        this.getCode().addInstruction(new GetName(nameExpression.toString()));

    }

    @Override
    public final void visitPrimitiveLiteralExpression(final PrimitiveLiteralExpression primitiveLiteralExpression) {

        // Initialize a handle to the current Code
        final ClassFile.Code code = this.getCode();

        if(primitiveLiteralExpression.getType().isBooleanType()
            || primitiveLiteralExpression.getType().isByteType()
            || primitiveLiteralExpression.getType().isShortType()
            || primitiveLiteralExpression.getType().isIntegerType())
            code.addInstruction(new GetConstantInteger((Integer) primitiveLiteralExpression.constantValue()));

        else if(primitiveLiteralExpression.getType().isLongType())
            code.addInstruction(new GetConstantLong((Long) primitiveLiteralExpression.constantValue()));

        else if(primitiveLiteralExpression.getType().isFloatType())
            code.addInstruction(new GetConstantFloat((Float) primitiveLiteralExpression.constantValue()));

        else if(primitiveLiteralExpression.getType().isDoubleType())
            code.addInstruction(new GetConstantDouble((Double) primitiveLiteralExpression.constantValue()));

    }

    @Override
    public final void visitTernaryExpression(final TernaryExpression ternaryExpression) throws Phase.Error {

        // Resolve the evaluation expression
        ternaryExpression.getEvaluationExpression().accept(this);

        final ClassFile.Code code = this.getCode();

        // Open Up new Code
        final ClassFile.Code thenCode   = new ClassFile.Code();
        final ClassFile.Code elseCode   = new ClassFile.Code();

        this.setCode(thenCode);

        // Resolve the Then Expression
        ternaryExpression.getThenExpression().accept(this);

        this.setCode(elseCode);

        // Resolve the Else Expression
        ternaryExpression.getElseExpression().accept(this);

        // Close New Code

    }

    static class TestClass<Type> {

        public TestClass(Type type) {


        }

    }

}
