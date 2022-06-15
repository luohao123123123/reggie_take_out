package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.Category;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.bean.SetmealDish;
import com.luohao.reggie.common.CustomException;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.dto.SetmealDto;
import com.luohao.reggie.mapper.SetmealMapper;
import com.luohao.reggie.service.CategoryService;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealDishService;
import com.luohao.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImp extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    //分类Service
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;


    /**
     * 用于套餐管理页面的分页
     * 由于前端页面需要分类name，而套餐中只有分类id，所有需要根据分类id获取分类name
     * 由于setmael中没有分类name这个字段，所有要封装成setmealDto后，传给前端
     * @param page 第几页
     * @param pageSize 一页多少条数据
     * @param name  套餐name
     * @return
     */
    @Transactional
    @Override
    public Page<SetmealDto> dtoPageWithCategoryName(int page, int pageSize, String name) {
        //创建套餐分页构造器
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);
        //创建SetmealDto分页构造器
        Page<SetmealDto> setmealDtoPage=new Page<>();

        //创建条件查询构造器
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //根据套餐名字过滤
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //按照创建时间排序
        queryWrapper.orderByDesc(Setmeal::getCreateTime);
        //执行分页查询
        this.page(setmealPage,queryWrapper);

        //进行数据拷贝，不拷贝records字段
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        //获取所有的套餐信息，因为分页信息中，所有的数据的存在Records这个字段中
        List<Setmeal> setmealList = setmealPage.getRecords();
        //根据套餐信息中的分类id获取分类name
        List<SetmealDto> setmealDtoList = setmealList.stream().map(x -> {
            //创建setmealDto对象
            SetmealDto setmealDto = new SetmealDto();
            //把setmeal的信息复制给setmealDto
            BeanUtils.copyProperties(x,setmealDto);

            //获取分类id
            Long categoryId = x.getCategoryId();
            //根据分类id获取分类信息
            Category category = categoryService.getById(categoryId);
            //判断是否有分类信息

            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            } else {
                setmealDto.setCategoryName("此套餐无分类");
            }
            return setmealDto;
        }).collect(Collectors.toList());


        //把setmealDtoList装进setmealDtoPage
        setmealDtoPage.setRecords(setmealDtoList);

        return setmealDtoPage;
    }


    /**
     * 用于套餐修改页面的套餐信息的回显
     * @param id 套餐id
     * @return
     */
    @Transactional
    @Override
    public SetmealDto get(Long id){
        //获取套餐的信息
        Setmeal setmeal = this.getById(id);

        //获取套餐的id，根据套餐的id，查询套餐关联的菜品信息
        Long setmealId = setmeal.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);

        //创建SetmealDto对象
        SetmealDto setmealDto=new SetmealDto();
        //复制数据
        BeanUtils.copyProperties(setmeal,setmealDto,"setmealDishes");
        //把套餐关联的菜品信息放进SetmealDto
        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    /**
     * 用于套餐页面的套餐修改
     * @param setmealDto
     */
    @Transactional
    @Override
    public void updateSetmealAndDish(SetmealDto setmealDto) {
        //套餐信息的修改保存
        this.updateById(setmealDto);
        //套餐关联菜品信息的修改保存
        //先根据套餐id把关联的菜品信息全部删除，防止出现更新的时候菜品比之前的少，直接更新会导致之前的关联菜品没有删除
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //保存更新后的套餐关联菜品信息
        //获取更新后的套餐关联菜品信息
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
        //给菜品信息设置套餐id
        setmealDishList=setmealDishList.stream().map(x->{
            x.setSetmealId(setmealDto.getId());
            return x;
        }).collect(Collectors.toList());
        //执行套餐关联的菜品信息的保存
        setmealDishService.saveBatch(setmealDishList);
    }


    /**
     * 用于套餐页面的套餐删除
     * @param ids
     */
    @Transactional
    @Override
    public void deleteWithDish(List<Long> ids) {
        //删除之前要判断套餐的状态是否为在售状态，如果状态为在售状态，则无法删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        long count = this.count(queryWrapper);  //这里代表，状态为在售状态的套餐个数
        if(count>0){ //如果在售状态个数大于0，则说明要删除的套餐中，存在状态为在售状态的套餐，则无法删除，抛出自定义异常
            throw new CustomException("有"+count+"个套餐为在售状态，无法删除");
        }

        //如果不满足上面的条件，说明不存在状态为在售状态的套餐，则可以删除
            //删除套餐的基本信息
        this.removeByIds(ids);
            //删除套餐关联的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);
    }

    /**
     * 用于套餐页面的套餐状态修改
     * @param status
     * @param ids
     */
    @Override
    public void updateWithStatus(Integer status, List<Long> ids) {
        //获取套餐信息
        List<Setmeal> setmealList = this.listByIds(ids);
        //更新套餐的状态
        setmealList=setmealList.stream().map(x->{
            x.setStatus(status);
            return x;
        }).collect(Collectors.toList());
        //更新套餐信息
        this.updateBatchById(setmealList);

    }


    /**
     * 用于移动端点击套餐查看套餐中的菜品信息
     *
     * 这里需要回显给前端菜品的基本信息，并且需要套装中这个菜品的份数，所以需要使用Dish的前后端数据传输模型DishDto
     * @param setmealId
     * @return
     */
    @Override
    public List<DishDto> getSetmealWithDish(Long setmealId) {
        //根据套餐的id查询套餐中包含的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        //执行查询，获取套装中包含的所有菜品
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        //获取套餐中所有的菜品id
        List<Long> dishIdList = setmealDishList.stream().map(SetmealDish::getDishId).collect(Collectors.toList());
        //根据菜品id查询套餐中所有的菜品信息
        List<Dish> dishList = dishService.listByIds(dishIdList);
        //将Dish封装成DishDto，并且获取菜品的份数
        List<DishDto> dishDtoList = dishList.stream().map(x -> {
            //创建DishDto对象
            DishDto dishDto = new DishDto();
            //进行数据的复制
            BeanUtils.copyProperties(x, dishDto);
            //获取菜品的id
            Long dishId = dishDto.getId();
            //根据菜品id和套餐id，查询菜品在这个套餐中的份数
            LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(SetmealDish::getSetmealId, setmealId);
            queryWrapper1.eq(SetmealDish::getDishId, dishId);
            //执行查询
            SetmealDish setmealDish = setmealDishService.getOne(queryWrapper1);
            //获取套餐中菜品的份数
            Integer copies = setmealDish.getCopies();
            //把份数封装给DishDto
            dishDto.setCopies(copies);
            //返回dishDto
            return dishDto;

        }).collect(Collectors.toList());

        return dishDtoList;
    }


}
