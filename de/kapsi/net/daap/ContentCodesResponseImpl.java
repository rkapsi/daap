
package de.kapsi.net.daap;

import java.lang.reflect.*;
import java.util.ArrayList;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.ChunkClasses;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class ContentCodesResponseImpl extends ContentCodesResponse {
	
	private static final Log LOG = LogFactory.getLog(ContentCodesResponseImpl.class);
	
	public ContentCodesResponseImpl() {
		super();
		
		add(new Status(200));
		
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
				
				String type = (String)methodChunkType.invoke(inst, arg2);
				String name = (String)methodChunkName.invoke(inst, arg2);
				int code = ((Integer)methodChunkTypeCode.invoke(inst, arg2)).intValue();
				
				add(new ContentCode(type, name, code));
				
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
