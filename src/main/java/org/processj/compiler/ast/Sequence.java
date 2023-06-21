package org.processj.compiler.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processj.compiler.phase.Phase;
import org.processj.compiler.phase.Visitor;

public class Sequence<T extends AST> extends AST implements Iterable<T> {

    public ArrayList<T> children = new ArrayList<T>();

    public Sequence() {
        super();
    }

    public Sequence(T element) {
        super(element);
        children.add(element);
    }

    public T child(int i) {
        return children.get(i);
    }

    public Sequence<T> append(T element) {
        children.add(element);
        return this;
    }

    public <S extends T> Sequence<T> merge(Sequence<S> others) {
        for (T e : others)
            children.add(e);
        return this;
    }


    /**
     * <p>Constructs a {@link String} consisting of the {@link String} representation of each of the elements
     * delimited by the specified {@link String} separator.</p>
     * @param separator The {@link String} to separate the elements with except for the first & last element.
     * @return {@link String} consisting of the {@link String} representation of each of the elements
     * delimited by the specified {@link String} separator.
     * @since 0.1.0
     */
    public final String synthesizeStringWith(final String separator) {

        // Initialize the StringBuilder
        final StringBuilder stringBuilder = new StringBuilder();

        // Append each child with the specified separator
        if(this.children != null) for(int index = 0; (index < this.children.size()); index++)
            stringBuilder.append(this.children.get(index))
                    .append((index == (this.children.size() - 1)) ? "" : separator);

        // Return the result
        return stringBuilder.toString();

    }

    /**
     * <p>Returns the last element in the {@link Sequence}.</p>
     * @return Last element in the {@link Sequence}.
     * @since 0.1.0
     */
    public final T getLast() {

        return this.children.get(this.children.size() - 1);

    }

    /**
     * <p>Removes & returns the last element in the {@link Sequence}.</p>
     * @return The removed element in the {@link Sequence}
     * @since 0.1.0
     */
    public final T removeLast() {

        return this.children.remove(this.children.size() - 1);

    }

    public Iterator<T> iterator() {
        return children.iterator();
    }

    public void clear() {
        children.clear();
    }

    public <S extends T> Sequence<T> merge(S other) {
        children.add(other);
        return this;
    }

    public final boolean isEmpty() {

        return this.children.isEmpty();

    }

    public final boolean containsDuplicates() {

        // Initialize the set & result
        final Set<String> members = new HashSet<>();
        boolean doesContainDuplicates = false;

        for(final T element: this.children) {

            doesContainDuplicates = !members.add(element.toString());

            if(doesContainDuplicates) break;

        }

        return doesContainDuplicates;

    }

    public int size() {
        return children.size();
    }

    public void insert(int index, T e) {
        children.set(index, e);
    }

    public void insert(int index, final Sequence<T> sequence) {

        // If the index is not out of bound by more than one
        if(index >= this.children.size()) sequence.forEach(element -> this.children.add(element));

        else {

            // Initialize a suffix
            final ArrayList<T> suffix = new ArrayList<>();

            // Iterate through the specified Sequence
            for(int jindex = 0; jindex < sequence.size(); jindex++) {

                final int place = index + jindex;

                if(place < this.children.size()) {

                    // If we're not at the beginning, place the child in the suffix
                    if(jindex > 0) suffix.add(this.children.get(place));

                    // Overwrite the current child
                    this.children.set(place, sequence.child(jindex));

                // Otherwise, just aggregate
                } else this.children.add(sequence.child(jindex));

            }

            // Append the suffix
            this.children.addAll(suffix);

        }

    }

    public void accept(Visitor v) throws Phase.Error {
        v.visitSequence(this);
    }
}