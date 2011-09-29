package eu.webteq.services;

import com.yubico.client.v2.YubicoResponse;

public interface YubikeyService {
	public YubicoResponse verify(String otp);
	public boolean isOwner(String username, String deviceId);
	public String getDevice(String username);
}
