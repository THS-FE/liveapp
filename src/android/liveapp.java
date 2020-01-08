package cn.com.ths.wyyx.liveapp;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class liveapp extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            String username = args.getString(0);
            String pwd = args.getString(1);
            this.login(username, pwd, callbackContext);
            return true;
        }else if (action.equals("createLiveRoom")) {
            String theme = args.getString(0);
            this.createLiveRoom(theme, callbackContext);
            return true;
        }else if (action.equals("createAudieneLive")) {
            String currentMode = args.getString(0);
            String roomNo = args.getString(1);
            this.createAudieneLive(currentMode, roomNo, callbackContext);
            return true;
        }
        return false;
    }
   
    private void login(String username,String pwd, CallbackContext callbackContext) {
        if (username != null && username.length() > 0 && pwd != null && pwd.length() > 0) {
            callbackContext.success(username);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    
    private void createLiveRoom(String theme, CallbackContext callbackContext) {
        if (theme != null && theme.length() > 0) {
            callbackContext.success(theme);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void createAudieneLive(String currentMode,String roomNo, CallbackContext callbackContext) {
        if (currentMode != null && currentMode.length() > 0 && roomNo != null && roomNo.length() > 0) {
            callbackContext.success(currentMode);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
