package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐
     * @param dishIds
     * @return
     */
    //Select setmeal_id from setmeal_dish where dish_id in (1,2,3,4...)
    List<Long> getSetmealByDishIds(List<Long> dishIds);
}
