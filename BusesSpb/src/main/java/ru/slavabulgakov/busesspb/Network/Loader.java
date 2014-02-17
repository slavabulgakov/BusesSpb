package ru.slavabulgakov.busesspb.Network;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Files;
import ru.slavabulgakov.busesspb.model.Model;

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
	private ArrayList<Listener> _listeners;
	public void setListener(Listener listener) {
        if (_listeners == null) {
            _listeners = new ArrayList<Listener>();
        }
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }
    private void _staticLoadedListeners() {
        for (Listener listener : _listeners) {
            listener.staticLoaded(Loader.this);
        }
    }
    private void _netLoadedListeners() {
        for (Listener listener : _listeners) {
            listener.netLoaded(Loader.this);
        }
    }
    private void _netErrorListeners() {
        for (Listener listener : _listeners) {
            listener.netError(Loader.this);
        }
    }

	private LoaderContainer _container;
	private Model _model;
	private State _state = State.staticLoading;
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
                Object data = null;
                if (_container.getCacheFileName() != null) {
                    data = Files.loadFromFile(_container.getCacheFileName(), _model);
                }
				if (data == null) {
                    if (_container.getStaticFileName() != null) {
                        ArrayList<String> strings = Files.stringsArrayFromFile(_container.getStaticFileName(), _model);
                        _container.handler(strings);
                        _state = State.netLoading;
                        _staticLoadedListeners();
                    }
				} else {
					_container.loadData(data);
                    _state = State.netLoading;
                    _staticLoadedListeners();
				}
				
                Request request = (Request)FacadeLoader.createRequest(_container.isJson(), _container.getUrlString(), new FacadeLoader.Listener() {

                            @Override
                            public void onResponse(final Object obj) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        _container.handler(obj);
                                        _state = State.complete;
                                        _netLoadedListeners();
                                        _cache();
                                    }
                                }).start();
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                _netErrorListeners();
                            }
                        }
                );
				_queue.add(request);
			}
		}).start();
	}
}
