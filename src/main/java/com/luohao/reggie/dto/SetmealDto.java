package com.luohao.reggie.dto;



import com.luohao.reggie.bean.Setmeal;
import com.luohao.reggie.bean.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
