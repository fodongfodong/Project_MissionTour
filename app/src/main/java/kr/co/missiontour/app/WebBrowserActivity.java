package kr.co.missiontour.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.watosys.utils.Library.WebUtilsCookieSet;

import kr.co.missiontour.app.popup.BlankNewPopupDialog;
import kr.co.missiontour.app.util.CustomGPSManager;
import kr.co.missiontour.app.util.CustomWebChromeClient;
import kr.co.missiontour.app.util.CustomWebViewClient;
import kr.co.missiontour.app.util.CustomWebViewManager;
import kr.co.missiontour.app.util.CustomWebViewUtils;
import kr.co.missiontour.app.util.Global;


public class WebBrowserActivity extends AppCompatActivity{

    private static WebBrowserActivity instance = null;
    private String intentLinkUrl = "";
    public static WebBrowserActivity getInstance() {
        return instance;
    }
    //종료처리
    private Handler mCloseCancelHandler = null;
    private boolean GoBack = false;

    public CustomWebViewManager xmlWvWebBrowser = null;
    private CustomWebViewClient mCustomWebViewClient = null;
    private CustomWebChromeClient mCustomWebChromeClient = null;

    private BlankNewPopupDialog mBlankNewPopupDialog = null;
    private CustomGPSManager mCustomGPSManager = null;
    private boolean IS_GPS_ENABLE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //좌표 변수 초기화
        Global.mUserData.myLat = "";
        Global.mUserData.myLng = "";

        instance = this;

        //GPS 시작
        getLocation();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        overridePendingTransition(R.anim.slide_in_right, R.anim.zoom_out);
        setContentView(R.layout.activity_webbrowser);

        Intent datas = getIntent();
        try {
            if(datas.getStringExtra("linkurl") != null){
                intentLinkUrl = datas.getStringExtra("linkurl");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //백키 종료 취소
        mCloseCancelHandler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.what == 0)
                    GoBack = false;
            }
        };

        if(WebUtilsCookieSet.getInstance() != null){
            WebUtilsCookieSet.getInstance().cookieSyncON();
        }

        xmlWvWebBrowser = (CustomWebViewManager)findViewById(R.id.xml_webbrowser);
        xmlWvWebBrowser.setWebViewClient(mCustomWebViewClient = new CustomWebViewClient(this));
        xmlWvWebBrowser.setWebChromeClient(mCustomWebChromeClient = new CustomWebChromeClient(this));
        String userAgent = xmlWvWebBrowser.getSettings().getUserAgentString();
        xmlWvWebBrowser.setPutStringUserAgent(userAgent + " "+Global.USER_AGENT_ADD_KEY);

        String[] parametersKey = {

        };
        String[] parametersValue = {

        };

        CustomWebViewUtils.loadUrlForRequestProtocolType(xmlWvWebBrowser, Global.URL_ROOT, parametersKey, parametersValue, CustomWebViewUtils.WebViewUtils_protocol.GET);


        //override 함수
        mCustomWebViewClient.setOnCallBackReturnWebBrowser(new CustomWebViewClient.CallBackReturnWebBrowser() {
            @Override
            public void selectPicture(String returnUrl, int gallerymaxnumber, String... data ) {

            }
            @Override
            public void hideWebviewIntroLoadingImage() {
            }
            @Override
            public void historyBack() {
                if(xmlWvWebBrowser.canGoBack())
                    xmlWvWebBrowser.goBack();
            }
            @Override
            public void moveUrl(String linkurl) {
                if(xmlWvWebBrowser!=null)
                    xmlWvWebBrowser.loadUrl(linkurl);
            }
            @Override
            public void updatePushToken() {


            }
            @Override
            public void closePopup() {

            }
        });
        mCustomWebChromeClient.setOnCallBackReturnWebBrowser(new CustomWebChromeClient.CallBackReturnWebBrowser() {
            @Override
            public void startLoading() {

            }

            @Override
            public void stopLoading() {

            }

            @Override
            public void openPopupWindow(WebView view) {
                Log.d(Global.DEBUG_LOG, "[openPopupWindow]");
                mBlankNewPopupDialog = new BlankNewPopupDialog(WebBrowserActivity.this, view);
                mBlankNewPopupDialog.show();
            }

            @Override
            public void closePopupWindow(WebView view) {
                Log.d(Global.DEBUG_LOG, "[closePopupWindow]");
                mBlankNewPopupDialog.dismiss();
            }
        });

        //Intent 데이터가 있으면 페이지 이동
        if(!intentLinkUrl.equals("") && intentLinkUrl != null){
            xmlWvWebBrowser.loadUrl(intentLinkUrl);
        }
    }


    public void getLocation(){
        mCustomGPSManager = new CustomGPSManager(this, CustomGPSManager.CustomGPSManager_USE_TYPE.GoogleApi);
        mCustomGPSManager.setOnCallBackReturnResult(new CustomGPSManager.CallBackReturnResult() {
            @Override
            public void canUseGPS() {

            }

            @Override
            public void canNotUseGPS() {

            }

            @Override
            public void catchCurrentLocation(Location location) {

                //트레킹 스탑 한번만 사용할 경우
                mCustomGPSManager.stopGPSManager();
                Log.e(Global.DEBUG_LOG, "catchCurrentLocation lat :: "+location.getLatitude()+" lng :: "+location.getLongitude());

                if(location.getLatitude() != 0 && location.getLongitude() != 0) {
                    Global.mUserData.myLat = String.valueOf(location.getLatitude());
                    Global.mUserData.myLng = String.valueOf(location.getLongitude());
                }

//                updateGps(Global.mUserData.myLat, Global.mUserData.myLng);

                if(!IS_GPS_ENABLE){
                    Log.e(Global.DEBUG_LOG, "좌표 갱신");
                    IS_GPS_ENABLE = true;
                    xmlWvWebBrowser.loadUrl(Global.URL_ROOT+"?lat="+Global.mUserData.myLat+"&lng="+Global.mUserData.myLng);
                }
            }
            @Override
            public void catchLocationChanged(Location location) {
            }
        });
        mCustomGPSManager.excute();
    }
    public void closeApp() {
        if(!GoBack) {
            Toast.makeText(WebBrowserActivity.this, "뒤로가기를 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            GoBack = true;
            mCloseCancelHandler.sendEmptyMessageDelayed(0, 2000);//2초내
        } else {
            instance = null;
            mBlankNewPopupDialog = null;
            try {
                mCustomWebChromeClient.onHideCustomView();
            } catch (Exception e ) {
                e.printStackTrace();
            }
            xmlWvWebBrowser.clearCache(true);
            xmlWvWebBrowser.clearHistory();
            xmlWvWebBrowser.clearView();
            xmlWvWebBrowser.loadUrl("");
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        if(xmlWvWebBrowser.getUrl().equals(Global.URL_ROOT+"")
                || xmlWvWebBrowser.getUrl().equals(Global.URL_ROOT+"/")
                || xmlWvWebBrowser.getUrl().equals(Global.URL_ROOT+"?")
                || xmlWvWebBrowser.getUrl().contains(Global.URL_ROOT+"randing.php")
                || xmlWvWebBrowser.getUrl().contains(Global.URL_ROOT+"index.php")
                || xmlWvWebBrowser.getUrl().contains(Global.URL_ROOT+"?agency_lat=")
                ) {
            Log.d(Global.DEBUG_LOG, "메인페이지 인 경우 처리");

            closeApp();
        }
        else {
            Log.d(Global.DEBUG_LOG,"메인페이지 가 아닌 경우");

            if(xmlWvWebBrowser.getUrl().contains("agreement")){
                closeApp();
            }else {
                if (xmlWvWebBrowser.canGoBack() == false) {
                    xmlWvWebBrowser.loadUrl(Global.URL_ROOT);
                } else {
                    xmlWvWebBrowser.goBack();
                }
            }
        }
    }
    public void sendGps(){
        Log.e(Global.DEBUG_LOG, "GPS 송신--JS 함수 호출");
        xmlWvWebBrowser.loadUrl("javascript:resgps("+Global.mUserData.myLat+", "+Global.mUserData.myLng+")");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCustomGPSManager.stopGPSManager();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Global.DEBUG_LOG, "[onActivityResult] " + requestCode + " / " + resultCode);
        if ( requestCode == mCustomWebChromeClient.WEBVIEW_REQ_CODE || requestCode == mCustomWebChromeClient.WEBVIEW_REQ_CODE_PRE_LOLLIPOP ){
            // process webview request code
            mCustomWebChromeClient.onActivityResultProcess(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.webbrower_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


}
