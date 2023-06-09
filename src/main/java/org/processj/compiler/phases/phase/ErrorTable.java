package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

import java.io.FileInputStream;
import java.util.*;

import org.processj.compiler.phases.phase.Phase.ImportAssert;
import org.processj.compiler.phases.phase.Phase.SemanticAssert;
import org.processj.compiler.phases.phase.Phase.ReachabilityAssert;

public class ErrorTable {

    /// ------------------------
    /// Private Static Constants

    private final static String PropertiesPath = "src/main/resources/properties/";

    //** --------------------------------------------------------------------------------------------------------- **//
    //** Resolve Imports                                                                                           **//
    //** --------------------------------------------------------------------------------------------------------- **//

    private final static Object[] Index_101 = new Object[]
            { ImportAssert.AmbiguousImportFile.class, String.class, List.class };

    private final static Object[] Index_102 = new Object[]
            { ImportAssert.PackageNotFound.class, String.class };

    private final static Object[] Index_103 = new Object[]
            { ImportAssert.FileNotFound.class, String.class, "", String.class };

    private final static Object[] Index_104 = new Object[]
            { ImportAssert.FileNotFound.class, String.class, String.class, String.class };

    //** --------------------------------------------------------------------------------------------------------- **//
    //** Top-Level Declarations                                                                                    **//
    //** --------------------------------------------------------------------------------------------------------- **//

    // TODO: See about Error 207; they are quickly resolved and replaced in the Namechecker Phase
    private final static Object[] Index_200 = new Object[]
            { Phase.DeclarationAssert.TypeDefined.class, ConstantDecl.class };

    private final static Object[] Index_201 = new Object[]
            { Phase.DeclarationAssert.NonProcedureTypeDefined.class, ProcTypeDecl.class };

    private final static Object[] Index_202 = new Object[]
            { Phase.DeclarationAssert.TypeDefined.class, ProcTypeDecl.class };

    private final static Object[] Index_203 = new Object[]
            { Phase.DeclarationAssert.TypeDefined.class, ProtocolTypeDecl.class };

    private final static Object[] Index_204 = new Object[]
            { Phase.DeclarationAssert.TypeDefined.class, RecordTypeDecl.class };

    private final static Object[] Index_205 = new Object[]
            { Phase.DeclarationAssert.MobileProcedureSpecifiesNonVoidReturnType.class, ProcTypeDecl.class };

    private final static Object[] Index_206 = new Object[]
            { Phase.DeclarationAssert.MobileProcedureOverloaded.class, ProcTypeDecl.class };

    private final static Object[] Index_208 = new Object[]
            { Phase.DeclarationAssert.NonMobileProcedureTypeDefined.class, ProcTypeDecl.class };

    //** --------------------------------------------------------------------------------------------------------- **//
    //** NameChecker                                                                                               **//
    //** --------------------------------------------------------------------------------------------------------- **//

    // TODO: 401 & 407
    private final static Object[] Index_400 = new Object[]
            { Phase.NameAssert.NameDefined.class, LocalDecl.class };

    private final static Object[] Index_402 = new Object[]
            { Phase.NameAssert.NameDefined.class, ParamDecl.class };

    private final static Object[] Index_403 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, Invocation.class };

    private final static Object[] Index_404 = new Object[]
            { Phase.NameAssert.NonProcedureInvocation.class, Invocation.class };

    private final static Object[] Index_405 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, NameExpr.class };

    private final static Object[] Index_406 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, NewMobile.class };

    private final static Object[] Index_408 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, ProcTypeDecl.class };

    private final static Object[] Index_409 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, ProtocolLiteral.class };

    private final static Object[] Index_410 = new Object[]
            { Phase.NameAssert.NonProcedureImplements.class, ProcTypeDecl.class };

    private final static Object[] Index_412 = new Object[]
            { Phase.NameAssert.NonProtocolName.class, ProtocolLiteral.class };

    private final static Object[] Index_413 = new Object[]
            { Phase.NameAssert.TagNotDefined.class, ProtocolLiteral.class };

    private final static Object[] Index_414 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, ProtocolTypeDecl.class };

    private final static Object[] Index_415 = new Object[]
            { Phase.NameAssert.NonProtocolName.class, ProtocolTypeDecl.class };

    private final static Object[] Index_416 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, RecordLiteral.class };

    private final static Object[] Index_417 = new Object[]
            { Phase.NameAssert.NonRecordName.class, RecordLiteral.class };

    private final static Object[] Index_418 = new Object[]
            { Phase.NameAssert.NameNotDefined.class, RecordTypeDecl.class };

    private final static Object[] Index_419 = new Object[]
            { Phase.NameAssert.NonRecordName.class, RecordTypeDecl.class };

    private final static Object[] Index_420 = new Object[]
            { Phase.NameAssert.ExtendDefined.class, RecordTypeDecl.class };

    private final static Object[] Index_421 = new Object[]
            { Phase.TypeAssert.SwitchLabelExpressionNotConstantOrProtocolTag.class, SwitchLabel.class };

    //** --------------------------------------------------------------------------------------------------------- **//
    //** Reachability                                                                                              **//
    //** --------------------------------------------------------------------------------------------------------- **//

    private final static Object[] Index_800 = new Object[]
            { ReachabilityAssert.BranchConditionalContextNotReachable.class, IfStat.class };

    private final static Object[] Index_801 = new Object[]
            { ReachabilityAssert.ConditionalContextNotReachable.class, IfStat.class };

    private final static Object[] Index_802 = new Object[]
            { ReachabilityAssert.EnclosingIterativeContextDoesNotTerminate.class, WhileStat.class, Statement.class };

    private final static Object[] Index_803 = new Object[]
            { ReachabilityAssert.ContextDoesNotTerminate.class, StopStat.class, Block.class };

    private final static Object[] Index_804 = new Object[]
            { ReachabilityAssert.EnclosingIterativeContextIsNotReachable.class, ForStat.class, Statement.class };

    private final static Object[] Index_805 = new Object[]
            { ReachabilityAssert.EnclosingIterativeContextDoesNotTerminate.class, ForStat.class, Statement.class };

    private final static Object[] Index_806 = new Object[]
            { ReachabilityAssert.NotEnclosedInBreakableContext.class, SymbolMap.Context.class, BreakStat.class };

    private final static Object[] Index_807 = new Object[]
            { ReachabilityAssert.EnclosedInParallelContext.class, SymbolMap.Context.class, ReturnStat.class };

    private final static Object[] Index_808 = new Object[]
            { ReachabilityAssert.EnclosedInParallelContext.class, ParBlock.class, BreakStat.class };

    private final static Object[] Index_809 = new Object[]
            { ReachabilityAssert.EnclosingIterativeContextDoesNotTerminate.class, DoStat.class, Statement.class };

    private final static Object[] Index_810 = new Object[]
            { ReachabilityAssert.EnclosingIterativeContextIsNotReachable.class, WhileStat.class, Statement.class };

    private final static Object[] Index_811 = new Object[]
            { ReachabilityAssert.EnclosedInParallelContext.class, ParBlock.class, ContinueStat.class };

    private final static Object[] Index_812 = new Object[]
            { ReachabilityAssert.NotEnclosedInIterativeContext.class, SymbolMap.Context.class, ContinueStat.class };

    private final static Object[] Index_813 = new Object[]
            { SemanticAssert.ParallelContextEmpty.class, SymbolMap.Context.class };

    /// --------------------------
    /// Protected Static Constants

    protected final static Map<Object[], String> Messages = new HashMap<>();

    /// ----------------------
    /// Private Static Methods

    private static void LoadMessages() {

        // Initialize the Properties
        final Properties properties = new Properties();

        // Attempt to
        try {

            // Load the properties file
            properties.load(new FileInputStream(PropertiesPath));

        } catch(final Exception exception) {

            // Empty

        }

        // Initialize the error messages if we successfully loaded the file
        if(!properties.isEmpty()) {

            Messages.put(Index_101, properties.getProperty("RESOLVE_IMPORTS_101"));
            Messages.put(Index_102, properties.getProperty("RESOLVE_IMPORTS_102"));
            Messages.put(Index_103, properties.getProperty("RESOLVE_IMPORTS_103"));
            Messages.put(Index_104, properties.getProperty("RESOLVE_IMPORTS_104"));

            Messages.put(Index_200, properties.getProperty("TOP_LEVEL_DECLS_200"));
            Messages.put(Index_201, properties.getProperty("TOP_LEVEL_DECLS_201"));
            Messages.put(Index_202, properties.getProperty("TOP_LEVEL_DECLS_202"));
            Messages.put(Index_203, properties.getProperty("TOP_LEVEL_DECLS_203"));
            Messages.put(Index_204, properties.getProperty("TOP_LEVEL_DECLS_204"));
            Messages.put(Index_205, properties.getProperty("TOP_LEVEL_DECLS_205"));
            Messages.put(Index_206, properties.getProperty("TOP_LEVEL_DECLS_206"));
            Messages.put(Index_208, properties.getProperty("TOP_LEVEL_DECLS_208"));

            Messages.put(Index_400, properties.getProperty("NAME_CHECKER_400"));
            Messages.put(Index_402, properties.getProperty("NAME_CHECKER_402"));
            Messages.put(Index_403, properties.getProperty("NAME_CHECKER_403"));
            Messages.put(Index_404, properties.getProperty("NAME_CHECKER_404"));
            Messages.put(Index_405, properties.getProperty("NAME_CHECKER_405"));
            Messages.put(Index_406, properties.getProperty("NAME_CHECKER_406"));
            Messages.put(Index_408, properties.getProperty("NAME_CHECKER_408"));
            Messages.put(Index_409, properties.getProperty("NAME_CHECKER_409"));
            Messages.put(Index_410, properties.getProperty("NAME_CHECKER_410"));
            Messages.put(Index_412, properties.getProperty("NAME_CHECKER_412"));
            Messages.put(Index_413, properties.getProperty("NAME_CHECKER_413"));
            Messages.put(Index_414, properties.getProperty("NAME_CHECKER_414"));
            Messages.put(Index_415, properties.getProperty("NAME_CHECKER_415"));
            Messages.put(Index_416, properties.getProperty("NAME_CHECKER_416"));
            Messages.put(Index_417, properties.getProperty("NAME_CHECKER_417"));
            Messages.put(Index_418, properties.getProperty("NAME_CHECKER_418"));
            Messages.put(Index_419, properties.getProperty("NAME_CHECKER_419"));
            Messages.put(Index_420, properties.getProperty("NAME_CHECKER_420"));
            Messages.put(Index_421, properties.getProperty("NAME_CHECKER_421"));

            Messages.put(Index_800, properties.getProperty("REACHABILITY_800"));
            Messages.put(Index_801, properties.getProperty("REACHABILITY_801"));
            Messages.put(Index_802, properties.getProperty("REACHABILITY_802"));
            Messages.put(Index_803, properties.getProperty("REACHABILITY_803"));
            Messages.put(Index_804, properties.getProperty("REACHABILITY_804"));
            Messages.put(Index_805, properties.getProperty("REACHABILITY_805"));
            Messages.put(Index_806, properties.getProperty("REACHABILITY_806"));
            Messages.put(Index_807, properties.getProperty("REACHABILITY_807"));
            Messages.put(Index_808, properties.getProperty("REACHABILITY_808"));
            Messages.put(Index_809, properties.getProperty("REACHABILITY_809"));
            Messages.put(Index_810, properties.getProperty("REACHABILITY_810"));
            Messages.put(Index_811, properties.getProperty("REACHABILITY_811"));
            Messages.put(Index_812, properties.getProperty("REACHABILITY_812"));
            Messages.put(Index_813, properties.getProperty("REACHABILITY_813"));

        }

    }

}