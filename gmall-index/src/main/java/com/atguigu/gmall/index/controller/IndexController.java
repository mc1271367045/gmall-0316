package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping
    public String toIndex(Model model){
        List<CategoryEntity> categories = this.indexService.queryLv1lCategories();
        model.addAttribute("categories",categories);
        return "index";
    }

}
