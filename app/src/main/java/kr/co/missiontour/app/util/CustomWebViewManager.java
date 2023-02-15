package kr.co.missiontour.app.util;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.net.URLDecoder;

import kr.co.missiontour.app.R;

public class CustomWebViewManager extends WebView {

    Handler handler = null;
    private Context mContext = null;

    public CustomWebViewManager(Context context) {
        super(context);
        init(context);
    }

    public CustomWebViewManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomWebViewManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    
    
    private void init(Context context) {

        mContext = context;
        handler = new Handler();

        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.getSettings().setLoadsImagesAutomatically(true);
        this.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        this.getSettings().setAppCacheEnabled(true);
        this.getSettings().setSaveFormData(true); // 비밀번호저장 사용
        this.getSettings().setSavePassword(true); // 폼 데이터 저장(자동완성) 사용

        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setDisplayZoomControls(false);
        this.getSettings().setUseWideViewPort(false);
        this.getSettings().setLoadWithOverviewMode(false);
        //this.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        //this.getSettings().setLightTouchEnabled(true);
        if (Build.VERSION.SDK_INT > 18) {
            this.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        this.getSettings().setSupportMultipleWindows(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.getSettings().setAllowContentAccess(true);
        //this.getSettings().setAllowFileAccess(true);
        this.getSettings().setLoadsImagesAutomatically(true);

        //상하단 오버스크롤 영역 처리
        //this.setOverScrollMode(View.OVER_SCROLL_NEVER);

        //스크롤바 강제 설정
        this.setVerticalScrollBarEnabled(false);
        this.setVerticalFadingEdgeEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.setHorizontalFadingEdgeEnabled(false);
        this.addJavascriptInterface(new AndroidBridge(), "android");

        //https SSL 인증서 관련
        this.getSettings().setDomStorageEnabled(true);


        //웹뷰 속도 개선
        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11){
            // older android version, disable hardware acceleration
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            this.getSettings().setTextZoom(100);
        }

        this.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                Log.e("CHECK :: ", "onDownloadStart");
                Log.e("CHECK :: ", url);
                Log.e("CHECK :: ", userAgent);

                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    //request.addRequestHeader("User-Agent", userAgent);
                    String cookie = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("Cookie", cookie);
                    request.setDescription("Downloading file");
                    contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8"); //디코딩
                    String fileName = contentDisposition.replace("attachment; filename=", "");
                    fileName = fileName.replaceAll("\"", "");
                    request.setTitle(fileName);
                    request.setAllowedOverMetered(true);
                    request.setAllowedOverRoaming(true);
                    request.allowScanningByMediaScanner();
                    request.setAllowedOverMetered(true);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    DownloadManager dm = (DownloadManager) mContext.getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(mContext, mContext.getString(R.string.str_start_download), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private class AndroidBridge {

        @JavascriptInterface
        public void callDial(final String num, final String code) {
            handler.post(new Runnable() {
                public void run() {
                    //전화걸기
                    String tel = num;
                }
            });
        }

    }

    public void setPutStringUserAgent(String value) {
        //user-agent 내용 추가
        System.out.println("setPutStringUserAgent :: "+value);
        String userAgent = this.getSettings().getUserAgentString();
        this.getSettings().setUserAgentString(value);
    }

}
