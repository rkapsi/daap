
package de.kapsi.net.daap;

import de.kapsi.net.daap.chunks.*;
import java.io.OutputStream;
import java.io.IOException;

public class ServerInfo extends ServerInfoResponse {
	
	private final Status status = new Status(200);
	private final TimeoutInterval timeout = new TimeoutInterval(1800);
	private final DmapProtocolVersion dmapProt = new DmapProtocolVersion(0x00020000);
	private final DaapProtocolVersion daapProt = new DaapProtocolVersion(0x00020000);
	private final ItemName itemName = new ItemName();
	private final LoginRequired loginRequired = new LoginRequired(false);
	private final SupportsAutoLogout supportsAutoLogout = new SupportsAutoLogout(false);
	private final SupportsUpdate supportsUpdate = new SupportsUpdate(false);
	private final SupportsPersistentIds supportsPersistentIds = new SupportsPersistentIds(false);
	private final SupportsExtensions supportsExtensions = new SupportsExtensions(false);
	private final SupportsBrowse supportsBrowse = new SupportsBrowse(false);
	private final SupportsQuery supportsQuery = new SupportsQuery(false);
	private final SupportsIndex supportsIndex = new SupportsIndex(false);
	private final SupportsResolve supportsResolve = new SupportsResolve(false);
	private final DatabaseCount databaseCount = new DatabaseCount(1);
	
	public ServerInfo(String name) {
		super();
		
		itemName.setValue(name);
		
		add(status);
		add(timeout);
		add(dmapProt);
		add(daapProt);
		add(itemName);
		add(loginRequired);
		add(supportsAutoLogout);
		add(supportsUpdate);
		add(supportsPersistentIds);
		add(supportsExtensions);
		add(supportsBrowse);
		add(supportsQuery);
		add(supportsIndex);
		add(supportsResolve);
		add(databaseCount);
	}
}
