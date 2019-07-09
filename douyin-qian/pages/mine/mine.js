// var videoUtil = require('../../utils/videoUtil.js')

const app = getApp()

Page({
  data: {
    faceUrl: "../resource/images/noneface.png", //头像默认路径
    isMe: true,           //是否是本人
    isFollow: false,     //是否关注

    videoSelClass: "video-info",
    isSelectedWork: "video-info-selected",
    isSelectedLike: "",
    isSelectedFollow: "",

    myVideoList: [],
    myVideoPage: 1,
    myVideoTotal: 1,

    likeVideoList: [],
    likeVideoPage: 1,
    likeVideoTotal: 1,

    followVideoList: [],
    followVideoPage: 1,
    followVideoTotal: 1,

    myWorkFalg: false,
    myLikesFalg: true,
    myFollowFalg: true,

    publisherId:""
   
  },

  //初始化加载用户数据
  onLoad: function (params) {
    var me = this;

    // var user = app.userInfo;
    // fixme 修改原有的全局对象为本地缓存
    var user = app.getGlobalUserInfo();
    console.log('用户信息：');
    console.log(user);
    var userId = user.id;
    var publisherId = params.publisherId;  //视频发布者id
    if (publisherId != null && publisherId != '' && publisherId != undefined) { //当视频发布者id不为空时
      if (publisherId != user.id) {
        me.setData({
          isMe: false,         //视频发布者和自己是否是同一个人
        })
      }
      me.setData({
        publisherId: publisherId,
        serverUrl: app.serverUrl
      })
    }else{
      me.setData({
        publisherId: user.id
      })
    }
    me.setData({
      userId: userId
    })

    wx.showLoading({
      title: '请等待...',
    });
    var serverUrl = app.serverUrl;
    var publisherId = me.data.publisherId;
    // 调用后端
    wx.request({
      url: serverUrl + '/user/queryUserInfo?userId=' + publisherId + "&fanId=" + user.id,
      method: "POST",
      header: {
        'content-type': 'application/json', // 默认值
        'headerUserId': user.id,
        'headerUserToken': user.userToken
      },
      success: function (res) {
        console.log('打印查询query回来的返回用户信息：');
        console.log(res.data);
        wx.hideLoading();
        if (res.data.status == 200) {
          var userInfo = res.data.data;
          var faceUrl = "../resource/images/noneface.png";
          if (userInfo.faceImage != null && userInfo.faceImage != '' && userInfo.faceImage != undefined) {
            faceUrl = serverUrl + userInfo.faceImage;
          }

          me.setData({
            faceUrl: faceUrl,
            fansCounts: userInfo.fansCounts,
            followCounts: userInfo.followCounts,
            receiveLikeCounts: userInfo.receiveLikeCounts,
            nickname: userInfo.nickname,
            isFollow: userInfo.follow
          });
        } else if (res.data.status == 502) {
          wx.showToast({
            title: res.data.msg,
            duration: 3000,
            icon: "none",
            success: function () {
              wx.redirectTo({
                url: '../userLogin/login',
              })
            }
          })
        }
      }
    })

    me.getMyVideoList(1);
  },
  //注销登录
  logout:function(){
    var userInfo = app.getGlobalUserInfo();
    console.log(userInfo);
    var serverUrl = app.serverUrl;

    wx.showLoading({
      title: '注销中...',
    })
    wx.request({
      url: serverUrl + "/logout?userId="+userInfo.id,
      method: "POST",
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        wx.hideLoading();
        console.log(res);
        var status = res.data.status;
        if (status == 200) {
          wx.showToast({
            title: '注销成功',
            icon: "success",
            duration: 2000,
            
          })
          wx.navigateTo({
            url: '../userLogin/login'
          });
          
          // app.userInfo = null;  
          //fix me 注销以后清空缓存
          wx.removeStorageSync('userInfo');
          //页面跳转到
        }

      }
    })
  },
  //关注
  followMe: function (e) {
    var me = this;

    var user = app.getGlobalUserInfo();
    var userId = user.id;
    var publisherId = me.data.publisherId;

    var followType = e.currentTarget.dataset.followtype;

    // 1：关注 0：取消关注
    var url = '';
    if (followType == '1') {
      url = '/user/beyourfans?userId=' + publisherId + '&fanId=' + userId;
    } else {
      url = '/user/dontbeyourfans?userId=' + publisherId + '&fanId=' + userId;
    }

    wx.showLoading();
    wx.request({
      url: app.serverUrl + url,
      method: 'POST',
      header: {
        'content-type': 'application/json', // 默认值
        'headerUserId': user.id,
        'headerUserToken': user.userToken
      },
      success: function () {
        wx.hideLoading();
        if (followType == '1') {
          me.setData({
            isFollow: true,
            fansCounts: ++me.data.fansCounts
          })
        } else {
          me.setData({
            isFollow: false,
            fansCounts: --me.data.fansCounts
          })
        }
      }
    })
  },
  //上传头像
  changeFace:function(){
    var me =this;
    var userInfo = app.getGlobalUserInfo();
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album'],
      success(res) {

        
        // tempFilePath可以作为img标签的src属性显示图片
        const tempFiles = res.tempFiles[0]
        console.log("结果：" + tempFiles);
        const tempFilePaths = res.tempFilePaths
        console.log("头像图片临时地址："+tempFilePaths);
        var serverUrl = app.serverUrl;
        wx.showLoading({
          title: '上传中',
        })

        wx.uploadFile({
          url: serverUrl + "/user/uploadFace?userId=" + userInfo.id, //仅为示例，非真实的接口地址
          filePath: tempFilePaths[0],
          name: 'file',
          success(res) {
            const data = JSON.parse(res.data);
            var status = data.status;
            console.log(data);
            wx.hideLoading();
            if (status == 200) {
              wx.showToast({
                title: '上传成功',
                duration:2000,
                icon: "success"
              });
              var imageUrl = data.data;
              me.setData({
                faceUrl:serverUrl+imageUrl
              })

            }else if(status==500)
              wx.showToast({
                title: data.msg,
              })
            //do something
          }
        })

      }
    })

    
  },
  doSelectWork: function () {
    this.setData({
      isSelectedWork: "video-info-selected",
      isSelectedLike: "",
      isSelectedFollow: "",

      myWorkFalg: false,
      myLikesFalg: true,
      myFollowFalg: true,

      myVideoList: [],
      myVideoPage: 1,
      myVideoTotal: 1,

      likeVideoList: [],
      likeVideoPage: 1,
      likeVideoTotal: 1,

      followVideoList: [],
      followVideoPage: 1,
      followVideoTotal: 1
    });

    this.getMyVideoList(1);
  },

  doSelectLike: function () {
    this.setData({
      isSelectedWork: "",
      isSelectedLike: "video-info-selected",
      isSelectedFollow: "",

      myWorkFalg: true,
      myLikesFalg: false,
      myFollowFalg: true,

      myVideoList: [],
      myVideoPage: 1,
      myVideoTotal: 1,

      likeVideoList: [],
      likeVideoPage: 1,
      likeVideoTotal: 1,

      followVideoList: [],
      followVideoPage: 1,
      followVideoTotal: 1
    });

    this.getMyLikesList(1);
  },

  doSelectFollow: function () {
    this.setData({
      isSelectedWork: "",
      isSelectedLike: "",
      isSelectedFollow: "video-info-selected",

      myWorkFalg: true,
      myLikesFalg: true,
      myFollowFalg: false,

      myVideoList: [],
      myVideoPage: 1,
      myVideoTotal: 1,

      likeVideoList: [],
      likeVideoPage: 1,
      likeVideoTotal: 1,

      followVideoList: [],
      followVideoPage: 1,
      followVideoTotal: 1
    });

    this.getMyFollowList(1)
  },
  getMyVideoList: function (page) {
    var me = this;

    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    var userId = me.data.userId
    var publisherId = me.data.publisherId;
    wx.request({
      url: serverUrl + '/video/showAll/?page=' + page + '&pageSize=6' + '&userId=' + publisherId,
      method: "POST",
      data: {
        // userId: userId
      },
      header: {
        'Content-Type': 'application/json;charset=UTF-8;'
      },
      success: function (res) {
        console.log(res.data);
        var myVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.myVideoList;
        me.setData({
          myVideoPage: page,
          myVideoList: newVideoList.concat(myVideoList),
          myVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },
  getMyLikesList: function (page) {
    var me = this;
    var userId = me.data.userId;

    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    var publisherId = me.data.publisherId;
    wx.request({
      url: serverUrl + '/video/showMyLike/?userId=' + publisherId + '&page=' + page + '&pageSize=6',
      method: "POST",
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        console.log(res.data);
        var likeVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.likeVideoList;
        me.setData({
          likeVideoPage: page,
          likeVideoList: newVideoList.concat(likeVideoList),
          likeVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },

  getMyFollowList: function (page) {
    var me = this;
    var userId = me.data.userId;

    // 查询视频信息
    wx.showLoading();
    // 调用后端
    var serverUrl = app.serverUrl;
    var publisherId = me.data.publisherId;
    wx.request({
      url: serverUrl + '/video/showMyFollow/?userId=' + publisherId + '&page=' + page + '&pageSize=6',
      method: "POST",
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        console.log(res.data);
        var followVideoList = res.data.data.rows;
        wx.hideLoading();

        var newVideoList = me.data.followVideoList;
        me.setData({
          followVideoPage: page,
          followVideoList: newVideoList.concat(followVideoList),
          followVideoTotal: res.data.data.total,
          serverUrl: app.serverUrl
        });
      }
    })
  },
  //上传作品
  uploadVideo:function(){
    var me =this;
    wx.chooseVideo({
      sourceType: ['album'],
      maxDuration: 10,
      success(res) {
        console.log(res)
        console.log(res.tempFilePath)
        var duration = res.duration
        var tmpheight = res.height
        var tmpwidth = res.width
        var tmpVedioPath = res.tempFilePath
        var tmpCoverPath = res.thumbTempFilePath
        if(duration>11){
          wx.showToast({
            title: '视频时长不能超过10s',
            icon:'none',
            duration:2000
          })
        } else if (duration < 1){
          wx.showToast({
            title: '视频时长过小',
            icon: 'none',
            duration: 2000
          })
        }else{
          console.log('开始跳转bgm界面')
          // 选择bgm的页面
          wx.navigateTo({
            url: '../chooseBgm/chooseBgm?duration=' + duration
              + "&tmpheight=" + tmpheight
              + "&tmpwidth=" + tmpwidth
              + "&tmpVedioPath=" + tmpVedioPath
              + "&tmpCoverPath=" + tmpCoverPath,
          })
        }
      }
    })
  },
  // 点击跳转到视频详情页面
  showVideo: function (e) {

    console.log(e);

    var myWorkFalg = this.data.myWorkFalg;
    var myLikesFalg = this.data.myLikesFalg;
    var myFollowFalg = this.data.myFollowFalg;

    if (!myWorkFalg) {
      var videoList = this.data.myVideoList;
    } else if (!myLikesFalg) {
      var videoList = this.data.likeVideoList;
    } else if (!myFollowFalg) {
      var videoList = this.data.followVideoList;
    }

    var arrindex = e.target.dataset.arrindex;
    var videoInfo = JSON.stringify(videoList[arrindex]);

    wx.redirectTo({
      url: '../videoinfo/videoinfo?videoInfo=' + videoInfo
    })

  },

  // 到底部后触发加载
  onReachBottom: function () {
    var myWorkFalg = this.data.myWorkFalg;
    var myLikesFalg = this.data.myLikesFalg;
    var myFollowFalg = this.data.myFollowFalg;

    if (!myWorkFalg) {
      var currentPage = this.data.myVideoPage;
      var totalPage = this.data.myVideoTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyVideoList(page);
    } else if (!myLikesFalg) {
      var currentPage = this.data.likeVideoPage;
      var totalPage = this.data.myLikesTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyLikesList(page);
    } else if (!myFollowFalg) {
      var currentPage = this.data.followVideoPage;
      var totalPage = this.data.followVideoTotal;
      // 获取总页数进行判断，如果当前页数和总页数相等，则不分页
      if (currentPage === totalPage) {
        wx.showToast({
          title: '已经没有视频啦...',
          icon: "none"
        });
        return;
      }
      var page = currentPage + 1;
      this.getMyFollowList(page);
    }

  }

})
