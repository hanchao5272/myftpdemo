package pers.hanchao.myftpdemo.conf;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.*;
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
     * <p>Title: 下载单个文件至指定目录（默认客户端文件命名与服务器一致）</p>
     * @param serverFilePath ftp服务器上的路径
     * @param serverFileName ftp服务器上的文件名
     * @param localFilePath 本地需要保存的的路径
     * @author 韩超 2018/3/9 15:55
     */
    public static boolean downloadSingleFile(String serverFilePath,String serverFileName,String localFilePath){
        return downloadSingleFile(serverFilePath,serverFileName,localFilePath,serverFileName);
    }

    /**
     * <p>Title: 下载单个文件至指定目录（需指定本地文件名）</p>
     * @param serverFilePath ftp服务器上的路径
     * @param serverFileName ftp服务器上的文件名
     * @param localFilePath 本地需要保存的的路径
     * @param localFileName 本地需要保存的的文件名
     * @author 韩超 2018/3/9 15:55
     */
    public static boolean downloadSingleFile(String serverFilePath,String serverFileName,String localFilePath,String localFileName){
        //记录开始时间
        ThreadLocal<Long> startTime = new ThreadLocal<Long>();
        startTime.set(System.currentTimeMillis());
        //使用UUID作为一次上传的标识
        String ftpClientUUID = UUID.randomUUID().toString();
        String serverLocalFileName = serverFilePath +  File.separator + serverFileName;
        String localFullFileName = localFilePath + File.separator + localFileName;
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]开始下载单个文件,服务端文件：" + serverLocalFileName + "---->客户端文件：" + localFullFileName);
        //连接服务器并登录
        FTPClient ftpClient = getFTPClient();
        //切换目录
        try {
            ftpClient.changeWorkingDirectory(serverFilePath);
        } catch (IOException e) {
            LOGGER.info("FTPClient切换目录失败：" + serverFilePath);
            shutdwon(ftpClient);
            e.printStackTrace();
            return false;
        }
        //获取当前目录下的所有FTP文件
        FTPFile[] ftpFiles = null;
        try {
            ftpFiles = ftpClient.listFiles();
        } catch (IOException e) {
            LOGGER.info("FTPClient获取目录下文件失败：" + serverFilePath);
            shutdwon(ftpClient);
            e.printStackTrace();
            return false;
        }
        //如果ftpFiles为空，则表示未找到文件
        if(null == ftpFiles){
            LOGGER.info("FTPClient未找到相关文件！");
            return false;
        }
        Boolean find = false;//是否文文件
        for (FTPFile ftpFile : ftpFiles){
            //如果找到文件，则下载
            if(serverFileName.equalsIgnoreCase(ftpFile.getName())){
                find = true;
                File localFile = new File(localFullFileName);
                try {
                    find = ftpClient.retrieveFile(serverFileName,new FileOutputStream(localFile));
                    LOGGER.debug("FTPClient....下载完成");
                } catch (IOException e) {
                    LOGGER.info("FTPClient下载件失败");
                    shutdwon(ftpClient);
                    e.printStackTrace();
                }
            }
        }
        if(!find){
            LOGGER.info("FTPClient未找到相关文件！");
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
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]单个文件下载完成...用时：" + useTime + "ms");
        return true;
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
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]开始上传单个文件,客户端文件：" + sourceFile.getName() + "---->服务端文件：" + serverFileName);
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
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]单个文件上传完成...用时：" + useTime + "ms");
        return true;
    }


    /**
     * <p>Title: 上传一个目录及子目录下所有的文件</p>
     * @param localDirectory 本地需要上传的目录
     * @param serverFilePath 服务端保存的目录位置
     * @author 韩超 2018/3/9 16:38
     */
    public static boolean uploadDirectory(File localDirectory, String serverFilePath){
        //记录开始时间
        ThreadLocal<Long> startTime = new ThreadLocal<Long>();
        startTime.set(System.currentTimeMillis());
        //使用UUID作为一次上传的标识
        String ftpClientUUID = UUID.randomUUID().toString();
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]开始上传目录及子目录所有文件,客户端文件：" + localDirectory.getAbsolutePath() + "---->服务端文件：" + serverFilePath);
        //连接服务器并登录
        FTPClient ftpClient = getFTPClient();
        //上传目文件
        try {
            storeAllFiles(localDirectory.getParentFile().getAbsolutePath(),localDirectory,ftpClient,serverFilePath);
            LOGGER.debug("FTPClient....批量上传完成");
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("FTPClient批量上传文件失败");
            shutdwon(ftpClient);
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
        LOGGER.info("FTPClient[" + ftpClientUUID+ "]批量文件上传完成...用时：" + useTime + "ms");
        return true;
    }

    /**
     * <p>Title: 递归访问指定目录及子目录下的文件</p>
     * @param ftpClient FTPClient实例
     * @param file 本地需要上传的目录
     * @param serverFilePath ftp服务器保存的目录
     * @author 韩超 2018/3/9 16:53
     */
    public static void storeAllFiles(String superDirectoryPath,File file,FTPClient ftpClient,String serverFilePath) throws IOException {
        //如果当前文件是文件夹，则继续进行递归
        if (file.isDirectory()){
            LOGGER.info("directory: " + file.getAbsolutePath());
            //获取当前文件的父目录
            File supFile = file.getParentFile();
            //获取当前文件的后缀
            String localFileSuffix = supFile.getAbsolutePath().substring(superDirectoryPath.length());
            //获取当前文件的服务路径
            String serverFullFilePath = serverFilePath + File.separator + localFileSuffix;
            //创建服务端目录
            boolean reuslt1 = ftpClient.makeDirectory(serverFullFilePath);
            LOGGER.info("创建目录：" + serverFullFilePath + "==" + reuslt1);
            //列出所有文件
            File[] files = file.listFiles();
            for (File subFile : files){
                storeAllFiles(superDirectoryPath,subFile,ftpClient,serverFilePath);
            }
        }else{//如果当前文件是文件，则打印输出
            //获取当前文件的父目录
            File supFile = file.getParentFile();
            //获取当前文件的后缀
            String localFileSuffix = supFile.getAbsolutePath().substring(superDirectoryPath.length());
            //获取当前文件的服务路径
            String serverFullFilePath = serverFilePath + File.separator + localFileSuffix;
            //创建服务端目录
            boolean reuslt1 = ftpClient.makeDirectory(serverFullFilePath);
            LOGGER.info("创建目录：" + serverFullFilePath + "==" + reuslt1);
            //切换服务端目录
            boolean reuslt2 =  ftpClient.changeWorkingDirectory(serverFullFilePath);
            LOGGER.info("切换目录：" + serverFullFilePath + "==" + reuslt2);
            //上传文件
            LOGGER.info(file.getAbsolutePath() + "--->" + serverFullFilePath + File.separator + file.getName());
            ftpClient.storeFile(file.getName(),new FileInputStream(file));
        }
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
            //设置本地被动模式
            ftpClient.enterLocalPassiveMode();
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
     * <p>Title: 自定义线程进行多线程下载测试</p>
     * @author 韩超 2018/3/9 16:19
     */
    static class MyThreadForSingleDownload extends Thread{
        String serverFilePath;
        String serverFileName;
        String localFilePath;
        String localFileName;

        public MyThreadForSingleDownload(String serverFilePath, String serverFileName, String localFilePath, String localFileName) {
            this.serverFilePath = serverFilePath;
            this.serverFileName = serverFileName;
            this.localFilePath = localFilePath;
            this.localFileName = localFileName;
        }

        @Override
        public void run(){
            FtpServerUtils.downloadSingleFile(serverFilePath,serverFileName,localFilePath,localFileName);
        }
    }


    /**
     * <p>Title: 测试</p>
     * @author 韩超 2018/3/9 13:59
     */
    public static void main(String[] args) throws IOException {
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

        //单个文件下载测试
//        String serverFilePath = "\\pic\\png";
//        String serverFileName = "55.jpg";
//        String localFilePath = "F:\\myftpservertest\\";
//        String localFileName = "5555.jpg";
//        FtpServerUtils.downloadSingleFile(serverFilePath,serverFileName,localFilePath,localFileName);
//        FtpServerUtils.downloadSingleFile(serverFilePath,serverFileName,localFilePath);

//        for(int i = 0; i < 5; i++) {
//            String serverFilePath = "\\pic\\png";
//            String serverFileName = i + "" + i + ".jpg";
//            String localFilePath = "F:\\myftpservertest\\";
//            String localFileName = i + "" + i + i + "" + i + ".jpg";
//            new MyThreadForSingleDownload(serverFilePath,serverFileName,localFilePath,localFileName).start();
//        }

//        storeAllFiles("D:\\",new File("D:\\JsonViewerPackage"),null,"\\pic\\png");
        uploadDirectory(new File("H:\\eclipse"),"\\pic\\png");
        //408MB -- 86992ms -- 87s -- 4.69MB/s
        //多检查上传多个目录文件
    }
}
