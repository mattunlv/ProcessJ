package org.processj.compiler.phase.jvm;

/**
 *
 */
public class ConstantPool {


    /// -------
    /// Classes

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
     * TODO: Write Tests that check for length of info[] since each subclass varies the length of info[]
     * cp_info {
     *     u1 tag;
     *     u1 info[];
     * }
     */
    private static abstract class ConstantPoolInfo {

        abstract byte[] toByteArray();

    }

    protected interface LoadableConstant { /* Empty */ }

}
