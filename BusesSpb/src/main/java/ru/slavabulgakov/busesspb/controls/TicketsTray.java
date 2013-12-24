package ru.slavabulgakov.busesspb.controls;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class TicketsTray extends HorizontalScrollView implements Ticket.OnRemoveListener, android.view.View.OnClickListener {
	
	LayoutInflater _inflater;
	Model _model;
	LinearLayout _ticketsLayout;
	Context _context;
	Listener _listener;
	
	public interface Listener {
		void willRemoveTicket();
		void didRemoveTicket();
	}
	
	public void inition(Model model, Listener listener) {
		_model = model;
		_listener = listener;
		for (Route route : _model.getFavorite()) {
			Ticket ticket = new Ticket(_context);
			ticket.setRoute(route);
			ticket.setOnRemoveListener(this);
			_ticketsLayout.addView(ticket);
		}
		putCloseAllButtonToTicketsLayout();
	}
	
	public void update() {
		if (_model.getFavorite().size() > 0) {
			setVisibility(View.VISIBLE);
		} else {
			setVisibility(View.GONE);
		}
	}

	public TicketsTray(Context context) {
		super(context);
		_load(context, null);
	}

	public TicketsTray(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	public TicketsTray(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}

	private void _load(Context context, AttributeSet attrs) {
		View.inflate(context, R.layout.tickets_tray, this);
		_ticketsLayout = (LinearLayout)findViewById(R.id.ticketsTrayLayout);
		_context = context;
	}
	
	public void putCloseAllButtonToTicketsLayout() {
		if (_model.getFavorite().size() > 1) {
			if (_ticketsLayout.getChildAt(0).getClass() != CloseAllTickets.class) {
				CloseAllTickets closeAllBtn = new CloseAllTickets(_context, _model);
				closeAllBtn.setOnClickListener(this);
				_ticketsLayout.addView(closeAllBtn, 0);
				closeAllBtn.animatedShow(_model.dpToPx(60));
			}
		} else {
			if (_ticketsLayout.getChildCount() > 0) {
				View closeAllBtn = _ticketsLayout.getChildAt(0);
				if (closeAllBtn.getClass() == CloseAllTickets.class) {
					((CloseAllTickets)closeAllBtn).animatedRemove(new CloseAllTickets.OnAnimationEndListener() {
						
						@Override
						public void onAnimated(final CloseAllTickets button) {
							button.setVisibility(View.GONE);
							((View) button.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)button.getParent()).removeView(button);
					            }
					        });
						}
					});
				}
			}
		}
	}
	
	public void addTicket(Route route) {
		final Ticket ticket = new Ticket(_context, null);
		ticket.setRoute(route);
		ticket.setOnRemoveListener(this);
		
		putCloseAllButtonToTicketsLayout();
		if (_model.getFavorite().size() > 1) {
			int width = 73;
			if (_model.getFavorite().size() == 2) {
				width += 35;
			}
			for (int i = 0; i < _ticketsLayout.getChildCount(); i++) {
				View ticket_ = (View)_ticketsLayout.getChildAt(i);
				if (ticket_.getClass() == Ticket.class) {
					
					((Ticket)ticket_).animatedOffsetRight(_model.dpToPx(width), null);
				}
			}
			_ticketsLayout.addView(ticket, 1);
			ticket.animatedShow(_model.dpToPx(60));
		} else {
			_ticketsLayout.addView(ticket);
			ticket.animatedShow(_model.dpToPx(60));
		}
	}

	@Override
	public void willRemove(Ticket ticket) {
		_model.setRouteToAll(ticket.getRoute());
		_listener.willRemoveTicket();
		putCloseAllButtonToTicketsLayout();
		removeTicket(ticket);
	}

	@Override
	public void didRemove(Ticket ticket) {
		_listener.didRemoveTicket();
		if (_model.getFavorite().size() == 0) {
			setVisibility(View.GONE);
		}
	}
	
	public void removeTicket(Ticket ticket) {
		int width = ticket.getWidth();
		int closeAllBtnWidth = _ticketsLayout.getChildAt(0).getWidth();
		if (_model.getFavorite().size() == 2) {
			width += closeAllBtnWidth;
		}
		
		for(int i = 0; i < _ticketsLayout.getChildCount(); i++) {
			if (_ticketsLayout.getChildAt(i).getClass() == Ticket.class) {
				Ticket t = (Ticket)_ticketsLayout.getChildAt(i);
				if (t.getRoute().id.equals(ticket.getRoute().id)) {
					if (_ticketsLayout.getChildCount() == 3) {
						if (i == _ticketsLayout.getChildCount() - 1) {
							Ticket ti = (Ticket)_ticketsLayout.getChildAt(1);
							ti.animatedOffsetLeft(_model.dpToPx(closeAllBtnWidth), null);
						} else {
							for(int j = i + 1; j < _ticketsLayout.getChildCount(); j++) {
								if(_ticketsLayout.getChildAt(j).getClass() == Ticket.class) {
									Ticket ti = (Ticket)_ticketsLayout.getChildAt(j);
									ti.animatedOffsetLeft(_model.dpToPx(width), null);
								}
							}
						}
					} 
					break;
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getClass() == CloseAllTickets.class) {
			for (Route route : _model.getFavorite()) {
				_model.getAllRoutes().add(route);
			}
			_model.getFavorite().clear();
			_listener.willRemoveTicket();
			
			for (int i = 0; i < _ticketsLayout.getChildCount(); i++) {
				View view = _ticketsLayout.getChildAt(i);
				if (view.getClass() == Ticket.class) {
					Ticket ticket = (Ticket)view;
					ticket.animatedRemove(new Ticket.OnAnimationEndListener() {

						@Override
						public void onAnimated(final Ticket ticket) {
							ticket.setVisibility(View.GONE);
							((View)ticket.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)ticket.getParent()).removeView(ticket);
					            }
					        });
						}
					});
				} else if (view.getClass() == CloseAllTickets.class) {
					CloseAllTickets closeAllTickets = (CloseAllTickets)view;
					closeAllTickets.animatedRemove(new CloseAllTickets.OnAnimationEndListener() {
						
						@Override
						public void onAnimated(final CloseAllTickets button) {
							button.setVisibility(View.GONE);
							((View)button.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)button.getParent()).removeView(button);
					            }
					        });
							setVisibility(View.GONE);
							_listener.didRemoveTicket();
						}
					});
				}
			}
		}
	}
}
