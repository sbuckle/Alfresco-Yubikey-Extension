/**
 * Copyright (C) 2011 Simon Buckle, WebTeq Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public YubicoResponse verify(String otp) {
	    try {
	        return client.verify(otp); // Delegate
	    } catch (Exception e) {
	        return null;
	    }
	}

	public boolean isOwner(String username, String deviceId) {
		return keymanager.isOwner(username, deviceId); // Delegate
	}
	
	public String getDevice(String username) {
		return keymanager.getDevice(username);
	}

}
