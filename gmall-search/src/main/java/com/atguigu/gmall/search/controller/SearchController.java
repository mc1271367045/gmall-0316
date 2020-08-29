package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/29/9:27
 * @Description:
 *
 * search.gmall.com 域名
 *
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("search")
    public ResponseVo<SearchResponseVo> search(SearchParamVo paramVo){
        SearchResponseVo responseVo = this.searchService.search(paramVo);
        return ResponseVo.ok(responseVo);
    }
}
