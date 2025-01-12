package com.sky.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 * Serializable 是 Java 的一个标记接口，它表示该类可以被序列化。序列化的过程是将对象的状态转换成字节流的过程，通常用于将对象写入文件、网络传输等场景。
 * 在分页查询的场景中，如果需要将 PageResult 对象传输到远程服务或存储到磁盘上时，实现 Serializable 可以确保类的对象能够顺利地进行序列化
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {

    private long total; //总记录数

    private List records; //当前页数据集合

}
