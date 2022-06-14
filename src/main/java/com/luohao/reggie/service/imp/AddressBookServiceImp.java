package com.luohao.reggie.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luohao.reggie.bean.AddressBook;
import com.luohao.reggie.common.BaseContext;
import com.luohao.reggie.mapper.AddressBookMapper;
import com.luohao.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImp extends ServiceImpl<AddressBookMapper,AddressBook> implements AddressBookService {

    /**
     * 设置地址为默认地址
     * @param addressBook
     * @return
     */
    @Transactional
    @Override
    public void setDefault(AddressBook addressBook) {
        //根据用户id，查询用户的所有地址
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getId());
        //先把用户的所有地址都设置为0，即都不是默认地址
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        this.update(wrapper);

        //再把需要设置为默认地址的地址信息改为1，即为默认地址
        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        //最后更新
        this.updateById(addressBook);
    }


    /**
     * 查询指定用户的全部地址
     * 这里用于用户地址页面的全部地址显示
     */
    @Override
    public List<AddressBook> getAllAddressByUserId(AddressBook addressBook) {
        //给地址信息设置用户id
        addressBook.setUserId(BaseContext.getId());


        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //根据用户id查询地址信息
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        //执行查询
        List<AddressBook> addressBookList = this.list(queryWrapper);
        //SQL:select * from address_book where user_id = ? order by update_time desc
        return addressBookList;
    }
}
