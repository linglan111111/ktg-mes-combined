// fix-hostname-for-build.js
const os = require('os');
const originalHostname = os.hostname;

// 修复 os.hostname
os.hostname = function () {
  try {
    return originalHostname.call(os);
  } catch (e) {
    return 'localhost';
  }
};

// 修复子进程通信
const originalSend = process.send;
if (process.send) {
  process.send = function(message, sendHandle, options, callback) {
    try {
      return originalSend.call(this, message, sendHandle, options, callback);
    } catch (e) {
      // 忽略通信错误
      return false;
    }
  };
}

// 设置环境变量
process.env.NODE_SKIP_PLATFORM_CHECK = '1';
process.env.GENERATE_SOURCEMAP = 'false';

// 执行构建
require('./node_modules/@vue/cli-service/bin/vue-cli-service.js');