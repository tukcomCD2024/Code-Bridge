const { createProxyMiddleware } = require('http-proxy-middleware');

const targetUrl = process.env.NODE_ENV === 'production' ? 'http://sharenote.shop:8080' : 'http://localhost:8080';

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: targetUrl, 
      changeOrigin: true,
    })
  );
};