
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;

import java.lang.reflect.*;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentCodes extends ContainerChunk {
	
	private static final Log LOG = LogFactory.getLog(ContentCodes.class);
	
	private final Status status = new Status(200);
	
	public ContentCodes() {
		super("mccr", "dmap.contentcodesresponse", new ArrayList());
		
		add(status);
		
		String[] names = ChunkClasses.names;
		
		final Class[] arg1 = new Class[]{};
		final Object[] arg2 = new Object[]{};
		
		for(int i = 0; i < names.length; i++) {
			try {
				Class clazz = Class.forName(names[i]);
				
				Method methodChunkType = clazz.getMethod("getChunkType", arg1);
				Method methodChunkName = clazz.getMethod("getChunkName", arg1);
				Method methodChunkTypeCode = clazz.getMethod("chunkTypeCode", arg1);
				
				Object inst = clazz.newInstance();
				
				String chunkType = (String)methodChunkType.invoke(inst, arg2);
				String chunkName = (String)methodChunkName.invoke(inst, arg2);
				int chunkTypeCode = ((Integer)methodChunkTypeCode.invoke(inst, arg2)).intValue();
				//System.out.println(chunkType + ", " + chunkName + ", " + chunkTypeCode);
				
				add(new ContentCode(chunkType, chunkName, chunkTypeCode));
				
			} catch (ClassNotFoundException err) {
				LOG.error(err);
			} catch (NoSuchMethodException err) {
				LOG.error(err);
			} catch (InstantiationException err) {
				LOG.error(err);
			} catch (IllegalAccessException err) {
				LOG.error(err);
			} catch (IllegalArgumentException err) {
				LOG.error(err);
			} catch (InvocationTargetException err) {
				LOG.error(err);
			} catch (SecurityException err) {
				LOG.error(err);
			}
		}
	}
}
