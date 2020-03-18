# Fresco源码

## 参考
* https://github.com/sucese/android-open-framework-analysis/blob/master/doc/Android%E5%BC%80%E6%BA%90%E6%A1%86%E6%9E%B6%E6%BA%90%E7%A0%81%E9%89%B4%E8%B5%8F%EF%BC%9AFresco.md

## 摘要
* Fresco里还大量运用各种设计模式，例如：Builder、Factory、Wrapper、Producer/Consumer、Adapter等
* Fresco的设计很好的体现了面向接口编程这一点，大部分功能都基于接口设计，然后设计出抽象类AbstractXXX，用来封装通用的功能，个别具体的功能交由其子类实现

## 记录
* DraweeView：继承于ImageView，只是简单的读取xml文件的一些属性值和做一些初始化的工作，图层管理交由Hierarchy负责，图层数据获取交由负责。
* DraweeHierarchy：由多层Drawable组成，每层Drawable提供某种功能（例如：缩放、圆角）。
    * 图层管理，由多层Drawable组成，每层Drawable提供某种功能
* DraweeController：控制数据的获取与图片加载，向pipeline发出请求，并接收相应事件，并根据不同事件控制Hierarchy，从DraweeView接收用户的事件，然后执行取消网络请求、回收资源等操作。
    * 所有的图片数据的处理都是由它来完成的
* DraweeHolder：统筹管理Hierarchy与DraweeHolder。
* ImagePipeline：Fresco的核心模块，用来以各种方式（内存、磁盘、网络等）获取图像。
* Producer/Consumer：Producer也有很多种，它用来完成网络数据获取，缓存数据获取、图片解码等多种工作，它产生的结果由Consumer进行消费。
* IO/Data：这一层便是数据层了，负责实现内存缓存、磁盘缓存、网络缓存和其他IO相关的功能。

## Fresco 源码流程
1.1 初始化Fresco
1. 加载so库
2. 初始化ImagePipelineConfig
    * ImagePipelineConfig是干嘛的？
        * ImagePipeline：Fresco的核心模块，用来以各种方式（内存、磁盘、网络等）获取图像。
3. 初始化SimpleDraweeView。
    * 提供DraweeControllerBuilder用来构建DraweeController
        * DraweeController是干嘛的？
            * 负责图片数据的获取与加载
4. 日志跟踪

1.2 获取DataSource
* DataSource是一个接口其实现类是AbstractDataSource，它可以提交数据请求，并能获取progress、fail result与success result等信息
    * CloseableProducerToDataSourceAdapter：继承自AbstractProducerToDataSourceAdapter，实现了closeResult()方法，绘制自己销毁时同时销毁Result，这个是最主要使用的DataSource
* ImagePipeline.fetchDecodedImage()
    * 获取缓存级别，RequestLevel将缓存分为四级
        * FULL_FETCH(1) 从网络或者本地存储获取，
        * DISK_CACHE(2) 从磁盘缓存获取，
        * ENCODED_MEMORY_CACHE(3)从未解码的内存缓存获取，
        * BITMAP_MEMORY_CACHE(4)已解码的内存缓存获取。
    * 将ImageRequest、RequestListener等信息封装进SettableProducerContext，ProducerContext是Producer 的上下文环境，利用ProducerContext可以改变Producer内部的状态。
    * 创建CloseableProducerToDataSourceAdapter，CloseableProducerToDataSourceAdapter是DataSource的一种。

1.3 绑定DraweeController与DraweeHierarchy
* DraweeHolder用来统筹管理Controller与Hierarchy
    * 每次都会设置新的Hierarchy

1.4 从内存缓存/磁盘缓存/网络获取图片，并设置到对应的Drawable层
* 最终调用的GenericDraweeHierarchy.setImage , mActualImageWrapper.setDrawable(drawable);

## DraweeHierarchy
2.1 图层的层级构造
* Fresco里定义了许多Drawable，它们都直接或者间接的继承了Drawable，来实现不同的功能
2.2 图层的创建流程
* GenericDraweeHierarchy是负责加载每个图层信息的载体

## Producer与Consumer
* Fresco里实现了多个Producer，按照功能划分可以分为以下几类:
    * LocalFetchProducer
    * NetworkFetchProducer
* 那么这些Producer是在哪里构建的呢？
    * 我们前面说过，在构建DataSource的时候
* 从网络获取图片的时候Producer序列
    * PostprocessedBitmapMemoryCacheProducer，非必须 ，在Bitmap缓存中查找被PostProcess过的数据。
    * BitmapMemoryCacheGetProducer，必须，使Producer序列只读。
    * BitmapMemoryCacheProducer，必须，从已解码的内存缓存中获取数据。
    * EncodedMemoryCacheProducer，必须，从未解码的内存缓存中获取数据。
    * DiskCacheProducer，必须，从文件缓存中获取数据。
    * WebpTranscodeProducer，非必须，将下层Producer产生的Webp（如果是的话）进行解码。
    * NetworkFetchProducer，必须，从网络上获取数据。
* Producer在处理数据时是向下传递的，而Consumer来接收结果时则是向上传递的

## 缓存机制
* Fresco里有三级缓存，两级内存缓存，一级磁盘缓存
    * 未编码图片内存缓存
    * 已编码图片内存缓存
    * 磁盘缓存
        * 缓冲缓存层：由BufferedDiskCache实现，提供缓冲功能。
        * 文件缓存层：由DiskStroageCache实现，提供实际的缓存功能。
        * 文件存储层：由DefaultDiskStorage实现，提供磁盘文件读写的功能。
3.1 内存缓存
 * CountingLruMap直接使用LinkedHashMap，它内部有一个双向链表