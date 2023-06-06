package org.processj.compiler.phases.legacy;

import java.util.LinkedHashSet;
import java.util.Set;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.ProtocolTypeDecl;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;
import org.processj.compiler.utilities.Log;

/**
 * <p>Rewrite {@link Visitor} that aggregates all inherited {@link Name}s from a {@link ProtocolTypeDecl}'s
 * inheritance hierarchy in the {@link ProtocolTypeDecl} in question, since multiple inheritance is not supported in
 * Java.</p>
 * @see Name
 * @see ProtocolTypeDecl
 * @see Visitor
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class ProtocolRewrite extends Phase {

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Recursively aggregates all of the {@link Name}s in the {@link ProtocolTypeDecl}'s inheritance
     * hierarchy using the {@link SymbolMap} to resolve the ancestor {@link ProtocolTypeDecl} names.</p>
     * @param members The {@link Set} where all the {@link Name}s are aggregated.
     * @param symbolMap The {@link SymbolMap} that is used to resolve all ancestor {@link ProtocolTypeDecl} names.
     * @param protocolTypeDeclaration The {@link ProtocolTypeDecl} to recur from.
     * @since 0.1.0
     */
    private static void EmplaceAncestorNames(final Set<Name> members, final SymbolMap symbolMap,
                                             final ProtocolTypeDecl protocolTypeDeclaration) {

        // Iterate through any parent names
        for(final Name parent: protocolTypeDeclaration.extend()) {

            // Initialize the preliminary result
            final Object result = symbolMap.get(parent.toString());

            // Check for a ProtocolTypeDecl (This probably shouldn't be necessary) & recur
            if(result instanceof ProtocolTypeDecl)
                EmplaceAncestorNames(members, symbolMap, (ProtocolTypeDecl) result);

        }

        // Iterate through the local RecordMembers
        for(final Name ancestorName: protocolTypeDeclaration.extend()) {

            Log.log(protocolTypeDeclaration, "adding member " + ancestorName);

            if(!members.add(ancestorName))
                Log.log(protocolTypeDeclaration, String.format("Name '%s' already in (%s)",
                        ancestorName, protocolTypeDeclaration));

        }

    }

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ProtocolRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public ProtocolRewrite(final Phase.Listener listener) {
        super(listener);
    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    /**
     * <p>Recursively aggregates all the {@link Name}s in the {@link ProtocolTypeDecl}'s inheritance hierarchy
     * & places them into the specified {@link ProtocolTypeDecl}.</p>
     * @param protocolTypeDecl The {@link ProtocolTypeDecl} to aggregate the {@link Name}s.
     * @since 0.1.0
     */
    @Override
    public final Void visitProtocolTypeDecl(final ProtocolTypeDecl protocolTypeDecl) {

        Log.log(protocolTypeDecl, "Visiting a RecordTypeDecl (" + protocolTypeDecl + ")");

        // Initialize the Set of Names
        final Set<Name> ancestors = new LinkedHashSet<>();

        // Recursively aggregate the Name
        EmplaceAncestorNames(ancestors, this.getScope(), protocolTypeDecl);

        // Clear the existing Names
        protocolTypeDecl.extend().clear();

        // Emplace all the RecordMembers
        ancestors.forEach(name -> protocolTypeDecl.extend().append(name));

        // Log
        Log.log(protocolTypeDecl, String.format("record %s with %s member(s)",
                protocolTypeDecl, protocolTypeDecl.extend().size()));

        // Print
        protocolTypeDecl.extend().forEach(name ->
                Log.log(protocolTypeDecl, "> Ancestor: " + name));

        return null;

    }

}
