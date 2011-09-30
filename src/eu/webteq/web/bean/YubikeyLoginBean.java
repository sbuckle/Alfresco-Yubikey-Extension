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

package eu.webteq.web.bean;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;

import eu.webteq.services.KeyManager;
import eu.webteq.services.YubikeyService;

public class YubikeyLoginBean extends LoginBean 
{
	private static final Log logger = LogFactory.getLog(YubikeyLoginBean.class);
	/** OTP **/
	private String otp = null;
	private YubikeyService yubikeyService;
	
	public YubikeyService getYubikeyService()
	{
		return this.yubikeyService;
	}
	
	public void setYubikeyService(YubikeyService yubikeyService)
	{
		this.yubikeyService = yubikeyService;
	}
	
	public void setOtp(String otp)
	{
		this.otp = otp;
	}
	
	public String getOtp()
	{
		return this.otp;
	}
	
	@Override
	public String login()
	{
		String outcome = super.login();
		if (outcome == null) return null; // Only check the OTP if regular authentication succeeds first
		
	    if (yubikeyService.isOwner(this.getUsername(), 
	    		YubicoClient.getPublicId(this.getOtp()))) {
	    	YubicoResponse response = yubikeyService.verify(otp);
		    if (response != null) {
		    	if (YubicoResponseStatus.OK.equals(response.getStatus())) {
		    		return outcome;
		    	}
		    }
	    } else {
	    	logger.info("User does not own device");
	    }
	    
	    return null;
	}
	
}
