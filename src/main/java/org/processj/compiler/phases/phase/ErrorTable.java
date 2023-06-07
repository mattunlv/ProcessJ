package org.processj.compiler.phases.phase;

import org.processj.compiler.ast.*;

import java.io.FileInputStream;
import java.util.*;

import org.processj.compiler.phases.phase.Phase.SemanticAssert;
import org.processj.compiler.phases.phase.Phase.ReachabilityAssert;

public class ErrorTable {

    /// ------------------------
    /// Private Static Constants

    private final static String PropertiesPath = "src/main/resources/properties/";

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
