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
import ru.slavabulgakov.busesspb.ParserWebPageTask.*;

public class VkApp {
    //constants for OAUTH AUTHORIZE in Vkontakte
	public static final String CALLBACK_URL = "http://api.vkontakte.ru/blank.html";
	private static final String APP_ID = "3565209";
	private static final String OAUTH_AUTHORIZE_URL = "http://api.vkontakte.ru/oauth/authorize?client_id=" + APP_ID + "&scope=wall&redirect_uri=http://api.vkontakte.ru/blank.html&display=touch&response_type=token"; 
		 
	private Context _context;
	private VkSession _vkSess;
	
	private String VK_API_URL = "https://api.vkontakte.ru/method/";
	private String VK_POST_TO_WALL_URL = VK_API_URL + "wall.post?";
	
	public VkApp(){}
	
	public VkApp(Context context){
		_context = context;
		_vkSess = new VkSession(_context);
	}
	
	public void showLoginDialog(VkDialogListener listener){
	    new VkDialog(_context,OAUTH_AUTHORIZE_URL,listener).show();	
	}
	
	private boolean parseResponse(String jsonStr){
		boolean errorFlag = false;
		
		JSONObject jsonObj = null;
		try {
		   jsonObj = new JSONObject(jsonStr);
		   JSONObject errorObj = null;
		   
		   if( jsonObj.has("error") ) {
		       errorObj = jsonObj.getJSONObject("error");
		       int errCode = errorObj.getInt("error_code");
		       if( errCode == 14){
		    	   errorFlag = true;
		       }
		   }
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		
		return errorFlag;	
	}
	
	//publicate message to vk users' wall 
	public void postToWall(final String message, final VkPostWallListener listener) {
		showLoginDialog(new VkDialogListener() {
			
			@Override
			public void onError(String description) {
				listener.onErrorPost();
			}
			
			@Override
			public void onComplete(String url) {
				String[] params = getAccessToken(url);
				saveAccessToken(params[0], params[1], params[2]);
				ParserWebPageTask request = new ParserWebPageTask(new IRequest() {
					
					private int _step = 0;
					boolean _errorFlag = false;
					
					@Override
					public void setCanceled() {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void nextExecute() {
						String[] params = _vkSess.getAccessToken();
						
						String accessToken = params[0];
						String ownerId = params[2];
						
					    //set request uri params
						VK_POST_TO_WALL_URL += "owner_id="+ownerId.split("=")[1]+"&message="+Uri.encode(message)+"&access_token="+accessToken.split("=")[1];
						
						//send request to vkontakte api
						HttpClient client = new DefaultHttpClient();
				        HttpGet request = new HttpGet(VK_POST_TO_WALL_URL);
				        
				        try {
				        	_step++;
				            HttpResponse response = client.execute(request);
				            HttpEntity entity = response.getEntity();
				            String responseText = EntityUtils.toString(entity);
				            _errorFlag = parseResponse(responseText);
				        }
				        catch(ClientProtocolException cexc){
				        	_errorFlag = true;
				        	cexc.printStackTrace();
				        }
				        catch(IOException ioex){
				        	_errorFlag = true;
				        	ioex.printStackTrace();
				        }
					}
					
					@Override
					public boolean needExecute() {
						return _step == 0;
					}
					
					@Override
					public int getRequestId() {
						// TODO Auto-generated method stub
						return 0;
					}
					
					@Override
					public void finish() {
						if (_errorFlag) {
							listener.onErrorPost();
						} else {
							listener.onCompletePost();
							
						}
					}
				});
				request.execute((Void)null);
			}
		});
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
	
	public interface VkPostWallListener {
		void onCompletePost();
		void onErrorPost();
	}
}