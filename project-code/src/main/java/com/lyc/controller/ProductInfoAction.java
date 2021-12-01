package com.lyc.controller;


import com.github.pagehelper.PageInfo;
import com.lyc.pojo.ProductInfo;
import com.lyc.pojo.vo.ProductInfoVo;
import com.lyc.service.ProductInfoService;
import com.lyc.utils.FileNameUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/prod")
public class ProductInfoAction {
    //每页显示的记录数
    public static final int PAGE_SIZE = 5;

    //异步上传的图片的名称
    String saveFileName="";
    //切记：在界面层中，一定会有业务逻辑层的对象
    @Autowired
    ProductInfoService productInfoService;
    //显示全部商品不分页
    @RequestMapping("/getAll")
    public String getAll(HttpServletRequest request){
        List<ProductInfo> list = productInfoService.getAll();
        request.setAttribute("list",list);
        return "product";
    }

    //显示第一页的5条记录
    @RequestMapping("/split")
    public String split(HttpServletRequest request){
        PageInfo info = null;
        Object vo = request.getSession().getAttribute("prodVo");
        if (vo != null){
            info = productInfoService.splitPageVo((ProductInfoVo) vo,PAGE_SIZE);
            request.getSession().removeAttribute("prodVo");
        }else {
            //得到第一特的数据
            info = productInfoService.splitPage(1,PAGE_SIZE);
        }
        request.setAttribute("info",info);
        return "product";
    }

    //ajax分页翻页处理
    @ResponseBody
    @RequestMapping("/ajaxSplit")
    /*
        request范围较小一些，只是一个请求。request对象的生命周期是针对一个客户端(说确切点就是一个浏览器应用程序)的一次请求，
    当请求完毕之后，request里边的内容也将被释放点 。
        session可以跨越很多页面。而session的生命周期也是针对一个客户端，但是却是在别人设置的会话周期内(一般是20-30分钟)，
        session里边的内容将一直存在，即便关闭了这个客户端浏览器 session也不一定会马上释放掉的。可以理解是客户端同一个IE窗口发
        出的多个请求。这之间都可以传递参数，比如很多网站的用户登录都用到了。
    */
    public void ajaxSplit(ProductInfoVo vo, HttpSession session){
        //取得当前page参数的页面的数据
        PageInfo info = productInfoService.splitPageVo(vo,PAGE_SIZE);
        session.setAttribute("info",info);
    }

    //多条件查询功能的实现
    @RequestMapping("/condition")
    @ResponseBody
    public void condition(ProductInfoVo vo,HttpSession session){
        List<ProductInfo> list = productInfoService.selectCondition(vo);
        session.setAttribute("list",list);
    }

    //异步ajax文件上传处理
    @RequestMapping("/ajaxImg")
    @ResponseBody
    public Object ajaxImg(MultipartFile pimage,HttpServletRequest request){//和图片标签中的name属性名称一样
        //提取生成文件名 UUID+上传图片的后缀  .jpg  .png
        saveFileName = FileNameUtil.getUUIDFileName()+FileNameUtil.getFileType(pimage.getOriginalFilename());
        //得到项目中图片存储的路径
        String path = request.getServletContext().getRealPath("/image_big");
        //转存
        /*其实 File.separator 的作用相当于 ' \  '
        在 windows 中 文件文件分隔符 用 ' \ ' 或者 ' / ' 都可以
        但是在 Linux 中，是不识别 ' \ '  的，而 File.separator 是系统默认的文件分隔符号，在 UNIX 系统上，此字段的值为 ' / '
        在 Microsoft Windows 系统上，它为 ' \ ' 屏蔽了这些系统的区别。
        所以用 File.separator 保证了在任何系统下不会出错。*/

        //E:\learnweb\springMVC\ssmProject\project-code\src\main\webapp\image_big\3c1c4296260f45b5b6acbe0e3f0649e4.jpg
        //transferTo()方法是将上传文件写到服务器指定的地址  文件
        try {
            pimage.transferTo(new File(path+File.separator+saveFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回客户端JSON对象，封装图像的路径，为了在页面实现立即回显
        JSONObject object = new JSONObject();
        object.put("imgurl",saveFileName);
        return object.toString();
    }
    @RequestMapping("/save")
    public String save(ProductInfo info,HttpServletRequest request){
        info.setpImage(saveFileName);
        info.setpDate(new Date());
        //info对象有表单提交上来的5个数据，有异步ajax上来的图片名称数据，有商家时间的数据
        int num= -1;

        try {
            num = productInfoService.save(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(num > 0){
            request.setAttribute("msg","增加成功！");
        }else {
            request.setAttribute("msg","增加失败！");
        }
        //清空saveFileName变量中的内容，为下次增加或修改的异步ajax的上传处理
        saveFileName = "";
        //增加成功后应该重新访问数据库，所以跳转到显示的action上
        return "forward:/prod/split.action";
    }

    @RequestMapping("/one")
    public String one(int pid,ProductInfoVo vo, Model model,HttpSession session){
        ProductInfo info = productInfoService.getByID(pid);
        model.addAttribute("prod",info);
        //将多条件及页码放入session中，更新处理结束后分页时读取条件和页码进行处理
        session.setAttribute("prodVo",vo);
        return "update";
    }

    @RequestMapping("/update")
    public String update(ProductInfo info,HttpServletRequest request){
        //如果使用过ajax异步上传，则svaFileName有上传的名称，如果没有使用过ajax
        // 上传图片，saveFileName="";实体类info使用隐藏表单域提供上来的PImage
        // 如果没有使用异步ajax;
        if(!saveFileName.equals("")){
            info.setpImage(saveFileName);
        }
        //完成更新处理
        int num = -1;
        try {
            //对于增删改操作一定要进行异常捕获
            num = productInfoService.update(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(num > 0){
            //此时说明更新成功
            request.setAttribute("msg","更新成功！");
        }else{
            //更新失败
            request.setAttribute("msg","更新失败！");
        }
        //同样，清空saveFileName
        saveFileName = "";
        //使用redirect没有回显是因为重新发送了请求
        return "forward:/prod/split.action";
    }

    @RequestMapping("/delete")//这里不能加@ResponseBody  因为删除之后需要跳转到分页的界面  ajaxSplit
    public String delete(int pid,ProductInfoVo vo,HttpServletRequest request){
        int num = -1;
        try {
            num = productInfoService.delete(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (num > 0){
            request.setAttribute("msg","删除成功！");
            request.getSession().setAttribute("deleteProdVo",vo);
        }else {
            request.setAttribute("msg","删除失败！");
        }
        return "forward:/prod/deleteAjaxSplit.action";
    }

    @RequestMapping(value = "/deleteAjaxSplit",produces = "text/html;charset=UTF-8")
    @ResponseBody
    public Object deleteAjaxSplit(HttpServletRequest request){
        PageInfo info = null;
        Object vo =request.getSession().getAttribute("deleteProdVo");
        if (vo != null){
            info = productInfoService.splitPageVo((ProductInfoVo) vo,PAGE_SIZE);
        }else {
            //先取得第一页的数据
            info = productInfoService.splitPage(1,PAGE_SIZE);
        }
        request.getSession().setAttribute("info",info);
        return request.getAttribute("msg");
    }

    //批量删除商品
    @RequestMapping("/deleteBatch")
    //pids="1,4,5"  ps[]={"1","4","5"}
    /*
         String.split(regex, int limit)  第一个是分隔符 被什么分割  第二个是切割次数
         limit > 0  切割 limit-1次
         limit < 0  切割无限次直至切割完成
         limit = 0  切割无限次，不过最后的空字符串不计入
    */
    public String deleteBatch(String pids,HttpServletRequest request){
        //将上传的字符串解开，形成商品id的字符数组
        String[] ps = pids.split(",");

        try {
            int num = productInfoService.deleteBatch(ps);
            if(num > 0){
                request.setAttribute("msg","批量删除成功！");
            }else {
                request.setAttribute("msg","批量删除失败！");
            }
        } catch (Exception e) {
            request.setAttribute("msg","商品不可删除！");
        }
        //需要放到ajax返回  通过@ResponseBody返回数值
        return "forward:/prod/deleteAjaxSplit.action";
    }


}
