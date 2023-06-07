package org.processj.compiler.ast;

import org.processj.compiler.phases.phase.Phase;

import java.util.*;

import static org.processj.compiler.utilities.Reflection.*;

/**
 * <p>Encapsulates a scope that contains names that should be visible to their owner.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class SymbolMap {

    /// ----------------------
    /// Private Static Methods

    /**
     * <p>Merges the variadic list of {@link SymbolMap} instances into one {@link Map}</p>
     * @param symbolMaps The {@link SymbolMap}s to merge.
     * @return {@link Map} containing each of the {@link SymbolMap} parameter entries.
     * @see Map
     * @since 0.1.0
     */
    private static Map<String, Object> Merged(final SymbolMap... symbolMaps) {

        // Initialize the resultant Map
        final Map<String, Object> result = new HashMap<>();

        // Iterate through each map and aggregate the entries
        Arrays.stream(symbolMaps).forEach(symbolMap -> result.putAll(symbolMap.entries));

        // Return the result
        return result;

    }

    /**
     * <p>Merges the variadic list of {@link Map} instances into one {@link Map}</p>
     * @param maps The {@link Map}s to merge.
     * @return {@link Map} containing each of the {@link Map} parameter entries.
     * @see Map
     * @since 0.1.0
     */
    private static Map<String, Object> Merged(final Map<String, Object>... maps) {

        // Initialize the resultant Map
        final Map<String, Object> result = new HashMap<>();

        // Iterate through each map and aggregate the entries
        Arrays.stream(maps).forEach(result::putAll);

        // Return the result
        return result;

    }

    /// --------------
    /// Private Fields

    /**
     * <p>The set of {@link String}-{@link Object} pairs that compose the {@link SymbolMap}.</p>
     */
    private final Map<String, Object>       entries         ;

    /**
     * <p>The set of {@link String}-{@link Object} pairs of constructs that were not defined in the top-most scope.</p>
     */
    private final Map<String, SymbolMap>    importedEntries ;

    /**
     * <p>The scope that contains this {@link SymbolMap}.</p>
     */
    private final SymbolMap                 enclosingScope  ;

    /**
     * <p>The representing {@link Context}.</p>
     */
    private final Context                   context         ;

    /**
     * <p>The package name corresponding with the {@link SymbolMap}.</p>
     */
    private final String                    packageName     ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link SymbolMap} to its default state with the specified {@link SymbolMap}. This constructor
     * will initialize a new set of entries for the immediate scope, but will assign the specified {@link SymbolMap}'s
     * imported entries & package name. This constructor is to instantiate inner-scope {@link SymbolMap}s that define
     * names for a contained scope.</p>
     * @param parent The {@link SymbolMap} value to inherit from.
     * @since 0.1.0
     */
    private SymbolMap(final SymbolMap parent, final Context context) {

        this.entries            = new HashMap<>()                                                       ;
        this.enclosingScope     = parent                                                                ;
        this.context            = context                                                               ;
        this.importedEntries    = (parent != null) ? parent.importedEntries : new HashMap<>()           ;
        this.packageName        = (parent != null) ? parent.packageName
                : ((context != null) ? context.getPackageName() : "")  ;

    }

    /**
     * <p>Initializes the {@link SymbolMap} to its default state with the specified {@link SymbolMap}. This constructor
     * will initialize a new set of entries for the immediate scope, but will assign the specified {@link SymbolMap}'s
     * imported entries & package name. This constructor is to instantiate inner-scope {@link SymbolMap}s that define
     * names for a contained scope.</p>
     * @param parent The {@link SymbolMap} value to inherit from.
     * @since 0.1.0
     */
    private SymbolMap(final SymbolMap parent, final Context context, final SymbolMap... symbolMaps) {

        this.entries            = Merged(symbolMaps)        ;
        this.importedEntries    = parent.importedEntries    ;
        this.enclosingScope     = parent                    ;
        this.packageName        = parent.packageName        ;
        this.context            = context                   ;

    }

    public SymbolMap() {
        this(null, null);
    }

    /// ---------------
    /// Private Methods

    /**
     * <p>Retrieves a {@link List} of instances that match the specified {@link String} name where each instance
     * is accessible from the current scope.</p>
     * @param name The {@link String} value of the name to resolve.
     * @param results The {@link List} of results.
     * @param caller The {@link SymbolMap} that originally invoked the method.
     * @return {@link List} of instances that match the specified {@link String} name.
     * @see List
     * @see SymbolMap
     * @since 0.1.0
     */
    private List<Object> get(final String name, final List<Object> results, final SymbolMap caller) {

        // If we have the name in the immediate vicinity, append it to the results
        if(this.entries.containsKey(name)) results.add(this.entries.get(name));

        // Aggregate any results if nothing in the immediate scope was retrieved & we have an enclosing scope
        if(results.isEmpty() && (this.enclosingScope != null))
            results.addAll(this.enclosingScope.get(name, results, caller));

        // If the results are still empty, check the imported names if we're at the original call site
        // since they're only visible to the caller's context
        if(results.isEmpty() && (this == caller))
            this.importedEntries.forEach((key, value) -> results.addAll(value.get(name, results, caller)));

        // Finally, return the list of results
        return results;

    }

    /**
     * <p>Retrieves a {@link List} of instances that match the specified {@link String} name & {@link String} package
     * name where each instance is accessible from the current scope.</p>
     * @param name The {@link String} value of the name to resolve.
     * @param packageName The {@link String} value of the package name to resolve.
     * @param results The {@link List} of results.
     * @param caller The {@link SymbolMap} that originally invoked the method.
     * @return {@link List} of instances that match the specified {@link String} name.
     * @see List
     * @see SymbolMap
     * @since 0.1.0
     */
    private List<Object> get(final String name, final String packageName,
                             final List<Object> results, final SymbolMap caller) {

        // If the specified package name is empty or if it matches the current package name;
        // add all the reachable symbols.
        if((packageName == null)
                || packageName.isBlank()
                || packageName.isEmpty()
                || this.packageName.equals(packageName))
            results.addAll(this.get(name, results, this));

        // Otherwise, if that package is available to us, retrieve anything from the imports that we can find
        else if((this == caller) && this.importedEntries.containsKey(packageName))
            results.addAll(this.importedEntries.get(packageName).get(name, packageName, results, caller));

        // Finally, return the list of results
        return results;

    }

    /// --------------
    /// Public Methods

    /**
     * <p>Returns the {@link SymbolMap}'s enclosing scope.</p>
     * @return The {@link SymbolMap}'s enclosing scope.
     * @since 0.1.0
     */
    public final SymbolMap getEnclosingScope() {

        return this.enclosingScope;

    }

    /**
     * <p>Retrieves an instance that matches the {@link String} name within the {@link String} package name.</p>
     * @param name The {@link String} name to resolve.
     * @param packageName The {@link String} package name to resolve.
     * @return Instances matching the {@link String} name within the {@link String} package name.
     * @since 0.1.0
     */
    public final List<Object> get(final String packageName, final String name) {

        // Return the result
        return this.get(name, packageName, new ArrayList<>(), this);

    }

    /**
     * <p>Retrieves an aggregated {@link List} of instances that match the specified {@link String} name. This is
     * to cover the (error) cases where a name resolves more than one definition due to multiple imports specifying
     * the same name, but with the absence of a fully-qualified name.</p>
     * @param name The {@link String} name to resolve.
     * @return {@link List} of instances matching the {@link String} name.
     * @since 0.1.0
     */
    public final List<Object> get(final String name) {

        // Return the results. It is possible that an import might define the same name as
        // another import, so we naturally just return the list of results so the caller can
        // throw an error.
        return this.get(null, name);

    }

    /**
     * <p>Emplaces the specified {@link Object} instance corresponding with the name into the immediate scope &
     * returns a flag indicating if the specified {@link String} name already defined an instance within the scope.</p>
     * @param name The {@link String} value corresponding to the {@link Object} instance.
     * @param instance The {@link Object} instance to emplace
     * @return Flag indicating if the specified {@link String} name already defined an instance within the scope.
     * @since 0.1.0
     */
    public final boolean put(final String name, final Object instance) {

        // Initialize the result flag
        final boolean doesExist = this.entries.containsKey(name);

        // If the entity does not exist, emplace it
        if(!doesExist) this.entries.put(name, instance);

        // Return the flag
        return !doesExist;

    }

    /**
     * <p>Emplaces the specified {@link Object} instance into the immediate scope & returns a flag indicating if the
     * specified {@link String} name already defined an instance within the scope. This method will use the
     * {@link Object}'s {@link Object#toString()} method to index the {@link Object}.</p>
     * @param instance The {@link Object} instance to emplace
     * @return Flag indicating if the specified {@link String} name already defined an instance within the scope.
     * @since 0.1.0
     */
    public final boolean put(final Object instance) {

        return this.put(instance.toString(), instance);

    }

    /**
     * <p>Emplaces the specified {@link SymbolMap} instance corresponding with the name as an imported scope &
     * returns a flag indicating if the specified {@link String} name already defined an imported instance.</p>
     * @param name The {@link String} value corresponding to the {@link Object} instance.
     * @param importedSymbolMap The {@link SymbolMap} instance to emplace
     * @return Flag indicating if the specified {@link String} name already defined an instance within the scope.
     * @since 0.1.0
     */
    public final boolean putImport(final String name, final SymbolMap importedSymbolMap) {

        // Initialize the result flag
        final boolean doesExist = this.importedEntries.containsKey(name);

        // If the entity does not exist, emplace it
        if(!doesExist) this.importedEntries.put(name, importedSymbolMap);

        // Return the flag
        return doesExist;

    }

    public final Context getContext() {

        return this.context;

    }

    public final void forEachSymbol(final EntryCallback entryCallback) {

        // If the entries & callback are valid
        if((this.entries != null) && (entryCallback != null)) {

            // Iterate each key-value pair
            this.entries.forEach((key, value) -> entryCallback.Invoke(value));

        }

    }

    public final void forEachEnclosingContext(final ContextCallback contextCallback) throws Phase.Error {

        // If the context & callback are valid
        if((this.context != null) && (contextCallback != null)) {

            // Initialize the enclosing Scope
            SymbolMap scope = this;

            do {

                // Call back with the Context
                contextCallback.Invoke(scope.context);

                // Update
                scope = scope.getEnclosingScope();

            } while((scope != null) && (scope.context != null));

        }

    }

    public final boolean forEachContextUntil(final UntilContextCallback untilContextCallback) throws Phase.Error {

        boolean result = false;

        // If the context & callback are valid
        if((this.context != null) && (untilContextCallback != null)) {

            // Initialize the enclosing Scope
            SymbolMap scope = this;

            do {

                // Call back with the context
                result = untilContextCallback.Invoke(scope.context);

                // Update
                scope = scope.getEnclosingScope();

            } while(!result && (scope != null) && (scope.context != null));

        }

        return result;

    }

    public final SymbolMap.Context forEachContextRetrieve(final UntilContextCallback untilContextCallback) throws Phase.Error {

        // Initialize the result
        SymbolMap.Context context = null;

        // If the callback is valid
        if(untilContextCallback != null) {

            // Initialize the enclosing Scope
            SymbolMap scope = this;

            // Iterate
            do {

                // Update the result
                context = scope.context;

                // Set the next scope
                scope   = scope.getEnclosingScope();

            } while(scope != null && !untilContextCallback.Invoke(context));

        }

        return context;

    }

    public final void forEachEntryUntil(final UntilEntryCallback untilEntryCallback) throws Phase.Error {

        // If the entries & callback are valid, iterate through each entry
        if((this.entries != null) && (untilEntryCallback != null))
            for(final Map.Entry<String, Object> entry: this.entries.entrySet())
                if(untilEntryCallback.Invoke(entry.getValue())) break;

    }

    /// ----------
    /// Interfaces

    /**
     * <p>The entity that the {@link SymbolMap} represents.</p>
     * @author Carlos L. Cuenca
     * @version 1.0.0
     * @since 0.1.0
     */
    public interface Context {

        default String getPackageName() {

            return "";

        }

        default boolean setYields() { return false; }

        default SymbolMap openScope(final SymbolMap symbolMap) throws ContextDoesNotDefineScopeException {

            // Check if the Context defines a scope
            if(!DoesDeclareField(this, "scope"))
                throw new ContextDoesNotDefineScopeException(this);

            // Otherwise, retrieve the value of the field
            SymbolMap scope = (SymbolMap) GetFieldValue(this, "scope");

            // If the scope is null, instantiate a new one
            if(scope == null) {

                // Instantiate the scope
                scope = new SymbolMap(symbolMap, this);

                // Update the field value
                SetFieldValueOf(this, "scope", scope);

            }

            return scope;

        }

        default SymbolMap openScope() throws ContextDoesNotDefineScopeException {

            return this.openScope(null);

        }

        /// ----------
        /// Exceptions

        final class ContextDoesNotDefineScopeException extends Phase.Error {

            private final static String Message = "Context: '%s' does not define scope.";

            final Context culprit;

            private ContextDoesNotDefineScopeException(final Context context) {
                super(null); // TODO: Check this

                this.culprit = context;

            }

            @Override
            public String getMessage() {

                return String.format(Message, this.culprit);

            }

        }

    }

    @FunctionalInterface
    public interface EntryCallback {

        void Invoke(final Object instance);

    }

    @FunctionalInterface
    public interface ContextCallback {

        void Invoke(final Context context) throws Phase.Error;

    }

    @FunctionalInterface
    public interface UntilContextCallback {

        boolean Invoke(final Context context) throws Phase.Error;

    }

    @FunctionalInterface
    public interface UntilEntryCallback {

        boolean Invoke(final Object object) throws Phase.Error;

    }

}
