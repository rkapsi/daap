
package de.kapsi.net.daap;

/**
 * This interfce enables us to implement a custom authenticator.
 */
public interface DaapAuthenticator {
    
    /**
     * Return <tt>true</tt> if authentication is required
     */
    public boolean requiresAuthentication();
    
    /**
     * Return <tt>true</tt> if username and password are
     * correct. Note: iTunes doesn't support usernames
     * currently!
     */
    public boolean authenticate(String username, String password);
}
