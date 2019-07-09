const app = getApp()

Page({
    data: {

    },
  doRegist:function(e){ //e可以获取到form对象
    var formObject = e.detail.value;
    var username= formObject.username;
    var password = formObject.password;
    // console.log(formObject);
    if(username.length==0 || password.length==0){
      wx.showToast({
        title: '用户名或密码不能为空',
        icon:'none',
        duration:2000
      })
    }else{
      var serverUrl = app.serverUrl;
      wx.showLoading({
        title: '注册中...',
      })
      wx.request({
        url: serverUrl+"/regist",
        method:"POST",
        data: {
          username: username,
          password: password
        },
        header: {
          'content-type': 'application/json' // 默认值
        },
        success:function(res){
          wx.hideLoading();
          console.log(res);
          var status = res.data.status;
          console.log(status);
          if(status==200){
            wx.showToast({
              title:'注册成功,跳转至登录界面',
              icon:"success",
              duration: 3000,
              success: function () {
                wx.navigateTo({
                  url: '../userLogin/login'
                });
              }
            }),
              // app.userInfo=res.data.data;   // 给全局赋予这个变量
              // fixme 修改原有的全局对象为缓存中的对象
              app.setGlobalUserInfo(res.data.data);
          }else if(status==500){
            var msg =res.data.msg;
            wx.showToast({
              title:msg,
              icon:"none",
              duration: 3000
            })
            
          }

        }
      })
    }
  },
  //跳转到登录界面：
  goLoginPage: function () {
    wx.navigateTo({
      url: '../userLogin/login',
    })
  }

    
})