package ru.slavabulgakov.busesspb.model;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import ru.slavabulgakov.busesspb.model.Loader.State;
import ru.slavabulgakov.busesspb.model.RoutesNamesLoaderContainer.RouteName;

public class RightMenuModel {
	private Model _model;
	private RequestQueue _queue;

	public RightMenuModel(Model model) {
		_model = model;
	}
	
	private RoutesNamesLoaderContainer _routesNamesContainer;
	public RouteName getRouteName(int id) {
		if (_routesNamesContainer == null) {
			_routesNamesContainer = (RoutesNamesLoaderContainer)getLoader(RoutesNamesLoaderContainer.class).getContainer();
		}
		for (Object obj : _routesNamesContainer.getData()) {
			RouteName routeName = (RouteName)obj;
			if (routeName.id == id) {
				return routeName;
			}
		}
		return null;
	}
	
	private RequestQueue _getQueue() {
		if (_queue == null) {
			_queue = Volley.newRequestQueue(_model);
		}
		return _queue;
	}
	
	private ArrayList<Loader> _loaders;
	private ArrayList<Loader> _getLoaders() {
		if (_loaders == null) {
			_loaders = new ArrayList<Loader>();
		}
		return _loaders;
	}
	public void loadForContainer(LoaderContainer container, Loader.Listener listener) {
        Loader loader = getLoader(container.getClass());
        boolean needNewLoader = false;
		if (loader == null) {
            needNewLoader = true;
		} else if (!loader.getContainer().isEqual(container)) {
            needNewLoader = true;
        }
        if (needNewLoader) {
            loader = new Loader(container, _model, _getQueue());
            loader.setListener(listener);
            _getLoaders().add(loader);
        }
        loader.load();
	}
	
	public Loader getLoader(Class<?> cl) {
		for (Loader loader : _getLoaders()) {
			if (loader.getContainer().getClass() == cl) {
                return loader;
			}
		}
		return null;
	}
}
