const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'https://sharenote.shop:8080',	// 서버 URL or localhost:설정한포트번호
      changeOrigin: true,
    })
  );
};