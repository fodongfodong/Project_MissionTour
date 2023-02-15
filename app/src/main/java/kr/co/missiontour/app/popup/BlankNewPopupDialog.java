package kr.co.missiontour.app.popup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import kr.co.missiontour.app.R;
import kr.co.missiontour.app.util.CustomWebChromeClient;
import kr.co.missiontour.app.util.CustomWebViewClient;
import kr.co.missiontour.app.util.Global;


public class BlankNewPopupDialog extends Dialog {

    private Context mContext = null;
    private FrameLayout xmlFvWebBrowserContainer = null;
    private WebView targetWebView = null;
    private CustomWebViewClient mCustomWebViewClient = null;
    private CustomWebChromeClient mCustomWebChromeClient = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        lpWindow.windowAnimations = R.style.CustomDialogAnimation;
        getWindow().setAttributes(lpWindow);
         
        setContentView(R.layout.layout_dialog_layer);

        String userAgent = targetWebView.getSettings().getUserAgentString();
        targetWebView.getSettings().setUserAgentString(userAgent+" "+Global.USER_AGENT_ADD_KEY);
        //targetWebView.setWebViewClient(mCustomWebViewClient = new CustomWebViewClient(mContext));
        targetWebView.setWebViewClient(new WebViewClient());
        targetWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg)
            {

                final WebView childView = new WebView(view.getContext());
                final WebSettings settings = childView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setSupportMultipleWindows(true);
                childView.setWebChromeClient(this);
                childView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                });


                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(childView);
                resultMsg.sendToTarget();

                return true;
            };

            @Override
            public void onCloseWindow(WebView window) {
                window.setVisibility(View.GONE);
                dismiss();
            }
        });
        mCustomWebViewClient.setOnCallBackReturnWebBrowser(new CustomWebViewClient.CallBackReturnWebBrowser() {
            @Override
            public void selectPicture(String returnUrl, int gallerymaxnumber, String... data ) {

            }
            @Override
            public void hideWebviewIntroLoadingImage() {
            }
            @Override
            public void historyBack() {
            }
            @Override
            public void moveUrl(String linkurl) {
            }
            @Override
            public void updatePushToken() {


            }
            @Override
            public void closePopup() {

            }
        });
        xmlFvWebBrowserContainer = (FrameLayout) findViewById(R.id.xml_webbrowser_container);
        xmlFvWebBrowserContainer.addView(targetWebView);

        TextView xmlTxtDialogLabel = (TextView) findViewById(R.id.xml_webbrowser_dialog_label);
        //xmlTxtDialogLabel.setText("POPUP");

        Button xmlBtnDialogClose = (Button) findViewById(R.id.xml_webbrowser_dialog_btn_close);
        xmlBtnDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                targetWebView.loadUrl("javascript:window.close()");
            }
        });

    }
	
	public BlankNewPopupDialog(Context context, WebView v) {
		//super(context);
        super(context, android.R.style.Theme_Translucent_NoTitleBar);//투명처리
        mContext = context;
        targetWebView = v;
	}

    @Override
    public void dismiss() {
        try {
            targetWebView.clearCache(true);
            targetWebView.clearHistory();
            targetWebView.clearView();
            targetWebView.loadUrl("");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            xmlFvWebBrowserContainer.removeView(targetWebView);
        }
        super.dismiss();

    }

    @Override
    public void onBackPressed(){
        Log.i("call", "onBackPressed");
        if (targetWebView.canGoBack()) {
            targetWebView.goBack();
            Log.d("onBackPressed","canGoBack");
        } else {
            Log.d("onBackPressed","canNotGoBack");
            targetWebView.loadUrl("javascript:window.close()");
        }
    }

}
