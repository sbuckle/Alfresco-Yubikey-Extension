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

package eu.webteq.web.scripts.bean;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.bean.LoginPost;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;

import eu.webteq.services.YubikeyService;

public class YubicoLoginPost extends LoginPost {

	private YubikeyService yubikeyService;
	
	/**
	 * @param yubikeyService
	 */
	public void setYubikeyService(YubikeyService yubikeyService)
	{
		this.yubikeyService = yubikeyService;
	}
	
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Content c = req.getContent();
        if (c == null) {
        	throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Missing POST body.");
        }
        
        JSONObject json;
        try {
            json = new JSONObject(c.getContent());
            String username = json.getString("username");
            String password = json.getString("password");
            
            if (username == null || username.length() == 0) {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
            }
            
            if (password == null) {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Password not specified");
            }
            
            // Extract the OTP from the password field.
            String deviceid = yubikeyService.getDevice(username);
            int idx = password.indexOf(deviceid);
            if (idx == -1) {
            	// Either the user is using the wrong key or no OTP has been included
            	throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Failed to extract OTP");
            }
            String otp = password.substring(idx);
            
            Map<String, Object> result = login(username, password.substring(0, idx));
            
            YubicoResponse response = yubikeyService.verify(otp);
    		if (response == null || !YubicoResponseStatus.OK.equals(response.getStatus())) {
    			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Invalid OTP");
    		}
            
            return result;
        } 
        catch (JSONException jErr) {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Unable to parse JSON POST body: " + jErr.getMessage());
        } 
        catch (IOException ioErr) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Unable to retrieve POST body: " + ioErr.getMessage());
        }
    }
}
