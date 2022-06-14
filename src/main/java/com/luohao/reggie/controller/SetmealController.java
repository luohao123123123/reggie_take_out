package com.luohao.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.Dish;
import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.bean.SetmealDish;
import com.luohao.reggie.dto.DishDto;
import com.luohao.reggie.dto.SetmealDto;
import com.luohao.reggie.service.DishService;
import com.luohao.reggie.service.SetmealDishService;
import com.luohao.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 套餐Controller
 */
@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;



    /**
     * 新增套餐
     * @param setmealDto 前端页面与后端传输的数据模型
     * @return
     */
    @PostMapping
    public R<String>  add(@RequestBody SetmealDto setmealDto){
        //保存套餐的基本信息到套餐表
        setmealService.save(setmealDto);
        //保存套餐里面的菜品到套餐菜品关联表
            //获取套餐的关联菜品信息
        List<SetmealDish> setmealDishList = setmealDto.getSetmealDishes();
            //给关联的菜品信息设置套餐id
        setmealDishList=setmealDishList.stream().map(x->{
            x.setSetmealId(setmealDto.getId());
            return x;
        }).collect(Collectors.toList());
            //执行保存
        setmealDishService.saveBatch(setmealDishList);

        return  R.success("套餐保存成功");
    }


    /**
     * 套餐管理页面的分页展示
     * @return
     */

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        //执行自定义的分页方法
        Page<SetmealDto> setmealDtoPage = setmealService.dtoPageWithCategoryName(page, pageSize, name);
        //最后返回setmealDtoPage
        return R.success(setmealDtoPage);

    }


    /**
     * 套餐修改页面，需要回显套餐的信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        //调用自定义的套餐信息获取方法
        SetmealDto setmealDto = setmealService.get(id);
        //返回SetmealDto数据
        return  R.success(setmealDto);
    }

    /**
     * 套餐信息的修改
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        //调用自定义的套餐修改方法
        setmealService.updateSetmealAndDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 用于套餐页面的套餐删除，需要删除套餐的基本信息和套餐中包含的菜品关系
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam(value ="ids" ) List<Long> ids){
        //调用自定义的套餐删除方法，需要删除套餐的基本信息和套餐中包含的菜品关系
        setmealService.deleteWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 用于套餐状态的更新
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateWithStatus(@PathVariable Integer status,@RequestParam(value = "ids") List<Long> ids){
        setmealService.updateWithStatus(status,ids);
        return R.success("状态更新成功");
    }

    /**
     * 用于移动端的套餐显示
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //根据套餐分类id查询所有的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        //过滤套餐status为1
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }

    /**
     * 这里用于移动端获取套餐的全部菜品
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getSetmealWithDish(@PathVariable(value = "id") Long setmealId){
        //调用自定义的查询套餐中菜品信息的方法
        List<DishDto> dishDtoList = setmealService.getSetmealWithDish(setmealId);

        return R.success(dishDtoList);
    }
}
