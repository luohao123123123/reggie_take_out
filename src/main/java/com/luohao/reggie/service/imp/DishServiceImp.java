package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Category;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.DishFlavor;
import com.luohao.reggie.bean.SetmealDish;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.mapper.DishMapper;
import com.luohao.reggie.service.CategoryService;
import com.luohao.reggie.service.DishFlavorService;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImp extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 用于新增菜品时，进行数据保存，需要保存菜品的基本信息，同时保存菜品的口味，
     * 所以需要同时操作Dish和DishFlavor表
     * @param dishDto 新增菜品时的数据传输模型
     */
    @Override
    @Transactional  //开启事务
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到Dish表
        this.save(dishDto);

        //保存菜品的口味到DishFLavor表
            //获取dishId
        Long dishId = dishDto.getId();
            //把dishId赋给每一个口味信息中
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map(x-> {
            x.setDishId(dishId);
            return x;
        }).collect(Collectors.toList());
            //保存口味信息
        dishFlavorService.saveBatch(flavors);


    }


    /**
     * 用于菜品管理页面的分页查询，同时需要根据菜品id查询对应的菜品分类的name，返回给前端
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Transactional
    @Override
    public Page<DishDto> dtoPageWithCategoryName(int page,int pageSize,String name){
        //构建分页构造器
        Page<Dish> pageInfo=new Page<>(page,pageSize);
        //前端需要分类类别字段，而Dish中只有分类id字段，所以需要使用DishDto数据传输模型
        Page<DishDto> dishDtoPage=new Page<>();

        //构建条件查询器
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //过滤name
        queryWrapper.like(name!=null,Dish::getName,name);
        //按照创建时间排序
        queryWrapper.orderByDesc(Dish::getCreateTime);
        //执行分页查询
        this.page(pageInfo,queryWrapper);

        //获取菜品的分类id
        //对象的拷贝，不拷贝records字段的数据
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //获取所有的菜品信息
        List<Dish> records = pageInfo.getRecords();
        //使用DishDto封装菜品信息和菜品分类
        List<DishDto> dishDtoList = records.stream().map(x -> {
            //创建DishDto对象
            DishDto dishDto = new DishDto();
            //把菜品信息拷贝到dishDto上，x代表每一个Dish实体
            BeanUtils.copyProperties(x, dishDto);
            //获取菜品分类id
            Long categoryId = x.getCategoryId();
            //根据菜品分类id查询分类信息
            Category category = categoryService.getById(categoryId);
            //判断菜品分类表中是否有这个分类
            if(category!=null) {
                //获取菜品分类
                String categoryName = category.getName();
                //把菜品分类name封装到DishDto
                dishDto.setCategoryName(categoryName);
            }
            else {
                dishDto.setCategoryName("此菜品无分类");
            }
            return dishDto;
        }).collect(Collectors.toList());

        //把DishDtoList封装到dishDtoPage中
        dishDtoPage.setRecords(dishDtoList);
        return dishDtoPage;
    }


    /**
     * 用于修改菜品信息时的菜品信息回显
     * 这里需要回显菜品的基本信息，同时需要回显菜品的口味信息
     * @param id 菜品id
     * @return
     */
    @Override
    public DishDto getDishWithFlavor(Long id) {
        //根据菜品id获取菜品信息
        Dish dish = this.getById(id);


        //根据菜品id获取菜品口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> dishFlavorList = dishFlavorService.list(queryWrapper);

        //创建DishDto对象
        DishDto dishDto=new DishDto();
        //进行数据的拷贝
        BeanUtils.copyProperties(dish,dishDto);

        //把菜品口味信息放进DishDto中
        dishDto.setFlavors(dishFlavorList);

        return dishDto;
    }


    /**
     * 用于保存修改后的菜品基本信息和菜品口味信息
     * @param dishDto
     */
    @Transactional //添加事务注解，保持一致性
    @Override
    public void updateDishWithFlavor(DishDto dishDto) {
        //保存更新的菜品基本信息
        this.updateById(dishDto);
        //把菜品的口味信息先删除，这样做的原因是因为如果修改后的菜品口味比修改前的菜品不一致，那么如果直接更新的话，之前的那些菜品口味并不会删除
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //把修改后的菜品口味信息再进行保存
            //获取修改后的菜品口味信息
        List<DishFlavor> dishFlavorList = dishDto.getFlavors();
            //把dishId赋给每一个菜品口味
        dishFlavorList = dishFlavorList.stream().map(x -> {
            x.setDishId(dishDto.getId());
            return x;
        }).collect(Collectors.toList());

        //执行保存
        dishFlavorService.saveBatch(dishFlavorList);
    }


    /**
     * 用于菜品的单个删除和多个删除
     * @param ids
     * @return
     */
    @Override
    public R<String> delete(List<Long> ids) {
        //判断菜品是否为在售状态，如果是在售状态，则无法删除
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        //1为在售状态，0为禁售状态
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.in(Dish::getId,ids);
        long count = this.count(queryWrapper);  //这里获取的count是状态为在售状态的菜品的个数
        //如果count大于0,说明，有状态为在售状态的菜品，则抛出业务异常
        if(count>0){
            throw new CustomException("有"+count+"个菜品正在销售无法删除");
        }

        //判断菜品是否包含在套餐中
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        long count1 = setmealDishService.count(queryWrapper1);//这里获取的是套餐中包含菜品的个数
        //如果count1大于0，说明要删除的菜品中有包含在套餐的菜品，无法删除
        if(count1>0){
            throw new CustomException("有菜品包含在套餐中，无法删除");
        }

        //如果以上两种不能删除的判断都不满足，则可以直接删除菜品
        this.removeByIds(ids);
        return R.success("删除成功");

    }
}
