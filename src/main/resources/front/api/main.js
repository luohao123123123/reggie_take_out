//获取所有的菜品分类  (已完成)
function categoryListApi() {
    return $axios({
      'url': '/category/list',
      'method': 'get',
    })
  }

//获取菜品分类对应的菜品   (已完成)
function dishListApi(data) {
    return $axios({
        'url': '/dish/list',
        'method': 'get',
        params:{...data}
    })
}

//获取菜品分类对应的套餐 （已完成）
function setmealListApi(data) {
    return $axios({
        'url': '/setmeal/list',
        'method': 'get',
        params:{...data}
    })
}

//获取购物车内商品的集合 (已完成)
function cartListApi(data) {
    return $axios({
        'url': '/shoppingCart/list',
        'method': 'get',
        params:{...data}
    })
}

//购物车中添加商品  (已完成)
function  addCartApi(data){
    return $axios({
        'url': '/shoppingCart/add',
        'method': 'post',
        data
      })
}

//购物车中修改商品  (已完成)
function  updateCartApi(data){
    return $axios({
        'url': '/shoppingCart/sub',
        'method': 'post',
        data
      })
}

//删除购物车的商品  (已完成)
function clearCartApi() {
    return $axios({
        'url': '/shoppingCart/clean',
        'method': 'delete',
    })
}

//获取套餐的全部菜品  (已完成)
function setMealDishDetailsApi(id) {
    return $axios({
        'url': `/setmeal/dish/${id}`,
        'method': 'get',
    })
}


