package com.pajamas.user.mapper;

import com.pajamas.user.domain.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-04-22
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
