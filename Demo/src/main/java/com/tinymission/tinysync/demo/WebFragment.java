package com.tinymission.tinysync.demo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinymission.tinysync.demo.models.DemoContext;
import com.tinymission.tinysync.web.DbWebView;


/**
 *
 */
public class WebFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WebFragment.
     */
    public static WebFragment newInstance() {
        WebFragment fragment = new WebFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public WebFragment() {
        // Required empty public constructor
    }

    private DbWebView _webview;
    private DemoContext _dbContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.web, container, false);

        _webview = (DbWebView)view.findViewById(R.id.webview);
        _dbContext = new DemoContext(getActivity());
        _webview.setDbContext(_dbContext);

        _webview.loadUrl("file:///android_asset/index.html");

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
