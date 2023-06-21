package org.processj.compiler.phase.jvm;

import org.processj.compiler.phase.jvm.instruction.Instruction;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassFile {

    /// -----------------
    /// Private Constants

    private final static int Magic = 0xCAFEBABE ;

    /// --------------
    /// Private Fields

    /**
     * <p>The {@link String} value of the class's name corresponding to the {@link ClassFile}.</p>
     * @since 0.1.0
     */
    private final String        className              ;

    /**
     * <p>The {@link String} value of the package name corresponding to the {@link ClassFile}.</p>
     * @since 0.1.0
     */
    private final String        packageName             ;

    /**
     * <p>The minimum major version supporting the {@link ClassFile}.</p>
     * @since 0.1.0
     */
    private final short         majorVersion            ;

    /**
     * <p>The minimum minor version supporting the {@link ClassFile}.</p>
     */
    private final short         minorVersion            ;

    protected final List<Field> fields                  ;

    protected final List<Info>  constantPool            ;

    /// ------------
    /// Constructors

    /**
     * <p>Initializes the {@link ClassFile} to its' default state with the specified {@link String} values
     * of the class & package name.</p>
     * @param className The class name.
     * @param packageName The package name.
     * @since 0.1.0
     */
    public ClassFile(final String className, final String packageName) {

        this.className      = (className != null)   ? className     : "";
        this.packageName    = (packageName != null) ? packageName   : "";
        this.majorVersion   = 0x0034                                    ; // Version 8 TODO: Add the others
        this.minorVersion   = 0x0000                                    ; // Version 8
        this.fields         = new ArrayList<>();
        this.constantPool   = new ArrayList<>();

    }

    /// -------
    /// Classes

    /**
     * <p>Attributes are used in the {@link ClassFile}, {@link FieldInfo}, method_info, Code_attribute, and
     * record_component_info structures of the {@link ClassFile} format (§4.1, §4.5, §4.6, §4.7.3, §4.7.30).
     * For all attributes, the attribute_name_index item must be a valid unsigned 16-bit index into the constant pool
     * of the class. The constant_pool entry at attribute_name_index must be a {@link ConstantUtf8} structure (§4.4.7)
     * representing the name of the attribute. The value of the attribute_length item indicates the length of the
     * subsequent information in bytes. The length does not include the initial six bytes that contain the
     * attribute_name_index and attribute_length items.
     * 30 attributes are predefined by this specification. They are listed three times, for ease of navigation:
     *      Table 4.7-A is ordered by the attributes' section numbers in this chapter.
     *      Each attribute is shown with the first version of the class file format in which it was defined.
     *      Also shown is the version of the Java SE Platform which introduced that version of the class
     *      file format (§4.1).
     *      Table 4.7-B is ordered by the first version of the class file format in which each attribute was defined.
     *      Table 4.7-C is ordered by the location in a class file where each attribute is defined to appear.
     *      Within the context of their use in this specification, that is, in the attributes tables of the
     *      {@link ClassFile} structures in which they appear, the names of these predefined attributes are reserved.
     * Any conditions on the presence of a predefined attribute in an attributes table are specified explicitly in
     * the section which describes the attribute. If no conditions are specified, then the attribute may appear any
     * number of times in an attributes table.</p>
     * @see ClassFile
     * @see FieldInfo
     * @see ConstantUtf8
     * @author Carlos L. Cuenca
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class Attribute { }

    public static class Code extends Attribute {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link Map} containing the {@link Code}'s {@link Local} variables.</p>
         * @since 0.1.0
         */
        private final Map<String, Local>        locals          ;

        /**
         * <p>The {@link Map} containing the {@link Code}'s {@link Parameter}s.</p>
         * @since 0.1.0
         */
        private final Map<String, Parameter>    parameters      ;

        /**
         * <p>The {@link Map} containing the {@link Code}'s
         * {@link Instruction}s.</p>
         * @since 0.1.0
         */
        private final List<Instruction>         instructions    ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Code} to its' default state.</p>
         * @since 0.1.0
         */
        public Code() {

            this.locals         = new HashMap<>()   ;
            this.parameters     = new HashMap<>()   ;
            this.instructions   = new ArrayList<>() ;

        }

        /// --------------
        /// Public Methods

        public final void addLocal(final Local local) {

            if(local != null)
                this.locals.put(local.getName(), local);

        }

        public final void addParameter(final Parameter parameter) {

            if(parameter != null)
                this.parameters.put(parameter.getName(), parameter);

        }

        public final void addInstruction(final Instruction instruction) {

            this.instructions.add(instruction);

        }

        public final void addConstantInteger(final int integer) {



        }

        public final void addCode(final Code code) {



        }


    }

    /**
     * <p>Represents a local corresponding to the current {@link Code}.</p>
     * @see ClassFile
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class Local {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value corresponding to the {@link Local}.</p>
         * @since 0.1.0
         */
        private final String name   ;

        /**
         * <p>The {@link String} value corresponding the {@link Local}'s type.</p>
         * @since 0.1.0
         */
        private final String type   ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Local} to its default state.</p>
         * @param name The {@link String} value of the name corresponding to the {@link Local}.
         * @since 0.1.0
         */
        public Local(final String name) {

            this.type = "";
            this.name = (name != null) ? name : "";

        }

        /// ------
        /// Object

        /**
         * <p>Returns the {@link String} representation of the {@link Local}.</p>
         * @return The {@link String} representation of the {@link Local}.
         * @since 0.1.0
         */
        @Override
        public final String toString() {

            return this.type + ' ' + this.name;

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns the {@link String} value of the {@link Local}'s name.</p>
         * @return The {@link String} value of the {@link Local}'s name.
         * @since 0.1.0
         */
        public final String getName() {

            return this.name;

        }

        /**
         * <p>Returns the {@link String} value of the {@link Local}'s type.</p>
         * @return The {@link String} value of the {@link Local}'s type.
         * @since 0.1.0
         */
        public final String getType() {

            return this.type;

        }

    }

    /**
     * <p>Represents a parameter corresponding to the current {@link Code}.</p>
     * @see ClassFile
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class Parameter {

        /// --------------
        /// Private Fields

        /**
         * <p>The {@link String} value corresponding to the {@link Parameter}.</p>
         * @since 0.1.0
         */
        private final String name   ;

        /**
         * <p>The {@link String} value corresponding the {@link Parameter}'s type.</p>
         * @since 0.1.0
         */
        private final String type   ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Parameter} to its default state.</p>
         * @param name The {@link String} value of the name corresponding to the {@link Parameter}.
         * @since 0.1.0
         */
        public Parameter(final String name) {

            this.type = "";
            this.name = (name != null) ? name : "";

        }

        /// ------
        /// Object

        /**
         * <p>Returns the {@link String} representation of the {@link Parameter}.</p>
         * @return The {@link String} representation of the {@link Parameter}.
         * @since 0.1.0
         */
        @Override
        public final String toString() {

            return this.type + ' ' + this.name;

        }

        /// --------------
        /// Public Methods

        /**
         * <p>Returns the {@link String} value of the {@link Parameter}'s name.</p>
         * @return The {@link String} value of the {@link Parameter}'s name.
         * @since 0.1.0
         */
        public final String getName() {

            return this.name;

        }

        /**
         * <p>Returns the {@link String} value of the {@link Parameter}'s type.</p>
         * @return The {@link String} value of the {@link Parameter}'s type.
         * @since 0.1.0
         */
        public final String getType() {

            return this.type;

        }

    }









    /**
     * <p>Represents a field corresponding to the current class. Provides convenience methods to create the appropriate
     * {@link ClassFile} entries.</p>
     * @see ClassFile
     * @see FieldInfo
     * @see ConstantFieldRef
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class Field {

        /// --------------
        /// Private Fields

        /**
         * <p>The set of {@link Modifier}s corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final List<Modifier>    modifiers   ;

        /**
         * <p>The set of {@link Attribute}s corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final List<Attribute>   attributes  ;

        /**
         * <p>The {@link String} value corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final String            name        ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Field} to its default state with the specified {@link Modifier}s or
         * {@link Attribute} instances.</p>
         * @param name The {@link String} value of the name corresponding to the {@link Field}.
         * @param modifiersAndAttributes Variadic parameters specifying any {@link Modifier} or {@link Attribute}
         *                               instances corresponding to the {@link Field}.
         * @since 0.1.0
         */
        public Field(final String name, final Object... modifiersAndAttributes) {

            this.name       = name              ;
            this.modifiers  = new ArrayList<>() ;
            this.attributes = new ArrayList<>() ;

            if(modifiersAndAttributes != null) for(final Object object: modifiersAndAttributes) {

                if(object instanceof Modifier)
                    this.modifiers.add((Modifier) object);

                else if(object instanceof Attribute)
                    this.attributes.add((Attribute) object);

            }

        }

        /// -------
        /// Classes

        /**
         * <p>Encapsulates the possible values of access modifiers that may be specified to a {@link Field}.</p>
         * <p>The value of the access_flags item is a mask of flags used to denote access permission to and properties
         * of this field.</p>
         * <p>Fields of classes may set any of the flags in Table 4.5-A. However, each field of a class may have at
         * most one of its ACC_PUBLIC, ACC_PRIVATE, and ACC_PROTECTED flags set (JLS §8.3.1), and must not have both
         * its ACC_FINAL and ACC_VOLATILE flags set (JLS §8.3.1.4).
         * Fields of interfaces must have their ACC_PUBLIC, ACC_STATIC, and ACC_FINAL flags set; they may have their
         * ACC_SYNTHETIC flag set and must not have any of the other flags in Table 4.5-A set (JLS §9.3).
         * The ACC_SYNTHETIC flag indicates that this field was generated by a compiler and does not appear in source
         * code.
         * The ACC_ENUM flag indicates that this field is used to hold an element of an enum class (JLS §8.9).
         * All bits of the access_flags item not assigned in Table 4.5-A are reserved for future use. They should be
         * set to zero in generated class files and should be ignored by Java Virtual Machine implementations.</p>
         * @see Field
         * @author Carlos L. Cuenca
         * @since 0.1.0
         * @version 1.0.0
         */
        public static class Modifier {

            /**
             * <p>Declared public; may be accessed from outside its package.</p>
             * @since 0.1.0
             */
            private final static short ACC_PUBLIC	    = 0x0001    ;

            /**
             * <p>Declared private; accessible only within the defining class and other classes belonging to the same
             * nest (§5.4.4).</p>
             * @since 0.1.0
             */
            private final static short ACC_PRIVATE    = 0x0002    ;

            /**
             * <p>Declared protected; may be accessed within subclasses.</p>
             * @since 0.1.0
             */
            private final static short ACC_PROTECTED  = 0x0004    ;

            /**
             * <p>Declared static.</p>
             * @since 0.1.0
             */
            private final static short ACC_STATIC     = 0x0008    ;

            /**
             * <p>Declared final; never directly assigned to after object construction (JLS §17.5).</p>
             * @since 0.1.0
             */
            private final static short ACC_FINAL      = 0x0010    ;

            /**
             * <p>Declared volatile; cannot be cached.</p>
             * @since 0.1.0
             */
            private final static short ACC_VOLATILE   = 0x0040    ;

            /**
             * <p>Declared transient; not written or read by a persistent object manager.</p>
             * @since 0.1.0
             */
            private final static short ACC_TRANSIENT  = 0x0080    ;

            /**
             * <p>Declared synthetic; not present in the source code.</p>
             * @since 0.1.0
             */
            private final static short ACC_SYNTHETIC  = 0x1000    ;

            /**
             * <p>Declared as an element of an enum class.</p>
             * @since 0.1.0
             */
            private final static short ACC_ENUM       = 0x4000    ;

            /// --------------
            /// Private Fields

            /**
             * <p>The short value corresponding to the {@link Modifier}.</p>
             * @since 0.1.0
             */
            private final short value;

            /// ------------
            /// Constructors

            /**
             * <p>Initializes the {@link Modifier} to its' default state with the specified short value.</p>
             * @param value the short value that specifies which bit to enable in the final field encoding.
             * @since 0.1.0
             */
            private Modifier(final short value) {

                this.value = value;

            }

            /// --------------
            /// Public Methods

            /**
             * <p>Returns a combined {@link Modifier} with the OR result of the specified {@link Modifier}s.</p>
             * @param thoseModifiers The {@link Modifier}s whose value to OR
             * @return combined {@link Modifier}.
             * @since 0.1.0
             */
            public final Modifier combinedWith(final Modifier... thoseModifiers) {

                // Initialize the result
                short result = this.value;

                // Iterate through the modifiers
                if(thoseModifiers != null) for(final Modifier modifier: thoseModifiers) result |= modifier.value;

                // Return the combined Modifier
                return new Modifier(result);

            }

            /**
             * <p>Returns the byte array of the {@link Modifier}'s contents.</p>
             * @return The byte array of the {@link Modifier}'s contents.
             * @since 0.1.0
             */
            public final byte[] getBytes() {

                return new byte[] { (byte) (this.value >> 8), (byte) (this.value) };

            }

            /// -------
            /// Classes

            /**
             * <p>Encapsulates a 'public' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Public extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Public() {
                    super(Modifier.ACC_PUBLIC);
                }

            }

            /**
             * <p>Encapsulates a 'private' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Private extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Private() {
                    super(Modifier.ACC_PRIVATE);
                }

            }

            /**
             * <p>Encapsulates a 'protected' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Protected extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Protected() {
                    super(Modifier.ACC_PROTECTED);
                }

            }

            /**
             * <p>Encapsulates a 'static' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Static extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Static() {
                    super(Modifier.ACC_STATIC);
                }

            }

            /**
             * <p>Encapsulates a 'final' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Final extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Final() {
                    super(Modifier.ACC_FINAL);
                }

            }

            /**
             * <p>Encapsulates a 'volatile' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Volatile extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Volatile() {
                    super(Modifier.ACC_VOLATILE);
                }

            }

            /**
             * <p>Encapsulates a 'transient' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Transient extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Transient() {
                    super(Modifier.ACC_TRANSIENT);
                }

            }

            /**
             * <p>Encapsulates a 'synthetic' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Synthetic extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Synthetic() {
                    super(Modifier.ACC_SYNTHETIC);
                }

            }

            /**
             * <p>Encapsulates a 'enum' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Enum extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Enum() {
                    super(Modifier.ACC_ENUM);
                }

            }

        }

    }

    /**
     * <p>Represents a method corresponding to the current class. Provides convenience methods to create the appropriate
     * {@link ClassFile} entries.</p>
     * @see ClassFile
     * @see MethodInfo
     * @see ConstantMethodRef
     * @author Carlos L. Cuenca
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class Method {

        /// --------------
        /// Private Fields

        /**
         * <p>The set of {@link Modifier}s corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final List<Modifier>    modifiers   ;

        /**
         * <p>The set of {@link Attribute}s corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final List<Attribute>   attributes  ;

        /**
         * <p>The {@link String} value corresponding to the {@link Field}.</p>
         * @since 0.1.0
         */
        private final String            name        ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link Method} to its default state with the specified {@link Modifier}s or
         * {@link Attribute} instances.</p>
         * @param name The {@link String} value of the name corresponding to the {@link Method}.
         * @param modifiersAndAttributes Variadic parameters specifying any {@link Modifier} or {@link Attribute}
         *                               instances corresponding to the {@link Method}.
         * @since 0.1.0
         */
        public Method(final String name, final Object... modifiersAndAttributes) {

            this.name       = (name != null) ? name : ""    ;
            this.modifiers  = new ArrayList<>()             ;
            this.attributes = new ArrayList<>()             ;

            if(modifiersAndAttributes != null) for(final Object object: modifiersAndAttributes) {

                if(object instanceof Modifier)
                    this.modifiers.add((Modifier) object);

                else if(object instanceof Attribute)
                    this.attributes.add((Attribute) object);

            }

        }

        /// -------
        /// Classes

        /**
         * <p>Encapsulates the possible values of access modifiers that may be specified to a {@link Method}.</p>
         * <p>The value of the access_flags item is a mask of flags used to denote access permission to and properties
         * of this method.</p>
         * <p>The value 0x0800 is interpreted as the ACC_STRICT flag only in a class file whose major version number
         * is at least 46 and at most 60. For methods in such a class file, the rules below determine whether the
         * ACC_STRICT flag may be set in combination with other flags. (Setting the ACC_STRICT flag constrained a
         * method's floating-point instructions in Java SE 1.2 through 16 (§2.8).) For methods in a class file whose
         * major version number is less than 46 or greater than 60, the value 0x0800 is not interpreted as the
         * ACC_STRICT flag, but rather is unassigned; it is not meaningful to "set the ACC_STRICT flag" in such a
         * class file.
         * Methods of classes may have any of the flags in Table 4.6-A set. However, each method of a class may have
         * at most one of its ACC_PUBLIC, ACC_PRIVATE, and ACC_PROTECTED flags set (JLS §8.4.3).
         * Methods of interfaces may have any of the flags in Table 4.6-A set except ACC_PROTECTED, ACC_FINAL,
         * ACC_SYNCHRONIZED, and ACC_NATIVE (JLS §9.4). In a class file whose version number is less than 52.0,
         * each method of an interface must have its ACC_PUBLIC and ACC_ABSTRACT flags set; in a class file whose
         * version number is 52.0 or above, each method of an interface must have exactly one of its ACC_PUBLIC and
         * ACC_PRIVATE flags set.
         * If a method of a class or interface has its ACC_ABSTRACT flag set, it must not have any of its ACC_PRIVATE,
         * ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, or ACC_NATIVE flags set, nor (in a class file whose major version
         * number is at least 46 and at most 60) have its ACC_STRICT flag set.
         * An instance initialization method (§2.9.1) may have at most one of its ACC_PUBLIC, ACC_PRIVATE, and
         * ACC_PROTECTED flags set, and may also have its ACC_VARARGS and ACC_SYNTHETIC flags set, and may also
         * (in a class file whose major version number is at least 46 and at most 60) have its ACC_STRICT flag set,
         * but must not have any of the other flags in Table 4.6-A set.
         * In a class file whose version number is 51.0 or above, a method whose name is <clinit> must have its
         * ACC_STATIC flag set.
         * A class or interface initialization method (§2.9.2) is called implicitly by the Java Virtual Machine.
         * The value of its access_flags item is ignored except for the setting of the ACC_STATIC flag and (in a class
         * file whose major version number is at least 46 and at most 60) the ACC_STRICT flag, and the method is exempt
         * from the preceding rules about legal combinations of flags.
         * The ACC_BRIDGE flag is used to indicate a bridge method generated by a compiler for the Java programming
         * language.
         * The ACC_VARARGS flag indicates that this method takes a variable number of arguments at the source code
         * level. A method declared to take a variable number of arguments must be compiled with the ACC_VARARGS flag
         * set to 1. All other methods must be compiled with the ACC_VARARGS flag set to 0.
         * The ACC_SYNTHETIC flag indicates that this method was generated by a compiler and does not appear in
         * source code, unless it is one of the methods named in §4.7.8.
         * All bits of the access_flags item not assigned in Table 4.6-A are reserved for future use.
         * (This includes the bit corresponding to 0x0800 in a class file whose major version number is less than 46
         * or greater than 60.) They should be set to zero in generated class files and should be ignored by Java
         * Virtual Machine implementations.</p>
         * @see Field
         * @author Carlos L. Cuenca
         * @since 0.1.0
         * @version 1.0.0
         */
        public static class Modifier {

            /**
             * <p>Declared public; may be accessed from outside its package.</p>
             * @since 0.1.0
             */
            private final static short ACC_PUBLIC	    = 0x0001    ;

            /**
             * <p>Declared private; accessible only within the defining class and other classes belonging to the same
             * nest (§5.4.4).</p>
             * @since 0.1.0
             */
            private final static short ACC_PRIVATE      = 0x0002    ;

            /**
             * <p>Declared protected; may be accessed within subclasses.</p>
             * @since 0.1.0
             */
            private final static short ACC_PROTECTED    = 0x0004    ;

            /**
             * <p>Declared static.</p>
             * @since 0.1.0
             */
            private final static short ACC_STATIC       = 0x0008    ;

            /**
             * <p>Declared final; must not be overridden (§5.4.5).</p>
             * @since 0.1.0
             */
            private final static short ACC_FINAL        = 0x0010    ;

            /**
             * <p>Declared synchronized; invocation is wrapped by a monitor use.</p>
             * @since 0.1.0
             */
            private final static short ACC_SYNCHRONIZED = 0x0020    ;

            /**
             * <p>A bridge method, generated by the compiler.</p>
             * @since 0.1.0
             */
            private final static short ACC_BRIDGE       = 0x0040    ;

            /**
             * <p>Declared with variable number of arguments.</p>
             * @since 0.1.0
             */
            private final static short ACC_VARARGS      = 0x0080    ;

            /**
             * <p>Declared native; implemented in a language other than the Java programming language.</p>
             * @since 0.1.0
             */
            private final static short ACC_NATIVE       = 0x0100    ;

            /**
             * <p>Declared abstract; no implementation is provided.</p>
             * @since 0.1.0
             */
            private final static short ACC_ABSTRACT     = 0x0400    ;

            /**
             * <p>In a class file whose major version number is at least 46 and at most 60: Declared strictfp.</p>
             * @since 0.1.0
             */
            private final static short ACC_STRICT       = 0x0800    ;

            /**
             * <p>Declared synthetic; not present in the source code.</p>
             * @since 0.1.0
             */
            private final static short ACC_SYNTHETIC    = 0x4000    ;

            /// --------------
            /// Private Fields

            /**
             * <p>The short value corresponding to the {@link Modifier}.</p>
             * @since 0.1.0
             */
            private final short value;

            /// ------------
            /// Constructors

            /**
             * <p>Initializes the {@link Modifier} to its' default state with the specified short value.</p>
             * @param value the short value that specifies which bit to enable in the final field encoding.
             * @since 0.1.0
             */
            private Modifier(final short value) {

                this.value = value;

            }

            /// --------------
            /// Public Methods

            /**
             * <p>Returns a combined {@link Modifier} with the OR result of the specified {@link Modifier}s.</p>
             * @param thoseModifiers The {@link Modifier}s whose value to OR
             * @return combined {@link Modifier}.
             * @since 0.1.0
             */
            public final Modifier combinedWith(final Modifier... thoseModifiers) {

                // Initialize the result
                short result = this.value;

                // Iterate through the modifiers
                if(thoseModifiers != null) for(final Modifier modifier: thoseModifiers) result |= modifier.value;

                // Return the combined Modifier
                return new Modifier(result);

            }

            /**
             * <p>Returns the byte array of the {@link Modifier}'s contents.</p>
             * @return The byte array of the {@link Modifier}'s contents.
             * @since 0.1.0
             */
            public final byte[] getBytes() {

                return new byte[] { (byte) (this.value >> 8), (byte) (this.value) };

            }

            /// -------
            /// Classes

            /**
             * <p>Encapsulates a 'public' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Public extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Public() {
                    super(Modifier.ACC_PUBLIC);
                }

            }

            /**
             * <p>Encapsulates a 'private' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Private extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Private() {
                    super(Modifier.ACC_PRIVATE);
                }

            }

            /**
             * <p>Encapsulates a 'protected' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Protected extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Protected() {
                    super(Modifier.ACC_PROTECTED);
                }

            }

            /**
             * <p>Encapsulates a 'static' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Static extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Static() {
                    super(Modifier.ACC_STATIC);
                }

            }

            /**
             * <p>Encapsulates a 'final' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Final extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Final() {
                    super(Modifier.ACC_FINAL);
                }

            }

            /**
             * <p>Encapsulates a 'volatile' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Synchronized extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Synchronized() {
                    super(Modifier.ACC_SYNCHRONIZED);
                }

            }

            /**
             * <p>Encapsulates a 'bridge' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Bridge extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Bridge() {
                    super(Modifier.ACC_BRIDGE);
                }

            }

            /**
             * <p>Encapsulates a 'varargs' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Varargs extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Varargs() {
                    super(Modifier.ACC_VARARGS);
                }

            }

            /**
             * <p>Encapsulates a 'native' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Native extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Native() {
                    super(Modifier.ACC_NATIVE);
                }

            }

            /**
             * <p>Encapsulates a 'abstract' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Abstract extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Abstract() {
                    super(Modifier.ACC_ABSTRACT);
                }

            }

            /**
             * <p>Encapsulates a 'strictfp' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Strict extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Strict() {
                    super(Modifier.ACC_STRICT);
                }

            }

            /**
             * <p>Encapsulates a 'synthetic' {@link Modifier}</p>
             * @see Modifier
             * @author Carlos L. Cuenca
             * @since 0.1.0
             * @version 1.0.0
             */
            public static class Synthetic extends Modifier {

                /// ------------
                /// Constructors

                /**
                 * <p>Initializes the {@link Modifier} to its' default state.</p>
                 * @since 0.1.0
                 */
                public Synthetic() {
                    super(Modifier.ACC_SYNTHETIC);
                }

            }

        }

    }

    protected interface Info  {

        byte[] toByteArray();

    }

    protected interface AttributeInfo extends Info {

    }

    /**
     * <p>Java Virtual Machine instructions do not rely on the run-time layout of classes, interfaces, class instances,
     * or arrays. Instead, instructions refer to symbolic information in the constant_pool table.</p>
     * <p>Each entry in the constant_pool table must begin with a 1-byte tag indicating the kind of constant
     * denoted by the entry. There are 17 kinds of constant, listed in Table 4.4-A with their corresponding tags,
     * and ordered by their section number in this chapter. Each tag byte must be followed by two or more bytes
     * giving information about the specific constant. The format of the additional information depends on the tag
     * byte, that is, the content of the info array varies with the value of tag.</p>
     * <p>Some entries in the constant_pool table are loadable because they represent entities that can be pushed onto
     * the stack at run time to enable further computation. In a class file whose version number is v, an entry in the
     * constant_pool table is loadable if it has a tag that was first deemed to be loadable in version v or earlier of
     * the class file format. Table 4.4-C lists each tag with the first version of the class file format in which it
     * was deemed to be loadable. Also shown is the version of the Java SE Platform which introduced that version of
     * the class file format.
     * In every case except CONSTANT_Class, a tag was first deemed to be loadable in the same version of the class
     * file format that first defined the tag.</p>
     * <p>Each entry in the constant_pool table must begin with a 1-byte tag indicating the kind of constant denoted
     * by the entry. There are 17 kinds of constant, listed in Table 4.4-A with their corresponding tags, and ordered
     * by their section number in this chapter. Each tag byte must be followed by two or more bytes giving information
     * about the specific constant. The format of the additional information depends on the tag byte, that is, the
     * content of the info array varies with the value of tag.</p>
     * <p>In a class file whose version number is v, each entry in the constant_pool table must have a tag that was
     * first defined in version v or earlier of the class file format (§4.1). That is, each entry must denote a kind
     * of constant that is approved for use in the class file. Table 4.4-B lists each tag with the first version of
     * the class file format in which it was defined. Also shown is the version of the Java SE Platform which introduced
     * that version of the class file format.</p>
     * TODO: Write Tests that check for length of info[] since each subclass varies the length of info[]
     */
    protected interface ConstantPoolInfo extends Info {

    }

    protected interface LoadableConstant { /* Empty */ }

    public static class FieldInfo implements Info {

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the access_flags item is a mask of flags used to denote access permission to and properties
         * of the field. The interpretation of each flag, when set, is specified in Table 4.5-A.</p>
         * @since 0.1.0
         */
        private final short                 accessFlags     ;

        /**
         * <p>The value of the name_index item must be a valid index into the {@link ClassFile#constantPool} table.
         * The constant pool entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) which represents a
         * valid unqualified name denoting a field (§4.2.2).</p>
         * @since 0.1.0
         */
        private final short                 nameIndex       ;

        /**
         * <p>The value of the descriptor_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) which represents a
         * valid field descriptor (§4.3.2).</p>
         * @since 0.1.0
         */
        private final short                 descriptorIndex ;

        /**
         * <p>Each value of the attributes table must be an {@link AttributeInfo} structure (§4.7).
         * A field can have any number of optional attributes associated with it.
         * The attributes defined by this specification as appearing in the attributes table of a {@link FieldInfo}
         * structure are listed in Table 4.7-C.
         * The rules concerning attributes defined to appear in the attributes table of a {@link FieldInfo} structure
         * are given in §4.7.</p>
         * @since 0.1.0
         */
        private final List<AttributeInfo>   attributesInfo  ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link FieldInfo} to its' default state with the specified access flags, name index, &
         * descriptor index.</p>
         * @param accessFlags The short value of the access flags that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @param nameIndex The short value of the name index that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @param descriptorIndex The short value of the descriptor index that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @since 0.1.0
         */
        protected FieldInfo(final short accessFlags, final short nameIndex, final short descriptorIndex) {

            this.accessFlags        = accessFlags       ;
            this.nameIndex          = nameIndex         ;
            this.descriptorIndex    = descriptorIndex   ;
            this.attributesInfo = new ArrayList<>() ;

        }

        /// ----
        /// Info

        @Override
        public final byte[] toByteArray() {

            // TODO
            return new byte[0];

        }

    }

    public static class MethodInfo implements Info {

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the access_flags item is a mask of flags used to denote access permission to and properties
         * of this method. </p>
         * @since 0.1.0
         */
        private final short                 accessFlags     ;

        /**
         * <p>The value of the name_index item must be a valid index into the constant_pool table. The constant_pool
         * entry at that index must be a {@link ConstantUtf8} structure (§4.4.7) representing either a valid
         * unqualified name denoting a method (§4.2.2), or (if this method is in a class rather than an interface)
         * the special method name <init>, or the special method name <clinit>.</p>
         * @since 0.1.0
         */
        private final short                 nameIndex       ;

        /**
         * <p>The value of the descriptor_index item must be a valid index into the constant_pool table. The
         * constant_pool entry at that index must be a CONSTANT_Utf8_info structure representing a valid method
         * descriptor (§4.3.3). Furthermore:
         *      If this method is in a class rather than an interface, and the name of the method is <init>,
         *      then the descriptor must denote a void method.
         *      If the name of the method is <clinit>, then the descriptor must denote a void method, and, in a class
         *      file whose version number is 51.0 or above, a method that takes no arguments.
         * A future edition of this specification may require that the last parameter descriptor of the method
         * descriptor is an array type if the {@link Method.Modifier.Varargs} flag is set in the access_flags item.</p>
         * @since 0.1.0
         */
        private final short                 descriptorIndex ;

        /**
         * <p>Each value of the attributes table must be an attribute_info structure (§4.7).
         * A method can have any number of optional attributes associated with it.
         * The attributes defined by this specification as appearing in the attributes table of a {@link MethodInfo}
         * structure are listed in Table 4.7-C.
         * The rules concerning attributes defined to appear in the attributes table of a method_info structure are
         * given in §4.7.
         * The rules concerning non-predefined attributes in the attributes table of a method_info structure are given
         * in §4.7.1.</p>
         * @since 0.1.0
         */
        private final List<AttributeInfo>   attributesInfo  ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link MethodInfo} to its' default state with the specified access flags, name index, &
         * descriptor index.</p>
         * @param accessFlags The short value of the access flags that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @param nameIndex The short value of the name index that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @param descriptorIndex The short value of the descriptor index that correspond to the {@link Field} the
         *      {@link FieldInfo} represents.
         * @since 0.1.0
         */
        protected MethodInfo(final short accessFlags, final short nameIndex, final short descriptorIndex) {

            this.accessFlags        = accessFlags       ;
            this.nameIndex          = nameIndex         ;
            this.descriptorIndex    = descriptorIndex   ;
            this.attributesInfo = new ArrayList<>() ;

        }

        /// ----
        /// Info

        @Override
        public final byte[] toByteArray() {

            // TODO
            return new byte[0];

        }


    }

    /**
     * <p>The Code attribute is a variable-length attribute in the attributes table of a {@link MethodInfo} structure
     * (§4.6). A Code attribute contains the Java Virtual Machine instructions and auxiliary information for a method,
     * including an instance initialization method and a class or interface initialization method (§2.9.1, §2.9.2).
     * If the method is either native or abstract, and is not a class or interface initialization method, then its
     * {@link MethodInfo} structure must not have a Code attribute in its attributes table. Otherwise, its
     * {@link MethodInfo} structure must have exactly one Code attribute in its attributes table.</p>
     * @author Carlos L. Cuenca
     * @see Method
     * @see AttributeInfo
     * @since 0.1.0
     * @version 1.0.0
     */
    public static class CodeInfo implements AttributeInfo {

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the attribute_name_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a {@link ConstantUtf8} structure (§4.4.7) representing the
         * string 'Code'.</p>
         */
        private final short                 attributeNameIndex      ;

        /**
         * <p>The value of the attribute_length item indicates the length of the attribute, excluding the initial six
         * bytes.</p>
         * @since 0.1.0
         */
        private final int                   attributeLength         ;

        /**
         * <p>The value of the max_stack item gives the maximum depth of the operand stack of this method (§2.6.2) at
         * any point during execution of the method.</p>
         * @since 0.1.0
         */
        private final short                 maxStack                ;

        /**
         * <p>The value of the max_locals item gives the number of local variables in the local variable array
         * allocated upon invocation of this method (§2.6.1), including the local variables used to pass parameters to
         * the method on its invocation.
         * The greatest local variable index for a value of type long or double is max_locals - 2. The greatest local
         * variable index for a value of any other type is max_locals - 1.</p>
         * @since 0.1.0
         */
        private final short                 maxLocals               ;

        /**
         * <p>The value of the code_length item gives the number of bytes in the code array for this method.
         * The value of code_length must be greater than zero (as the code array must not be empty) and less than
         * 65536.</p>
         * @since 0.1.0
         */
        private final int                   codeLength              ;

        /**
         * <p>The code array gives the actual bytes of Java Virtual Machine code that implement the method.
         * When the code array is read into memory on a byte-addressable machine, if the first byte of the array is
         * aligned on a 4-byte boundary, the tableswitch and lookupswitch 32-bit offsets will be 4-byte aligned.
         * (Refer to the descriptions of those instructions for more information on the consequences of code array
         * alignment.)
         * The detailed constraints on the contents of the code array are extensive and are given in a separate
         * section (§4.9).</p>
         * @since 0.1.0
         */
        private final byte[]                code                    ;

        /**
         * <p>Each value of the attributes table must be an {@link AttributeInfo} structure (§4.7).
         * A {@link Code} can have any number of optional attributes associated with it.
         * The attributes defined by this specification as appearing in the attributes table of a {@link Code}
         * structure are listed in Table 4.7-C.
         * The rules concerning attributes defined to appear in the attributes table of a {@link Code} attribute are
         * given in §4.7.
         * The rules concerning non-predefined attributes in the attributes table of a {@link Code} attribute are given
         * in §4.7.1.</p>
         * @since 0.1.0
         */
        private final List<AttributeInfo>   attributesInfo          ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link CodeInfo} to its' default state with the specified attribute name index,
         * attribute length (minus 6 initial bytes), max stack size, max local size, the code length, & the code
         * contents.</p>
         * @param attributeNameIndex The index into the current class's constant pool that contains the string 'Code'
         * @param attributeLength The length of the {@link CodeInfo} not including the 6 initial bytes.
         * @param maxStack The maximum Operand Stack
         * @param maxLocals The maximum amount of local variables including any method parameters
         * @param codeLength The length denoting the amount of bytes in the implementation
         * @param code The actual byte code implementation
         * @since 0.1.0
         */
        public CodeInfo(final short attributeNameIndex, final int attributeLength,
                        final short maxStack, final short maxLocals,
                        final short codeLength, final byte[] code) {

            this.attributeNameIndex     = attributeNameIndex    ;
            this.attributeLength        = attributeLength       ;
            this.maxStack               = maxStack              ;
            this.maxLocals              = maxLocals             ;
            this.codeLength             = codeLength            ;
            this.code                   = code                  ;
            // TODO: Make sure to insert exception_table_length = 0x00 in the generated byte code.
            this.attributesInfo         = new ArrayList<>()     ;

        }

        @Override
        public byte[] toByteArray() {
            return new byte[0];
        }
    }

    protected static class ConstantUtf8 implements ConstantPoolInfo {

        /// -----------------
        /// Private Constants

        /**
         * <p>The byte value that denotes the {@link ConstantUtf8} in the {@link ClassFile}'s constant pool.</p>
         * @since 0.1.0
         */
        private final static byte   Tag     = 0x0001    ;

        /**
         * <p>The minimum {@link ClassFile} format that supports the {@link ConstantUtf8}.</p>
         * @since 0.1.0
         */
        private final static String Format  = "45.3"    ;

        /**
         * <p>The minimum Java SE version that supports the {@link ConstantUtf8}.</p>
         * @since 0.1.0
         */
        private final static String SE      = "1.0.2"   ;

        /// --------------
        /// Private Fields

        /**
         * <p>The byte value corresponding to the length encoded by the {@link ConstantUtf8}.</p>
         * @since 0.1.0
         */
        private final byte      length  ;

        /**
         * <p>The collection of byte content encoded by the {@link ConstantUtf8}.</p>
         * @since 0.1.0
         */
        private final byte[]    encoded ;

        /// ------------
        /// Constructors

        /**
         * <p>Initializes the {@link ConstantUtf8} to its' default state with the specified {@link String} value.</p>
         * @param string The {@link String} value to encode in Utf8.
         * @since 0.1.0
         */
        protected ConstantUtf8(final String string) {

            this.encoded    = string.getBytes(StandardCharsets.UTF_8)   ;
            this.length     = (byte) this.encoded.length                ;

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantUtf8}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantUtf8}.
         */
        @Override
        public final byte[] toByteArray() {

            // Initialize a new byte array with the length of the encoded string plus space for the tag & length
            final byte[] bytes = new byte[this.length + 2];

            // Set the Tag & length
            bytes[0] = Tag          ;
            bytes[1] = this.length  ;

            // Copy over the string contents
            System.arraycopy(this.encoded, 0, bytes, 2, this.encoded.length);

            // Return the result
            return bytes;

        }

    }

    protected static class ConstantInteger implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0003;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantInteger}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantInteger}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantFloat implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0004;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantFloat}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantFloat}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantLong implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0005;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantLong}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantLong}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantDouble implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0006;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantDouble}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantDouble}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantString implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0008;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantString}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantString}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    /**
     * <p>The CONSTANT_Class_info structure is used to represent a class or an interface.</p>
     * <p>Because arrays are objects, the opcodes anewarray and multianewarray - but not the opcode new -
     * can reference array "classes" via CONSTANT_Class_info structures in the constant_pool table. For such array
     * classes, the name of the class is the descriptor of the array type (§4.3.2).
     * For example, the class name representing the two-dimensional array type int[][] is
     * [[I, while the class name representing the type Thread[] is [Ljava/lang/Thread;.
     * An array type descriptor is valid only if it represents 255 or fewer dimensions.</p>
     */
    protected static class ConstantClass implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0007;
        protected final static String Format = "49.0";
        protected final static String SE = "5.0";

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the name_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) representing a
         * valid binary class or interface name encoded in internal form (§4.2.1).</p>
         * @since 0.1.0
         */
        private final short nameIndex;

        /// ------------
        /// Constructors

        protected ConstantClass(final short nameIndex) {

            this.nameIndex = nameIndex;

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantClass}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantClass}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    /**
     * <p>The CONSTANT_NameAndType_info structure is used to represent a field or method, without indicating which
     * class or interface type it belongs to.</p>
     */
    protected static class ConstantNameAndType implements ConstantPoolInfo {

        protected final static byte Tag         = 0x000C    ;
        protected final static String Format    = "45.3"    ;
        protected final static String SE        = "1.0.2"   ;

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the name_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) representing
         * either a valid unqualified name denoting a field or method (§4.2.2), or the special method
         * name <init> (§2.9.1).</p>
         * @since 0.1.0
         */
        private final short nameIndex       ;

        /**
         * <p>The value of the descriptor_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Utf8_info structure (§4.4.7) representing a
         * valid field descriptor or method descriptor (§4.3.2, §4.3.3).</p>
         */
        private final short descriptorIndex ;

        /// ------------
        /// Constructors

        protected ConstantNameAndType(final short nameIndex, final short descriptorIndex) {

            this.nameIndex          = nameIndex         ;
            this.descriptorIndex    = descriptorIndex   ;

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantNameAndType}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantNameAndType}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    /**
     * <p>Represents a Reference to a class's field.</p>
     */
    protected static class ConstantFieldRef implements ConstantPoolInfo {

        protected final static byte Tag = 0x0009;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the class_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Class_info structure (§4.4.1) representing a
         * class or interface type that has the field or method as a member.</p>
         * <p>May be either a class type or an interface type.</p>
         * @since 0.1.0
         */
        private final short classIndex          ;

        /**
         * <p>The value of the name_and_type_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_NameAndType_info structure (§4.4.6).
         * This constant_pool entry indicates the name and descriptor of the field or method.</p>
         * <p>The indicated descriptor must be a field descriptor (§4.3.2)</p>
         * @since 0.1.0
         */
        private final short nameAndTypeindex    ;

        /// ------------
        /// Constructors

        protected ConstantFieldRef(final short classIndex, final short nameAndTypeIndex) {

            this.classIndex         = classIndex        ;
            this.nameAndTypeindex   = nameAndTypeIndex  ;

        }

        protected final byte[] classIndexBytes() {

            return new byte[] { (byte) (this.classIndex >> 8), (byte) this.classIndex };

        }

        protected final byte[] nameAndTypeIndexBytes() {

            return new byte[] { (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantFieldRef}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantFieldRef}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag,
                    (byte) (this.classIndex >> 8), (byte) this.classIndex,
                    (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

    }

    /**
     * <p>Represents a Reference to a class's method.</p>
     */
    protected static class ConstantMethodRef implements ConstantPoolInfo {

        protected final static byte Tag = 0x0A;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the class_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Class_info structure (§4.4.1) representing a
         * class or interface type that has the field or method as a member.</p>
         * <p>Should be a class type, not an interface type.</p>
         * @since 0.1.0
         */
        private final short classIndex          ;

        /**
         * <p>The value of the name_and_type_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_NameAndType_info structure (§4.4.6).
         * This constant_pool entry indicates the name and descriptor of the field or method.</p>
         * <p>the indicated descriptor must be a method descriptor (§4.3.3).</p>
         * <p>If the name of the method in a ConstantMethodRef structure begins with a '<' ('\u003c'),
         * then the name must be the special name <init>, representing an instance initialization method (§2.9.1).
         * The return type of such a method must be void.</p>
         * @since 0.1.0
         */
        private final short nameAndTypeindex    ;

        /// ------------
        /// Constructors

        protected ConstantMethodRef(final short classIndex, final short nameAndTypeIndex) {

            this.classIndex         = classIndex        ;
            this.nameAndTypeindex   = nameAndTypeIndex  ;

        }

        protected final byte[] classIndexBytes() {

            return new byte[] { (byte) (this.classIndex >> 8), (byte) this.classIndex };

        }

        protected final byte[] nameAndTypeIndexBytes() {

            return new byte[] { (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantMethodRef}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantMethodRef}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag,
                    (byte) (this.classIndex >> 8), (byte) this.classIndex,
                    (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

    }

    /**
     * <p>Represents a Reference to a interface's method.</p>
     */
    protected static class ConstantInterfaceMethodRef implements ConstantPoolInfo {

        protected final static byte Tag = 0x0B;
        protected final static String Format = "45.3";
        protected final static String SE = "1.0.2";

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the class_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_Class_info structure (§4.4.1) representing a
         * class or interface type that has the field or method as a member.</p>
         * <p>Should be an interface type, not a class type.</p>
         * @since 0.1.0
         */
        private final short classIndex          ;

        /**
         * <p>The value of the name_and_type_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be a CONSTANT_NameAndType_info structure (§4.4.6).
         * This constant_pool entry indicates the name and descriptor of the field or method.</p>
         * <p>the indicated descriptor must be a method descriptor (§4.3.3).</p>
         * @since 0.1.0
         */
        private final short nameAndTypeindex    ;

        /// ------------
        /// Constructors

        protected ConstantInterfaceMethodRef(final short classIndex, final short nameAndTypeIndex) {

            this.classIndex         = classIndex        ;
            this.nameAndTypeindex   = nameAndTypeIndex  ;

        }

        protected final byte[] classIndexBytes() {

            return new byte[] { (byte) (this.classIndex >> 8), (byte) this.classIndex };

        }

        protected final byte[] nameAndTypeIndexBytes() {

            return new byte[] { (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantInterfaceMethodRef}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantInterfaceMethodRef}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag,
                    (byte) (this.classIndex >> 8), (byte) this.classIndex,
                    (byte) (this.nameAndTypeindex >> 8), (byte) this.nameAndTypeindex };

        }

    }

    protected static class MethodHandle implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x000F;
        protected final static String Format = "51.0";
        protected final static String SE = "7";

        /// --------------
        /// Private Fields

        /**
         * <p>The value of the reference_kind item must be in the range 1 to 9. The value denotes the kind of
         * this method handle, which characterizes its bytecode behavior (§5.4.3.5).</p>
         * @since 0.1.0
         */
        private final short referenceKind       ;

        /**
         * <p>The value of the reference_index item must be a valid index into the constant_pool table.
         * The constant_pool entry at that index must be as follows:</p>
         * If the value of the reference_kind item is 1 (REF_getField), 2 (REF_getStatic), 3 (REF_putField),
         * or 4 (REF_putStatic):
         *      then the constant_pool entry at that index must be a {@link ConstantFieldRef} structure (§4.4.2)
         *      representing a field for which a method handle is to be created.
         * If the value of the reference_kind item is 5 (REF_invokeVirtual) or 8 (REF_newInvokeSpecial):
         *      then the constant_pool entry at that index must be a {@link ConstantMethodRef} structure (§4.4.2)
         *      representing a class's method or constructor (§2.9.1) for which a method handle is to be created.
         * If the value of the reference_kind item is 6 (REF_invokeStatic) or 7 (REF_invokeSpecial):
         *      then if the class file version number is less than 52.0, the constant_pool entry at that index
         *      must be a {@link ConstantMethodRef} structure representing a class's method for which a method handle
         *      is to be created; if the class file version number is 52.0 or above, the constant_pool entry at that
         *      index must be either a {@link ConstantMethodRef} structure or a {@link ConstantInterfaceMethodRef}
         *      structure (§4.4.2) representing a class's or interface's method for which a method handle is to be created.
         * If the value of the reference_kind item is 9 (REF_invokeInterface), then the constant_pool entry at that
         * index must be a {@link ConstantInterfaceMethodRef} structure representing an interface's method for which
         * a method handle is to be created.
         * If the value of the reference_kind item is 5 (REF_invokeVirtual), 6 (REF_invokeStatic),
         * 7 (REF_invokeSpecial), or 9 (REF_invokeInterface), the name of the method represented by a
         * {@link ConstantMethodRef} structure or a {@link ConstantInterfaceMethodRef} structure must not be
         * <init> or <clinit>.
         * If the value is 8 (REF_newInvokeSpecial), the name of the method represented by a {@link ConstantMethodRef}
         * structure must be <init>.</p>
         * @since 0.1.0
         */
        private final short referenceIndex;

        /// ------------
        /// Constructors

        protected MethodHandle(final short referenceKind, final short referenceIndex) {

            this.referenceKind  = referenceKind   ;
            this.referenceIndex = referenceIndex  ;

        }

        protected final byte[] classIndexBytes() {

            return new byte[] { (byte) (this.referenceKind >> 8), (byte) this.referenceKind};

        }

        protected final byte[] nameAndTypeIndexBytes() {

            return new byte[] { (byte) (this.referenceIndex >> 8), (byte) this.referenceIndex};

        }

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantInterfaceMethodRef}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantInterfaceMethodRef}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag,
                    (byte) (this.referenceKind >> 8), (byte) this.referenceKind,
                    (byte) (this.referenceIndex >> 8), (byte) this.referenceIndex};

        }

    }

    protected static class ConstantMethodType implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0010;
        protected final static String Format = "51.0";
        protected final static String SE = "7";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantMethodType}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantMethodType}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantDynamic implements ConstantPoolInfo, LoadableConstant {

        protected final static byte Tag = 0x0011;
        protected final static String Format = "55.0";
        protected final static String SE = "11";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantDynamic}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantDynamic}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantInvokeDynamic implements ConstantPoolInfo {

        protected final static byte Tag = 0x0012;
        protected final static String Format = "51.0";
        protected final static String SE = "7";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantInvokeDynamic}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantInvokeDynamic}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantModule implements ConstantPoolInfo {

        protected final static byte Tag = 0x0013;
        protected final static String Format = "53.0";
        protected final static String SE = "9";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantModule}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantModule}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

    protected static class ConstantPackage implements ConstantPoolInfo {

        protected final static byte Tag = 0x0014;
        protected final static String Format = "53.0";
        protected final static String SE = "9";

        /**
         * <p>Returns the byte array representation corresponding to the {@link ConstantPackage}.</p>
         * @return Returns the byte array representation corresponding to the {@link ConstantPackage}.
         */
        @Override
        public final byte[] toByteArray() {

            return new byte[] { Tag };

        }

    }

}
