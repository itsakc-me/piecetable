package io.itsakc.piecetable.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * DynamicList is a resizable array implementation that provides a flexible 
 * way to manage a collection of objects of type T. This class allows for 
 * dynamic resizing, enabling the list to grow and shrink as necessary 
 * while maintaining performance for common operations such as adding, 
 * removing, and accessing elements. It employs strategies for capacity 
 * management to optimize memory usage and performance.
 *
 * @param <T> the type of elements stored in this DynamicList
 */
public class DynamicList<T> implements Iterable<T> {
    private static final int DEFAULT_CAPACITY = 16;
    private final Class<?> clazz;
    private T[] elements;
    private int size;
    private boolean isAdjustable;
    
    /**
     * Constructs an empty list with an initial capacity of DEFAULT_CAPACITY.
     * The list will automatically adjust its capacity as needed.
     */
    public DynamicList() {
        this(Object.class, DEFAULT_CAPACITY, true);
    }

    /**
     * Constructs an empty list with an initial capacity of DEFAULT_CAPACITY.
     * The list will automatically adjust its capacity as needed.
     *
     * @param clazz the class of the elements in this list
     */
    public DynamicList(Class<?> clazz) {
        this(clazz, DEFAULT_CAPACITY, true);
    }
    
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @param isAdjustable indicates whether the list should automatically adjust its capacity
     */
    @SuppressWarnings("unchecked")
    public DynamicList(int initialCapacity, boolean isAdjustable) {
        this.clazz = Object.class;
        this.elements = (T[]) new Object[initialCapacity];
        this.size = 0;
        this.isAdjustable = isAdjustable;
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param clazz the class of the elements in this list
     * @param initialCapacity the initial capacity of the list
     * @param isAdjustable indicates whether the list should automatically adjust its capacity
     */
    @SuppressWarnings("unchecked")
    public DynamicList(Class<?> clazz, int initialCapacity, boolean isAdjustable) {
        this.clazz = clazz;
        this.elements = (T[]) Array.newInstance(clazz, initialCapacity);
        this.size = 0;
        this.isAdjustable = isAdjustable;
    }

    /**
     * Adds the specified element at the specified position in this list.
     *
     * @param index the index at which the specified element is to be inserted
     * @param element the element to be added
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public void add(int index, T element) {
        checkIndex(index, size + 1);
        if (isAdjustable && size == elements.length) elements = dynamicCapacityController(elements, size * 2);
        if (size == elements.length) size--;
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = element;
        size++;
    }

    /**
     * Adds the specified element to the end of this list.
     *
     * @param element the element to be added
     */
    public void add(T element) {
        add(size, element);
    }

    /**
     * Removes the element at the specified position in this list.
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public T remove(int index) {
        if (size == 0) return null;
        checkIndex(index, size);
        T removedElement = elements[index];
        System.arraycopy(elements, index + 1, elements, index, size - index - 1);
        size--;
        if (isAdjustable && size > DEFAULT_CAPACITY && size == elements.length / 4) {
            elements = dynamicCapacityController(elements, elements.length / 2);
        }
        return removedElement;
    }

    /**
     * Removes the first occurrence of the specified element from this list.
     *
     * @param element the element to be removed from this list, if present
     * @return the index of the removed element, or -1 if the element was not found
     */
    public int remove(T element) {
        int index = indexOf(element);
        if (index != -1) {
            remove(index);
        }
        return index;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index the index of the element to be returned
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public T get(int index) {
        checkIndex(index, size);
        return elements[index];
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     *
     * @param element the element to search for
     * @return the index of the first occurrence of the specified element in this list,
     *         or -1 if this list does not contain the element
     */
    public int indexOf(T element) {
        for (int i = 0; i < size; i++) {
            if (elements[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns an array containing all the elements in this list.
     *
     * @return an array containing all the elements in this list
     */
    public T[] copyOf() {
        return Arrays.copyOf(elements, size);
    }

    /**
     * Sets the capacity of the list to the specified value.
     * If the new capacity is less than the current size, the list will be trimmed.
     *
     * @param newCapacity the new capacity of the list
     */
    public void setCapacity(int newCapacity) {
        if (newCapacity < size) {
            size = newCapacity;
        }
        elements = dynamicCapacityController(elements, newCapacity);
    }

    /**
     * Clears the list, removing all elements.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        elements = (T[]) Array.newInstance(clazz, DEFAULT_CAPACITY);
        size = 0;
    }

    /**
     * Applies the specified action to each element in this list.
     *
     * @param action the action to be applied to each element
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < size; i++) {
            action.accept(elements[i]);
        }
    }

    /**
     * Applies the specified action to each element and its index in this list.
     *
     * @param action the action to be applied to each element and its index
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void forEach(BiConsumer<? super T, ? super Integer> action) {
        for (int i = 0; i < size; i++) {
            action.accept(elements[i], i);
        }
    }
    
    /**
     * Returns whether the list is empty.
     *
     * @return whether the list is empty, false otherwise.
     */
    public boolean isEmpty() {
        return size <= 0 || elements.length <= 0;
    }

    /**
     * Checks if the provided index is within the valid range.
     *
     * @param index the index to check
     * @param upperBound the upper bound of the index (exclusive)
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    private void checkIndex(int index, int upperBound) {
        if (index < 0 || index >= upperBound) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Adjusts the capacity of the elements array to the specified new capacity.
     *
     * @param oldElements the old elements array
     * @param newCapacity the new capacity of the elements array
     * @return the new elements array with the adjusted capacity
     */
    @SuppressWarnings("unchecked")
    private T[] dynamicCapacityController(T[] oldElements, int newCapacity) {
        if (!isAdjustable) {
            return oldElements;
        }
        T[] newElements = (T[]) Array.newInstance(clazz, newCapacity);
        System.arraycopy(oldElements, 0, newElements, 0, size);
        return newElements;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns whether the list is adjustable.
     *
     * @return true if the list is adjustable, false otherwise
     */
    public boolean isAdjustable() {
        return isAdjustable;
    }

    /**
     * Sets whether the list should be adjustable.
     *
     * @param isAdjustable true if the list should be adjustable, false otherwise
     */
    public void setAdjustable(boolean isAdjustable) {
        this.isAdjustable = isAdjustable;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public T next() {
                return elements[currentIndex++];
            }
        };
    }
}