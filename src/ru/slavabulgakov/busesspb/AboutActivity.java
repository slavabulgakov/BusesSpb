package ru.slavabulgakov.busesspb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class AboutActivity extends BaseActivity {
	
	private UiLifecycleHelper _fbUiLifecycleHelper;
	private boolean _isResumed = false;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private ImageButton _fbPublishBtn;
	private LoginButton _fbLoginBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		((Button)findViewById(R.id.aboutSendBtn)).setOnClickListener(Contr.getInstance());
		
		_fbUiLifecycleHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
    		@Override
			public void call(Session session, SessionState state,
					Exception exception) {
    			if (state == SessionState.OPENING) {
					_model.setfbLoggedInPressed(true);
				}
    			if (_isResumed) {
    				if (session.isOpened() && _model.fbIsLoggedInPressed()) {
    					_model.setfbLoggedInPressed(false);
    					publishStory();	
    		        }
    			}
			}
    	});
		_fbUiLifecycleHelper.onCreate(savedInstanceState);
		
		Session session = Session.getActiveSession();
		_fbPublishBtn = (ImageButton)findViewById(R.id.shareFBImageButton);
		_fbPublishBtn.setOnClickListener(Contr.getInstance());
		_fbLoginBtn = (LoginButton)findViewById(R.id.shareFBLoginButton);
		_fbLoginBtn.setText("");
		if (session.isOpened()) {
			_fbLoginBtn.setVisibility(View.GONE);
			_fbPublishBtn.setVisibility(View.VISIBLE);
		} else {
			_fbLoginBtn.setVisibility(View.VISIBLE);
			_fbPublishBtn.setVisibility(View.GONE);
		}
		
//		try {
//		    PackageInfo info = getPackageManager().getPackageInfo(
//		            "ru.slavabulgakov.busesspb", 
//		            PackageManager.GET_SIGNATURES);
//		    for (Signature signature : info.signatures) {
//		        MessageDigest md = MessageDigest.getInstance("SHA");
//		        md.update(signature.toByteArray());
//		        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//		        }
//		} catch (NameNotFoundException e) {
//
//		} catch (NoSuchAlgorithmException e) {
//
//		}
	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	public void publishStory() {
	    Session session = Session.getActiveSession();

	    if (session != null){

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	        session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }

	        Bundle postParams = new Bundle();
	        postParams.putString("name", getString(R.string.share_message_title));
//	        postParams.putString("caption", "Build great social apps and get more installs.");
	        postParams.putString("description", getString(R.string.share_message));
	        postParams.putString("link", "https://play.google.com/store/apps/details?id=ru.slavabulgakov.busesspb");
	        postParams.putString("picture", "http://yandex.st/morda-logo/i/logo.png");

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
//	                JSONObject graphResponse = response
//	                                           .getGraphObject()
//	                                           .getInnerJSONObject();
//	                String postId = null;
//	                try {
//	                    postId = graphResponse.getString("id");
//	                } catch (JSONException e) {
//	                    
//	                }
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(_model,
	                         R.string.share_cancel_message,
	                         Toast.LENGTH_SHORT).show();
	                    } else {
	                        Toast.makeText(_model, 
	                             R.string.share_success,
	                             Toast.LENGTH_LONG).show();
	                }
	            }
	        };

	        Request request = new Request(session, "me/feed", postParams, 
	                              HttpMethod.POST, callback);

	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	    }

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		_fbUiLifecycleHelper.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		_isResumed = false;
  		_fbUiLifecycleHelper.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		_isResumed = true;
  		_fbUiLifecycleHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		_fbUiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_fbUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}
}
