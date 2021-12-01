package com.lyc.service.impl;

import com.lyc.mapper.ProductTypeMapper;
import com.lyc.pojo.ProductType;
import com.lyc.pojo.ProductTypeExample;
import com.lyc.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ProductTypeServiceImpl")
public class ProductTypeServiceImpl implements ProductTypeService {

    //业务逻辑层一定会有数据访问层对象
    @Autowired
    ProductTypeMapper productTypeMapper;

    @Override
    public List<ProductType> getAll() {
        return productTypeMapper.selectByExample(new ProductTypeExample());
    }
}
