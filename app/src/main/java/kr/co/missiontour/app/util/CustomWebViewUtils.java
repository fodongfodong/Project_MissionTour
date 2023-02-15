package kr.co.missiontour.app.util;

import android.util.Log;
import android.webkit.WebView;

import java.net.URLEncoder;

public class CustomWebViewUtils {

    public static enum WebViewUtils_protocol {
        POST,
        GET,
    }

    public static void loadUrlForRequestProtocolType(WebView mWebBrowser, String domain, String[] parametersKey, String[] parametersValue, WebViewUtils_protocol requestType) {

        String querystring = "";

        if(parametersKey != null) {
            for(int i = 0; i < parametersKey.length; ++i) {
                if(parametersKey[i] != null && parametersValue[i] != null) {
                    if(requestType == WebViewUtils_protocol.POST)
                    {
                        try {
                            querystring += "&"+parametersKey[i]+"=" + URLEncoder.encode(parametersValue[i], "UTF-8");
                        } catch (Exception e) {
                            //e.printStackTrace();
                            querystring += "&"+parametersKey[i]+"=" + parametersValue[i];
                        }
                    }
                    else if(requestType == WebViewUtils_protocol.GET)
                    {
                        querystring += "&"+parametersKey[i]+"=" + parametersValue[i];
                    }

                }
            }
        }

        //url 필터
        if(!domain.contains("?"))
            domain += "?";


        if(requestType == WebViewUtils_protocol.POST)
        {
            mWebBrowser.postUrl(domain,querystring.getBytes());
        }
        else if(requestType == WebViewUtils_protocol.GET)
        {
            mWebBrowser.loadUrl(domain+querystring);
        }

        Log.d(Global.DEBUG_LOG, "CustomWebViewUtils --------------------------------------");
        Log.d(Global.DEBUG_LOG, "[ Full URL ] " + domain + querystring);
        Log.d(Global.DEBUG_LOG, "[ 호출 URL ] " + domain);
        Log.d(Global.DEBUG_LOG, "[ 파라미터 값 ] " + querystring);
        Log.d(Global.DEBUG_LOG, "[ 요청 타입 ] " + requestType);
        Log.d(Global.DEBUG_LOG, "---------------------------------------------------------");

    }



    public static void loadUrlForRequestProtocolTypeAndPutHeader(WebView mWebBrowser, String domain, String[] parametersKey, String[] parametersValue, WebViewUtils_protocol requestType, String[] putHeadersKey, String[] putHeadersValue) {

        String querystring = "";

        if(parametersKey != null) {
            for(int i = 0; i < parametersKey.length; ++i) {
                if(parametersKey[i] != null && parametersValue[i] != null) {
                    if(requestType == WebViewUtils_protocol.POST)
                    {
                        try {
                            querystring += "&"+parametersKey[i]+"=" + URLEncoder.encode(parametersValue[i], "UTF-8");
                        } catch (Exception e) {
                            //e.printStackTrace();
                            querystring += "&"+parametersKey[i]+"=" + parametersValue[i];
                        }
                    }
                    else if(requestType == WebViewUtils_protocol.GET)
                    {
                        querystring += "&"+parametersKey[i]+"=" + parametersValue[i];
                    }

                }
            }
        }


        String headerstring = "";


        //url 필터
        if(!domain.contains("?"))
            domain += "?";




            //mWebBrowser.postUrl(domain,querystring.getBytes());
        }



}
