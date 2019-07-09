const app = getApp()   //全局对象

Page({
  data: {
  },
  onLoad:function(params){
    var me = this;
    var redirectUrl = params.redirectUrl;
    // debugger;
    if (redirectUrl != null && redirectUrl != undefined && redirectUrl != '') {
      redirectUrl = redirectUrl.replace(/#/g, "?");
      redirectUrl = redirectUrl.replace(/@/g, "=");

      me.redirectUrl = redirectUrl;
    }
  },
  //登录函数
  doLogin:function(e){
    var me = this;
    var formObject = e.detail.value;
    var username = formObject.username;
    var password = formObject.password;
    console.log(formObject);
    if (username.length == 0 || password.length == 0) {
      wx.showToast({
        title: '用户名或密码不能为空',
        icon: 'none',
        duration: 2000
      })
    } else {
      var serverUrl = app.serverUrl;

      wx.showLoading({
        title: '登录中...',
      })
      wx.request({
        url: serverUrl + "/login",
        method: "POST",
        data: {
          username: username,
          password: password
        },
        header: {
          'content-type': 'application/json' // 默认值
        },
        success: function (res) {
          wx.hideLoading();
          console.log(res);
          var status = res.data.status;
          console.log(status);
          if (status == 200) {
            wx.showToast({
              title: "登录成功",
              icon: "success",
              duration: 3000
            }),
              // app.userInfo = res.data.data;   // 给全局赋予这个变量
              // fixme 修改原有的全局对象为缓存中的对象
              app.setGlobalUserInfo(res.data.data);
              var redirectUrl = me.redirectUrl;
              if (redirectUrl != null && redirectUrl != undefined && redirectUrl != '') {
                wx.redirectTo({
                  url: redirectUrl,
                })
              } else {
                wx.redirectTo({
                  url: '../index/index',
                })
              }
          } else if (status == 500) {
            wx.showToast({
              title: res.data.msg,
              icon:"none",
              duration: 3000
            })
          }

        }
      })
    }
  },
  //跳转到注册界面：
  goRegistPage:function(){
    wx.navigateTo({
      url: '../userRegist/regist'
    });
  }

})