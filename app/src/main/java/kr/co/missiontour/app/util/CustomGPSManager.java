package kr.co.missiontour.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class CustomGPSManager {

    /*

    && Manifest 설정

    <!-- GPS -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />


    && gradle 설정
    compile 'com.google.android.gms:play-services:7.5.0'
    compile 'com.google.android.gms:play-services-location:6.5.87'

    */

    //instance
    private CallBackReturnResult mCallBackReturnResult = null;
    public interface CallBackReturnResult {
        public void canUseGPS();
        public void canNotUseGPS();
        public void catchCurrentLocation(Location location);
        public void catchLocationChanged(Location location);
    }
    public void setOnCallBackReturnResult (CallBackReturnResult mCallBackReturnResult) {
        this.mCallBackReturnResult = mCallBackReturnResult;
    }


    public enum CustomGPSManager_USE_TYPE {
        Native,GoogleApi
    }
    private CustomGPSManager_USE_TYPE mCustomGPSManager_USE_TYPE = CustomGPSManager_USE_TYPE.Native;

    //content
    private final String LOG_TAG = "CustomGPSManager";
    private Context mContext = null;
    private Location mLocation = null;

    //googleApi
    private final int REQUEST_CHECK_SETTINGS_GOOGLE_API = 2351;
    private GoogleApiClient mGoogleApiClient = null;
    private LocationSettingsRequest.Builder mBuilder = null;
    private LocationRequest mLocationRequest = null;

    //native
    private final int REQUEST_CHECK_SETTINGS_NATIVE = 4589;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;// 최소 GPS 정보 업데이트 거리 10미터
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;// 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
    private static final long MIN_TIME_BW_UPDATES = 5000 * 10 * 1;// 최소 GPS 정보 업데이트 시간 밀리세컨이므로 5초로 갱신해봄..
    private LocationManager mLocationManager = null;



//    private boolean checkGPSPermission() {
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                ) {
//            return false;
//        } else {
//            return true;
//        }
//    }


    public double getLatitude(){
        if(mLocation != null){
            return mLocation.getLatitude();
        }
        return 0;
    }

    public double getLongitude(){
        if(mLocation != null){
            return mLocation.getLongitude();
        }
        return 0;
    }

    public void startGPSManager() {
        Log.d(LOG_TAG, "[onStart]");
        if(mCustomGPSManager_USE_TYPE == CustomGPSManager_USE_TYPE.Native) {

        } else if(mCustomGPSManager_USE_TYPE ==  CustomGPSManager_USE_TYPE.GoogleApi) {
            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();
        }
    }

    public void stopGPSManager() {
        Log.d(LOG_TAG, "[onStop]");
        if(mCustomGPSManager_USE_TYPE == CustomGPSManager_USE_TYPE.Native) {
            try {
                mLocationManager.removeUpdates(mNativeLocationListener);
            } catch (SecurityException se) {
                se.printStackTrace();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } else if(mCustomGPSManager_USE_TYPE ==  CustomGPSManager_USE_TYPE.GoogleApi) {
            if (mGoogleApiClient != null) {
                if(mGoogleApiClient.isConnected())
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                mGoogleApiClient.unregisterConnectionCallbacks(mConnectionCallbacks);
                mGoogleApiClient.unregisterConnectionFailedListener(mOnConnectionFailedListener);
                mGoogleApiClient.disconnect();
            }
        }
    }


    public void reStartGPSManager() {
        if(mCustomGPSManager_USE_TYPE == CustomGPSManager_USE_TYPE.Native) {
            startServiceNativeGPS();
        } else if(mCustomGPSManager_USE_TYPE ==  CustomGPSManager_USE_TYPE.GoogleApi) {
            startServiceGoogleApiGPS();
        }
    }



    public CustomGPSManager(Context context, CustomGPSManager_USE_TYPE mCustomGPSManager_USE_TYPE) {
        mContext = context;
        this.mCustomGPSManager_USE_TYPE = mCustomGPSManager_USE_TYPE;

        if(mCustomGPSManager_USE_TYPE == CustomGPSManager_USE_TYPE.Native)
        {

        }
        else if(mCustomGPSManager_USE_TYPE ==  CustomGPSManager_USE_TYPE.GoogleApi)
        {

            //-----------------------------------------------
            // GoogleApiClient
            //-----------------------------------------------
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mOnConnectionFailedListener)
                    .build();
            mGoogleApiClient.connect();
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//위치 조회 강도
            /*
                PRIORITY_HIGH_ACCURACY : 배터리소모를 고려하지 않으며 정확도를 최우선으로 고려
                PRIORITY_LOW_POWER : 저전력을 고려하며 정확도가 떨어짐
                PRIORITY_NO_POWER : 추가적인 배터리 소모없이 위치정보 획득
                PRIORITY_BALANCED_POWER_ACCURACY : 전력과 정확도의 밸런스를 고려. 정확도 다소 높음
            */
            mLocationRequest.setInterval(1 * 1000);//위치가 업데이트 되는 주기
            mLocationRequest.setFastestInterval(30 * 1000);//위치 획득 후 업데이트 되는 주기
            mBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

            // **************************
            mBuilder.setAlwaysShow(true); // 위치서비스 필수 여부 (예,아니오 만 출력 / false 인 경우 '나중에' 도 출력)
            mBuilder.setNeedBle(true); //블루투스 스캔 모드 활성화 여부?
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mBuilder.build());
            result.setResultCallback(mResultCallback);

            startServiceGoogleApiGPS();

        }

    }

    public void excute() {
        reStartGPSManager();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(LOG_TAG, "[onActivityResult][catch]");

        switch (requestCode)
        {
            case REQUEST_CHECK_SETTINGS_NATIVE :
            {
                if(isCanUseNativeGPS()) {
                    startServiceNativeGPS();
                } else {
                    mCallBackReturnResult.canNotUseGPS();
                }
            }
            break;
            case REQUEST_CHECK_SETTINGS_GOOGLE_API :
            {
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                        // All required changes were successfully made
                        Log.d(LOG_TAG, "[승 인 허용] " + states.toString());
                        mCallBackReturnResult.canUseGPS();
                        /*
                        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        }
                        */
                    }
                    break;
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Log.d(LOG_TAG, "[승인 거부]");
                        mCallBackReturnResult.canNotUseGPS();
                    }
                    break;
                }//end switch
            }
            break;
        }//end switch

    }



    //--------------------------------------------------------------------
    // Native
    //--------------------------------------------------------------------
    android.location.LocationListener mNativeLocationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "[onLocationChanged] " + location.toString());
            mLocation = location;
            mCallBackReturnResult.catchLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(LOG_TAG,"[onStatusChanged]");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(LOG_TAG,"[onProviderEnabled]");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(LOG_TAG,"[onProviderDisabled]");
        }
    };


    public boolean isCanUseNativeGPS() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGPSEnabled && !isNetworkEnabled){
            return false;
        } else {
            return true;
        }
    }


    private void startServiceNativeGPS() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGPSEnabled && !isNetworkEnabled){
            Log.d(LOG_TAG, "[startServiceNativeGPS] 위치서비스 미작동 상태");
            //mCallBackReturnResult.canNotUseGPS();
            new AlertDialog.Builder(mContext)
                    .setMessage("GPS가 활성화 되어 있지 않습니다.")
                    .setPositiveButton("설정하기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //GPS설정 화면으로 이동
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //startActivity(intent);
                            ((Activity) mContext).startActivityForResult(intent, REQUEST_CHECK_SETTINGS_NATIVE);
                        }
                    })
                    .setNegativeButton("사용안함", null)
                    .show();
        } else {
            Log.d(LOG_TAG,"[startServiceNativeGPS] 위치서비스 작동 상태");
            mCallBackReturnResult.canUseGPS();
            if (isNetworkEnabled) {
                runNetworkProvider();
            } else if (isGPSEnabled) {
                runGPSProvider();
            }
        }
    }

    private void runNetworkProvider() {
        Log.d(LOG_TAG,"[runNetworkProvider] 위치서비스 요청");
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mNativeLocationListener);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                // 위도 경도 저장
                mLocation = location;
                if(mCallBackReturnResult!=null)
                    mCallBackReturnResult.catchCurrentLocation(location);
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runGPSProvider() {
        Log.d(LOG_TAG,"[runGPSProvider] 위치서비스 요청");
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    mNativeLocationListener);
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                // 위도 경도 저장
                mLocation = location;
                if(mCallBackReturnResult!=null)
                    mCallBackReturnResult.catchCurrentLocation(location);
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //--------------------------------------------------------------------
    // GoogleApi
    //--------------------------------------------------------------------
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "[onLocationChanged] " + location.toString());
            mLocation = location;
            mCallBackReturnResult.catchCurrentLocation(location);
        }
    };
    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            // 구글 드라이브와 구글+ 중 연결 성공
            if(bundle!=null)
                Log.d(LOG_TAG, "[onConnected][OK] Google Play Service ");

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);

        }
        @Override
        public void onConnectionSuspended(int i) {
            Log.d(LOG_TAG,"[onConnectionSuspended][PAUSE] Google Play Service : "+i);
        }
    };
    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            // 구글 드라이브와 구글+ 중 적어도 하나의 서비스가 연결이 되지 않을 경우.
            Log.d(LOG_TAG,"[onConnectionFailed][FAIL] Google Play Service");
            try {
                connectionResult.startResolutionForResult((Activity)mContext, REQUEST_CHECK_SETTINGS_GOOGLE_API);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    };
    ResultCallback<LocationSettingsResult> mResultCallback = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult locationSettingsResult) {
            final Status status = locationSettingsResult.getStatus();
            final LocationSettingsStates state = locationSettingsResult.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                //초기 로딩시 위치서비스에 승인여부를 이곳에서 확인함
                //아래 항목들은 onActivityResult 에서 캐치되어 일괄 처리됨. 별도 처리 필요 없음.
                case LocationSettingsStatusCodes.SUCCESS:
                    //위치서비스를 승인 한 경우
                    Log.d(LOG_TAG, "[ResultCallback][onResult][위치서비스가 승인된 상태]");
                    mCallBackReturnResult.canUseGPS();
                    //Log.d(LOG_TAG, "[locationSettingsResult] "+locationSettingsResult.toString());
                    //Log.d(LOG_TAG, "[state] "+state.toString());
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    //위치서비스를 승인하지 않는 경우
                    Log.d(LOG_TAG, "[ResultCallback][onResult][위치서비스가 승인되지 않은 상태]");
                    //Log.d(LOG_TAG, "[locationSettingsResult] "+locationSettingsResult.toString());
                    //Log.d(LOG_TAG, "[state] "+state.toString());
                    try {
                        // Show the dialog by calling
                        // startResolutionForResult(),
                        // and check the result in onActivityResult().
                        mCallBackReturnResult.canNotUseGPS();
                        status.startResolutionForResult(((Activity)mContext), REQUEST_CHECK_SETTINGS_GOOGLE_API);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have
                    // no way to fix the
                    // settings so we won't show the dialog.
                    Log.d(LOG_TAG, "[ResultCallback][onResult][위치서비스를 사용할 수 없는 상태");
                    break;
            }
        }
    };

    private void startServiceGoogleApiGPS() {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mBuilder.build());
        result.setResultCallback(mResultCallback);
    }

    //--------------------------------------------------------------------


}
