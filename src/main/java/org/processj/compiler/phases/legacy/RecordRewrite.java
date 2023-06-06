package org.processj.compiler.phases.legacy;

import java.util.LinkedHashSet;
import java.util.Set;

import org.processj.compiler.ast.Name;
import org.processj.compiler.ast.RecordMember;
import org.processj.compiler.ast.RecordTypeDecl;
import org.processj.compiler.ast.SymbolMap;
import org.processj.compiler.phases.phase.Phase;
import org.processj.compiler.phases.phase.Visitor;
import org.processj.compiler.utilities.Log;

/**
 * <p>Rewrite {@link Visitor} that aggregates all inherited {@link RecordMember}s from a {@link RecordTypeDecl}'s
 * inheritance hierarchy in the {@link RecordTypeDecl} in question, since multiple inheritance is not supported in
 * Java.</p>
 * @see RecordMember
 * @see RecordTypeDecl
 * @see Visitor
 * @author Benjamin Cisneros
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class RecordRewrite extends Phase {

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Recursively aggregates all of the {@link RecordMember}s in the {@link RecordTypeDecl}'s inheritance
     * hierarchy using the {@link SymbolMap} to resolve the ancestor {@link RecordTypeDecl} names.</p>
     * @param members The {@link Set} where all the {@link RecordMember}s are aggregated.
     * @param symbolMap The {@link SymbolMap} that is used to resolve all ancestor {@link RecordTypeDecl} names.
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to recur from.
     * @since 0.1.0
     */
    private static void EmplaceRecordMembers(final Set<RecordMember> members, final SymbolMap symbolMap,
                                             final RecordTypeDecl recordTypeDeclaration) {

        // Iterate through any parent names
        for(final Name parent: recordTypeDeclaration.getExtends()) {

            // Initialize the preliminary result
            final Object result = symbolMap.get(parent.toString());

            // Check for a RecordTypeDecl (This probably shouldn't be necessary) & recur
            if(result instanceof RecordTypeDecl)
                EmplaceRecordMembers(members, symbolMap, (RecordTypeDecl) result);

        }

        // Iterate through the local RecordMembers
        for(final RecordMember recordMember: recordTypeDeclaration.getBody()) {

            Log.log(recordTypeDeclaration, "adding member " + recordMember.getType() + " " + recordMember);

            if(!members.add(recordMember))
                Log.log(recordTypeDeclaration, String.format("Name '%s' already in (%s)",
                        recordMember, recordTypeDeclaration));

        }

    }

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link RecordRewrite} to its' default state with the specified {@link Phase.Listener}.</p>
     * @param listener The {@link Phase.Listener} that receives any {@link Phase.Message}, {@link Phase.Warning},
     *                 or {@link Phase.Error} messages.
     * @since 0.1.0
     */
    public RecordRewrite(final Phase.Listener listener) {
        super(listener);
    }

    /// ------------------------------
    /// org.processj.utilities.Visitor

    /**
     * <p>Recursively aggregates all the {@link RecordMember}s in the {@link RecordTypeDecl}'s inheritance hierarchy
     * & places them into the specified {@link RecordTypeDecl}.</p>
     * @param recordTypeDeclaration The {@link RecordTypeDecl} to aggregate the {@link RecordMember}s.
     * @since 0.1.0
     */
    @Override
    public final Void visitRecordTypeDecl(final RecordTypeDecl recordTypeDeclaration) {

        Log.log(recordTypeDeclaration, "Visiting a RecordTypeDecl (" + recordTypeDeclaration + ")");

        // Initialize the Set of RecordMembers
        final Set<RecordMember> members = new LinkedHashSet<>();

        // Recursively aggregate the RecordMembers
        EmplaceRecordMembers(members, this.getScope(), recordTypeDeclaration);

        // Clear the existing RecordMembers
        recordTypeDeclaration.getBody().clear();

        // Emplace all the RecordMembers
        members.forEach(recordMember -> recordTypeDeclaration.getBody().append(recordMember));

        // Log
        Log.log(recordTypeDeclaration, String.format("record %s with %s member(s)",
                recordTypeDeclaration, recordTypeDeclaration.getBody().size()));

        // Print
        recordTypeDeclaration.getBody().forEach(recordMember ->
                Log.log(recordTypeDeclaration, "> member " + recordMember.getType() + " " + recordMember.getName()));

        return null;

    }

}
