const { override, addWebpackModuleRule } = require('customize-cra');

module.exports = override(
  addWebpackModuleRule({
    test: /\.css$/,
    use: ['style-loader', 'css-loader'],
  })
);