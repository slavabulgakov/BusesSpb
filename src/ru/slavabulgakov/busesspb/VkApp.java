package ru.slavabulgakov.busesspb;

import java.io.IOException;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

//import net.octobersoft.android.caucasiancuisinefree.common.Constants;
import android.content.Context;
import android.net.Uri;
import org.json.*;

public class VkApp {
    //constants for OAUTH AUTHORIZE in Vkontakte
	public static final String CALLBACK_URL = "http://api.vkontakte.ru/blank.html";
	private static final String OAUTH_AUTHORIZE_URL = "http://api.vkontakte.ru/oauth/authorize?client_id=2844577&scope=wall&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token"; 
		 
	private Context _context;
	private VkDialogListener _listener;
	private VkSession _vkSess;
	
	private String VK_API_URL = "https://api.vkontakte.ru/method/";
	private String VK_POST_TO_WALL_URL = VK_API_URL + "wall.post?";
	
	public VkApp(){}
	
	public VkApp(Context context){
		_context = context;
		_vkSess = new VkSession(_context);
	}
	
	public void setListener(VkDialogListener listener) { _listener = listener; }
	
	public void showLoginDialog(){
	    new VkDialog(_context,OAUTH_AUTHORIZE_URL,_listener).show();	
	}
	
	//parse vkontakte JSON response
	private boolean parseResponse(String jsonStr){
		boolean errorFlag = true;
		
		JSONObject jsonObj = null;
		try {
		   jsonObj = new JSONObject(jsonStr);
		   JSONObject errorObj = null;
		   
		   if( jsonObj.has("error") ) {
		       errorObj = jsonObj.getJSONObject("error");
		       int errCode = errorObj.getInt("error_code");
		       if( errCode == 14){
		    	   errorFlag = false;
		       }
		   }
		}
		catch (JSONException e) {
			e.printStackTrace();
			//Log.d(Constants.DEBUG_TAG,"exception when creating json object");
		}
		
		return errorFlag;	
	}
	
	//publicate message to vk users' wall 
	public boolean postToWall(String message) {
        boolean errorFlag = true;
        
		String[] params = _vkSess.getAccessToken();
		
		String accessToken = params[0];
		String ownerId = params[2];
		
	    //set request uri params
		VK_POST_TO_WALL_URL += "owner_id="+ownerId.split("=")[1]+"&message="+Uri.encode(message)+"&access_token="+accessToken.split("=")[1];
		
		//send request to vkontakte api
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(VK_POST_TO_WALL_URL);
        
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String responseText = EntityUtils.toString(entity);
            
            //parse response for error code or not
            errorFlag = parseResponse(responseText);
            
            //Log.d(Constants.DEBUG_TAG,"response text="+responseText);
        }
        catch(ClientProtocolException cexc){
        	cexc.printStackTrace();
        }
        catch(IOException ioex){
        	ioex.printStackTrace();
        }
        
        return errorFlag;
	}
	
	public String[] getAccessToken(String url) {
		String[] query = url.split("#");
		String[] params = query[1].split("&");
		//params[0] - access token=value, params[1] - expires_in=value, 
		//params[2] - user_id=value
		return params;
	}
	
	public boolean hasAccessToken() {
		String[] params = _vkSess.getAccessToken();
		if( params != null ) {
			long accessTime = Long.parseLong(params[3]); 
			long currentTime = System.currentTimeMillis();
			long expireTime = (currentTime - accessTime) / 1000;
			
			//Log.d(Constants.DEBUG_TAG,"expires time="+expireTime);
			
			if( params[0].equals("") & params[1].equals("") & params[2].equals("") & Long.parseLong(params[3]) ==0 ){
				//Log.d(Constants.DEBUG_TAG,"access token empty");  
				return false;
			}
			else if( expireTime >= Long.parseLong(params[1]) ) {
			    //Log.d(Constants.DEBUG_TAG,"access token time expires out");
				return false;
			}
			else {
				//Log.d(Constants.DEBUG_TAG,"access token ok");
				return true;
			}
		}
		return false;
	}
	
	public void saveAccessToken(String accessToken, String expires, String userId) {
		_vkSess.saveAccessToken(accessToken, expires, userId);
	}
	
	public void resetAccessToken() { _vkSess.resetAccessToken(); }
	
	public interface VkDialogListener {
		void onComplete(String url);
		void onError(String description);
	}
}