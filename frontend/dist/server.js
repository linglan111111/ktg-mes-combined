// server.js - 增强版静态文件服务器（带浏览器自动打开）
const http = require('http');
const fs = require('fs');
const path = require('path');
const { networkInterfaces } = require('os');
const { exec } = require('child_process');

// 配置
const FRONTEND_PORT = 3000;
const BACKEND_HOST = 'localhost';
const BACKEND_PORT = 8080;
const BACKEND_TIMEOUT = 10000; // 10秒超时
const AUTO_OPEN_BROWSER = true; // 是否自动打开浏览器

// 检查后端是否运行
function checkBackend(callback) {
  console.log('检查后端服务...');
  const req = http.request({
    hostname: BACKEND_HOST,
    port: BACKEND_PORT,
    path: '/',
    method: 'HEAD',
    timeout: 3000
  }, (res) => {
    console.log(`? 后端服务正常 (端口: ${BACKEND_PORT})`);
    callback(true);
  });
  
  req.on('error', (err) => {
    console.error(`? 后端服务不可用: ${err.message}`);
    console.log('请确保后端服务已启动:');
    console.log(`  地址: http://${BACKEND_HOST}:${BACKEND_PORT}`);
    console.log('然后重新启动本服务器');
    callback(false);
  });
  
  req.on('timeout', () => {
    console.error('? 连接后端超时');
    callback(false);
  });
  
  req.end();
}

// 获取本机IP地址
function getLocalIP() {
  const nets = networkInterfaces();
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        return net.address;
      }
    }
  }
  return 'localhost';
}

// 自动打开浏览器
function openBrowser(url) {
  const start = (process.platform == 'darwin' ? 'open' : 
                process.platform == 'win32' ? 'start' : 'xdg-open');
  
  exec(`${start} ${url}`, (error) => {
    if (error) {
      console.log(`${colors.yellow}[提示]${colors.reset} 无法自动打开浏览器，请手动访问: ${url}`);
    } else {
      console.log(`${colors.green}[提示]${colors.reset} 已尝试打开浏览器`);
    }
  });
}

// 彩色控制台输出
const colors = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
  gray: '\x1b[90m',
  white: '\x1b[37m'
};

function logInfo(message) {
  console.log(`${colors.cyan}[INFO]${colors.reset} ${message}`);
}

function logSuccess(message) {
  console.log(`${colors.green}[SUCCESS]${colors.reset} ${message}`);
}

function logError(message) {
  console.log(`${colors.red}[ERROR]${colors.reset} ${message}`);
}

function logWarning(message) {
  console.log(`${colors.yellow}[WARNING]${colors.reset} ${message}`);
}

function logRequest(method, url, status) {
  const statusColor = status >= 400 ? colors.red : 
                      status >= 300 ? colors.yellow : colors.green;
  console.log(`${colors.gray}[${new Date().toLocaleTimeString()}]${colors.reset} ${method} ${url} ${statusColor}${status}${colors.reset}`);
}

// 显示启动横幅
function showBanner(localIP) {
  console.log(colors.cyan + '='.repeat(60) + colors.reset);
  console.log(colors.green + '  ███╗   ███╗███████╗███████╗' + colors.reset);
  console.log(colors.green + '  ████╗ ████║██╔════╝██╔════╝' + colors.reset);
  console.log(colors.green + '  ██╔████╔██║█████╗  ███████╗' + colors.reset);
  console.log(colors.green + '  ██║╚██╔╝██║██╔══╝  ╚════██║' + colors.reset);
  console.log(colors.green + '  ██║ ╚═╝ ██║███████╗███████║' + colors.reset);
  console.log(colors.green + '  ╚═╝     ╚═╝╚══════╝╚══════╝' + colors.reset);
  console.log(colors.cyan + '='.repeat(60) + colors.reset);
  console.log(colors.white + '  MES系统 - 生产管理系统' + colors.reset);
  console.log(colors.cyan + '='.repeat(60) + colors.reset);
  console.log('');
  console.log(colors.green + '? 前端服务器已启动' + colors.reset);
  console.log(colors.blue + '  本地访问:    ' + colors.white + `http://localhost:${FRONTEND_PORT}` + colors.reset);
  console.log(colors.blue + '  网络访问:    ' + colors.white + `http://${localIP}:${FRONTEND_PORT}` + colors.reset);
  console.log(colors.blue + '  后端服务:    ' + colors.white + `http://${BACKEND_HOST}:${BACKEND_PORT}` + colors.reset);
  console.log('');
  console.log(colors.yellow + '?? API代理配置:' + colors.reset);
  console.log(colors.white + '  /prod-api/*  → 后端服务' + colors.reset);
  console.log(colors.white + '  /mes/*       → 后端服务' + colors.reset);
  console.log('');
  console.log(colors.magenta + '?? 控制台操作:' + colors.reset);
  console.log(colors.white + '  Ctrl+C     停止服务器' + colors.reset);
  console.log(colors.white + '  R          重新加载页面' + colors.reset);
  console.log(colors.white + '  B          在浏览器中打开' + colors.reset);
  console.log(colors.white + '  I          显示IP地址' + colors.reset);
  console.log(colors.white + '  S          显示服务器状态' + colors.reset);
  console.log(colors.white + '  H          显示帮助' + colors.reset);
  console.log('');
  console.log(colors.cyan + '='.repeat(60) + colors.reset);
}

// 主服务器
function startServer() {
  const server = http.createServer((req, res) => {
    const startTime = Date.now();
    
    // 处理预检请求
    if (req.method === 'OPTIONS') {
      res.writeHead(200, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Token, Content-Disposition',
        'Access-Control-Allow-Credentials': 'true',
        'Access-Control-Max-Age': '86400'
      });
      res.end();
      logRequest(req.method, req.url, 200);
      return;
    }
    
    // 如果是API请求，转发到后端
    if (req.url.startsWith('/prod-api/') || req.url.startsWith('/mes/')) {
      // 映射路径：/prod-api/xxx -> /xxx
      let targetPath = req.url;
      if (req.url.startsWith('/prod-api/')) {
        targetPath = req.url.replace('/prod-api/', '/');
      }
      
      const options = {
        hostname: BACKEND_HOST,
        port: BACKEND_PORT,
        path: targetPath,
        method: req.method,
        headers: {
          ...req.headers,
          host: `${BACKEND_HOST}:${BACKEND_PORT}`,
          'X-Forwarded-For': req.connection.remoteAddress,
          'X-Forwarded-Host': req.headers.host
        },
        timeout: BACKEND_TIMEOUT
      };
      
      const proxyReq = http.request(options, (proxyRes) => {
        // 允许跨域
        res.writeHead(proxyRes.statusCode, {
          ...proxyRes.headers,
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Expose-Headers': '*'
        });
        proxyRes.pipe(res);
        
        const duration = Date.now() - startTime;
        logRequest(req.method, req.url, proxyRes.statusCode, `(${duration}ms)`);
      });
      
      proxyReq.on('timeout', () => {
        res.writeHead(504, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ 
          code: 504, 
          msg: '后端服务响应超时',
          data: null 
        }));
        logError(`后端请求超时: ${req.url}`);
      });
      
      proxyReq.on('error', (err) => {
        res.writeHead(502, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ 
          code: 502, 
          msg: '后端服务不可用: ' + err.message,
          data: null 
        }));
        logError(`后端代理错误: ${err.message}`);
      });
      
      req.pipe(proxyReq);
      return;
    }
    
    // 静态文件处理
    const file = req.url === '/' ? 'index.html' : req.url;
    const filePath = path.join(__dirname, file);
    
    fs.readFile(filePath, (err, data) => {
      if (err) {
        // 文件不存在，尝试返回 index.html（支持前端路由）
        fs.readFile(path.join(__dirname, 'index.html'), (err, data) => {
          if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('404 - File Not Found');
            logRequest(req.method, req.url, 404);
          } else {
            res.writeHead(200, { 
              'Content-Type': 'text/html',
              'Cache-Control': 'no-cache'
            });
            res.end(data);
            logRequest(req.method, req.url, 200);
          }
        });
      } else {
        // 设置正确的Content-Type
        const ext = path.extname(file).toLowerCase();
        const contentTypes = {
          '.html': 'text/html; charset=utf-8',
          '.htm': 'text/html; charset=utf-8',
          '.js': 'application/javascript; charset=utf-8',
          '.css': 'text/css; charset=utf-8',
          '.json': 'application/json; charset=utf-8',
          '.png': 'image/png',
          '.jpg': 'image/jpeg',
          '.jpeg': 'image/jpeg',
          '.gif': 'image/gif',
          '.svg': 'image/svg+xml',
          '.ico': 'image/x-icon',
          '.woff': 'font/woff',
          '.woff2': 'font/woff2',
          '.ttf': 'font/ttf',
          '.eot': 'application/vnd.ms-fontobject',
          '.map': 'application/json'
        };
        
        res.writeHead(200, {
          'Content-Type': contentTypes[ext] || 'application/octet-stream',
          'Cache-Control': 'public, max-age=86400'
        });
        res.end(data);
        logRequest(req.method, req.url, 200);
      }
    });
  });
  
  // 启动服务器
  server.listen(FRONTEND_PORT, () => {
    const localIP = getLocalIP();
    showBanner(localIP);
    
    // 自动打开浏览器
    if (AUTO_OPEN_BROWSER) {
      setTimeout(() => {
        openBrowser(`http://localhost:${FRONTEND_PORT}`);
      }, 1000);
    }
    
    // 设置控制台交互
    setupConsoleInteraction(localIP, server);
  });
  
  server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
      logError(`端口 ${FRONTEND_PORT} 已被占用，请检查：`);
      logError(`1. 是否已经运行了本服务器`);
      logError(`2. 其他程序占用了端口 ${FRONTEND_PORT}`);
      logError('');
      logInfo('解决方案：');
      logInfo(`1. 停止占用端口的程序`);
      logInfo(`2. 修改 server.js 中的 FRONTEND_PORT 为其他端口`);
    } else {
      logError(`服务器启动失败: ${err.message}`);
    }
    process.exit(1);
  });
  
  return server;
}

// 控制台交互
function setupConsoleInteraction(localIP, server) {
  const readline = require('readline');
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  
  // 设置原始模式，支持单键输入
  process.stdin.setRawMode(true);
  process.stdin.resume();
  process.stdin.setEncoding('utf8');
  
  console.log(colors.cyan + '\n等待命令 (按 H 显示帮助)...' + colors.reset);
  
  process.stdin.on('data', (key) => {
    // Ctrl+C
    if (key === '\u0003') {
      gracefulShutdown(server);
      return;
    }
    
    const command = key.toString().toLowerCase();
    
    switch (command) {
      case 'r':
        console.log(colors.yellow + '[命令] 重新加载浏览器页面' + colors.reset);
        openBrowser(`http://localhost:${FRONTEND_PORT}`);
        break;
        
      case 'b':
        console.log(colors.yellow + '[命令] 打开浏览器' + colors.reset);
        openBrowser(`http://localhost:${FRONTEND_PORT}`);
        break;
        
      case 'i':
        console.log(colors.cyan + '[IP地址]' + colors.reset);
        console.log(`  本地: http://localhost:${FRONTEND_PORT}`);
        console.log(`  网络: http://${localIP}:${FRONTEND_PORT}`);
        break;
        
      case 's':
        console.log(colors.cyan + '[服务器状态]' + colors.reset);
        console.log(`  前端端口: ${FRONTEND_PORT}`);
        console.log(`  后端地址: ${BACKEND_HOST}:${BACKEND_PORT}`);
        console.log(`  运行时间: ${process.uptime().toFixed(0)}秒`);
        break;
        
      case 'h':
        console.log(colors.magenta + '[帮助]' + colors.reset);
        console.log('  Ctrl+C  停止服务器');
        console.log('  R       重新加载页面');
        console.log('  B       在浏览器中打开');
        console.log('  I       显示IP地址');
        console.log('  S       显示服务器状态');
        console.log('  H       显示帮助');
        break;
        
      default:
        // 忽略其他按键
        break;
    }
  });
}

// 优雅关闭
function gracefulShutdown(server) {
  console.log(colors.yellow + '\n正在关闭服务器...' + colors.reset);
  
  server.close(() => {
    console.log(colors.green + '服务器已安全关闭' + colors.reset);
    process.exit(0);
  });
  
  // 5秒后强制退出
  setTimeout(() => {
    console.log(colors.red + '强制关闭服务器' + colors.reset);
    process.exit(1);
  }, 5000);
}

// 程序入口
function main() {
  console.clear();
  logInfo('正在启动MES系统前端服务器...');
  logInfo('='.repeat(60));
  
  // 检查必要文件
  if (!fs.existsSync(path.join(__dirname, 'index.html'))) {
    logError('index.html 文件不存在！');
    logError('请确保在 dist 目录中运行本程序');
    process.exit(1);
  }
  
  // 检查后端服务
  checkBackend((backendAvailable) => {
    if (!backendAvailable) {
      logWarning('注意：后端服务不可用，前端可能无法正常工作');
      logWarning('是否继续启动？(y/n)');
      
      const readline = require('readline');
      const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
      });
      
      rl.question('> ', (answer) => {
        if (answer.toLowerCase() === 'y' || answer === '') {
          logInfo('继续启动服务器...');
          const server = startServer();
          
          // 处理进程信号
          process.on('SIGINT', () => gracefulShutdown(server));
          process.on('SIGTERM', () => gracefulShutdown(server));
        } else {
          logInfo('已取消启动');
          process.exit(0);
        }
        rl.close();
      });
    } else {
      const server = startServer();
      
      // 处理进程信号
      process.on('SIGINT', () => gracefulShutdown(server));
      process.on('SIGTERM', () => gracefulShutdown(server));
    }
  });
}

// 启动程序
main();