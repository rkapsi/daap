
package de.kapsi.net.daap;

import java.util.Iterator;

public class ArrayIterator implements Iterator {
	
	private Object[] array;
	private int from;
	private int to;
	
	public ArrayIterator(Object[] array) {
		this(array, 0, array.length);
	}
	
	public ArrayIterator(Object[] array, int from, int to) {
		this.array = array;
		this.from = from;
		this.to = to;
	}
	
	public boolean hasNext() {
		return (from < to);
	}
	
	public Object next() {
		return array[from++];
	}
	
	public void remove() {
	}
}
