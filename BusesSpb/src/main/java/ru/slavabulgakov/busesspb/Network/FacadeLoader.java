package ru.slavabulgakov.busesspb.Network;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

/**
 * Created by user on 24.12.13.
 */
public class FacadeLoader {
    interface Listener {
        void onResponse(Object obj);
    }

    static private StringRequest createStringRequest(String url, final Listener listener, Response.ErrorListener errorListener) {
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                listener.onResponse(response);
            }
        }, errorListener);

        return request;
    }

    static private JsonObjectRequest createJsonRequest(String urlString, final Listener listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(urlString, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                listener.onResponse(response);
            }
        }, errorListener);
        return request;
    }

    static public Object createRequest(boolean isJson, String urlString, final Listener listener, Response.ErrorListener errorListener) {
        Object request;
        if (isJson) {
            request = createJsonRequest(urlString, listener, errorListener);
        } else {
            request = createStringRequest(urlString, listener, errorListener);
        }
        return request;
    }
}
