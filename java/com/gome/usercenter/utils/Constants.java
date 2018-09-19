package com.gome.usercenter.utils;
import android.os.SystemProperties;

/**
 * Created by jianfeng.xue on 2017/7/6.
 */

public class Constants {
    public static final String VERSION = "inner_20171019";
    public static final String TAG_HEADER = "GMOS_UserCenter-->";

    public static final String GOME_BASE_URL = getBaseUrl();
    private static final String BASE_PRE_URL = "https://www.prep.gomefota.com/iuvfota-web/"; //预发环境
    private static final String BASE_PRODUCT_URL = "https://www.gomefota.com/iuvfota-web/"; //生产环境
    private static String getBaseUrl(){
        if (SystemProperties.getBoolean("persist.sys.inPreTestNet", false)){
            return BASE_PRE_URL;
        }else {
            return BASE_PRODUCT_URL;
        }
    }
    public static final String GUIDELINE_ITEM_URL = "http://gome.gomefota.com/";
    /* communicate with wf api server start*/
    public static final String WF_API_URL = getWfURL();
    public static final String WF_API_URL_PRO = "http://service.gometech.com.cn/";         //维服生产环境
    public static final String WF_API_URL_PRE = "http://47.90.46.65:8820/";                //维服预发环境
    public static final String QINIU_BASE_URL = "http://gome.gomefota.com/";
    public static String getWfURL(){
        if (SystemProperties.getBoolean("persist.sys.inPreTestNet", false)){
            return WF_API_URL_PRE;        //维服预发环境
        }else {
            return WF_API_URL_PRO;        //维服生产环境
        }
    }


    public static final String URL_AREA_LIST = "api/area!list.action";
    public static final String SIGN_AREA_LIST = "F31D20463B2A2FC554B75BE7B0E244CF";

    public static final String URL_NETWORK_QUERY = "api/area!orgs.action";
    public static final String URL_REPAIR_QUERY = "api/repair!progress.action";
    public static final String URL_RESERVE_QUERY = "api/reserve!query.action";
    public static final String URL_RESERVE_APPLY = "api/reserve!apply.action";
    public static final String URL_RESERVE_CANCEL = "api/reserve!cancel.action";

    public static String WF_RESPONSE_CODE_SUCCESS = "200";
    public static String WF_RESPONSE_CODE_ERROR = "500";
    /* communicate with wf api server end*/

    public static final String BUILD_PRODUCT_MODEL = "ro.product.model";
    public static final String BUILD_NUMBER_EXTERNAL_RELEASE = "ro.build.display.id";
    public static final String BUILD_NUMBER_INTERNAL_RELEASE = "ro.build.version.incremental";
    public static final String BUILD_INFO_DEFAULT_VALUE = "Unknown";

    /* html file path*/
    public static String PATH_USER_AGREEMENT = "User_Center_User_Agreement";
    public static String PATH_PRIVACY_POLICY = "Gome_Privacy_Policy";
    public static String PATH_SERVICE_POLICY = "Gome_Service_Policy";
    public static String PATH_DISCLAIMER_WARRANTY = "Disclaimer_of_Warranty";
    public static String PATH_APPLY_FOR_BETA_VERSION = "apply_for_beta_version";

    /* file for application to cache some data */
    public static String FILE_CACHE_FEEDBACK_HISTORY = "feedback_history";
    public static String FILE_CACHE_AFTER_SALE_ADDRESS = "after_sale_address";

    /* communicate with server start*/
    public static String RESPONSE_CODE_TIMEOUT = "-1";
    public static String RESPONSE_CODE_NO_NETWORK = "0";
    public static String RESPONSE_CODE_SUCCESS = "2000";
    public static String RESPONSE_CODE_FAIL = "5000";
    public static String RESPONSE_CODE_WRONG_PARAMS = "4000";
    public static String RESPONSE_CODE_NO_UPDATE = "4003";
    public static String RESPONSE_CODE_NOT_INVITE_IMEI = "4004";
    public static String RESPONSE_CODE_PLEASE_LOGIN = "4005";
    public static String RESPONSE_CODE_TOKEN_INVAILD = "4006";
    public static String RESPONSE_CODE_UPLOAD_PICTURE_FAIL = "4008";
    public static String RESPONSE_CODE_NOT_LOGIN = "4017";

    /* preference key */
    public static final String PREF_AGREEMENT_DIALOG_HAD_SHOWN = "PREF_AGREEMENT_DIALOG_HAD_SHOWN";
    public static final String PREF_GOME_ACCOUNT_TOKEN = "PREF_GOME_ACCOUNT_TOKEN";
    public static final String PREF_GOME_ACCOUNT_NICKNAME = "PREF_GOME_ACCOUNT_NICKNAME";
    public static final String PREF_GOME_ACCOUNT_MAILADDRESS= "PREF_GOME_ACCOUNT_MAILADDRESS";
    public static final String PREF_GOME_ACCOUNT_PHONENO = "PREF_GOME_ACCOUNT_PHONENO";
    public static final String PREF_GOME_ACCOUNT_ID = "PREF_GOME_ACCOUNT_ID";
    public static final String PREF_GOME_ACCOUNT_LOCALAVATAR = "PREF_GOME_ACCOUNT_LOCALAVATAR";

    /* action key */
    public static final String ACTION_START_ACTIVITY_LOGIN = "ACTION_START_ACTIVITY_LOGIN";
    public static final String ACTION_GET_ACTIVITY_LOGIN_STATE = "ACTION_GET_ACTIVITY_LOGIN_STATE";
    public static final String ACTION_GOME_ACCOUNT_LOGOUT = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_LOGOUT";
    public static final String ACTION_GOME_ACCOUNT_LOGIN = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_LOGIN";   //发送登录 成功广播action
    public static final String ACTION_GOME_ACCOUNT_UPDATE_INFO = "com.gome.gomeaccountservice.ACTION_GOME_ACCOUNT_UPDATE_INFO";   //发送账号数据更新广播
    public static final String KEY_AUTH_TOKEN_TYPE_GET_LOCAL_LOGIN_STATE = "key_auth_token_type_get_local_login_state";

    public static final String ACTION_UPDATE_ACCOUNT_INFO_LOCAL = "ACTION_UPDATE_ACCOUNT_INFO_LOCAL";
    public static final String ACTION_REFRESH_ACCOUNT_INFO = "ACTION_REFRESH_ACCOUNT_INFO";
    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.gome.gomeaccountservice";
    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.gome.gomeaccountservice";

    public static final String KEY_AUTH_TOKEN_TYPE_GET_LOGIN_STATE = "key_auth_token_type_get_login_state";
    public static final String KEY_AUTH_TOKEN_TYPE_LOGIN = "key_auth_token_type_login";
    public static final String KEY_AUTH_TOKEN_TYPE_LOGIN_INFO = "key_auth_token_type_login_info";
    public static final String KEY_AUTH_TOKEN_TYPE_RESET_PWD = "key_auth_token_type_reset_pwd";
    public static final String KEY_AUTH_TOKEN_TYPE_LOGIN_OUT = "key_auth_token_type_login_out";
    public static final String KEY_AUTH_TOKEN_TYPE_REGISTER = "key_auth_token_type_register";
    public static final String KEY_AUTH_TOKEN_TYPE_WEIBO_LOGIN = "key_auth_token_type_weibo_login";


    public static final String KEY_SERVER_AUTHEN_SUCCESS = "success";
    public static final String KEY_SERVER_AUTHEN_NAME_NOT_FOUND = "username not found";
    public static final String KEY_SERVER_AUTHEN_PASSWOR_ERROR = "password error";
    public static final String KEY_SERVER_AUTHEN_LOGIN_TIMEOUT = "login timout";

    public static final String SERVER_TIMEOUT_RESULT_CODE = "-1"; //请求超时resultcode

    //本地缓存信息key
    public static final String HEAD_PORTRAIT_DIR_PATH = "/sdcard/gomeaccount/";
    public static final String HEAD_PORTRAIT_SUFFIX = ".jpg";
    public static final String SHAREPREFEENCE_ACCOUNTS = "gome_accounts"; //临时保存账号信息
    public static final String SHAREPREFEENCE_ACCOUNT_INFO_PREFIX = "account_";//账号详细信息
    //server 账号信息key
    public static final String KEY_SERVER_TOKEN = "token";
    public static final String KEY_ACCOUNT_NAME = "nickName";
    public static final String KEY_ACCOUNT_PWD = "loginPwd";
    public static final String KEY_ACCOUNT_EMAIL = "mallAddress";
    public static final String KEY_ACCOUNT_PHONE_NUMBER = "phoneNo";
    public static final String KEY_SMS_CODE = "authCode";  //短信验证码
    public static final String KEY_ACCOUNT_REGISTER_TYPE = "registerType";  //注册类型0手机1邮箱3其他

    public static final String KEY_ACCOUNT_GOME_ID = "gomeId";
    public static final String KEY_ACCOUNT_CREATE_TIME = "createdTime";
    public static final String KEY_ACCOUNT_SEX = "sex";
    public static final String KEY_ACCOUNT_USER_LEVEL = "userLevel";

    public static final String KEY_LOCAL_AVATAR_PATH = "localAvatarPath";  //本地头像路径key

    //头像相关
    //public static final String ACCOUNT_SERVER_AVATAR_PREFIX = "http://192.168.1.129/";
    public static final String KEY_ACCOUNT_SERVER_AVATAR = "avatar";  //服务器头像路径key
    public static final String KEY_AVATARTYPE = "avatarType";
    //向server 请求key
    public static final String KEY_SERVER_REQUEST_SMS_MSG_TYPE = "msgType";
    public static final String SMS_MSG_TYPE_REGISTER = "1";
    public static final String SMS_MSG_TYPE_RETRIEVE = "2";
    public static final String REGISTER_TYPE_PHONE = "0";
    public static final String REGISTER_TYPE_EMAIL = "1";
    //server 返回状态key
    public static final String KEY_SERVER_RESULT_CODE = "resCode";
    public static final String KEY_SERVER_RESULT_MSG = "resMsg";
    public static final String SERVER_SUCCESS_RESULT_CODE = "000";
    //图片配置
    public static final float IMAGE_SCALE = 0.1f;
    //广播相关
    public static final String ACTION_GOME_ACCOUNT_LOGIN_OUT = "gome account login out";

    public static final int CHECK_NETWORK_REQUEST_CODE = 200;
    public static final int CHECK_ACCOUNT_REQUEST_CODE = 201;
    public static final int RESERVATION_REQUEST_CODE = 202;


    public static String KEY_START_MODE = "key_start_mode";
    public static String KEY_START_MODE_TARGET_CLASS = "key_start_mode_target_class";
    public static String VALUE_TARGET_HOME = "com.gome.usercenter.activity.HomeActivity";
    public static String VALUE_TARGET_FEED_BACK = "com.gome.usercenter.activity.FeedbackActivity";
    public static String VALUE_TARGET_FEED_BACK_TO_SERVER = "com.gome.usercenter.activity.FeedbackToServerActivity";
    public static String VALUE_TARGET_EXPERIENCE_VERSION = "com.gome.usercenter.activity.ExperienceVersionActivity";
    public static String VALUE_TARGET_RESERVATION = "com.gome.usercenter.activity.ReservationActivity";
    public static final String KEY_START_MODE_LOGIN_FEEDBACK = "startModeLoginFeedback";  //账号启动模式登录跳转的界面
    public static final String USER_CENTER_MODE= "userCenter";
}
