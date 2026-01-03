// fix-hostname.js
const os = require('os');
const originalHostname = os.hostname;

// 如果 os.hostname 抛错，就返回一个默认值
os.hostname = function () {
  try {
    return originalHostname.call(os);
  } catch (e) {
    console.warn('os.hostname() failed, using fallback "localhost"');
    return 'localhost';
  }
};

// 继续执行 vue-cli-service
require('./node_modules/@vue/cli-service/bin/vue-cli-service.js');