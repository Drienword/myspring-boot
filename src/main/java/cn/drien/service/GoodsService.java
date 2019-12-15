package cn.drien.service;

import cn.drien.entity.Goods;
import cn.drien.entity.PageBean;

public interface GoodsService extends BaseService<Goods> {

    /**
     * 分页查询
     * goods 查询条件
     * pageCode 当前页
     * pagesize 每页的记录数
     * **/
    PageBean findByPage(Goods goods, int pageCode, int pageSize);
}
