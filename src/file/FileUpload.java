package file;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
文件上传后，会写到当前目录的/file目录下
 */
public class FileUpload extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String msg = "";//上传提示信息
        String docPath = "";//数据库存储路径
        String rootPath = this.getServletContext().getRealPath("/");//获取当前文件的绝对路径
        String savePath = "/";//原代码
//        String savePath = "/file_upload";//修改的代码

        String downloadUrl="";                              //传到前端的下载链接
        String attachmentId="OBJ_XXXXXXXXX";                //传到前端的本次上传的唯一流水号
        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("file", "doc,docx,pdf,txt,xml,xls,xlsx,xml,ppt,pptx,jpg,jpeg,png");//设置上传文件到文件夹file下，文件类型只能为doc docx...这几类
        long maxSize = 1000000000;//设置上传的文件大小最大为1000000000
        response.setContentType("text/html; charset=UTF-8");//字符编码

        if(ServletFileUpload.isMultipartContent(request)){
            File uploadDir = new File(rootPath+savePath);//new一个file 路径为rootPath-savePath
            System.out.println("程序自动生成的文件路径是" + rootPath + savePath);
            System.out.println("程序自动生成的绝对文件路径是" + rootPath);
            System.out.println("程序自动生成的文件路径是" +savePath);
            if(!uploadDir.isDirectory()){
                uploadDir.mkdirs();
            }
            if(!uploadDir.canWrite()){//上传目录file是否有写入的权限
                msg = "1";//上传目录没有写权限
            }else{
                String dirName = "file";//设置上传目录为file/////////////////////////////////////之前代码
//                String dirName = "file_upload";//设置上传目录为file///////////////////////////////////
                if(!extMap.containsKey(dirName)){//判断上传目录是否正确
                    msg = "2";//目录名不正确
                }else{
                    savePath += dirName + "/";
                    File saveDirFile = new File(rootPath+savePath);
                    if (!saveDirFile.exists()) {
                        saveDirFile.mkdirs();
                    }
                    File dirFile = new File(rootPath+savePath);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    DiskFileItemFactory factory = new DiskFileItemFactory();
                    ServletFileUpload upload = new ServletFileUpload(factory);
                    upload.setHeaderEncoding("UTF-8");
                    List items = null;
                    try {
                        items = upload.parseRequest(request);
                    } catch (FileUploadException e) {
                        e.printStackTrace();
                    }
                    Iterator itr = items.iterator();
                    while (itr.hasNext()) {
                        FileItem item = (FileItem) itr.next();
                        String fileName = item.getName();
                        long fileSize = item.getSize();
                        if (!item.isFormField()) {
                            //检查文件大小
                            if(item.getSize() > maxSize){
                                msg = "3";//上传文件大小超过限制
                            }else{
                                System.out.println("[file_upload]fileName="+fileName);
                                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                                if(!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)){
                                    msg = "4";//上传文件扩展名是不允许的扩展名
                                }else{
                                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
                                    try{
                                        File uploadedFile = new File(rootPath+savePath, newFileName);
                                        item.write(uploadedFile);
                                        docPath = savePath+newFileName;
                                        downloadUrl=docPath;
                                        msg = "5";//上传文件成功
                                    }catch(Exception e){
                                        msg = "6";//上传文件失败
                                    }
                                }
                            }
                        }else{
                            //如果是FormField，就是前端的device_id,device_name这些
                            String fieldName=item.getFieldName();
                            String fieldValue=item.getString("UTF-8");
                            System.out.println("[file_upload][form_field]fieldName="+fieldName+"，fieldValue="+fieldValue);
                        }
                    }
                }
            }
        }
        System.out.println("[file_upload]&docPath="+docPath+"&msg="+msg);
        //然后返回给前端，根据同步还是异步方式
        /*----------------------------------------返回给前端 开始----------------------------------------*/
        boolean isAjax=true;if (request.getHeader("x-requested-with") == null || request.getHeader("x-requested-with").equals("com.tencent.mm")){isAjax=false;}	//判断是异步请求还是同步请求，腾讯的特殊
        if(isAjax){
            response.setContentType("application/json; charset=UTF-8");
            //构造json
            JSONObject json=new JSONObject();
            try {
                json.put("result_code",0);
                json.put("result_msg","ok");
                json.put("download_url",downloadUrl);
//                json.put("attachment_id",attachmentId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                response.getWriter().print(json);
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            String errorNo="0";
            String errorMsg="ok";
            String url = "add_object.html?result_code="+errorNo+ "&result_msg=" + errorMsg;
            try {
                response.sendRedirect(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*----------------------------------------返回给前端 结束----------------------------------------*/
    }
}
