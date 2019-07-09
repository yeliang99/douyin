const app = getApp()

Page({
    data: {
      uploadPercent:0,
    videoParams: {},
    bgmList:[],  
    serverUrl:null,
  },
  onLoad:function(parms){
    console.log(parms)
    var me = this;
    me.setData({
      videoParams:parms
    })
    var serverUrl = app.serverUrl;
    var userInfo = app.getGlobalUserInfo();
    wx.showLoading({
      title: '加载中...',
    })
    wx.request({
      url: serverUrl + "/bgm/list",
      method: "POST",
      success: function (res) {
        wx.hideLoading();
        var status = res.data.status;
        console.log(res);
        if (status == 200) {
          var bgmList = res.data.data;
          me.setData({
            bgmList: bgmList,
            serverUrl: serverUrl
          })
        }

      }
    })
  },

//上传视频与封面到后台
  upload:function(e){

    var me = this; //保留this对象
    var serverUrl= app.serverUrl; //获取全局服务器地址

    var bgmId = e.detail.value.bgmId
    var desc = e.detail.value.desc
    var duration = me.data.videoParams.duration

    var tmpheight = me.data.videoParams.tmpheight
    var tmpwidth = me.data.videoParams.tmpwidth
    var tmpVedioPath = me.data.videoParams.tmpVedioPath

    var tmpCoverPath = me.data.videoParams.tmpCoverPath

    wx.showLoading({
      title: '上传中,请耐心等待...',
    })
    var userInfo = app.getGlobalUserInfo();
    //上传视频
    const uploadTask =wx.uploadFile({
      url: serverUrl + "/video/upload", 
      name: 'file',   //需要和后端程序中的file对应
      formData: {
        userId: userInfo.id,
        bgmId: bgmId,
        videoSeconds: duration,
        videoWidth: tmpwidth,
        videoHeight: tmpheight,
        desc: desc
      },
      filePath: tmpVedioPath,
      header: {
        'content-type': 'multipart/form-data' // 默认值
      },
      success(res) {
        wx.hideLoading();

        const data = JSON.parse(res.data);

        var status = data.status;
        var videoId = data.data;
        if (status == 200) {
          me.tishis('上传成功');
          wx.navigateBack({
            delta: 1
          })
        } else if (status == 500)
          me.tishie('上传失败');
      },
      fail:function(){
        wx.hideLoading();
        me.tishie('上传超时');
      }
    })

    uploadTask.onProgressUpdate((res) => {
      const uploadProgress = res.progress;
      if (uploadProgress < 100) {
        // wx.uploadFile本身有一个this，所以要通过外部var _this = this 把this带进来
        me.setData({
          uploadPercent: uploadProgress
        });
      } else if (uploadProgress === 100) {
        me.setData({
          uploadPercent: 90
        });
      }
      console.log('上传进度', res.progress);
      console.log('已经上传的数据长度', res.totalBytesSent);
      console.log('预期需要上传的数据总长度', res.totalBytesExpectedToSend)
    });


  },
  tishis:function(msg){
    wx.showToast({
      title: msg,
      icon: 'success',
      duration: 2000
    });
  },
  tishie: function (msg) {
    wx.showToast({
      title: msg,
      icon: 'none',
      duration: 2000
    });
  }


   
})



