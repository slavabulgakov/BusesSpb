package ru.slavabulgakov.busesspb.Network;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.model.Model;

/**
 * Created by Slava Bulgakov on 10.02.14.
 */
public class Network {
    private RequestQueue _queue;
    private Model _model;

    public Network(Model model) {
        this._model = model;
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
        } else if (loader.getContainer().isEqual(container)) {
            if (loader.getState().getValue() < Loader.State.staticLoading.getValue()) {
                loader.setListener(listener);
            }
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

    public ArrayList<Loader> getLoaders(Class<?>class_) {
        ArrayList<Loader> loaders = new ArrayList<Loader>();
        for (Loader loader : _getLoaders()) {
            if (loader.getContainer().getClass() == class_) {
                loaders.add(loader);
            }
        }
        return loaders;
    }
}
