package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/09/01/16:05
 * @Description:
 */
@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    // 查询一级菜单
    @GetMapping
    public String toIndex(Model model){
        List<CategoryEntity> categories = this.indexService.queryLv1lCategories();
        model.addAttribute("categories",categories);
        return "index";
    }

    // 查询二级三级菜单
    @GetMapping("index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSubByPid(@PathVariable("pid")Long pid){
        List<CategoryEntity> categoryEntities = this.indexService.queryCategoriesWithSubByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }


    // 分布式锁demo
    @GetMapping("index/test/lock")
    @ResponseBody
    public ResponseVo<Object> testLock(){
        this.indexService.testLock1();
        return ResponseVo.ok();
    }



}
