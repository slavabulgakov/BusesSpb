package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.model.Model;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

public class Animations {
	
	public static final int ANIMATION_DURATION = 200;
	
	public interface OnAnimationEndListener {
		void onAnimated(View view);
	}
	
	static private MainActivity _getMainActivity() {
		Controller contr = Controller.getInstance();
		MainActivity mainActivity = null;
		if (contr.getActivity().getClass() == MainActivity.class) {
			mainActivity = (MainActivity)contr.getActivity();
		}
		return mainActivity;
	}
	
	static private Model _getModel() {
		return (Model)Controller.getInstance().getActivity().getApplicationContext();
	}
	
	
	
	static public void slideDownRoutesListView() {
		final MainActivity mainActivity = _getMainActivity();
		if (mainActivity == null) {
			return;
		}
		final Model model = _getModel();
		
		LinearLayout listViewAndProgressBarLinearLayout = (LinearLayout)mainActivity.findViewById(R.id.listViewAndProgressBarLinearLayout);
		TranslateAnimation animation = null;
		if (model.getFavorite().size() == 1) {
			animation = new TranslateAnimation(0, 0, 0, model.dpToPx(60));
			mainActivity.getLeftMenu().getTicketsTray().setVisibility(View.GONE);
		}
		if (animation != null) {
			animation.setDuration(Animations.ANIMATION_DURATION);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mainActivity.getLeftMenu().getTicketsTray().update();
				}
			});
			listViewAndProgressBarLinearLayout.startAnimation(animation);
		}
	}
	
	
	
	static public void listItemCollapse(final View view, final OnAnimationEndListener onEndListener) {
		AnimationListener animListener = new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				onEndListener.onAnimated(view);
			}
		};
		
		Animation animation = new FadeUpAnimation(view);
		animation.setDuration(ANIMATION_DURATION);
		animation.setAnimationListener(animListener);
		view.setAnimation(animation);
		view.startAnimation(animation);
	}
}
