const app = getApp()
// pages/searchPage/searchPage.js
var WxSearch = require('../../wxSearchView/wxSearchView.js');
Page({

  data: {},


  onLoad: function () {
    var that = this;
    var serverUrl = app.serverUrl;
    console.log(serverUrl + "video/hot")
    wx.request({
      url: serverUrl +"/video/hot",
      method:'POST',
      success:function(res){
        // console.log(res.data);
        var hotList = res.data;
        console.log(hotList)
        WxSearch.init(
          that,  // 本页面一个引用
          hotList,
          // ['穿搭', '美妆', "海宁", "桐乡", '宁波', '金华'], // 热点搜索推荐，[]表示不使用
          hotList,
          // ['湖北', '湖南', '北京', "南京"],// 搜索匹配，[]表示不使用
          that.mySearchFunction, // 提供一个搜索回调函数
          that.myGobackFunction //提供一个返回回调函数
        );
      }
    })
    // 2 搜索栏初始化
    var that = this;


  },

  // 3 转发函数，固定部分，直接拷贝即可
  wxSearchInput: WxSearch.wxSearchInput,  // 输入变化时的操作
  wxSearchKeyTap: WxSearch.wxSearchKeyTap,  // 点击提示或者关键字、历史记录时的操作
  wxSearchDeleteAll: WxSearch.wxSearchDeleteAll, // 删除所有的历史记录
  wxSearchConfirm: WxSearch.wxSearchConfirm,  // 搜索函数
  wxSearchClear: WxSearch.wxSearchClear,  // 清空函数

  // 4 搜索回调函数  
  mySearchFunction: function (value) {
    // do your job here
    // 示例：跳转
    wx.redirectTo({
      url: '../index/index?isSaveRecord=1&search=' + value
    })
  },

  // 5 返回回调函数
  myGobackFunction: function () {
    // do your job here
    // 示例：返回
    wx.redirectTo({
      url: '../index/index?searchValue=返回'
    })
  }

})