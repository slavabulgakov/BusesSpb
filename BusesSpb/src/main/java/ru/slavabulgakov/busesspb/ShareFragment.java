package ru.slavabulgakov.busesspb;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.flurry.android.FlurryAgent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ru.slavabulgakov.busesspb.ShareModel.IShareView;
import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.model.Model;

public class ShareFragment extends Fragment implements IShareView, OnClickListener {
	
	private UiLifecycleHelper _fbUiLifecycleHelper;
	private boolean _isResumed = false;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private ImageButton _fbPublishBtn;
	private LoginButton _fbLoginBtn;
	private Model _model;
	private ShareModel _shareModel;
	private ShareFragmentListener _listener;
	
	public void setListener(ShareFragmentListener listener) {
		_listener = listener;
	}
	
	interface ShareFragmentListener {
		void onSuccessShared();
	}

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
		
		_shareModel = _model.getShareModel();
		if (_shareModel == null) {
			_shareModel = new ShareModel();
			_model.setShareModel(_shareModel);
		}
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
		((ImageButton)v.findViewById(R.id.shareTwitterImageButton)).setOnClickListener(this);
		
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
	        postParams.putString("picture", "https://lh5.ggpht.com/1nCZiHrQQX-zcoXlRp6uZ0IZ5oV69wKs7QmzFoJAn30RIIHaQYsEuDGFxnRnBG-OYus=w124");

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(_model,
	                         R.string.share_cancel_message,
	                         Toast.LENGTH_SHORT).show();
	                    FlurryAgent.logEvent(FlurryConstants.shareFBDeny);
	                    } else {
	                        Toast.makeText(_model, 
	                             R.string.share_success,
	                             Toast.LENGTH_LONG).show();
	                        FlurryAgent.logEvent(FlurryConstants.shareFBSuccess);
	                        if (_listener != null) {
	                			_listener.onSuccessShared();
	                		}
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
	
	public void onActivityResult_(int requestCode, int resultCode, Intent data) {
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
  		String pin = (String)_model.getData("twitterPin"); 
  		if (pin != null) {
			_model.removeData("twitterPin");
			_shareModel.setPin(pin, getActivity());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		_fbUiLifecycleHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onVKError() {
		Toast.makeText(getActivity(), R.string.share_cancel_message, Toast.LENGTH_LONG).show();
		FlurryAgent.logEvent(FlurryConstants.shareVKDeny);
	}

	@Override
	public void onVKSendSuccess() {
		Toast.makeText(getActivity(), R.string.share_success, Toast.LENGTH_LONG).show();
		FlurryAgent.logEvent(FlurryConstants.shareVKSuccess);
		if (_listener != null) {
			_listener.onSuccessShared();
		}
	}

	@Override
	public void onTwitterGetPin(ShareModel share, String url) {
        if (url != null) {
            _model.setData("twitterUrl", url);
            getActivity().startActivity(new Intent(getActivity(), Browser.class));
        }
	}

	@Override
	public void onTwitterSuccesUpdating() {
		Toast.makeText(getActivity(), R.string.share_success, Toast.LENGTH_LONG).show();
		FlurryAgent.logEvent(FlurryConstants.shareTwitterSuccess);
		if (_listener != null) {
			_listener.onSuccessShared();
		}
	}

	@Override
	public void onTwitterErrorUpdating() {
		FlurryAgent.logEvent(FlurryConstants.shareTwitterDeny);
		showAlertDialog(R.string.connection_error_title, R.string.connection_error_message, android.R.drawable.ic_dialog_alert);
	}

	@Override
	public void onTwitterErrorDuplicateUpdating() {
		showAlertDialog(R.string.share_error_title, R.string.share_error_message_duplicate, android.R.drawable.ic_dialog_alert);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.shareFBImageButton:
			publishStory();
			FlurryAgent.logEvent(FlurryConstants.shareFBButtonPressed);
			break;
			
		case R.id.shareVKImageButton:
			_shareModel.sendMess2VK(getActivity());
			FlurryAgent.logEvent(FlurryConstants.shareVKButtonPressed);
			break;
			
		case R.id.shareEmailImageButton:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_message_title));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_message));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_with)));
			FlurryAgent.logEvent(FlurryConstants.shareEmailButtonPressed);
			break;
			
		case R.id.shareTwitterImageButton:
			_shareModel.sendMess2TW();
			FlurryAgent.logEvent(FlurryConstants.shareTwitterButtonPressed);
			break;

		default:
			break;
		}
		
	}

	public void showAlertDialog(final int titleId, final int messageId, final int iconId) {
        Controller.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(messageId)
                        .setTitle(titleId)
                        .setCancelable(false)
                        .setIcon(iconId)
                        .setPositiveButton(R.string.ok, null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

}
