## Method Documentation

Method documentation must follow the javadoc structure starting with /** and ending with **/ immediately before
the method implementation.

The general documentation content must be wrapped by the <p> </p> HTML tags.

Any mentions of classes must be referenced by the {@link ...} annotation.

Following the description of the Method, the following annotations must be present in the javadoc in verbatim order

    @param - (If present & Applicable to each parameter). A description of the parameter's function & category relative
             to its' use within the method implementation.
    @since - The version the method implementation was introduced
    @see - All classes mentioned in the content wrapped in the <p> </p> HTML tags

Any overridden methods must be annotated with @Override

Any non-overridden (or intended to be non-overridden) method implementations must be declared as 'final'.
Use the strongest access modifier starting with 'private'.

Any overridden methods invoked strictly in the defining package must be specified with at least 'protected' access
privileges.

All parameters must be declared 'final'

No class fields must be declared 'public'

Any referenced non-static class fields must be prefixed with 'this'
Any non-mutable class fields must be specified with the 'private' & 'final'

Accessing a class field requires a 'get' method

Any reference static Class fields must be prefixed with the class name

