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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.web.scripts.bean.Login;

import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;

import eu.webteq.services.YubikeyService;

/**
 * For some reason AbstractLoginBean, which is what we should be extending, 
 * is package private.
 * 
 * @author Simon Buckle <simon@webteq.eu>
 *
 */
public class YubicoLogin extends Login {
	
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
        String username = req.getParameter("u");
        if (username == null || username.length() == 0) {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Username not specified");
        }
        
        String password = req.getParameter("pw");
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
        
		// If the above call didn't throw an exception, then we are good
		YubicoResponse response = yubikeyService.verify(otp);
		if (response == null || !YubicoResponseStatus.OK.equals(response.getStatus())) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Invalid OTP");
		}
		
        return result;
    }
}
