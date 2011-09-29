package eu.webteq.web.scripts.bean;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.web.scripts.bean.Login;

import com.yubico.client.v2.YubicoClient;
import com.yubico.client.v2.YubicoResponse;
import com.yubico.client.v2.YubicoResponseStatus;

import eu.webteq.services.YubikeyService;

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
		Map<String, Object> result = super.executeImpl(req, status);
		
		// If the above call didn't throw an exception, then we are good
		String otp = req.getParameter("otp");
		if (otp == null || otp.length() == 0) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "OTP not specified");
		}
		
		if (!yubikeyService.isOwner(req.getParameter("u"), YubicoClient.getPublicId(otp))) {
        	throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Key does not match to user");
        }
		
		YubicoResponse response = yubikeyService.verify(otp);
		if (response == null || !YubicoResponseStatus.OK.equals(response.getStatus())) {
			throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Invalid OTP");
		}
		
        return result;
    }
}
