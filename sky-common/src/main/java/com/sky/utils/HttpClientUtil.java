package com.sky.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final  int TIMEOUT_MSEC = 5 * 1000;

    /**
     * 发送GET方式请求
     * @param url
     * @param paramMap
     * @return
     */
    public static String doGet(String url,Map<String,String> paramMap){
        // 创建Httpclient对象
        //创建一个默认的 CloseableHttpClient 实例，该实例具备发送请求、接收响应的能力
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        CloseableHttpResponse response = null;

        try{
            //URIBuilder 的优势在于，它能够自动处理 URL 编码的问题。
            // 当你添加查询参数时，它会确保所有特殊字符（如空格、&、= 等）都被正确编码，而不需要你手动处理 URL 编码。
            //url 是传入的基础 URL，通常是没有包含查询参数（?param=value）的部分。例如：https://example.com/api/resource
            URIBuilder builder = new URIBuilder(url);

            /*对于 paramMap 中的每个键值对，
            调用 builder.addParameter 方法将参数添加到 URIBuilder 对象中。
            每次调用都会将一个新的查询参数添加到 URL 中。
            假设 paramMap 为：{"key1": "value1", "key2": "value2"}
            那么，builder.addParameter 会将 key1=value1 和 key2=value2 添加到 URIBuilder 中，
            构成完整的查询字符串（如 ?key1=value1&key2=value2）。
             */
            if(paramMap != null){
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key,paramMap.get(key));
                }
            }
            URI uri = builder.build();

            //创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            //发送请求
            response = httpClient.execute(httpGet);

            //判断响应状态
            if(response.getStatusLine().getStatusCode() == 200){

                //将响应体内容转化为 String 类型，并指定编码格式为 UTF-8，获得 HTTP 响应的内容
                result = EntityUtils.toString(response.getEntity(),"UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    /*
    在 HTTP POST 请求中，通常会使用 URL 编码的方式将请求参数传递给服务器。
    通过 Apache HttpClient 的 UrlEncodedFormEntity 类，我们需要一个 List<NameValuePair> 类型的集合，来存储这些表单参数。
    而 BasicNameValuePair 就是 NameValuePair 的一个实现，表示一个表单中的单个参数。
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                /*
                List<NameValuePair> 类型的对象专门用于存储键值对形式的表单参数。
                每个 BasicNameValuePair 对象代表一个表单的参数。
                 */
                List<NameValuePair> paramList = new ArrayList();

                /*
                entrySet() 方法返回 paramMap 中所有键值对的 Set<Map.Entry<String, String>> 集合。
                Map.Entry<String, String> 表示 Map 中的一个键值对，即一个 Entry 对象，包含 key 和 value。
                这一步的作用是获取所有的键值对，供后续遍历
                 */
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }

                // 模拟表单
                //  UrlEncodedFormEntity：将 paramList 转换为一个 URL 编码的表单实体，这样生成的实体可以直接作为 POST 请求的请求体
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);

                // 设置请求体
                httpPost.setEntity(entity);
            }

            //超时配置
            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求
     * 通过 POST 请求向指定 URL 发送 JSON 数据的 HTTP 工具方法
     * @param url
     * @param paramMap
     * @return
     * @throws IOException
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //JSONObject 是 org.json 包中的一个类，用来构建 JSON 格式的字符串
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(),param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(),"utf-8");
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置请求体的类型为 application/json，表示传送的内容是 JSON 格式
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    //超时配置
    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)    //连接超时：连接到目标服务器的最大时间
                .setConnectionRequestTimeout(TIMEOUT_MSEC)  //请求获取超时：从连接池获取连接的最大时间
                .setSocketTimeout(TIMEOUT_MSEC).build();    //读取数据超时：从服务器读取数据的最大时间
    }

}
