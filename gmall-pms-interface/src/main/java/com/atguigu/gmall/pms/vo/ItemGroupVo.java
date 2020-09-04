package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/25/18:59
 * @Description:
 */
@Data
public class ItemGroupVo {

    private Long groupId;
    private String groupName;
    private List<AttrValueVo> attrs;

}
