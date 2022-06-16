package com.luohao.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Category;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.DishFlavor;
import com.luohao.reggie.bean.SetmealDish;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.mapper.SetmealDishMapper;
import com.luohao.reggie.service.CategoryService;
import com.luohao.reggie.service.DishFlavorService;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;


    /**
     * 新增菜品
     * 需要操作Dish和DishFlavor
     * @param dishDto 用于新增菜品时的数据传输模型
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody DishDto dishDto){
        //执行保存
        dishService.saveWithFlavor(dishDto);  //自定义的一个保存菜品信息和菜品口味信息的方法

        //todo:用户菜品查看优化，如果后台新增菜品，则需要把缓存的菜品信息清除
        HashOperations<Object, Object, Object> hashType = redisTemplate.opsForHash();
        hashType.delete("dishInfo","dish_"+dishDto.getCategoryId()+"_status_1");


        return R.success("新增菜品成功");
    }


    /**
     * 菜品管理页面，菜品信息分页展示
     * 由于前端需要展示菜品的分类name，直接返回Dish信息的话，只有菜品的分类id，所有需要根据菜品id去查询出菜品name，然后封装给DishDto数据传输模型
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page,int pageSize,String name){
        //执行自定义的分页查询方法dtoPageWithCategoryName
        Page<DishDto> dishDtoPage = dishService.dtoPageWithCategoryName(page, pageSize, name);
        return R.success(dishDtoPage);
    }

    /**
     * 删除单个菜品和批量删除菜品
     * @param ids 多个菜品id
     * @return
     */
    @DeleteMapping
    public  R<String> delete(@RequestParam(value = "ids") List<Long> ids){
        //直接返回自定义的删除方法
        //todo:用户菜品查看优化，如果后台删除菜品，则需要把缓存的菜品信息清除
        redisTemplate.delete("dishInfo");
        return dishService.delete(ids);
    }


    /**
     * 菜品修改时，进行菜品信息的查询，并且回显给前端修改页面
     * @param id 菜品id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getDishWithFlavor(id);
        //返回DishDto给前端，进行回显操作
        return R.success(dishDto);
    }


    /**
     * 菜品信息的修改
     * 菜品的修改，需要保存菜品的信息，同时需要保存菜品口味的信息
     * 需要操作菜品表喝菜品信息表
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto){
        dishService.updateDishWithFlavor(dishDto);
        //todo:用户菜品查看优化，如果后台修改菜品，则需要把缓存的菜品信息清除
        HashOperations<Object, Object, Object> hashType = redisTemplate.opsForHash();
        hashType.delete("dishInfo","dish_"+dishDto.getCategoryId()+"_status_1");
        return R.success("修改成功");
    }



    /**
     * 更新菜品状态，如果要停售这个菜品，首先要判断这个菜品是否包含在谋个套餐中，如果包含在某个套餐中则无法停售
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateDishStatus(@PathVariable Integer status,@RequestParam(value="ids") List<Long> ids){
        //判断菜品要停售时，这个菜品是否包含在某个套餐中，如果包含，则无法停售
        if(status==0){//要进行停售操作时,查询套餐菜品关系表中，这个菜品是否在某个套餐中
            LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.in(SetmealDish::getDishId,ids);
            long count = setmealDishService.count(queryWrapper); //这里的count是指在要停售的菜品中，包含在套餐中的菜品个数
            if(count>0){//如果count大于0，说明有菜品包含在套餐中，所有无法进行停售操作，抛出自定义异常
                throw new CustomException("有菜品包含在套餐中，无法进行停售操作");
            }
        }
        //不满足上面的条件，则可以进行菜品的起售和停售操作
        //先查询出菜品信息
        List<Dish> dishList = dishService.listByIds(ids);
        //修改status
        dishList=dishList.stream().map(x->{
            x.setStatus(status);
            return x;
        }).collect(Collectors.toList());
        //更新菜品信息
        dishService.updateBatchById(dishList);

        //todo:用户菜品查看优化，如果后台更新菜品信息，则需要把缓存的菜品信息清除
        redisTemplate.delete("dishInfo");
        return R.success("菜品状态修改成功");
    }


    /**
     * 1.套餐管理页面，新增或者修改套餐时点击添加菜品，回显对应的菜品信息
     * 2.移动端根据各菜品分类id查询相应的菜品信息以及菜品的口味信息
     * @param dish 菜品信息
     * @return
     */
    @GetMapping("list")
    public R<List<DishDto>> getDishByCategoryId(Dish dish){
        //todo:用户菜品查看优化，用户第一次查询分类菜品信息，先去redis中查询有无信息
            //定义redis中的key
        String key="dish_"+dish.getCategoryId()+"_status_"+dish.getStatus();
        HashOperations<Object, Object, Object> hashType = redisTemplate.opsForHash();
        //获取redis中的缓存转为List<DishDto>
        Object redisDishInfo = hashType.get("dishInfo", key);
        //todo:用户菜品查看优化，如果redis缓存中有菜品信息，则直接返回缓存中的菜品信息
        if(redisDishInfo!=null){
            List<DishDto> redisDishInfo1 = JSON.parseArray(redisDishInfo.toString(), DishDto.class);
            return R.success(redisDishInfo1);
        }

        //todo:用户菜品查看优化，如果redis缓存中不存在菜品信息，则直接查询数据库
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.like(dish.getName()!=null,Dish::getName,dish.getName());
            //过滤菜品状态为1的
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        List<Dish> dishList = dishService.list(queryWrapper);
        //根据查询的菜品信息中的菜品id，获取菜品的口味信息
        List<DishDto> dishDtoList = dishList.stream().map(x -> {
            //创建DishDto对象
            DishDto dishDto = new DishDto();
            //进行数据的复制
            BeanUtils.copyProperties(x, dishDto);
            //获取菜品的id
            Long dishId = x.getId();
            //根据菜品id查询菜品口味信息
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper1);  //菜品的口味信息
            //把菜品的口味信息封装到dishDto
            dishDto.setFlavors(dishFlavorList);
            //返回dishDto
            return dishDto;
        }).collect(Collectors.toList());

        //todo:用户菜品查看优化，如果redis缓存中不存在菜品信息，则把数据库中查询到的数据缓存到redis
        hashType.put("dishInfo",key,JSON.toJSONString(dishDtoList));
        redisTemplate.expire("dishInfo",60, TimeUnit.MINUTES); //设置一个小时的过期时间

        return R.success(dishDtoList);
    }
}
