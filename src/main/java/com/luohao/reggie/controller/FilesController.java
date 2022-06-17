package com.luohao.reggie.controller;


import com.luohao.reggie.R.R;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;


/**
 * 文件上传与下载
 */
@RestController
@Slf4j
@RequestMapping("/common")
public class FilesController {

    //获取yml中配置的文件下载保存路径
    @Value("${reggie.path.upload}")
    private  String uploadPath;




    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件
        //获取file的文件名
        String filename = file.getOriginalFilename();
        //获取文件名的后缀
        assert filename != null;  //断言filename不为null
        String suffix = filename.substring(filename.lastIndexOf("."));
        //随机生成一个文件名,还是赋给filename
        filename= UUID.randomUUID()+suffix;
//        判断这个保存路径是否存在，不存在则创建
        File dir=new File(uploadPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        //将这个临时文件转存到指定位置
        try {
            file.transferTo(new File(uploadPath+filename));
            System.out.println(uploadPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(filename);
    }


    /**
     * 文件下载
     * @param name
     * @param response
     * @return
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流获取文件内容
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(uploadPath+name));
            //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            BufferedOutputStream outputStream=new BufferedOutputStream(response.getOutputStream());
            response.setContentType("image/jpeg");
            int len=0;
            byte[] bytes=new byte[1024];
             while ((len=inputStream.read(bytes))!=-1){
                 outputStream.write(bytes,0,len);
                 outputStream.flush();
             }
             outputStream.close();
             inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
