package com.tinymission.tinysync.web;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;

import com.google.gson.Gson;
import com.tinymission.tinysync.db.DbModel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Wraps a JSON response object to an API call.
 */
public class ApiResponse {

    public enum Status {
        success, error
    }

    private Status _status;

    @JavascriptInterface
    public String status() {
        return _status.toString();
    }

    private String _collection;

    @JavascriptInterface
    public String collection() {
        return _collection;
    }

    private String _message;

    @JavascriptInterface
    public String message() {
        return _message;
    }

    private DbModel[] _data;

    @JavascriptInterface
    public String dataJson() {
        Gson gson = new Gson();
        return gson.toJson(_data);
    }

    public ApiResponse(String collection, Status status, String message, DbModel[] data) {
        this._collection = collection;
        this._status = status;
        this._message = message;
        this._data = data;
    }

    public static ApiResponse success(String collection, String message, DbModel[] data) {
        return new ApiResponse(collection, Status.success, message, data);
    }

    public static ApiResponse error(String collection, String message) {
        return new ApiResponse(collection, Status.error, message, null);
    }


    public WebResourceResponse toResponse() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        try {
            InputStream stream = new ByteArrayInputStream(json.getBytes("UTF-8"));
            WebResourceResponse response = new WebResourceResponse("application/json", "UTF-8", stream);
            stream.close();
            return response;
        }
        catch (Exception ex) {
            Log.w("tinysync.ApiResponse", "Error serializing API response", ex);
            return null;
        }
    }

}
