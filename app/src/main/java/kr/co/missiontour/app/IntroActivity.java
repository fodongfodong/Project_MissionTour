package kr.co.missiontour.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
//import com.wang.avi.AVLoadingIndicatorView;
import com.watosys.utils.Library.DisplayManager;
import com.watosys.utils.Library.ImageResizeCalculator;
import com.watosys.utils.Library.LocalMemory;
import com.watosys.utils.Library.WebUtilsCookieSet;
import com.watosys.utils.Library.WebUtilsVer2;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kr.co.missiontour.app.data.UserData;
import kr.co.missiontour.app.util.Global;
import kr.co.missiontour.app.util.PermissionManager;

public class IntroActivity extends Activity {

    private LodingHandler mCLodingHandler;//화면 전환 핸들러
    private Intent goIntent = null;
    private String intentLinkUrl = "";
    int REQUEST_CODE = 366;
    private AppUpdateManager appUpdateManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, 0);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_intro);
        Global.mUserData = new UserData();
        init();
        initSearchLocalMemory();

        goIntent = new Intent(IntroActivity.this, WebBrowserActivity.class);
        Intent data = getIntent();
        try {
            if (data.getStringExtra("linkurl") != null) {
                intentLinkUrl = data.getStringExtra("linkurl");
                goIntent.putExtra("linkurl", intentLinkUrl);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        AVLoadingIndicatorView avi = new AVLoadingIndicatorView(this);
//        avi.smoothToShow();


        if(mCLodingHandler!=null){
            mCLodingHandler.cancelMessage();//핸들 메시지 취소
        }

        //권한 체크
        if( !requestPermission(REQUEST_CODE_FOR_PERMISSION_START) ) {
            return;
        }


        if(Global.mUserData.localIsAutoLogin) {
            WebUtilsCookieSet.create(IntroActivity.this, Global.URL_ROOT);
            WebUtilsCookieSet.getInstance().cookieSyncON();
            autoLoginAct(Global.mUserData.user_id);
        }else {
            mCLodingHandler = new LodingHandler(2000);
        }

        //해시키 체크(카카오톡 등록용)
        System.out.println("getKeyHash :: "+getKeyHash(this));



        /*
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        // 업데이트를 체크하는데 사용되는 인텐트를 리턴한다.
        com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> { // appUpdateManager이 추가되는데 성공하면 발생하는 이벤트
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE // UpdateAvailability.UPDATE_AVAILABLE == 2 이면 앱 true
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) { // 허용된 타입의 앱 업데이트이면 실행 (AppUpdateType.IMMEDIATE || AppUpdateType.FLEXIBLE)
                // 업데이트가 가능하고, 상위 버전 코드의 앱이 존재하면 업데이트를 실행한다.
                requestUpdate(appUpdateInfo);
            } else {
                Log.e(Global.DEBUG_LOG, "Global.mUserData.localIsAutoLogin :: "+Global.mUserData.localIsAutoLogin);
                if(Global.mUserData.localIsAutoLogin) {
                    WebUtilsCookieSet.create(IntroActivity.this, Global.URL_ROOT);
                    WebUtilsCookieSet.getInstance().cookieSyncON();
                    autoLoginAct(Global.mUserData.user_id);
                }else {
                    mCLodingHandler = new LodingHandler(2000);
                }
            }
        });
         */

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(appUpdateManager != null) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    // If an in-app update is already running, resume the update.
                                    try {
                                        appUpdateManager.startUpdateFlowForResult(
                                                appUpdateInfo,
                                                AppUpdateType.IMMEDIATE,
                                                this,
                                                REQUEST_CODE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
    }

    // 업데이트 요청
    private void requestUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // 'getAppUpdateInfo()' 에 의해 리턴된 인텐트
                    appUpdateInfo,
                    // 'AppUpdateType.FLEXIBLE': 사용자에게 업데이트 여부를 물은 후 업데이트 실행 가능
                    // 'AppUpdateType.IMMEDIATE': 사용자가 수락해야만 하는 업데이트 창을 보여줌
                    AppUpdateType.IMMEDIATE,
                    // 현재 업데이트 요청을 만든 액티비티, 여기선 MainActivity.
                    this,
                    // onActivityResult 에서 사용될 REQUEST_CODE.
                    REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            Toast myToast = Toast.makeText(this.getApplicationContext(), "최신 버전으로 업데이트가 필요합니다.", Toast.LENGTH_SHORT);
            myToast.show();

            // 업데이트가 성공적으로 끝나지 않은 경우
            if (resultCode != RESULT_OK) {
                Log.d("CHECK", "Update flow failed! Result code: " + resultCode);
                // 업데이트가 취소되거나 실패하면 업데이트를 다시 요청할 수 있다.,
                // 업데이트 타입을 선택한다 (IMMEDIATE || FLEXIBLE).
                com.google.android.play.core.tasks.Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            // flexible한 업데이트를 위해서는 AppUpdateType.FLEXIBLE을 사용한다.
                            && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // 업데이트를 다시 요청한다.
                        requestUpdate(appUpdateInfo);
                    }
                });
            }
        }
    }

    //인트로 도중 back기능을 막기위함
    public void onBackPressed() {

    }


    //화면 전환 핸들러
    class LodingHandler extends Handler {
        public LodingHandler(int delay) {
            super.sendEmptyMessageDelayed(0, delay);
        }
        public void handleMessage(Message msg){
            startActivity(goIntent);
            finish();
            mCLodingHandler = null;
        }
        public void cancelMessage(){
            removeMessages(0);
            mCLodingHandler = null;
        }
    }

    private boolean requestPermission( int requestCode ) {

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        return (PermissionManager.getInstance(this).checkPermissions(requestCode, permissions) == PackageManager.PERMISSION_GRANTED);
    }


    //퍼미션 체크를 위한 requestCode 상수 정의
    private final static int REQUEST_PERMISSION_SETTINGS = 200;
    private final static int REQUEST_CODE_FOR_PERMISSION_START = 100;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case REQUEST_CODE_FOR_PERMISSION_START:
            {
                Log.d(Global.DEBUG_LOG, "[PermissionManager] [requestPermission] onRequestPermissions : " + grantResults);
                if (grantResults.length > 0) {

                    boolean bAllGreen = true;
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            bAllGreen = false;
                            break;
                        }
                    }

                    if (bAllGreen) {
                        Log.d(Global.DEBUG_LOG, "[PermissionManager] [requestPermission] 성공 : ");
                        mCLodingHandler = new LodingHandler(2000);
                    } else {
                        Log.d(Global.DEBUG_LOG, "[PermissionManager] [requestPermission] 거부 : ");

                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(IntroActivity.this);
                        alert_confirm.setMessage("앱 설정 화면에서 앱 권한을 모두 허용해 주셔야 서비스 이용이 가능합니다.")
                                .setCancelable(false)
                                .setPositiveButton("설정하기", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'YES'
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", IntroActivity.this.getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
                                    }
                                }).setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'No'
                                finish();
                            }
                        });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    }

                }
            }
            break;
        }
    }


    public void autoLoginAct(String id) {

        WebUtilsCookieSet.create(IntroActivity.this, Global.URL_ROOT);

        //통신부분
        String[] parametersKey = {
                "mb_id",
        };
        String[] parametersValue = {
                id,
        };

        String[] parametersHeaderKey = {
                "User-Agent"
        };

        String[] parametersHeaderValue = {
                Global.USER_AGENT_ADD_KEY
        };

        new WebUtilsVer2(IntroActivity.this)
                .setReqeustCommentStr("자동로그인")
                .enablePrintLog()
                .waitTimefinish(0)
                .replaceUserAgent(Global.USER_AGENT_ADD_KEY)
                .enableProtocalPOST()
                .enableProtocalCookie()
                .enableForceExcuteIsRunning()
                .enablePrintLogDetail()
                .setReqeustUrlStr(Global.URL_AUTO_LOGIN)
                .addHeaders(parametersHeaderKey, parametersHeaderValue)
                .addParameters(parametersKey, parametersValue)
                .setOnCallBackReturnResult(new WebUtilsVer2.CallBackReturnResult() {

                    @Override
                    public ProgressDialog catchLoadingAlertCustomOption(ProgressDialog progressDialog) {
                        return null;
                    }

                    @Override
                    public void isRunning() {
                        Log.d(Global.DEBUG_LOG, "isRunning");
                    }

                    @Override
                    public void ready(String[] returnParameters) {
                        Log.d(Global.DEBUG_LOG, "ready");
                    }

                    @Override
                    public void complete(String[] returnParameters, String resultStr) {
                        Log.d(Global.DEBUG_LOG, "resultStr : " + resultStr);
                        try {

                            JSONObject jsonData = new JSONObject(resultStr);
                            if (jsonData.getString("result").equals("success")) {
                                Global.mUserData.is_login = true;
                                mCLodingHandler = new LodingHandler(2000);
                            }else{
                                if (WebUtilsCookieSet.getInstance() != null) {
                                    WebUtilsCookieSet.getInstance().cookieSyncOFF();
                                }
                                mCLodingHandler = new LodingHandler(2000);
                                Global.mUserData.is_login = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (WebUtilsCookieSet.getInstance() != null) {
                                WebUtilsCookieSet.getInstance().cookieSyncOFF();
                            }
                            mCLodingHandler = new LodingHandler(2000);
                            Global.mUserData.is_login = false;
                        }
                    }

                    @Override
                    public void error(int code, String msg) {
                        Log.d(Global.DEBUG_LOG, "error code : " + code + " , msg : " + msg);
                    }
                })
                .request();
    }

    public void initSearchLocalMemory() {

        SharedPreferences pref = getSharedPreferences("thedushu", MODE_PRIVATE);

        if(pref.getString("app.first.run", "").isEmpty()) { //최초실행
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("app.first.run", "N");
            editor.putString("app.first.user_id", "");
            editor.putString("app.local.autologin", "N");
            editor.commit();

            Global.mUserData.localIsAutoLogin = pref.getString("app.local.autologin", "").equals("Y");
            Global.mUserData.user_id = pref.getString("app.first.user_id", "");
            Global.mUserData.is_login = false;


        }
        else
        {
            Global.mUserData.localIsAutoLogin = pref.getString("app.local.autologin", "").equals("Y");
            Global.mUserData.user_id = pref.getString("app.first.user_id", "");
            Global.mUserData.is_login = false;
            Global.mUserData.user_id = pref.getString("app.first.user_id", "");
            System.out.println("Global.mUserData.localIsAutoLogin :: "+Global.mUserData.localIsAutoLogin);
        }
    }

    public void init() {
        LocalMemory.create(this);
        ImageResizeCalculator.create(this, ImageResizeCalculator.ImageResizeCalculator_MODE.DEBUG);
        DisplayManager.Option(this, DisplayManager.DisplayManager_MODE.DEBUG);

    }

    public String getKeyHash(final Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (packageInfo == null)
                return null;

            for (Signature signature : packageInfo.signatures) {
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}