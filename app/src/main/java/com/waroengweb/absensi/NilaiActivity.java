package com.waroengweb.absensi;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.waroengweb.absensi.app.NetworkHelper;
import com.waroengweb.absensi.helpers.Session;

public class NilaiActivity extends AppCompatActivity {

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (NetworkHelper.isNetworkAvailable(this)){
            if (NetworkHelper.isInternetAvailable()){
                webView = new WebView(this);
                setContentView(webView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setDisplayZoomControls(true);
                webView.setWebViewClient(new NoErrorWebViewClient());
                webView.loadUrl("https://siap.kerincikab.go.id/api/get-presensi?nip="+ Session.getNip(this));
            } else {
                Toast.makeText(this,"Tidak Ada jaringan Internet",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"Tidak Ada jaringan Internet",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    class NoErrorWebViewClient extends WebViewClient {
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e(String.valueOf(errorCode), description);

            // API level 5: WebViewClient.ERROR_HOST_LOOKUP
            if (errorCode == -2) {
                String summary = "<html><body style='background: black;'><p style='color: red;'>Unable to load information. Please check if your network connection is working properly or try again later.</p></body></html>";
                view.loadData(summary, "text/html", null);
                return;
            }

            // Default behaviour
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}