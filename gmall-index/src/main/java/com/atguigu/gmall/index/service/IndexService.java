package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/01/16:06
 * @Description:
 */
@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;

    public List<CategoryEntity> queryLv1lCategories() {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesByPid(0l);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }
}
