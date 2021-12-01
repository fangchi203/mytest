package com.lyc.service.impl;

import com.lyc.mapper.AdminMapper;
import com.lyc.pojo.Admin;
import com.lyc.pojo.AdminExample;
import com.lyc.service.AdminService;
import com.lyc.utils.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    Admin admin;
    //在业务逻辑层中，一定会有数据库访问层的对象
    @Autowired
    AdminMapper adminMapper;
    @Override
    public Admin login(String name, String pwd) {
        /*
            根据传入的用户或到DB中查询响应用户对象
            如果有条件，则一定要创建AdminExampled 的对象，用来封装调教

         */
        AdminExample example = new AdminExample();
        /*如何添加条件：
        *   select * from admin where a_name = 'admin'
        *   实际开发中不能使用 * 来做列的选择，*不是高效的数据库的处理，当我们真正执行查询的时候，会把*替换成所有的列名，
        *   再完成查询的操作，会降低访问数据库的效率
        * */
        //添加用户名a_name条件
        example.createCriteria().andANameEqualTo(name);

        List<Admin> list = adminMapper.selectByExample(example);
        if (list.size() > 0){
            admin = list.get(0);
            //如果查询到用户对象，在进行密码比对，注意密码是密文
            /*
                admin.getApass = c984aed014aec7623a54f0591da07a85fd4b762d
                pwd==  000000
                在进行密码对比是，要将传入的pwd进行md5加密，在与数据库中查到的对象密码进行对比
             */
            String miPwd = MD5Util.getMD5(pwd);
            if(miPwd.equals(admin.getaPass())){
                return admin;
            }
        }
        return null;
    }
}
