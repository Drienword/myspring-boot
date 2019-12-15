package cn.drien.service.impl;

import cn.drien.entity.Goods;
import cn.drien.entity.PageBean;
import cn.drien.mapper.GoodsMapper;
import cn.drien.service.GoodsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    public List<Goods> findAll() { return null; }

    @Override
    public List<Goods> findById(Long id) {
        return goodsMapper.findById(id);
    }

    @Override
    public void create(Goods goods) {
        goodsMapper.create(goods);
    }

    @Override
    public void delete(Long... ids) {
        for (Long id : ids) {
            goodsMapper.delete(id);
        }
    }

    @Override
    public void update(Goods goods) {
        goodsMapper.update(goods);
    }


    /**
     * 分页查询-条件查询方法
     * **/
    public PageBean findByPage(Goods goods, int pageCode, int pageSize) {
        //使用Mybatis分页插件
        PageHelper.startPage(pageCode, pageSize);

        //调用分页查询方法，其实就是查询所有数据，mybatis自动帮我们进行分页计算
        Page<Goods> page = goodsMapper.findByPage(goods);

        return new PageBean(page.getTotal(), page.getResult());
    }
}
