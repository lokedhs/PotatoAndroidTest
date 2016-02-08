package com.dhsdevelopments.potato;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.dhsdevelopments.potato.initial.PotatoActivity;

public class WebLoginActivity extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_web_login );

        WebView webView = (WebView)findViewById( R.id.login_webview );
        webView.setWebViewClient( new Client() );

        webView.loadUrl( PotatoApplication.SERVER_URL_PREFIX + "login?m=1" );
    }

    private class Client extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading( WebView view, String url ) {
            Uri uri = Uri.parse( url );
            if( uri.getScheme().equals( "potato" ) && uri.getHost().equals( "authenticated" ) ) {
                String key = uri.getQueryParameter( "key" );
                if( key == null || key.equals( "" ) ) {
                    throw new RuntimeException( "did not find key parameter" );
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( WebLoginActivity.this );
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString( getString( R.string.pref_apikey ), key );
                editor.apply();

                Intent intent = new Intent( WebLoginActivity.this, PotatoActivity.class );
                startActivity( intent );
                finish();

                return true;
            }
            else {
                return super.shouldOverrideUrlLoading( view, url );
            }
        }
    }
}
