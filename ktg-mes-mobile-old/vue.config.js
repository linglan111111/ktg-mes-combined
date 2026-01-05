/**
 * Copyright (c) 2013-Now http://aidex.vip All rights reserved.
 */
module.exports = {
	configureWebpack: {
		devServer: {
			port: 8080,
			// 修复：必须用数组格式
			allowedHosts: ['localhost', '.localhost', '127.0.0.1'],
			hot: true,
			open: true,
			proxy: {
				"/js": {
					target: "https://demo.aidex.vip",
					changeOrigin: true,
					secure: false
				}
			}
		}
	},
	productionSourceMap: false,
}