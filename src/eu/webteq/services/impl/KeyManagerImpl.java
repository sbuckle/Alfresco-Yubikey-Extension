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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.webteq.services.KeyManager;

/**
 * File-backed implementation of user/key map.
 * Each entry should be on a separate line. Fields should be separated
 * by a colon and be of the form "username:deviceid", e.g. admin:abcdefgh
 * 
 * @author Simon Buckle <simon@webteq.eu>
 *
 */
public class KeyManagerImpl implements KeyManager 
{
	private static final Log logger = LogFactory.getLog(KeyManagerImpl.class);
	
	// TODO: periodically check file and reload if it has been changed
	private File keydb;
	// This is currently immutable once initialized so no need to be synchronized
	private Map<String, String> usernameKeyMap = new HashMap<String, String>();
	
	public KeyManagerImpl(String filename) {
		this(new File(filename));
	}
	
	public KeyManagerImpl(File keydb) {
		if (!keydb.exists()) {
			throw new IllegalArgumentException("File does not exist");
		}
		this.keydb = keydb;
		initDB();
	}
	
	private void initDB() {
		try {
			BufferedReader input = new BufferedReader(new FileReader(keydb));
			String line;
			while ((line = input.readLine()) != null) {
				if (line.startsWith("#")) continue; // skip comments
				String[] keyValue = line.split(":");
				usernameKeyMap.put(keyValue[0], keyValue[1]);
			}
		} catch (Exception e) {
			logger.error("Failed to open key database");
		}
	}
	
	/**
	 * Returns the id of user's key.
	 * 
	 * @return String 
	 */
	public String getDevice(String username) {
		return usernameKeyMap.get(username);
	}
	
	public boolean isOwner(String username, String deviceId) {
		String id = usernameKeyMap.get(username);
		if (id == null) return false;
		return deviceId.equals(id);
	}

}
