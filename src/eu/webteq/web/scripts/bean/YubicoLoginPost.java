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

import com.yubico.client.v2.YubicoClient;
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
		Map<String, Object> result = super.executeImpl(req, status);
		
        Content c = req.getContent();
        JSONObject json;
        try {
            json = new JSONObject(c.getContent());
            String otp = json.getString("otp");

            if (otp == null || otp.length() == 0) {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "OTP not specified");
            }

            if (!yubikeyService.isOwner(json.getString("username"), YubicoClient.getPublicId(otp))) {
            	throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Key does not match to user");
            }
            
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
