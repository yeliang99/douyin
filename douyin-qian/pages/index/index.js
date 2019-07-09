//index.js
//获取应用实例
const app = getApp()

Page({
  data: {
    screenWidth: 350,
    page:1,
    videolist:[],
    serverUrl: null,
    totalPage:null,
    searchContent: ""
  },
  onLoad: function (params) {
    console.log(params);
    var serverUrl = app.serverUrl;
    var me = this;
    var screenWidth = wx.getSystemInfoSync().screenWidth;
    var newVideoList = me.data.videoList;
    me.setData({
      screenWidth: screenWidth,
      serverUrl: serverUrl
    });
    var searchContent = params.search;
    var isSaveRecord = params.isSaveRecord;
    if (isSaveRecord == null || isSaveRecord == '' || isSaveRecord == undefined) {
      isSaveRecord = 0;
    }
    me.setData({
      searchContent: searchContent
    });
    // 获取当前的分页数
    var page = me.data.page;
    me.getAllvideos(page, isSaveRecord);
  },
  getAllvideos: function (page, isSaveRecord){
    var me = this;
    var serverUrl = app.serverUrl;
    var searchContent = me.data.searchContent;
    wx.request({
      url: serverUrl + '/video/showAll?page=' + page + "&isSaveRecord=" + isSaveRecord,
      method: "POST",
      data: {
        videoDesc: searchContent
      },
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        wx.hideLoading();
        wx.hideNavigationBarLoading();
        wx.stopPullDownRefresh(); //停止页面下拉刷新 让三个小点点快点消失
        console.log(res);
        var status = res.data.status;
        console.log(status);
        if (status == 200) {
          console.log("成功");
          //判断当前页page是否是第一页，如果是第一页，那么设置videoList为空if（page===1）{
          me.setData({
            videolist: []
          });
          var videolist = res.data.data.rows;
          var newvideolist = me.data.videolist;
          me.setData({
            videolist: newvideolist.concat(videolist),
            page: page,
            totalPage: res.data.data.total
          });


        } else if (status == 500) {
        }

      }
    })
  },
  //上拉刷新 加载更多视频
  onReachBottom: function () {
    var me = this;
    var currentPage = me.data.page;
    var totalPage = me.data.totalPage;

    // 判断当前页数和总页数是否相等，如果想的则无需查询
    if (currentPage === totalPage) {
      wx.showToast({
        title: '已经没有视频啦~~',
        icon: "none"
      })
      return;
    }
    var page = currentPage + 1;
    me.getAllvideos(page);
  },
  //下拉刷新 从第一页开始刷新
  onPullDownRefresh: function () {
    wx.showNavigationBarLoading();  //导航栏有刷新图标
    this.getAllvideos(1);


  },
  showVideoInfo: function (e) {
    var me = this;
    var videolist = me.data.videolist;
    console.log(videolist);
    console.log(e);
    var arrindex = e.target.dataset.arrindex;
    var videoInfo = JSON.stringify(videolist[arrindex]);
    wx.navigateTo({
      url: '../videoinfo/videoinfo?videoInfo=' + videoInfo
    })
  }
})
