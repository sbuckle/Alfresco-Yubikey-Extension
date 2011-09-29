package eu.webteq.services.impl;

import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;

import eu.webteq.services.KeyManager;
import eu.webteq.services.YubikeyService;

public class YubikeyServiceImpl implements YubikeyService {

	private YubicoClient client;
	private KeyManager keymanager;
	
	public YubicoClient getYubicoClient() {
		return this.client;
	}
	
	public void setYubicoClient(YubicoClient client) {
		this.client = client;
	}
	
	public KeyManager getKeyManager() {
		return this.keymanager;
	}
	
	public void setKeyManager(KeyManager keyManager) {
		this.keymanager = keyManager;
	}
	
	@Override
	public YubicoResponse verify(String otp) {
		return client.verify(otp); // Delegate
	}

	@Override
	public boolean isOwner(String username, String deviceId) {
		return keymanager.isOwner(username, deviceId); // Delegate
	}

}
