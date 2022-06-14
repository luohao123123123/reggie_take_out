package com.luohao.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luohao.reggie.bean.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
