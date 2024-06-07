const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = (env, argv) => {
  return {
    // mode:  'development', // 개발용
    mode:  'production', // 배포용
    entry: './src/index.js',
    output: {
      path: path.resolve(__dirname, 'dist'),
      filename: 'bundle.js',
      assetModuleFilename: "[name][ext]?[hash]",
    },
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/,
          exclude: /node_modules/,
          use: {
            loader: "babel-loader",
            options: {
              presets: [
                "@babel/preset-env", ["@babel/preset-react", {"runtime": "automatic"}]
              ]
            }
          }
        },
        {
          test: /\.css$/,
          use: ['style-loader', 'css-loader'],
        },
        {
          test: /\.(png|svg|jpg|gif)$/,
          type:'asset/resource',
        },
      ],
    },
    resolve: {
      extensions: ['.js', '.jsx'],
    },
    plugins: [
      new HtmlWebpackPlugin({
        template: './public/index.html',
      }),
      new CleanWebpackPlugin(),
      new MiniCssExtractPlugin(),
    ],
    devServer: {
      port: 3000,
      open: true,
      hot: true,
      compress: true,
      historyApiFallback: true,
      proxy: [
        {
          context: ['/api'],
          // target: 'http://localhost:8080', // 개발용(로컬)
          target: 'http://sharenote.shop:8080', // 배포용
          changeOrigin: true,
        },
      ],
    },
    // 권장 사항 출력(오류 X)
    performance: {
      hints: false,
      maxEntrypointSize: 512000,
      maxAssetSize: 512000
      },
  };
};
