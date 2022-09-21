package utilities;

// An interface that must be implemented by all 'enum' options
public interface EnumGet<S> {
    
    <E extends Enum<E>> E getValueOf(S name);
}
