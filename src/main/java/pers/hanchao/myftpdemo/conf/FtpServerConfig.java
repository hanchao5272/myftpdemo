package pers.hanchao.myftpdemo.conf;

/**
 * FTP服务器配置
 * Created by 韩超 on 2018/3/9.
 */
public class FtpServerConfig {
    /** IP地址 130.10.8.100 **/
    private String ip = "130.10.8.100";
//    private String ip = "127.0.0.1";
    /** 端口号 **/
    private Integer port = 21;
    /** 用户名 **/
    private String username = "anonymous";
    /** 密码 **/
    private String password = "test";
    /** 编码方式 **/
    private String encoding = "utf-8";

    @Override
    public String toString() {
        return "FtpServerConfig{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", encoding='" + encoding + '\'' +
                '}';
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
