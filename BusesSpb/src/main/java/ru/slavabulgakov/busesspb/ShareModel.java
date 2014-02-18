package ru.slavabulgakov.busesspb;

import android.app.Activity;
import android.content.Context;

import com.facebook.android.Facebook;

import ru.slavabulgakov.busesspb.ParserWebPageTask.IRequest;
import ru.slavabulgakov.busesspb.VkApp.VkPostWallListener;
import ru.slavabulgakov.busesspb.controller.Controller;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

public class ShareModel {
	public interface IShareView {
		public void onVKError();
		public void onVKSendSuccess();
		
		public void onTwitterGetPin(ShareModel share, String url);
		public void onTwitterSuccesUpdating();
		public void onTwitterErrorUpdating();
		public void onTwitterErrorDuplicateUpdating();
	}
	
	private static volatile ShareModel _instance;
	public static ShareModel getInstance() {
    	ShareModel localInstance = _instance;
    	if (localInstance == null) {
    		synchronized (Controller.class) {
    			localInstance = _instance;
    			if (localInstance == null) {
    				_instance = localInstance = new ShareModel();
    			}
    		}
    	}
    	return localInstance;
    }
	
	private IShareView _shareView;
	
	public ShareModel() {
		super();
	}
	
	public void setShareView(IShareView shareView) {
		_shareView = shareView;
	}
	
	
	
	/////////
	// VK ===
	private VkApp _vkApp;
	public void sendMess2VK(final Context context) {
		if (_vkApp == null) {
			_vkApp = new VkApp(context);
			_vkApp.postToWall(context.getString(R.string.share_message), new VkPostWallListener() {
				
				@Override
				public void onErrorPost() {
					_shareView.onVKError();
				}
				
				@Override
				public void onCompletePost() {
					_shareView.onVKSendSuccess();
				}
			});
		}
	}
	//=======
	/////////
	
	
	
	///////////////
	// Facebook ===
	private Facebook _facebook;
	
	public Facebook getFacebook() {
//		if (_facebook == null) {
//			_facebook = new Facebook("407111749305322");
//		}
		return _facebook;
	}
	
	public void sendMess2FB(Activity activity) {
//		getFacebook();
//		
//		_facebook.authorize(activity, new String[] {"publish_stream", "read_stream", "offline_access"}, new DialogListener() {
//
//			@Override
//			public void onComplete(Bundle values) {
//				
//				try {
//		            Bundle bundle = new Bundle();
//		            bundle.putString("message", _context.getString(R.string.share_message));
//		            bundle.putString(Facebook.TOKEN,Facebook.TOKEN);
//		            String response = _facebook.request("me/feed",bundle,"POST");
//		            if (response.contains("error")) {
//		            	_shareView.onFBErrorDuplicate();
//					} else {
//						_shareView.onFBSendSuccess();
//					}
//		        } catch (MalformedURLException e) {
//		        	_shareView.onFBError();
//		        } catch (IOException e) {
//		        	_shareView.onFBError();
//		        }
//				
//			}
//
//			@Override
//			public void onFacebookError(FacebookError e) {
//				if (e.getMessage().contains("invalid_key")) {
//					Log.i("into",e.getMessage());
//					_shareView.onFBInvalidKey(e.getMessage());
//				}
//				_shareView.onFBError();
//			}
//
//			@Override
//			public void onError(DialogError e) {
//				_shareView.onFBError();
//			}
//
//			@Override
//			public void onCancel() {
//				_shareView.onFBCanceled();
//			}
//            
//        });
	}
	//=============
	///////////////
	
	
	
	//////////////
	// Twitter ===
	private Twitter _twitter;
	private RequestToken _requestToken;
	private String _url;
	public void sendMess2TW() {
		ParserWebPageTask task = new ParserWebPageTask(new IRequest() {
			
			private int _step = 0;
			
			@Override
			public void setCanceled() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nextExecute() {
				_step++;
				_twitter = new TwitterFactory().getInstance();
				_twitter.setOAuthConsumer("H4GnHcr6R8fF9OpjZHJUA", "UXuCsR8x2wxXIebifAi9HSDnEG97KNxdY50QFmjVQ");
			    _requestToken = null;
				try {
					_requestToken = _twitter.getOAuthRequestToken();
                    _url = _requestToken.getAuthorizationURL();
				} catch (TwitterException e) {
					_shareView.onTwitterErrorUpdating();
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
				_shareView.onTwitterGetPin(ShareModel.this, _url);
			}
		});
		task.execute((Void)null);
	}
	
	private enum TwitterResult {
		SUCCESS,
		ERROR,
		ERROR_DUPLICATE
	};
	
	public void setPin(final String pin, final Context context) {
		ParserWebPageTask task = new ParserWebPageTask(new IRequest() {
			
			int _step = 0;
			TwitterResult _result;
			
			@Override
			public void setCanceled() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void nextExecute() {
				_step++;
				
				try {
					_twitter.getOAuthAccessToken(_requestToken, pin);
					_twitter.updateStatus(context.getString(R.string.share_message_twitter));
				} catch (TwitterException e) {
					if (e.getMessage().contains("duplicate")) {
						_result = TwitterResult.ERROR_DUPLICATE;
					} else {
						_result = TwitterResult.ERROR;
					}
					return;
				}
				 _result = TwitterResult.SUCCESS;
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
				switch (_result) {
				case SUCCESS:
					_shareView.onTwitterSuccesUpdating();
					break;
					
				case ERROR:
					_shareView.onTwitterErrorUpdating();
					break;
					
				case ERROR_DUPLICATE:
					_shareView.onTwitterErrorDuplicateUpdating();
					break;

				default:
					break;
				}
			}
		});
		task.execute((Void)null);
	}
	//============
	//////////////
}
