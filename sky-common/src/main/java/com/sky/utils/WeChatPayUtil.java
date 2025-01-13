package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * 微信支付工具类
 */
@Component
public class WeChatPayUtil {

    //微信支付下单接口地址
    public static final String JSAPI = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";

    //申请退款接口地址
    public static final String REFUNDS = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 获取调用微信接口的客户端工具对象
     *
     * @return
     */
    private CloseableHttpClient getClient() {
        PrivateKey merchantPrivateKey = null;
        try {
            //merchantPrivateKey商户API私钥，如何加载商户API私钥请看常见问题
            merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath())));

            //加载平台证书文件
            //PemUtil.loadCertificate 方法用于从指定路径加载微信支付平台的 公钥证书。
            //该证书用于验证微信支付平台的签名，确保服务器返回的数据是由微信支付平台发送的
            X509Certificate x509Certificate = PemUtil.loadCertificate(new FileInputStream(new File(weChatProperties.getWeChatPayCertFilePath())));

            //wechatPayCertificates微信支付平台证书列表。你也可以使用后面章节提到的“定时更新平台证书功能”，而不需要关心平台证书的来龙去脉
            List<X509Certificate> wechatPayCertificates = Arrays.asList(x509Certificate);

            /*
            WechatPayHttpClientBuilder 是微信支付 SDK 提供的一个构建器类，用于配置并创建一个可以与微信支付接口交互的 CloseableHttpClient。
            withMerchant 方法将商户的基本信息（如商户号 mchid，商户证书序列号 mchSerialNo，以及商户私钥 merchantPrivateKey）传递给构建器。商户号是微信支付给商户分配的唯一标识符。
            withWechatPay 方法将微信支付平台的公钥证书传递给构建器，微信支付将使用这个证书进行验签。
             */
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(weChatProperties.getMchid(), weChatProperties.getMchSerialNo(), merchantPrivateKey)
                    .withWechatPay(wechatPayCertificates);

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签
            //  httpClient 对象用于后续与微信支付接口的交互，包括下单、退款等操作
            CloseableHttpClient httpClient = builder.build();
            return httpClient;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 发送post方式请求
     *
     * @param url
     * @param body
     * @return
     */
    /*
    该方法发送一个 POST 请求到指定的 URL，并附带 JSON 格式的请求体。
    它设置了请求头，包括 Content-Type 和 Accept，并通过微信支付的商户证书序列号来进行验证
     */
    private String post(String url, String body) throws Exception {
        CloseableHttpClient httpClient = getClient();

        /*
        使用 addHeader 方法为 HTTP 请求添加头信息。
        HttpHeaders.ACCEPT：表示客户端希望接收服务器响应的内容类型，值为 application/json，即请求接受 JSON 格式的响应。
        HttpHeaders.CONTENT_TYPE：表示请求体的内容类型，设置为 application/json，表明请求体是 JSON 格式。
        "Wechatpay-Serial"：自定义的请求头字段，用于指定微信支付的商户证书序列号，这对于微信支付的请求验证是必需的。通过 weChatProperties.getMchSerialNo() 获取商户证书序列号。
        这些请求头确保微信支付 API 能够正确地识别并处理请求。
         */
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpPost.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());
        httpPost.setEntity(new StringEntity(body, "UTF-8"));

        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            httpClient.close();
            response.close();
        }
    }

    /**
     * 发送get方式请求
     *
     * @param url
     * @return
     */
    private String get(String url) throws Exception {
        CloseableHttpClient httpClient = getClient();

        HttpGet httpGet = new HttpGet
                (url);
        httpGet.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        httpGet.addHeader("Wechatpay-Serial", weChatProperties.getMchSerialNo());

        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            String bodyAsString = EntityUtils.toString(response.getEntity());
            return bodyAsString;
        } finally {
            httpClient.close();
            response.close();
        }
    }

    /**
     * jsapi下单
     *
     * @param orderNum    商户订单号
     * @param total       总金额
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return
     */
    private String jsapi(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appid", weChatProperties.getAppid());   //微信公众平台的应用ID，用于标识请求来源。通常通过 weChatProperties.getAppid() 从配置文件中读取
        jsonObject.put("mchid", weChatProperties.getMchid());   //微信支付商户号，用于唯一标识商户。也从配置中读取
        jsonObject.put("description", description); //商品描述，传入方法参数，显示在微信支付页面上，方便用户查看订单详情
        jsonObject.put("out_trade_no", orderNum);   //商户订单号，传入方法参数，用于唯一标识订单
        jsonObject.put("notify_url", weChatProperties.getNotifyUrl());  //微信支付成功后通知商户后台的回调地址。这个地址会在支付成功后，微信支付系统向商户的服务器发送支付结果

        /*
        amount 是一个嵌套的 JSON 对象，包含了支付金额和货币类型。
        total.multiply(new BigDecimal(100))：由于微信支付的金额单位是 分（而不是元），因此需要将金额从元转换为分，乘以 100。
        setScale(2, BigDecimal.ROUND_HALF_UP)：设置金额的小数位数，这里使用了 ROUND_HALF_UP 的四舍五入规则。
        intValue()：将 BigDecimal 转换为整数（单位是分）。
        currency：表示货币的种类，固定为 "CNY"（人民币）
         */
        JSONObject amount = new JSONObject();
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);

        JSONObject payer = new JSONObject();
        payer.put("openid", openid);    //支付者信息，即用户在该商户下的唯一标识符

        jsonObject.put("payer", payer);

        String body = jsonObject.toJSONString();

        //调用之前定义的 post 方法，向微信支付的 JSAPI 下单接口发送请求。JSAPI 是一个常量，表示微信支付的下单接口 URL
        return post(JSAPI, body);
    }

    /**
     * 小程序支付
     *
     * @param orderNum    商户订单号
     * @param total       金额，单位 元
     * @param description 商品描述
     * @param openid      微信用户的openid
     * @return
     */
    public JSONObject pay(String orderNum, BigDecimal total, String description, String openid) throws Exception {
        //统一下单，生成预支付交易单
        //调用了 jsapi 方法来向微信支付服务器发起统一下单请求，生成预支付交易单。
        //jsapi 方法会返回一个 JSON 字符串，其中包含了预支付交易单的 prepay_id，这是后续发起支付时需要的关键参数。
        String bodyAsString = jsapi(orderNum, total, description, openid);

        //解析返回结果
        JSONObject jsonObject = JSON.parseObject(bodyAsString);
        System.out.println(jsonObject);

        String prepayId = jsonObject.getString("prepay_id");
        if (prepayId != null) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);   //生成当前的时间戳（单位为秒），通过 System.currentTimeMillis() 获取当前毫秒级时间戳并除以 1000 转换为秒
            String nonceStr = RandomStringUtils.randomNumeric(32);  //生成一个 32 位的随机字符串，通常用于防止请求重放攻击

            //二次签名，调起支付需要重新签名
            ArrayList<Object> list = new ArrayList<>();
            list.add(weChatProperties.getAppid());
            list.add(timeStamp);
            list.add(nonceStr);
            list.add("prepay_id=" + prepayId);
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : list) {
                stringBuilder.append(o).append("\n");   //将 list 中的参数拼接成待签名的字符串，每个参数后面添加一个换行符 \n
            }
            String signMessage = stringBuilder.toString();
            byte[] message = signMessage.getBytes();

            Signature signature = Signature.getInstance("SHA256withRSA");   //使用 SHA256withRSA 签名算法进行加密签名
            signature.initSign(PemUtil.loadPrivateKey(new FileInputStream(new File(weChatProperties.getPrivateKeyFilePath()))));    //PemUtil.loadPrivateKey 从商户的私钥文件中加载私钥，该私钥用于签名请求数据
            signature.update(message);  //将待签名的消息数据传入签名对象

            //signature.sign()：执行签名操作，得到签名后的字节数组
            //使用 Base64 对签名结果进行编码，得到一个 packageSign，这是小程序支付请求中必须的签名参数
            String packageSign = Base64.getEncoder().encodeToString(signature.sign());

            //构造数据给微信小程序，用于调起微信支付
            /*
            构建一个 JSONObject，包含小程序支付请求所需的参数：
            timeStamp：时间戳。
            nonceStr：随机字符串。
            package：prepay_id 参数，用于标识预支付交易单。
            signType：签名类型，固定为 "RSA"。
            paySign：签名字符串，通过 RSA 签名生成，确保请求的合法性。
             */
            JSONObject jo = new JSONObject();
            jo.put("timeStamp", timeStamp);
            jo.put("nonceStr", nonceStr);
            jo.put("package", "prepay_id=" + prepayId);
            jo.put("signType", "RSA");
            jo.put("paySign", packageSign);

            return jo;
        }
        return jsonObject;
    }

    /**
     * 申请退款
     *
     * @param outTradeNo    商户订单号
     * @param outRefundNo   商户退款单号
     * @param refund        退款金额
     * @param total         原订单金额
     * @return
     */
    public String refund(String outTradeNo, String outRefundNo, BigDecimal refund, BigDecimal total) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("out_trade_no", outTradeNo);
        jsonObject.put("out_refund_no", outRefundNo);

        JSONObject amount = new JSONObject();
        amount.put("refund", refund.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("total", total.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).intValue());
        amount.put("currency", "CNY");

        jsonObject.put("amount", amount);
        jsonObject.put("notify_url", weChatProperties.getRefundNotifyUrl());

        String body = jsonObject.toJSONString();

        //调用申请退款接口
        return post(REFUNDS, body);
    }
}
