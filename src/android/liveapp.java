package cn.com.ths.wyyx.liveapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.netease.demo.live.DemoCache;
import com.netease.demo.live.activity.LiveRoomActivity;
import com.netease.demo.live.liveStreaming.PublishParam;
import com.netease.demo.live.nim.config.perference.Preferences;
import com.netease.demo.live.server.DemoServerHttpClient;
import com.netease.demo.live.server.entity.RoomInfoEntity;
import com.netease.demo.live.util.file.AssetCopyer;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 网易云信视频直播插件
 */
public class liveapp extends CordovaPlugin {
    private Activity context;
    private CallbackContext callbackContext;

    //-----------------
    public static final String QUALITY_HD = "HD";
    public static final String QUALITY_SD = "SD";
    public static final String QUALITY_LD = "LD";
    private String quality = QUALITY_SD;
    private String roomId;
    private String push_url;
    private boolean open_audio = true;
    private boolean open_video = true;
    private boolean useFilter = true; //默认开启滤镜
    private boolean faceBeauty = false; //默认关闭美颜

    private boolean cancelEnterRoom = false;

    public static final int MODE_ROOM = 0;
    public static final int MODE_ADDRESS =1;

    private int currentMode = MODE_ROOM;

    private  static  final String TAG = "liveapp";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.context =cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // 登录网易云信服务器
        if (action.equals("login")) {
            this.callbackContext =callbackContext;
            String username = args.getString(0);
            String pwd = args.getString(1);
            this.login(username, pwd);
            return true;
        // 创建直播聊天室
        }else if (action.equals("createLiveRoom")) {
            this.callbackContext =callbackContext;
            String theme = args.getString(0);
            this.createLiveRoom(theme);
            return true;
            //开启观看直播
        }else if (action.equals("createAudieneLive")) {
            this.callbackContext =callbackContext;
            int currentMode = args.getInt(0);
            String roomNo = args.getString(1);
            this.createAudieneLive(currentMode, roomNo);
            return true;
        }
        return false;
    }

    /**
     * 登录
     * @param username 用户名
     * @param pwd 密码
     */
    private void login(String username,String pwd) {
        if (username != null && username.length() > 0 && pwd != null && pwd.length() > 0) {
            //callbackContext.success(username);
            this.loginLiveApp(username,pwd);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void loginLiveApp(String mAccount,final String token){
        // 账号密码登录
        final String account = mAccount.toLowerCase();


        DemoServerHttpClient.getInstance().login(account, token, new DemoServerHttpClient.DemoServerHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DemoCache.setAccount(account);
                saveLoginInfo(account, token);
                loginNim(account, MD5.getStringMD5(token));
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                callbackContext.error("登录失败: " + errorMsg);
                Toast.makeText(context, "登录失败: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginInfo(final String account, final String token) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
        Preferences.saveVodToken(DemoCache.getVodtoken());
//        Preferences.saveRememberAccountToken(rememberPwdCheck.isChecked());
        Preferences.saveRememberAccountToken(true);
        Preferences.saveLoginState(true);
    }

    private void loginNim(final String account, final String token) {
        NIMClient.getService(AuthService.class).login(new LoginInfo(account, token)).setCallback(new RequestCallback() {
            @Override
            public void onSuccess(Object o) {
                // onLoginDone();
                onLoginDoneInit(account, token);
                callbackContext.success(getSuccessMsg("login","success"));
            }

            @Override
            public void onFailed(int i) {
                //onLoginDone();
                callbackContext.error("登录失败: "+i);
                Toast.makeText(context, "登录失败: " + i, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onException(Throwable throwable) {
                callbackContext.error("登录失败: 服务器发生异常");
                //onLoginDone();
            }
        });
    }

    private void onLoginDoneInit(String account, String token) {
        initAsset();

//        // 进入主界面
//        com.netease.demo.live.activity.MainActivity.start(MainActivity.this);
//        finish();
    }

    private void initAsset() {
        AssetCopyer assetCopyer = new AssetCopyer(context);
        try {
            assetCopyer.copy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建直播聊天室
     * @param theme 最新主题
     */
    private void createLiveRoom(String theme) {
        if (theme != null && theme.length() > 0) {
            DemoCache.setRoomTheme(theme);
            this.openLiveRoom();
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    /**
     * 开启直播聊天室
     */
    private void openLiveRoom() {
        if(!open_video && !open_audio){
            callbackContext.error("需至少开启音频或视频中的一项");
            showToast("需至少开启音频或视频中的一项");
            return;
        }
        //检测相机与录音权限
//        if(!bPermission){
//            showToast("请先允许app所需要的权限");
//            AskForPermission();
//            return;
//        }
        cancelEnterRoom = false;
        DialogMaker.showProgressDialog(context, null, "创建房间中", true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelEnterRoom = true;
            }
        }).setCanceledOnTouchOutside(false);

        DemoServerHttpClient.getInstance().createRoom(context, new DemoServerHttpClient.DemoServerHttpCallback<RoomInfoEntity>() {
            @Override
            public void onSuccess(RoomInfoEntity roomInfoEntity) {
                roomId = roomInfoEntity.getRoomid()+"";
                push_url = roomInfoEntity.getPushUrl();
                LogUtil.i(TAG, "live stream url:" + push_url);
                DemoCache.setRoomInfoEntity(roomInfoEntity);

                PublishParam publishParam = new PublishParam();
                publishParam.pushUrl = push_url;
                publishParam.definition = quality;
                publishParam.openVideo = open_video;
                publishParam.openAudio = open_audio;
                publishParam.useFilter = useFilter;
                publishParam.faceBeauty = faceBeauty;
                if(!cancelEnterRoom) {
                    LiveRoomActivity.startLive(context, roomId, publishParam);
                }
                DialogMaker.dismissProgressDialog();
                callbackContext.success(getSuccessMsg("openLiveRoom","success"));
                DemoServerHttpClient.getInstance().createOrUpdateRoomInfo(DemoCache.getAccount(),DemoCache.getRoomTheme(),roomInfoEntity.hlsPullUrl,roomInfoEntity.httpPullUrl,roomInfoEntity.pushUrl,roomInfoEntity.rtmpPullUrl,roomInfoEntity.roomid+"",new DemoServerHttpClient.DemoServerHttpCallback<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.i(TAG,"上传房间信息onSuccess");
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        Log.i(TAG,"上传房间信息onFailed");
                    }
                });

            }

            @Override
            public void onFailed(int code, String errorMsg) {
                showToast(errorMsg);
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 获取成功消息
     * @param target 消息的标识
     * @param res 消息的内容
     * @return
     */
    private String getSuccessMsg(String target,String res){
       return  "{'target':"+target+",'res':"+res+"}";
    }

    private Toast mToast;
    public void showToast(final String text){
        if(mToast == null){
            mToast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG);
        }
        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.setText(text);
                    mToast.show();
                }
            });
        }else {
            mToast.setText(text);
            mToast.show();
        }
    }

    /**
     * 检查输入的地址有效性
     * @return
     */
    private boolean checkUriValidate(String address) {
        if(currentMode == MODE_ROOM){
            //房间号只允许数字
            if(!address.matches("\\d+")){
                showToast("请输入正确的房间号");
                return false;
            }
        }
        return true;
    }
    /**
     * 创建观众观看直播
     * @param currentMode  模式  MODE_ROOM = 0;   MODE_ADDRESS =1
     * @param roomNo
     */
    private void createAudieneLive(int currentMode,String roomNo) {
        if (roomNo != null && roomNo.length() > 0) {
           // callbackContext.success(currentMode);
            this.openAudieneLive(currentMode,roomNo);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    public void openAudieneLive(final int mode, final String address){
        if(!checkUriValidate(address)) return;

        cancelEnterRoom = false;
        DialogMaker.showProgressDialog(context, null, "进入房间中", true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelEnterRoom = true;
            }
        }).setCanceledOnTouchOutside(false);

        DemoServerHttpClient.getInstance().getRoomInfo(mode, address, new DemoServerHttpClient.DemoServerHttpCallback<RoomInfoEntity>() {
            @Override
            public void onSuccess(RoomInfoEntity roomInfoEntity) {
                DialogMaker.dismissProgressDialog();
                DemoCache.setRoomInfoEntity(roomInfoEntity);
                if(roomInfoEntity.getStatus() !=1 && roomInfoEntity.getStatus()!=3){
                    showToast("当前房间, 不在直播中");
                    callbackContext.error("当前房间, 不在直播中");
                    return;
                }
                if(!cancelEnterRoom) {
                    if(mode == MODE_ROOM) {
                        LiveRoomActivity.startAudience(context, roomInfoEntity.getRoomid() + "", roomInfoEntity.getRtmpPullUrl(), true);
                    }else{
                        LiveRoomActivity.startAudience(context, roomInfoEntity.getRoomid() + "", address, true);
                    }
                }
                callbackContext.success(getSuccessMsg("createAudieneLive","success"));
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                showToast(errorMsg);
                callbackContext.error(errorMsg);
                DialogMaker.dismissProgressDialog();
            }
        });
    }
}
