package ru.slavabulgakov.busesspb.controls;

import ru.slavabulgakov.busesspb.model.Model;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;

public class InternetDenyImageButtonController implements OnClickListener {
	
	private ImageButton _button;
	private Handler _handler;
	private Model _model;
	private Activity _activity;

	public InternetDenyImageButtonController(ImageButton button, Model model, Activity activity) {
		_activity = activity;
		_model = model;
		_button = button;
		_handler = new Handler(Looper.getMainLooper());
		_button.setVisibility(_internetDenyIconIsShowed() ? View.VISIBLE : View.INVISIBLE);
		_button.setOnClickListener(this);
	}
	
	public void showInternetDenyIcon() {
    	if (!_internetDenyIconIsShowed()) {
    		_setInternetDenyIconShowed(true);
    		_handler.post(new Runnable() {
				
    			@Override
				public void run() {
					TranslateAnimation animation = new TranslateAnimation(_model.dpToPx(-100), 0, 0, 0);
		        	animation.setInterpolator(new BounceInterpolator());
		        	_button.setVisibility(View.VISIBLE);
		        	animation.setDuration(2000);
		        	animation.setAnimationListener(new AnimationListener() {
		    			
		    			@Override
		    			public void onAnimationStart(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationRepeat(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationEnd(Animation animation) {}
		    		});
		        	_button.startAnimation(animation);
				}
			});
		}
    }
	
	public void hideInternetDenyIcon() {
    	if (_internetDenyIconIsShowed()) {
    		_setInternetDenyIconShowed(false);
    		_handler.post(new Runnable() {
				
    			@Override
				public void run() {
					TranslateAnimation animation = new TranslateAnimation(0, _model.dpToPx(-100), 0, 0);
		        	animation.setInterpolator(new AnticipateInterpolator());
		        	animation.setDuration(2000);
		        	animation.setAnimationListener(new AnimationListener() {
		    			
		    			@Override
		    			public void onAnimationStart(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationRepeat(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationEnd(Animation animation) {
		    				_button.setVisibility(View.INVISIBLE);
		    			}
		    		});
		        	_button.startAnimation(animation);
				}
			});
		}
    }
	
	private Boolean _internetDenyIconIsShowed() {
    	Boolean showed = (Boolean)_model.getData("internetDenyIsShowed");
    	if (showed == null) {
			return false;
		}
    	return showed; 
    }
    private void _setInternetDenyIconShowed(Boolean showed) {
    	_model.setData("internetDenyIsShowed", showed);
    }

	@Override
	public void onClick(View arg0) {
		_activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
	}
}
