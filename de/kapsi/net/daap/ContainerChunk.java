
package de.kapsi.net.daap;

import java.io.OutputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Collection;

public class ContainerChunk extends AbstractChunk {
	
	private Collection collection;
	
	public ContainerChunk(String type, String name, Collection collection) {
		super(type, name);
		this.collection = collection;
	}
	
	protected void add(Chunk chunk) {
		collection.add(chunk);
	}
	
	/*public boolean remove(Chunk chunk) {
		return collection.remove(chunk);
	}
	
	public boolean contains(Chunk chunk) {
		return collection.contains(chunk);
	}
	
	public Iterator iterator() {
		return collection.iterator();
	}

	public void clear() {
		collection.clear();
	}*/
	
	public int size() {
		return collection.size();
	}

	public int getLength() {
		int length = 0;
		
		Iterator it = collection.iterator();
		
		while(it.hasNext()) {
			length += ((Chunk)it.next()).getSize();
		}
		
		return length;
	}
	
	public int getType() {
		return Chunk.CONTAINER_TYPE;
	}
	
	public void serialize(OutputStream os) throws IOException {
		super.serialize(os);
		
		Iterator it = collection.iterator();
		
		while(it.hasNext()) {
			((Chunk)it.next()).serialize(os);
		}
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString()).append("=[");
		Iterator it = collection.iterator();
		while(it.hasNext()) {
			buffer.append(it.next());
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append("]");
		return buffer.toString();
	}
}
