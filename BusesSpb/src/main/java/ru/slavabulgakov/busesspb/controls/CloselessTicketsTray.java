package ru.slavabulgakov.busesspb.controls;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CloselessTicketsTray extends RelativeLayout {
	Model _model;
	Context _context;
	ImageView _icon;
	LinearLayout _ticketsLayout;

	public CloselessTicketsTray(Context context) {
		super(context);
		_load(context, null);
	}

	public CloselessTicketsTray(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	public CloselessTicketsTray(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}

	private void _load(Context context, AttributeSet attrs) {
		View.inflate(context, R.layout.closeless_tickets_tray, this);
		_context = context;
		_icon = (ImageView)findViewById(R.id.menuIcon);
		_ticketsLayout = (LinearLayout)findViewById(R.id.mainRoutesScrollView);
		setBackgroundResource(R.drawable.btn_selected_black);
	}
	
	public void inition(Model model) {
		_model = model;
	}
	
	public void update() {
		int resId = _model.menuIsOpened(MenuKind.Left) ? R.drawable.menu_close_icon : R.drawable.menu_open_icon;
		_icon.setImageResource(resId);
		
		_ticketsLayout.removeAllViews();
		for (Route route : _model.getFavorite()) {
			TicketCloseLess ticket = new TicketCloseLess(_context);
			ticket.setRoute(route);
			_ticketsLayout.addView(ticket, 0);
		}
		
    	HorizontalScrollView routesBtnScrollView = (HorizontalScrollView)findViewById(R.id.mainRoutesBtnScrollView);
    	if (_model.getFavorite().size() > 0) {
    		routesBtnScrollView.setVisibility(View.VISIBLE);
		} else {
			routesBtnScrollView.setVisibility(View.GONE);
		}
    	
    	RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
		lp.setMargins(lp.leftMargin, lp.topMargin, (_model.getFavorite().size() > 0 ? _model.dpToPx(60) : 0), lp.bottomMargin);
		setLayoutParams(lp);
		setPadding(getPaddingLeft(), getPaddingTop(), (_model.getFavorite().size() > 0 ? _model.dpToPx(5) : 0), getPaddingBottom());
		
		RelativeLayout.LayoutParams lpMenuIcon = (RelativeLayout.LayoutParams)_icon.getLayoutParams();
		lpMenuIcon.setMargins(lpMenuIcon.leftMargin, lpMenuIcon.topMargin, (_model.getFavorite().size() > 0 ? 0 : _model.dpToPx(8)), lpMenuIcon.bottomMargin);
		_icon.setLayoutParams(lpMenuIcon);
	}
}
