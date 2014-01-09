package ru.slavabulgakov.busesspb.model;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Files;

public class Loader {
	
	public enum State {
		staticLoading(0),
		netLoading(1),
		complete(2);
		
		private final int _value;
		private State(int value) {
			_value = value;
		}
		public int getValue() {
			return _value;
		}
	};
	
	public interface Listener {
		void staticLoaded(Loader loader);
		void netLoaded(Loader loader);
		void netError(Loader loader);
	}
	private Listener _listener;
	public void setListener(Listener listener) {
		_listener = listener;
	}

	private LoaderContainer _container;
	private Model _model;
	private State _state;
	private RequestQueue _queue;
	public Loader(LoaderContainer container, Model model, RequestQueue queue) {
		_container = container;
		_model = model;
		_queue = queue;
	}
	
	public State getState() {
		return _state;
	}
	
	public LoaderContainer getContainer() {
		return _container;
	}
	
	private void _cache() {
        if (_container.getCacheFileName() != null) {
            Files.saveToFile(_container.getData(), _container.getCacheFileName(), _model);
        }
	}

	public void load() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				_state = State.staticLoading;
                ArrayList<Object> data = null;
                if (_container.getCacheFileName() != null) {
                    data = (ArrayList<Object>)Files.loadFromFile(_container.getCacheFileName(), _model);
                }
				if (data == null) {
                    if (_container.getStaticFileName() != null) {
                        ArrayList<String> strings = Files.stringsArrayFromFile(_container.getStaticFileName(), _model);
                        _container.handler(strings);
                        _state = State.netLoading;
                        _listener.staticLoaded(Loader.this);
                    }
				} else {
					_container.loadData(data);
                    _state = State.netLoading;
                    _listener.staticLoaded(Loader.this);
				}
				
                Request request = (Request)FacadeLoader.createRequest(_container.isJson(), _container.getUrlString(), new FacadeLoader.Listener() {

                            @Override
                            public void onResponse(Object obj) {
                                _container.handler(obj);
                                _state = State.complete;
                                _listener.netLoaded(Loader.this);
                                _cache();
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                _listener.netError(Loader.this);
                            }
                        }
                );
				_queue.add(request);
			}
		}).start();
	}

    public void reload() {
        if (_state == State.complete) {
            load();
        }
    }
}
