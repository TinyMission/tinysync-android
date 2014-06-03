package com.tinymission.tinysync.web;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.tinymission.tinysync.db.DbCollection;
import com.tinymission.tinysync.db.DbContext;
import com.tinymission.tinysync.db.DbSet;
import com.tinymission.tinysync.query.Query;

/**
 * A WebView that allows javascript to access a database context through a set of HTTP APIs.
 */
public class DbWebView extends WebView {

    static final String LogTag = "tinysync.DbWebView";
    static final String WrapperName = "tinysyncApi";

    public DbWebView(Context context) {
        super(context);

        setWebViewClient(new ViewClient());
        init();
    }

    public DbWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWebViewClient(new ViewClient());
        init();
    }

    private void init() {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);

    }

    private DbContext _dbContext = null;
    private ApiWrapper _wrapper;

    /**
     * Sets the database context used to access data in this web view.
     * This method MUST be called before the webview can be used.
     * @param context
     */
    public void setDbContext(DbContext context) {
        _dbContext = context;
        if (_wrapper != null) {
            _wrapper = null;
            removeJavascriptInterface(WrapperName);
        }
        _wrapper = new ApiWrapper(context);
        addJavascriptInterface(_wrapper, WrapperName);
    }

    private String _apiRoot = "/api/";

    public String getApiRoot() {
        return _apiRoot;
    }

    /**
     * The API root is the beginning of all API request URLs.
     */
    public void setApiRoot(String root) {
        _apiRoot = root;
        if (!root.endsWith("/"))
            _apiRoot += "/";
    }


    class ViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.v(LogTag, "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.v(LogTag, "onPageFinished: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.v(LogTag, "shouldOverrideUrlLoading: " + url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.v(LogTag, "onLoadResource: " + url);
        }

        public ApiResponse get(DbCollection collection, String rawQuery) {
            Query query = Query.fromParams(collection, rawQuery);
            try {
                DbSet results = collection.runQuery(query);
                return ApiResponse.success(collection.getTableName(), "Successfully executed query", results.toArray());
            }
            catch (Exception ex) {
                Log.w(LogTag, "Error executing get on " + collection.getTableName(), ex);
                return ApiResponse.error(collection.getTableName(), "Error executing query: " + ex.getMessage());
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.v(LogTag, "shouldInterceptRequest: " + url);

            if (url.startsWith(_apiRoot)) {
                url = url.replace(_apiRoot, "");
                String[] comps1 = Iterables.toArray(Splitter.on("/").omitEmptyStrings().split(url), String.class);
                if (comps1.length != 2)
                    return ApiResponse.error("unknown", "API requests must have the form /root/collection/method?query").toResponse();

                String collectionName = comps1[0];
                DbCollection collection = _dbContext.getCollection(collectionName);
                if (collection == null)
                    return ApiResponse.error(collectionName, "Unknown collection " + collectionName).toResponse();

                String[] comps2 = Iterables.toArray(Splitter.on("?").split(comps1[1]), String.class);
                if (comps2.length != 2)
                    return ApiResponse.error(collectionName, "Must specify a method and query").toResponse();
                String actionName = comps2[0];
                String params = comps2[1];
                if (actionName.equalsIgnoreCase("get"))
                    return get(collection, params).toResponse();
                else
                    return ApiResponse.error(collectionName, "Unknown action " + actionName).toResponse();
            }

            return super.shouldInterceptRequest(view, url);
        }
    }
}
