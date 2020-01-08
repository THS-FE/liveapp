var exec = require('cordova/exec');

/**
 * 登录直播服务器
 * @param  username 用户名
 * @param pwd 密码
 */
exports.login = function (username,pwd, success, error) {
    exec(success, error, 'liveapp', 'login', [username,pwd]);
};

/**
 * 创建直播间
 * @param theme 主题
 */
exports.createLiveRoom = function (theme,success, error) {
    exec(success, error, 'liveapp', 'createLiveRoom', [theme]);
};

/**
 * 观众进入直播间
 * @param currentMode 模式 int    MODE_ROOM = 0;   MODE_ADDRESS =1
 * @param roomNo 房间号 string
 */
exports.createAudieneLive = function (currentMode,roomNo,success, error) {
    exec(success, error, 'liveapp', 'createAudieneLive', [currentMode,roomNo]);
};


