package com.hj.supercoupon.settlement.dao.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.supercoupon.settlement.dao.entity.UserCouponDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户优惠券数据库持久层
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCouponDO> {
}
