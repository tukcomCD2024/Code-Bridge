const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = (env, argv) => {
  const isProduction = argv.mode === 'production';
  const targetUrl = isProduction ? 'http://sharenote.shop:8080' : 'http://localhost:8080';

  return {
    mode: isProduction ? 'production' : 'development',
    entry: './src/index.js',
    output: {
      path: path.resolve(__dirname, 'build'),
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
          use: [isProduction ? MiniCssExtractPlugin.loader : 'style-loader', 'css-loader'],
        },
        {
          test: /\.(png|svg|jpg|gif)$/,
          type: 'asset/resource',
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
          target: targetUrl,
          changeOrigin: true,
        },
      ],
    },
    performance: {
      hints: false,
      maxEntrypointSize: 512000,
      maxAssetSize: 512000,
    },
  };
};
