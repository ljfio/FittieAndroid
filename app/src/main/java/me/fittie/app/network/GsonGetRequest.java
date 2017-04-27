package me.fittie.app.network;

/**
 * Created by luke on 27/04/2017.
 * https://developer.android.com/training/volley/request-custom.html
 */

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class GsonGetRequest<T> extends Request<T> {
    private final Gson gson = new Gson();
    private final Class<T> theClass;
    private final Map<String, String> headers;
    private final Listener<T> listener;

    private Map<String, String> params = null;

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url      URL of the request to make
     * @param theClass Relevant class object, for Gson's reflection
     * @param headers  Map of request headers
     */
    public GsonGetRequest(String url, Class<T> theClass, Map<String, String> headers,
                          Listener<T> listener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.theClass = theClass;
        this.headers = headers;
        this.listener = listener;
    }

    public GsonGetRequest(String url, Class<T> theClass, Map<String, String> params, Map<String, String> headers,
                          Listener<T> listener, ErrorListener errorListener) {
        this(url, theClass, headers, listener, errorListener);
        this.params = params;
    }

    @Override
    public String getUrl() {
        if (getParams() != null && !getParams().isEmpty()) {
            String encoded = "";

            try {
                for (Map.Entry<String, String> pair : params.entrySet()) {
                    // Check if this is the first key add question mark to start or ampersand to continue
                    encoded += !encoded.contains("?") ? "?" : "&";
                    // Add the key value pair
                    encoded += pair.getKey() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8");
                }
            } catch(UnsupportedEncodingException ex) {
                Log.e("GsonGetRequest", ex.getMessage());
            }

            return super.getUrl() + encoded;
        }

        return super.getUrl();
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(gson.fromJson(json, theClass), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}