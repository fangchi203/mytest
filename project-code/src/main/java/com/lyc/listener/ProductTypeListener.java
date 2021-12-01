package com.lyc.listener;

import com.lyc.pojo.ProductType;
import com.lyc.service.ProductTypeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;

@WebListener
public class ProductTypeListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        /*获取全部的商品列表，放到当前的全局作用域中。如何获取所有的商品类型的列表
        *   因为是在监听器中，spring框架注册也是通过监听器来进行注册，二者都实现 ServletContextListener ，同一个监听器
        *   所以此时无法保证哪个监听器仙贝创建，仙贝执行，所以不能使用spring 的依赖注入
        *   所以要手动取
        * */
        //手工从Spring容器中取出ProductTypeServiceImpl的对象
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext_*.xml");
        ProductTypeService productTypeService = (ProductTypeService) context.getBean("ProductTypeServiceImpl");
        List<ProductType> typeList = productTypeService.getAll();
        //放入全局作用域对象，供新增页面，修改页面，前台的查询功能提供全部商品类别集合
        servletContextEvent.getServletContext().setAttribute("typeList",typeList);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
