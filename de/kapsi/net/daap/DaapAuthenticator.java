
package de.kapsi.net.daap;

/**
 *
 */
public interface DaapAuthenticator {
	
	public boolean requiresAuthentication();
	public boolean authenticate(String username, String password);
}
