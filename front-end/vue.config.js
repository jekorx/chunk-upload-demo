const path = require('path')
const resolve = dir => path.join(__dirname, dir)

module.exports = {
  chainWebpack: config => {
    config.resolve.alias
      .set('@', resolve('src'))
      .set('_v', resolve('src/views'))
      .set('_c', resolve('src/components'))
  },
  devServer: {
    // 访问端口
    port: 8089,
    // 启动后自动在浏览器中打开
    open: false,
    // no progress print
    progress: false,
    // 跨域
    proxy: {
      '/admin-api/': {
        target: 'http://127.0.0.1:8008/',
        changeOrigin: true
      }
    }
  }
}
