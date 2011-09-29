package eu.webteq.services;

/**
 * Service responsible for checking and associating users with keys.
 * 
 * @author simon@webteq.eu
 *
 */
public interface KeyManager {

	public boolean isOwner(String username, String deviceId);
	public String getDevice(String username);
}
