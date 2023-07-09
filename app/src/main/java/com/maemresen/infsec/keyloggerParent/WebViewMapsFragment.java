package com.maemresen.infsec.keyloggerParent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WebViewMapsFragment extends Fragment {
    private WebView webView;
    private String url;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_webview, container, false);
        
        webView = rootView.findViewById(R.id.locationWebView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        
        Bundle arguments = getArguments();
        if (arguments != null) {
            url = arguments.getString("url");
            if (url != null) {
                webView.loadUrl(url);
            }
        }
        
        return rootView;
    }
    
    public void setUrl(String url) {
        this.url = url;
        if (webView != null) {
            webView.loadUrl(url);
        }
    }
}