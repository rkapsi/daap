
package de.kapsi.net.daap;

import java.lang.reflect.*;
import java.util.ArrayList;

import de.kapsi.net.daap.chunks.Status;
import de.kapsi.net.daap.chunks.ChunkClasses;
import de.kapsi.net.daap.chunks.ContentCodesResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Groups many ContentCodes to one single chunk
 */
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
                
                Method methodContentCode = clazz.getMethod("getContentCode", arg1);
                Method methodName = clazz.getMethod("getName", arg1);
                Method methodType = clazz.getMethod("getType", arg1);
                
                Object inst = clazz.newInstance();
                
                String cotentCode = (String)methodContentCode.invoke(inst, arg2);
                String name = (String)methodName.invoke(inst, arg2);
                int type = ((Integer)methodType.invoke(inst, arg2)).intValue();
                
                add(new ContentCode(cotentCode, name, type));
                
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
