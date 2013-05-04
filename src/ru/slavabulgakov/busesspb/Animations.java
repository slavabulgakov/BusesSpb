package ru.slavabulgakov.busesspb;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class Animations {
	
	static final int ANIMATION_DURATION = 200;
	
	interface OnAnimationEndListener {
		void onAnimated(View view);
	}
	
	static private MainActivity _getMainActivity() {
		Contr contr = Contr.getInstance();
		MainActivity mainActivity = null;
		if (contr.getActivity().getClass() == MainActivity.class) {
			mainActivity = (MainActivity)contr.getActivity();
		}
		return mainActivity;
	}
	
	static private Model _getModel() {
		return (Model)Contr.getInstance().getActivity().getApplicationContext();
	}
	
	static public void addTicket(Route route) {
		Contr contr = Contr.getInstance();
		MainActivity mainActivity = _getMainActivity();
		if (mainActivity == null) {
			return;
		}
		Model model = _getModel();
		final LinearLayout ticketsLayout = (LinearLayout)mainActivity.findViewById(R.id.selectRouteTickets);
		
		final Ticket ticket = new Ticket(mainActivity, null);
		ticket.setRoute(route);
		ticket.setOnRemoveListener(contr);
		
		((MainActivity)mainActivity).putCloseAllButtonToTicketsLayout();
		if (model.getFavorite().size() > 1) {
			int width = 73;
			if (model.getFavorite().size() == 2) {
				width += 35;
			}
			for (int i = 0; i < ticketsLayout.getChildCount(); i++) {
				View ticket_ = (View)ticketsLayout.getChildAt(i);
				if (ticket_.getClass() == Ticket.class) {
					
					((Ticket)ticket_).animatedOffsetRight(model.dpToPx(width), null);
				}
			}
			ticketsLayout.addView(ticket, 1);
			ticket.animatedShow(model.dpToPx(60));
		} else {
			ticketsLayout.addView(ticket);
			ticket.animatedShow(model.dpToPx(60));
		}
	}
	
	static public void slideDownRoutesListView() {
		MainActivity mainActivity = _getMainActivity();
		if (mainActivity == null) {
			return;
		}
		final Model model = _getModel();
		
		final HorizontalScrollView routeTicketsScrollView = (HorizontalScrollView)mainActivity.findViewById(R.id.routeTicketsScrollView);
		FrameLayout frameLayout = (FrameLayout)mainActivity.findViewById(R.id.selectRouteFrameLayout);
		TranslateAnimation animation = null;
		if (model.getFavorite().size() == 1) {
			animation = new TranslateAnimation(0, 0, 0, model.dpToPx(60));
			routeTicketsScrollView.setVisibility(View.GONE);
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
					if (model.getFavorite().size() > 0) {
						routeTicketsScrollView.setVisibility(View.VISIBLE);
					} else {
						routeTicketsScrollView.setVisibility(View.GONE);
					}
				}
			});
			frameLayout.startAnimation(animation);
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
