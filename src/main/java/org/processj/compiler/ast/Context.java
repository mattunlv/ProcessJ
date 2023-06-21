package org.processj.compiler.ast;

import org.processj.compiler.ast.expression.resolve.NameExpression;
import org.processj.compiler.ast.type.ProcedureTypeDeclaration;
import org.processj.compiler.phase.Phase;

import java.util.*;

/**
 * <p>The entity that the {@link SymbolMap} represents.</p>
 *
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public abstract class Context {

    private Context enclosingContext;
    private SymbolMap scope;


    public void openScope() {

        if((this.scope == null) || (this != this.scope.owningContext))
            this.scope = new SymbolMap(this.enclosingContext);

    }

    public boolean setYields() {
        return false;
    }

    public String getPackageName() {

        return "";

    }

    public Context closeContext() {

        return this.enclosingContext;

    }

    public Context openContext(final Context enclosingContext) {

        // Update the enclosing Context
        this.enclosingContext = enclosingContext;

        // Update the Scope to the enclosing Context's if any
        this.scope = ((this.scope == null) && (this.enclosingContext != null)) ? this.enclosingContext.scope : null;

        // Return ourselves
        return this;

    }

    public SymbolMap getScope() {
        return this.scope;
    }

    public Context getEnclosingContext() {
        return this.enclosingContext;
    }

    public boolean forEachContextUntil(final UntilContextCallback untilContextCallback) throws Phase.Error {

        boolean result = false;

        Context context = this;

        if(untilContextCallback != null) do {

            // Callback with the Context
            result = untilContextCallback.Invoke(context);

            // Update
            context = context.getEnclosingContext();

        } while ((context != null) && !result);

        return result;

    }

    public final Context forEachContextRetrieve(final UntilContextCallback untilContextCallback) throws Phase.Error {

        boolean result = false;

        // Initialize the result
        Context context = this;

        // If the callback is valid
        if(untilContextCallback != null) {

            // Iterate
            do {

                context = context.getEnclosingContext();

                result = untilContextCallback.Invoke(context);

            } while (context != null && !result);

        }

        return context;

    }

    public final void forEachEnclosingContext(final ContextCallback contextCallback) throws Phase.Error {

        // If the context & callback are valid
        if(contextCallback != null) {

            Context context = this;

            do {

                // Call back with the Context
                contextCallback.Invoke(context);

                // Update
                context = context.getEnclosingContext();

            } while (context != null);

        }

    }

    /// -------
    /// Classes

    public static class SymbolMap {

        /// ----------------------
        /// Private Static Methods

        /**
         * <p>Merges the variadic list of {@link SymbolMap} instances into one {@link Map}</p>
         *
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
         *
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
        private final Map<String, Object> entries;

        /**
         * <p>The set of {@link String}-{@link Object} pairs of constructs that were not defined in the top-most scope.</p>
         */
        private final Map<String, SymbolMap> importedEntries;

        /**
         * <p>The collection of {@link Pragma}s decoded from the highest order ancestor.</p>
         */
        private final Map<String, String> pragmaMap;

        /**
         * <p>The scope that contains this {@link SymbolMap}.</p>
         */
        private final SymbolMap enclosingScope;

        /**
         * <p>Flag indicating if the scope is specified as a native library</p>
         */
        private boolean isNativeLibrary;

        /**
         * <p>Flag indicating if the Scope is specified as native</p>
         */
        private boolean isNative;

        /**
         * <p>{@link List} containing generated values of native {@link ProcedureTypeDeclaration} signatures.</p>
         */
        private List<String> nativeSignatures;

        private NameExpression previousYieldingName;

        private final Context owningContext;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link SymbolMap} to its default state with the specified {@link SymbolMap}. This constructor
         * will initialize a new set of entries for the immediate scope, but will assign the specified {@link SymbolMap}'s
         * imported entries & package name. This constructor is to instantiate inner-scope {@link SymbolMap}s that define
         * names for a contained scope.</p>
         *
         * @param owningContext The {@link org.processj.compiler.ast.Context} value to inherit from.
         * @since 0.1.0
         */
        private SymbolMap(final Context owningContext) {

            final SymbolMap parent = owningContext.getEnclosingContext().getScope();

            this.entries            = new HashMap<>();
            this.importedEntries    = (parent != null) ? parent.importedEntries : new HashMap<>();
            this.enclosingScope     = parent;
            this.owningContext      = owningContext;
            this.pragmaMap = new HashMap<>();
            this.nativeSignatures = new ArrayList<>();
            this.isNative = false;
            this.isNativeLibrary = false;
            this.previousYieldingName = null;

        }

        public SymbolMap() {
            this(null);
        }

        /// ---------------
        /// Private Methods

        /**
         * <p>Retrieves a {@link List} of instances that match the specified {@link String} name where each instance
         * is accessible from the current scope.</p>
         *
         * @param name    The {@link String} value of the name to resolve.
         * @param results The {@link List} of results.
         * @param caller  The {@link SymbolMap} that originally invoked the method.
         * @return {@link List} of instances that match the specified {@link String} name.
         * @see List
         * @see SymbolMap
         * @since 0.1.0
         */
        private List<Object> get(final String name, final List<Object> results, final SymbolMap caller) {

            // If we have the name in the immediate vicinity, append it to the results
            if (this.entries.containsKey(name)) results.add(this.entries.get(name));

            // Aggregate any results if nothing in the immediate scope was retrieved & we have an enclosing scope
            if (results.isEmpty() && (this.enclosingScope != null))
                results.addAll(this.enclosingScope.get(name, results, caller));

            // If the results are still empty, check the imported names if we're at the original call site
            // since they're only visible to the caller's context
            if (results.isEmpty() && (this == caller))
                this.importedEntries.forEach((key, value) -> results.addAll(value.get(name, results, caller)));

            // Finally, return the list of results
            return results;

        }

        /**
         * <p>Retrieves a {@link List} of instances that match the specified {@link String} name & {@link String} package
         * name where each instance is accessible from the current scope.</p>
         *
         * @param name        The {@link String} value of the name to resolve.
         * @param packageName The {@link String} value of the package name to resolve.
         * @param results     The {@link List} of results.
         * @param caller      The {@link SymbolMap} that originally invoked the method.
         * @return {@link List} of instances that match the specified {@link String} name.
         * @see List
         * @see SymbolMap
         * @since 0.1.0
         */
        private List<Object> get(final String name, final String packageName,
                                 final List<Object> results, final SymbolMap caller) {

            // If the specified package name is empty or if it matches the current package name;
            // add all the reachable symbols.
            if ((packageName == null)
                    || packageName.isBlank()
                    || packageName.isEmpty())
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
         *
         * @return The {@link SymbolMap}'s enclosing scope.
         * @since 0.1.0
         */
        public final SymbolMap getEnclosingScope() {

            return this.enclosingScope;

        }

        /**
         * <p>Retrieves an instance that matches the {@link String} name within the {@link String} package name.</p>
         *
         * @param name        The {@link String} name to resolve.
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
         *
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
         *
         * @param name     The {@link String} value corresponding to the {@link Object} instance.
         * @param instance The {@link Object} instance to emplace
         * @return Flag indicating if the specified {@link String} name already defined an instance within the scope.
         * @since 0.1.0
         */
        public final boolean put(final String name, final Object instance) {

            // Initialize the result flag
            final boolean doesExist = this.entries.containsKey(name);

            // If the entity does not exist, emplace it
            if (!doesExist) this.entries.put(name, instance);

            // Return the flag
            return !doesExist;

        }

        /**
         * <p>Emplaces the specified {@link Object} instance into the immediate scope & returns a flag indicating if the
         * specified {@link String} name already defined an instance within the scope. This method will use the
         * {@link Object}'s {@link Object#toString()} method to index the {@link Object}.</p>
         *
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
         *
         * @param name              The {@link String} value corresponding to the {@link Object} instance.
         * @param importedSymbolMap The {@link SymbolMap} instance to emplace
         * @return Flag indicating if the specified {@link String} name already defined an instance within the scope.
         * @since 0.1.0
         */
        public final boolean putImport(final String name, final SymbolMap importedSymbolMap) {

            // Initialize the result flag
            final boolean doesExist = this.importedEntries.containsKey(name);

            // If the entity does not exist, emplace it
            if (!doesExist) this.importedEntries.put(name, importedSymbolMap);

            // Return the flag
            return doesExist;

        }

        public final void setPragmasTo(final Map<String, String> decodedPragmaMap) {

            // Assert the Collections are cleared
            this.nativeSignatures.clear();
            this.pragmaMap.clear();

            // Assert the decoded Pragma Map is valid & emplace the decoded Pragmas
            if (decodedPragmaMap != null) this.pragmaMap.putAll(decodedPragmaMap);

            // Reinitialize the native lib specifications
            this.isNative = this.pragmaMap.containsKey("NATIVE");
            this.isNativeLibrary = this.pragmaMap.containsKey("NATIVELIB");

        }

        public final NameExpression getLatestYieldingName() {

            final NameExpression previousYieldingName = this.previousYieldingName;

            this.previousYieldingName = null;

            return previousYieldingName;

        }

        public final void setPreviousYieldingName(final NameExpression nameExpression) {

            this.previousYieldingName = nameExpression;

        }

        public final boolean definesLibraryPragma() {

            return this.pragmaMap.containsKey("LIBRARY");

        }

        public final boolean definesNativeSignatures() {

            return this.nativeSignatures.isEmpty();

        }

        public final Map<String, String> getPragmaMap() {

            return this.pragmaMap;

        }

        public final boolean isNative() {

            return this.isNative;

        }

        public final String getNativeFilename() {

            return this.pragmaMap.getOrDefault("FILE", "");

        }

        public final boolean isNativeLibrary() {

            return this.isNativeLibrary;

        }

        public final List<String> getNativeSignatures() {

            return this.nativeSignatures;

        }

        public final String getNativeLibrary() {

            return this.pragmaMap.getOrDefault("NATIVELIB", "");

        }

        public final void aggregateNativeSignature(final String signature) {

            this.nativeSignatures.add(signature);

        }

        public final void forEachSymbol(final SymbolMap.EntryCallback entryCallback) {

            // If the entries & callback are valid
            if ((this.entries != null) && (entryCallback != null)) {

                // Iterate each key-value pair
                this.entries.forEach((key, value) -> entryCallback.Invoke(value));

            }

        }

        public final void forEachEntryUntil(final UntilEntryCallback untilEntryCallback) throws Phase.Error {

            // If the entries & callback are valid, iterate through each entry
            if ((this.entries != null) && (untilEntryCallback != null))
                for (final Map.Entry<String, Object> entry : this.entries.entrySet())
                    if (untilEntryCallback.Invoke(entry.getValue())) break;

        }

        public final void clear() {

            this.entries.clear();

        }

        @FunctionalInterface
        public interface EntryCallback {

            void Invoke(final Object instance);

        }

        @FunctionalInterface
        public interface UntilEntryCallback {

            boolean Invoke(final Object object) throws Phase.Error;

        }

    }

    @FunctionalInterface
    public interface UntilContextCallback {

        boolean Invoke(final Context context) throws Phase.Error;

    }

    @FunctionalInterface
    public interface ContextCallback {

        void Invoke(final Context context) throws Phase.Error;

    }

}
