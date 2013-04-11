package ru.slavabulgakov.busesspb;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ru.slavabulgakov.busesspb.ShareModel.IShareView;

import com.facebook.*;
import com.facebook.widget.LoginButton;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class ShareFragment extends Fragment implements IShareView, OnClickListener {
	
	private UiLifecycleHelper _fbUiLifecycleHelper;
	private boolean _isResumed = false;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private ImageButton _fbPublishBtn;
	private LoginButton _fbLoginBtn;
	private Model _model;
	private ShareModel _shareModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.share_fragment, parent, false);
		
		_model = (Model)getActivity().getApplicationContext();
		_shareModel = new ShareModel();
		_shareModel.setShareView(this);
		
		_fbUiLifecycleHelper = new UiLifecycleHelper(getActivity(), new Session.StatusCallback() {
    		@Override
			public void call(Session session, SessionState state,
					Exception exception) {
    			if (state == SessionState.OPENING) {
					_model.setfbLoggedInPressed(true);
				}
    			if (_isResumed) {
    				if (session.isOpened() && _model.fbIsLoggedInPressed()) {
    					_updateFacebookButtons();
    					publishStory();	
    		        }
    			}
			}
    	});
		_fbUiLifecycleHelper.onCreate(savedInstanceState);
		
		_fbPublishBtn = (ImageButton)v.findViewById(R.id.shareFBImageButton);
		_fbPublishBtn.setOnClickListener(this);
		_fbLoginBtn = (LoginButton)v.findViewById(R.id.shareFBLoginButton);
		((ImageButton)v.findViewById(R.id.shareVKImageButton)).setOnClickListener(this);
		((ImageButton)v.findViewById(R.id.shareEmailImageButton)).setOnClickListener(this);
		
		_updateFacebookButtons();
		
		return v;
	}
	
	private void _updateFacebookButtons() {
		Session session = Session.getActiveSession();
		if (session.isOpened()) {
			_fbLoginBtn.setVisibility(View.GONE);
			_fbPublishBtn.setVisibility(View.VISIBLE);
		} else {
			_fbLoginBtn.setVisibility(View.VISIBLE);
			_fbPublishBtn.setVisibility(View.GONE);
		}
	}
	
	private boolean _isSubsetOf(Collection<String> subset, Collection<String> superset) {
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
	        if (!_isSubsetOf(PERMISSIONS, permissions)) {
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
	        _model.setfbLoggedInPressed(false);
	    }

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_fbUiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_fbUiLifecycleHelper.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		_isResumed = false;
  		_fbUiLifecycleHelper.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		_isResumed = true;
  		_fbUiLifecycleHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		_fbUiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onVKError() {
		Toast.makeText(getActivity(), R.string.share_cancel_message, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onVKSendSuccess() {
		Toast.makeText(getActivity(), R.string.share_success, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onTwitterGetPin(ShareModel share, String url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTwitterEnterPin(ShareModel share) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTwitterSuccesUpdating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTwitterErrorUpdating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTwitterErrorDuplicateUpdating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.shareFBImageButton:
			publishStory();
			break;
			
		case R.id.shareVKImageButton:
			_shareModel.sendMess2VK(getActivity());
			break;
			
		case R.id.shareEmailImageButton:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_message_title));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_message));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_with)));
			break;

		default:
			break;
		}
		
	}

}
