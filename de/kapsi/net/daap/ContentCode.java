
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;
import java.io.OutputStream;
import java.io.IOException;

public class ContentCode extends Dictionary {
	
	public ContentCode(String type, String name, int value) {
		super();
		
		add(new ContentCodesNumber(DaapUtil.toContentCodeNumber(type)));
		add(new ContentCodesName(name));
		add(new ContentCodesType(value));
	}
}
