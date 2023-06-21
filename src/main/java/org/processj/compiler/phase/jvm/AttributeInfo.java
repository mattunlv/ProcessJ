package org.processj.compiler.phase.jvm;

/**
 * <p>Attributes are used in the ClassFile, field_info, method_info, Code_attribute, and record_component_info
 * structures of the class file format (§4.1, §4.5, §4.6, §4.7.3, §4.7.30).</p>
 * <p>For all attributes, the attribute_name_index item must be a valid unsigned 16-bit index into the constant pool
 * of the class. The constant_pool entry at attribute_name_index must be a CONSTANT_Utf8_info structure (§4.4.7)
 * representing the name of the attribute. The value of the attribute_length item indicates the length of the
 * subsequent information in bytes. The length does not include the initial six bytes that contain the
 * attribute_name_index and attribute_length items.
 * 30 attributes are predefined by this specification. They are listed three times, for ease of navigation:
 * Table 4.7-A is ordered by the attributes' section numbers in this chapter. Each attribute is shown with the
 * first version of the class file format in which it was defined. Also shown is the version of the Java SE Platform
 * which introduced that version of the class file format (§4.1).
 * Table 4.7-B is ordered by the first version of the class file format in which each attribute was defined.
 * Table 4.7-C is ordered by the location in a class file where each attribute is defined to appear.
 * Within the context of their use in this specification, that is, in the attributes tables of the class file
 * structures in which they appear, the names of these predefined attributes are reserved.
 * Any conditions on the presence of a predefined attribute in an attributes table are specified explicitly in the
 * section which describes the attribute. If no conditions are specified, then the attribute may appear any number
 * of times in an attributes table.</p>
 * <p>Compilers are permitted to define and emit class files containing new attributes in the attributes tables of
 * class file structures, field_info structures, method_info structures, and Code attributes (§4.7.3).
 * Java Virtual Machine implementations are permitted to recognize and use new attributes found in these attributes
 * tables. However, any attribute not defined as part of this specification must not affect the semantics of the class
 * file. Java Virtual Machine implementations are required to silently ignore attributes they do not recognize.
 * For instance, defining a new attribute to support vendor-specific debugging is permitted. Because Java Virtual
 * Machine implementations are required to ignore attributes they do not recognize, class files intended for that
 * particular Java Virtual Machine implementation will be usable by other implementations even if those implementations
 * cannot make use of the additional debugging information that the class files contain.
 * Java Virtual Machine implementations are specifically prohibited from throwing an exception or otherwise refusing
 * to use class files simply because of the presence of some new attribute. Of course, tools operating on class files
 * may not run correctly if given class files that do not contain all the attributes they require.
 * Two attributes that are intended to be distinct, but that happen to use the same attribute name and are of the
 * same length, will conflict on implementations that recognize either attribute. Attributes defined other than in
 * this specification should have names chosen according to the package naming convention described in The Java
 * Language Specification, Java SE 20 Edition (JLS §6.1).
 * Future versions of this specification may define additional attributes.</p>
 * @author Carlos L. Cuenca
 * @version 1.0.0
 * @since 0.1.0
 */
public class AttributeInfo {
}
