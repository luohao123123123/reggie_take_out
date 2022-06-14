package com.luohao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.luohao.reggie.R.R;
import com.luohao.reggie.bean.AddressBook;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        //给地址信息设置用户id
        addressBook.setUserId(BaseContext.getId());
        log.info("addressBook:{}", addressBook);
        //执行保存
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        //调用自定义的设置默认地址方法
       addressBookService.setDefault(addressBook);

        return R.success("设置默认地址成功");
    }

    /**
     * 根据id查询地址
     * 这里用于修改地址时的地址信息回显
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        //根据地址id查询地址信息
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("该地址不存在");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault() {
        //根据用户id查询用户的默认地址信息
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        //执行查询
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("该地址不存在");
        } else {
            return R.success(addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     * 这里用于用户地址页面的全部地址显示
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        //调用自定义的获取指定用户的全部地址方法
        List<AddressBook> allAddressByUserId = addressBookService.getAllAddressByUserId(addressBook);
        return R.success(allAddressByUserId);
    }


    /**
     * 根据地址id删除地址信息
     * @param ids 地址ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        addressBookService.removeById(ids);
        return R.success("地址删除成功");
    }

    /**
     * 地址的修改
     * @param addressBook 地址信息
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }
}
