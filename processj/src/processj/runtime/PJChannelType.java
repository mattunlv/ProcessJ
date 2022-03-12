package processj.runtime;

/**
 * The {@link PJChannelType} represents channel type constants
 * describing various forms of communication between {@link PJProcess}s.
 * A channel of type {@code ONE2ONE} specifies a one-to-one
 * channel object for use by one writer and one reader; type
 * {@code ONE2MANY} specifies a one-to-many channel object for
 * use by one writer and many readers; type {@code MANY2ONE}
 * specifies a many-to-one channel object for use by many
 * writers and one reader; type {@code MANY2MANY} specifies
 * a many-to-many channel object for use by many writers and
 * many readers.
 * 
 * @author Ben
 * @version 08/29/2018
 * @since 1.2
 */
public enum PJChannelType {
    
    ONE2ONE ("one-to-one PJChannel object for use by one writer and one reader"),
    
    ONE2MANY ("one-to-many PJChannel object for use by one writer and many readers"),
    
    MANY2ONE ("many-to-one PJChannel object for use by many writers and one reader"),
    
    MANY2MANY ("many-to-many PJChannel object for use by many writers and many readers");
    
    private final String text;
    
    PJChannelType(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
}
