//（已完成）
function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
}

//（已完成）
function sendMsgApi(data) {
    return $axios({
        'url': '/user/sendMsg',
        'method': 'post',
        data
    })
}

//（已完成）
function loginoutApi() {
  return $axios({
    'url': '/user/loginout',
    'method': 'post',
  })
}

  