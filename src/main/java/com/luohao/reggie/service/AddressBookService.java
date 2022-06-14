package com.luohao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luohao.reggie.bean.AddressBook;

import java.util.List;


/**
 * 地址薄Service
 */
public interface AddressBookService extends IService<AddressBook> {

    /**
     * 设置地址为默认地址
     * @param addressBook
     * @return
     */
    public void setDefault(AddressBook addressBook);


    /**
     * 查询指定用户的全部地址
     * 这里用于用户地址页面的全部地址显示
     */
    public List<AddressBook> getAllAddressByUserId(AddressBook addressBook);

}
