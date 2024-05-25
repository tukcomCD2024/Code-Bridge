const { createProxyMiddleware } = require('http-proxy-middleware');

const targetUrl = process.env.NODE_ENV === 'production' ? 'http://sharenote.shop:8080' : 'http://localhost:8080';

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
       target: 'http://localhost:8080',	// 서버 URL or localhost:설정한포트번호
      //target: 'http://sharenote.shop:8080',	// 서버 URL or localhost:설정한포트번호
      changeOrigin: true,
    })
  );
};