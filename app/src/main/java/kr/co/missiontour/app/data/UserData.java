package kr.co.missiontour.app.data;

public class UserData {

    public String user_id;
    public String myLat;
    public String myLng;
    public boolean is_login;
    public boolean localIsAutoLogin;

    //clear
    public void clear() {
        myLat = "";
        myLng = "";
        user_id = "";
        localIsAutoLogin = false;
        is_login = false;
    }

}
