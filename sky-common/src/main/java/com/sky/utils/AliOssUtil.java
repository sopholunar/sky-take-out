package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;    //OSS 服务的域名地址，决定了你的存储桶所在的数据中心
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;  //表示阿里云 OSS 中的存储桶名称，用来存放文件

    /**
     * 文件上传
     * 用于实现文件上传到阿里云 OSS（对象存储服务）的功能。
     * 它包含了文件上传的逻辑，并提供了详细的错误处理和日志记录。
     *
     * upload 方法接收两个参数：
     * bytes：表示要上传的文件内容，以字节数组的形式传入。
     * objectName：在 OSS 上存储的文件名称，也就是上传后在 OSS 中的路径。
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        // OSSClientBuilder()：通过构建器模式创建 OSSClient 实例，OSSClient 是操作阿里云 OSS 的客户端。
        // build(endpoint, accessKeyId, accessKeySecret)：构建 OSSClient 实例并传入配置信息，包括 endpoint（OSS的域名）、accessKeyId 和 accessKeySecret（用于身份认证）。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            // putObject：将文件上传到指定的 OSS 存储桶。该方法的参数：
            // bucketName：目标存储桶名称。
            // objectName：文件在 OSS 上的名称。
            // new ByteArrayInputStream(bytes)：将字节数组转为输入流，putObject 需要一个输入流来上传文件。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            //// OSSException 表示请求已经到达 OSS，但由于某些原因被拒绝。
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            //// ClientException 表示客户端发生了严重错误，无法访问 OSS 服务。
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();   //关闭ossClient，释放资源
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString()); //使用 Lombok 提供的 log 对象，记录上传成功后的文件 URL

        return stringBuilder.toString();
    }
}
