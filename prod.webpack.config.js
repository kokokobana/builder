var webpack = require('webpack');
var path = require('path');
var ClosureCompilerPlugin = require('webpack-closure-compiler');

module.exports = require('./scalajs.webpack.config');

module.exports.plugins = (module.exports.plugins || []).concat([
  new webpack.DefinePlugin({
    'process.env': {
      'NODE_ENV': JSON.stringify('production')
    }
  }),
  new ClosureCompilerPlugin({
    compiler: {
      compilation_level: 'SIMPLE'
    },
    concurrency: 8,
  })
]);