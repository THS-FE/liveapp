var exec = require('cordova/exec');

/**
 * 登录直播服务器
 * @username 用户名
 * @pwd 密码
 */
exports.login = function (username,pwd, success, error) {
    exec(success, error, 'liveapp', 'login', [username,pwd]);
};

/**
 * 创建直播间
 */
exports.createLiveRoom = function (theme,success, error) {
    exec(success, error, 'liveapp', 'createLiveRoom', [theme]);
};

/**
 * 观众进入直播间
 */
exports.createAudieneLive = function (currentMode,roomNo,success, error) {
    exec(success, error, 'liveapp', 'createAudieneLive', [currentMode,roomNo]);
};
