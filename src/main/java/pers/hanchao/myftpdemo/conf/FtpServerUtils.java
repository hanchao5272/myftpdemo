package pers.hanchao.myftpdemo.conf;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.UUID;

/**
 * FTP服务工具类
 * Created by 韩超 on 2018/3/9.
 */
public class FtpServerUtils {
    private final static Logger LOGGER = Logger.getLogger(FtpServerUtils.class);

    private static FtpServerConfig config = new FtpServerConfig();

    public FtpServerConfig getFtpServerConfig() {
        return config;
    }

    public void setFtpServerConfig(FtpServerConfig ftpServerConfig) {
        this.config = ftpServerConfig;
    }

    /**
     * <p>Title: 上传单个文件</p>
     * @param sourceFile 待上传文件
     * @param serverFilePath 服务器文件保存路径
     * @author 韩超 2018/3/9 15:01
     */
    public static boolean uploadSingleFile(File sourceFile,String serverFilePath){
        String clientFileName = sourceFile.getName();
        return uploadSingleFile(sourceFile,serverFilePath,clientFileName);
    }
    /**
     * <p>Title: 上传单个文件</p>
     * @param sourceFile 待上传文件
     * @param serverFilePath 服务器文件保存路径
     * @param serverFileName 服务器文件名
     * @author 韩超 2018/3/9 14:42
     */
    public static boolean uploadSingleFile(File sourceFile,String serverFilePath,String serverFileName){

        //记录开始时间
        ThreadLocal<Long> startTime = new ThreadLocal<Long>();
        startTime.set(System.currentTimeMillis());
        //使用UUID作为一次上传的标识
        String ftpClientUUID = UUID.randomUUID().toString();
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]开始上传单个文件,客户端文件：" + sourceFile.getName() + ",服务端文件：" + serverFileName);
        //连接服务器并登录
        FTPClient ftpClient = getFTPClient();
        //确保文件路径存在
        try {
            ftpClient.makeDirectory(serverFilePath);
        } catch (IOException e) {
            LOGGER.info("FTPClient创建目录失败：" + serverFilePath);
            shutdwon(ftpClient);
            e.printStackTrace();
            return false;
        }
        //切换目录
        try {
            ftpClient.changeWorkingDirectory(serverFilePath);
        } catch (IOException e) {
            LOGGER.info("FTPClient切换目录失败：" + serverFilePath);
            shutdwon(ftpClient);
            e.printStackTrace();
            return false;
        }
        //上传文件
        try {
            boolean result = ftpClient.storeFile(serverFileName, new FileInputStream(sourceFile));
            LOGGER.debug("FTPClient....上传完成");
        } catch (IOException e) {
            LOGGER.info("FTPClient上传文件失败");
            shutdwon(ftpClient);
            e.printStackTrace();
            return false;
        }
        //登出
        try {
            ftpClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("FTPClient[" + ftpClientUUID+ "]服务器登出失败");
            shutdwon(ftpClient);
            return false;
        }
        //关闭连接
        shutdwon(ftpClient);
        Long useTime = System.currentTimeMillis() - startTime.get();
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]上传完成...用时：" + useTime + "ms");
        return true;
    }


    /**
     * <p>Title: 连接FTP服务器，并登陆。然后返回这个连接。如果获取连接失败，则返回null</p>
     * @author 韩超 2018/3/9 11:31
     */
    private static FTPClient getFTPClient(){
        LOGGER.info("FTPClient连接配置：" + config.toString());

        FTPClient ftpClient = new FTPClient();
        //设置编码方式
        ftpClient.setControlEncoding(config.getEncoding());
        try {
            //连接服务器
            ftpClient.connect(config.getIp(),config.getPort());
            //登陆服务器
            ftpClient.login(config.getUsername(),config.getPassword());
            //获取当前连接状态
            Integer replyCode = ftpClient.getReplyCode();
            LOGGER.debug("FTPClient当前连接状态：" + replyCode);
            //如果请求操作失败，则返回null
            //isPositiveCompletion()指的是replyCode为2xx,即请求操作成功
            if(!FTPReply.isPositiveCompletion(replyCode)){
                return null;
            }
            //设置文件类型
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (Exception e) {
            LOGGER.info("FTPClient获取连接失败！");
            shutdwon(ftpClient);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <p>Title: 关闭连接</p>
     * @author 韩超 2018/3/9 14:00
     */
    private static boolean shutdwon(FTPClient ftpClient){
        if(null == ftpClient){
            LOGGER.info("FTPClient为空！");
            return false;
        }else{
            try {
                //如果连接存在，则断开连接
                if(ftpClient.isConnected()){
                    //断开连接
                    ftpClient.disconnect();
                    LOGGER.debug("FTPClient断开连接成功!");
                }
                return true;
            } catch (IOException e) {
                LOGGER.info("FTPClient关闭异常！");
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * <p>Title: 自定义线程进行多线程上传测试</p>
     * @author 韩超 2018/3/9 15:45
     */
    static class MyThreadForSingleFileUpload extends Thread{
        private String fileName;
        private String serverFilePath1;
        private String serverFileName1;

        public MyThreadForSingleFileUpload(String fileName, String serverFilePath1, String serverFileName1) {
            this.fileName = fileName;
            this.serverFilePath1 = serverFilePath1;
            this.serverFileName1 = serverFileName1;
        }

        @Override
        public void run(){
            FtpServerUtils.uploadSingleFile(new File(fileName),serverFilePath1,serverFileName1);
        }
    }
    /**
     * <p>Title: 测试</p>
     * @author 韩超 2018/3/9 13:59
     */
    public static void main(String[] args){
        //测试单个文件上传
//        String filePath = "F:\\myftpservertest\\1.jpg";
//        String filePathPrefix = "F:\\myftpservertest\\";
//        String serverFilePath = "\\pic\\png";
//        String serverFileName = "111.jpg";
//        //重命名
//        FtpServerUtils.uploadSingleFile(new File(filePath),serverFilePath,serverFileName);
//        //使用客户端的名称
//        FtpServerUtils.uploadSingleFile(new File(filePath),serverFilePath);

//        //多线程上传ftp
//        for(int i = 1; i < 6; i++) {
//            String fileName = "F:\\myftpservertest\\" + i + ".jpg";
//            String serverFilePath1 = "\\pic\\png";
//            String serverFileName1 = i + "" + i + ".jpg";
//            new MyThreadForSingleFileUpload(fileName,serverFilePath1,serverFileName1).start();
//        }
    }
}
