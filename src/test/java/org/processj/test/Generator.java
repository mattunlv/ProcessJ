package org.processj.test;

public class Generator {

    /// ---------------------
    /// Public Static Methods

    public static String GeneratePragmaWith(final String identifier, final String stringLiteral) {

        // Initialize the string builder
        final StringBuilder stringBuilder = new StringBuilder();

        // Check for a valid identifier
        if(identifier != null && !identifier.isBlank()) {

            // Append the keyword & identifier
            stringBuilder.append("#pragma").append(identifier);

            // If the string literal is not null, append it
            if(stringLiteral != null && !stringLiteral.isBlank())
                stringBuilder.append('\"').append(stringLiteral).append('\"');

            // Finally append the semicolon
            stringBuilder.append(';');

        }

        // Return the result
        return stringBuilder.toString();

    }

}
