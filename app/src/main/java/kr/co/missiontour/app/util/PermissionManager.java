package kr.co.missiontour.app.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by userpc on 2016-01-13.
 */
public class PermissionManager {


    private static PermissionManager instance = null;

    private Activity mActivity = null;

    public static PermissionManager getInstance(Activity activity) {

        if( instance == null ) {
            instance = new PermissionManager(activity);
        } else {
            instance.setActivity(activity);
        }
        return instance;
    }

    private PermissionManager( Activity activity ) {
        mActivity = activity;
    }

    private void setActivity( Activity activity ) {
        mActivity = activity;
    }

    public int checkPermissions(int requestCode, String[] permissions ) {

        int retVal = PackageManager.PERMISSION_DENIED;

        String[] requiredPermissions = getRequriedPermission(permissions);

        if( requiredPermissions.length > 0  ) {


            String[] checkAllowedPermissions = shouldShowRequestPermissionRationale(requiredPermissions);

            ActivityCompat.requestPermissions(mActivity,
                    checkAllowedPermissions,
                    requestCode);

        } else {
            retVal = PackageManager.PERMISSION_GRANTED;
        }

        return retVal;
    }


    public int checkPermission(int requestCode, String permission ) {

        int retVal = PackageManager.PERMISSION_DENIED;

        if( ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED ) {

            if( ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission) ) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{permission},
                        requestCode);
            } else {

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{ permission },
                        requestCode);
            }

        } else {
            retVal = PackageManager.PERMISSION_GRANTED;
        }

        return retVal;
    }


    //1 단계 체크
    public String[] getRequriedPermission(String[] permissions) {

        List<String> requiredPermissions = new ArrayList<String>();

        for (String permission:permissions) {

            if( ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED ) {
                Log.d(Global.DEBUG_LOG, "[PermissionManager] getRequriedPermission");
                requiredPermissions.add(permission);
            }
        }

        return requiredPermissions.toArray(new String[requiredPermissions.size()]);
    }

    //2단계 shouldShowRequestPermissionRationale 팝업 요청?
    public String[] shouldShowRequestPermissionRationale(String[] requiredPermissions) {

        for (String permission:requiredPermissions) {

            if( ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission) ) {
                Log.d(Global.DEBUG_LOG, "[PermissionManager] shouldShowRequestPermissionRationale");
            }
        }

        return requiredPermissions;
    }

}

