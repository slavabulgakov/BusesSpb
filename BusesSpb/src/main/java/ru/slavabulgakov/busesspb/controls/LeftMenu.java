package ru.slavabulgakov.busesspb.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;

import ru.slavabulgakov.busesspb.Adapter;
import ru.slavabulgakov.busesspb.Animations;
import ru.slavabulgakov.busesspb.FlurryConstants;
import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.TransportKind;

public class LeftMenu extends LinearLayout implements TextWatcher, AdapterView.OnItemClickListener {
	private Model _model;
	private TicketsTray _ticketsTray;
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;
	private CheckButton _menuBusFilter;
	private CheckButton _menuTrolleyFilter;
	private CheckButton _menuTramFilter;
	private CheckButton _menuShipFilter;
	private ImageButton _clearButton;
	private Context _context;
	
	public EditText getInput() {
		return _editText;
	}
	
	public TicketsTray getTicketsTray() {
		return _ticketsTray;
	}
	
	public void setModel(Model model) {
		_model = model;
		
		if (_listView.getAdapter() == null) {
			Adapter adapter = new Adapter(_context, _model);
			_listView.setAdapter(adapter);
			adapter.getFilter().filter(_editText.getText());
		}
	}
	
	private void _load(Context context, AttributeSet attrs) {
		_context = context;
		View.inflate(context, R.layout.left_menu, this);
		_ticketsTray = (TicketsTray)findViewById(R.id.routeTicketsScrollView);
		_progressBar = (ProgressBar)findViewById(R.id.selectRouteProgressBar);
		
		_editText = (EditText)findViewById(R.id.selectRouteText);
		_editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		_editText.addTextChangedListener(this);
		_editText.setOnKeyListener(Controller.getInstance());
		
		_clearButton = (ImageButton)findViewById(R.id.clearRouteText);
		_clearButton.setOnClickListener(Controller.getInstance());
		if (_editText.getText().length() > 0) {
			_clearButton.setVisibility(View.VISIBLE);
		} else {
			_clearButton.setVisibility(View.GONE);
		}
		
		_listView = (ListView)findViewById(R.id.selectRouteListView);
		_listView.setOnItemClickListener(this);
        LinearLayout linearLayout = (LinearLayout)View.inflate(_context, R.layout.under_nav_bar_footer, null);
        _listView.addFooterView(linearLayout);

		_menuBusFilter = (CheckButton)findViewById(R.id.menuBusFilter);
    	_menuBusFilter.setOnClickListener(Controller.getInstance());
    	
    	_menuTrolleyFilter = (CheckButton)findViewById(R.id.menuTrolleyFilter);
		_menuTrolleyFilter.setOnClickListener(Controller.getInstance());
		
		_menuTramFilter = (CheckButton)findViewById(R.id.menuTramFilter);
		_menuTramFilter.setOnClickListener(Controller.getInstance());
		
		_menuShipFilter = (CheckButton)findViewById(R.id.menuShipFilter);
		_menuShipFilter.setOnClickListener(Controller.getInstance());
	}

	public LeftMenu(Context context) {
		super(context);
		_load(context, null);
	}

	public LeftMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	@SuppressLint("NewApi")
	public LeftMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}
	
	public void move(double percent) {
    	double delta = 100;
    	if (percent > 0) {
    		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
        	lp.setMargins(_model.dpToPx(-delta + delta * percent), 0, 0, 0);
        	setLayoutParams(lp);
		}
    }
	
	public void updateListView() {
        Adapter adapter = _getAdapter();
		if (adapter != null) {
			adapter.getFilter().filterByCurrentPrams();
		}
	}

    private Adapter _getAdapter() {
        Adapter adapter = null;
        if (_listView != null) {
            if (_listView.getAdapter() != null) {
                if (_listView.getAdapter().getClass() == HeaderViewListAdapter.class) {
                    adapter = (Adapter)((HeaderViewListAdapter)_listView.getAdapter()).getWrappedAdapter();
                } else {
                    adapter = (Adapter)_listView.getAdapter();
                }
            }
        }
        return adapter;
    }

	public void showMenuContent() {
		_progressBar.setVisibility(View.INVISIBLE);
		_listView.setVisibility(View.VISIBLE);
		_editText.setEnabled(true);
		_menuBusFilter.setEnabled(true);
		_menuTrolleyFilter.setEnabled(true);
		_menuTramFilter.setEnabled(true);
		updateListView();
	}
	
	public void setFiltersButtonsVisibility(boolean visible) {
		_menuBusFilter.setVisibility(visible ? View.VISIBLE : View.GONE);
		_menuTrolleyFilter.setVisibility(visible ? View.VISIBLE : View.GONE);
		_menuTramFilter.setVisibility(visible ? View.VISIBLE : View.GONE);
		_menuShipFilter.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	public void setInputVisible(boolean visible) {
		_editText.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	public void setProgressBarVisible(boolean visible) {
		_progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	public void loadMenuContent() {
    	_progressBar.setVisibility(View.VISIBLE);
		_editText.setEnabled(false);
		_menuBusFilter.setEnabled(false);
		_menuTrolleyFilter.setEnabled(false);
		_menuTramFilter.setEnabled(false);
		_menuShipFilter.setEnabled(true);
		_model.loadDataForAllRoutes();
    }
	
	public void updateFilterButtons() {
		_menuBusFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Bus));
		_menuTrolleyFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Trolley));
		_menuTramFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Tram));
		_menuShipFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Ship));
	}

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        Adapter adapter = _getAdapter();
        if (adapter != null) {
            adapter.getFilter().filter(text);
        }

        if (text.length() > 0) {
            _clearButton.setVisibility(View.VISIBLE);
        } else {
            _clearButton.setVisibility(View.GONE);
        }

        if (!(Boolean)_model.getData("TextEditUsed2", false)) {
            _model.setData("TextEditUsed2", true, false);
            FlurryAgent.logEvent(FlurryConstants.textEditUsed);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {}

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final Adapter adapter = _getAdapter();
        final Route route = adapter.getItem(position);
        _model.setRouteToFavorite(route);
        Animations.listItemCollapse(view, new Animations.OnAnimationEndListener() {

            @Override
            public void onAnimated(View view) {
                adapter.removeRoute(position, view);
            }
        });

        getTicketsTray().addTicket(route);
        Animations.slideDownRoutesListView();
        getInput().setText("");
    }
}
