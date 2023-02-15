package kr.co.missiontour.app.util;

import kr.co.missiontour.app.data.UserData;

public class Global {

    public static final String DEBUG_LOG = "missiontour";
//    public static String URL_ROOT = "https://app.missiontour.kr/api/auto_login.php?&mb_id=whdtjr2529";
    public static String URL_ROOT = "https://app.missiontour.kr/";
    public static String URL_AUTO_LOGIN = URL_ROOT+"api/autologin.php";
    public static String URL_GPS_SEND = URL_ROOT+"api/get.gps.php";
    public static String USER_AGENT_ADD_KEY = "missiontour";
    public static UserData mUserData = null;
    public static String device_uuid = "";
    public static int notificationCount = 0;
    public static final int NOTIFY_ID = 818974224; //NOTI 커스텀ID
    // 1) 자동로그인 확인
    // 2)


    //어플리케이션 설정 값ad
    public static boolean IS_USE_PROCESS_SHOW_TOAST_LOG = false; //알림 로그 출력 활성화 여부
    public static boolean IS_USE_PROCESS_SHOW_CONSOLE_TOAST_LOG = false;//웹 콘솔 알림 로그 출력 활성화 여부
    public static boolean IS_USE_PROCESS_SHOW_CONSOLE_LOG = true; //웹 콘솔 일반 로그 출력 활성화 여부
    public static boolean IS_USE_PROCESS_LOADING_ANIMATION_BAR = false; //로딩바 기능 활성화 여부
}
