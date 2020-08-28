package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Auther: Gork_Mc
 * @Date: 2020/08/28/14:24
 * @Description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long>  {
}
