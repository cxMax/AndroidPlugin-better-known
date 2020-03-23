# okhttp源码
https://github.com/sucese/android-open-framework-analysis/blob/master/doc/Android%E5%BC%80%E6%BA%90%E6%A1%86%E6%9E%B6%E6%BA%90%E7%A0%81%E9%89%B4%E8%B5%8F%EF%BC%9AOkhttp.md#%E4%B8%80-%E8%AF%B7%E6%B1%82%E4%B8%8E%E5%93%8D%E5%BA%94%E6%B5%81%E7%A8%8B

## HttpUrlConnection 和 OKhttp的区别

1. HttpUrlConnection ：
    1. Google官方提供的
    2. 仅仅支持http 1.0/1.1协议
    3. 并没有上面讲的多路复用，如果碰到app大量网络请求的时候，性能比较差
2. OkHttp ：
    1. 和HttpUrlConnection一样实现了一个网络连接的过程
    2. IO方面用到的是InputStream和OutputStream，但是OkHttp用的是sink和source，这两个是在Okio这个开源库里的，sink相当于outputStream，source相当于是inputStream。sink和source比InputStream和OutputStream更加强大，单拿sink举例，他的子类有BufferedSink(支持缓冲)、GzipSink（支持Gzip压缩）、ForwardingSink和InflaterSink（后面这两者服务于GzipSink）
    3. HTTP/2 支持允许所有访问同一主机的请求共享一个socket
    4. 利用连接池减少请求延迟（如果HTTP/2不可用）
    5. 支持GZIP压缩
    6. 响应缓存减少重复请求

## 请求与响应流程
1.1 请求的封装
* RealCall实现了Call接口，它封装了请求的调用，这个构造函数的逻辑也很简单：赋值外部传入的OkHttpClient、Request与forWebSocket，并 创建了重试与重定向拦截器RetryAndFollowUpInterceptor

1.2 请求的发送
* RealCall将请求分为两种：
    * 同步请求
    * 异步请求

1.3 请求的调度
* Dispatcher是一个任务调度器，它内部维护了三个双端队列：
    * readyAsyncCalls：准备运行的异步请求
    * runningAsyncCalls：正在运行的异步请求
    * runningSyncCalls：正在运行的同步请求

1.4 请求的处理
* Interceptor将网络请求、缓存、透明压缩等功能统一了起来，它的实现采用责任链模式，各司其职， 每个功能都是一个Interceptor，上一级处理完成以后传递给下一级，它们最后连接成了一个Interceptor.Chain
    * RetryAndFollowUpInterceptor：负责重定向。
    * BridgeInterceptor：负责把用户构造的请求转换为发送给服务器的请求，把服务器返回的响应转换为对用户友好的响应。
    * CacheInterceptor：负责读取缓存以及更新缓存。
    * ConnectInterceptor：负责与服务器建立连接。
    * CallServerInterceptor：负责从服务器读取响应的数据

## 拦截器
2.1 RetryAndFollowUpInterceptor
* 构建一个StreamAllocation对象，StreamAllocation相当于是个管理类，维护了Connections、Streams和Calls之间的管理，该类初始化一个Socket连接对象，获取输入/输出流对象。
* 继续执行下一个Interceptor，即BridgeInterceptor
* 抛出异常，则检测连接是否还可以继续，以下情况不会重试：
    * 客户端配置出错不再重试
    * 出错后，request body不能再次发送
    * 发生以下Exception也无法恢复连接：
        * ProtocolException：协议异常
        * InterruptedIOException：中断异常
        * SSLHandshakeException：SSL握手异常
        * SSLPeerUnverifiedException：SSL握手未授权异常
没有更多线路可以选择
* 根据响应码处理请求，返回Request不为空时则进行重定向处理，重定向的次数不能超过20次。

2.2 BridgeInterceptor
* BridgeInterceptor主要就是针对Header做了一些处理
    * 开发者没有添加Accept-Encoding时，自动添加Accept-Encoding: gzip
    * 自动添加Accept-Encoding，会对request，response进行自动解压
    * 手动添加Accept-Encoding，不负责解压缩
    * 自动解压时移除Content-Length，所以上层Java代码想要contentLength时为-1
    * 自动解压时移除 Content-Encoding
    * 自动解压时，如果是分块传输编码，Transfer-Encoding: chunked不受影响。

2.3 CacheInterceptor
* 整个方法的流程如下所示：
    * 读取候选缓存，具体如何读取的我们下面会讲。
    * 创建缓存策略，强制缓存、对比缓存等，关于缓存策略我们下面也会讲。
    * 根据策略，不使用网络，又没有缓存的直接报错，并返回错误码504。
    * 根据策略，不使用网络，有缓存的直接返回。
    * 前面两个都没有返回，继续执行下一个Interceptor，即ConnectInterceptor。
    * 接收到网络结果，如果响应code式304，则使用缓存，返回缓存结果。
    * 读取网络结果。
    * 对数据进行缓存。
    * 返回网络读取的结果。

2.4 ConnectInterceptor
* 在RetryAndFollowUpInterceptor里初始化了一个StreamAllocation对象，我们说在这个StreamAllocation对象里初始化了一个Socket对象用来做连接，但是并没有 真正的连接，等到处理完hader和缓存信息之后，才调用ConnectInterceptor来进行真正的连接
* ConnectInterceptor在Request阶段建立连接，处理方式也很简单，创建了两个对象：
    * HttpCodec：用来编码HTTP requests和解码HTTP responses
    * RealConnection：连接对象，负责发起与服务器的连接。

2.5 CallServerInterceptor
* CallServerInterceptor负责从服务器读取响应的数据。
* 我们通过ConnectInterceptor已经连接到服务器了，接下来我们就是写入请求数据以及读出返回数据了。整个流程：
    * 写入请求头
    * 写入请求体
    * 读取响应头
    * 读取响应体

## 连接机制
* 连接的创建是在StreamAllocation对象统筹下完成的，我们前面也说过它早在RetryAndFollowUpInterceptor就被创建了，StreamAllocation对象 主要用来管理两个关键角色：
    * RealConnection：真正建立连接的对象，利用Socket建立连接。
    * ConnectionPool：连接池，用来管理和复用连接。
* 在里初始化了一个StreamAllocation对象，我们说在这个StreamAllocation对象里初始化了一个Socket对象用来做连接，但是并没有

3.1 创建连接
* StreamAllocation.newStream()最终调动findConnect()方法来建立连接。
    * 整个流程如下：
        * 查找是否有完整的连接可用：
            * Socket没有关闭
            * 输入流没有关闭
            * 输出流没有关闭
            * Http2连接没有关闭
        * 连接池中是否有可用的连接，如果有则可用。
        * 如果没有可用连接，则自己创建一个。
        * 开始TCP连接以及TLS握手操作。
        * 将新创建的连接加入连接池。
上述方法完成后会创建一个RealConnection对象，然后调用该方法的connect()方法建立连接，我们再来看看RealConnection.connect()方法的实现。

3.2 连接池
* 我们知道在负责的网络环境下，频繁的进行建立Sokcet连接（TCP三次握手）和断开Socket（TCP四次分手）是非常消耗网络资源和浪费时间的，HTTP中的keepalive连接对于 降低延迟和提升速度有非常重要的作用。
* 复用连接就需要对连接进行管理，这里就引入了连接池的概念。
* Okhttp支持5个并发KeepAlive，默认链路生命为5分钟(链路空闲后，保持存活的时间)，连接池有ConectionPool实现，对连接进行回收和管理。
* ConectionPool在内部维护了一个线程池，来清理连接


* 清理cleanup()方法的具体实现:
    * 查询此连接内部的StreanAllocation的引用数量。
    * 标记空闲连接。
    * 如果空闲连接超过5个或者keepalive时间大于5分钟，则将该连接清理掉。
    * 返回此连接的到期时间，供下次进行清理。
    * 全部都是活跃连接，5分钟时候再进行清理。
    * 没有任何连接，跳出循环。
    * 关闭连接，返回时间0，立即再次进行清理。
* 在RealConnection里有个StreamAllocation虚引用列表，每创建一个StreamAllocation，就会把它添加进该列表中，如果留关闭以后就将StreamAllocation 对象从该列表中移除，正是利用利用这种引用计数的方式判定一个连接是否为空闲连接,查找引用计数由pruneAndGetAllocationCount()方法实现

## 缓存机制
3.1 缓存策略
* HTTP的缓存可以分为两种：
    * 强制缓存：需要服务端参与判断是否继续使用缓存，当客户端第一次请求数据是，服务端返回了缓存的过期时间（Expires与Cache-Control），没有过期就可以继续使用缓存，否则则不适用，无需再向服务端询问。
    * 对比缓存：需要服务端参与判断是否继续使用缓存，当客户端第一次请求数据时，服务端会将缓存标识（Last-Modified/If-Modified-Since与Etag/If-None-Match）与数据一起返回给客户端，客户端将两者都备份到缓存中 ，再次请求数据时，客户端将上次备份的缓存 标识发送给服务端，服务端根据缓存标识进行判断，如果返回304，则表示通知客户端可以继续使用缓存。
* 强制缓存优先于对比缓存。

* 上面提到强制缓存使用的的两个标识：
    * Expires：Expires的值为服务端返回的到期时间，即下一次请求时，请求时间小于服务端返回的到期时间，直接使用缓存数据。到期时间是服务端生成的，客户端和服务端的时间可能有误差。
    * Cache-Control：Expires有个时间校验的问题，所有HTTP1.1采用Cache-Control替代Expires。

* Cache-Control的取值有以下几种：
    * private: 客户端可以缓存。
    * public: 客户端和代理服务器都可缓存。
    * max-age=xxx: 缓存的内容将在 xxx 秒后失效
    * no-cache: 需要使用对比缓存来验证缓存数据。
    * no-store: 所有内容都不会缓存，强制缓存，对比缓存都不会触发。

* 我们再来看看对比缓存的两个标识：
    * Last-Modified/If-Modified-Since
    * Last-Modified 表示资源上次修改的时间。
        * 当客户端发送第一次请求时，服务端返回资源上次修改的时间：
        * Last-Modified: Tue, 12 Jan 2016 09:31:27 GMT
        * 客户端再次发送，会在header里携带If-Modified-Since。将上次服务端返回的资源时间上传给服务端。
        * If-Modified-Since: Tue, 12 Jan 2016 09:31:27 GMT
        * 服务端接收到客户端发来的资源修改时间，与自己当前的资源修改时间进行对比，如果自己的资源修改时间大于客户端发来的资源修改时间，则说明资源做过修改， 则返回200表示需要重新请求资源，否则返回304表示资源没有被修改，可以继续使用缓存。

* 上面是一种时间戳标记资源是否修改的方法，还有一种资源标识码ETag的方式来标记是否修改，如果标识码发生改变，则说明资源已经被修改，ETag优先级高于Last-Modified。
    * Etag/If-None-Match
        * ETag是资源文件的一种标识码，当客户端发送第一次请求时，服务端会返回当前资源的标识码：
        * ETag: "5694c7ef-24dc"
        * 客户端再次发送，会在header里携带上次服务端返回的资源标识码：
        * If-None-Match:"5694c7ef-24dc"
        * 服务端接收到客户端发来的资源标识码，则会与自己当前的资源吗进行比较，如果不同，则说明资源已经被修改，则返回200，如果相同则说明资源没有被修改，返回 304，客户端可以继续使用缓存。

* 以上便是HTTP缓存策略的相关理论知识，我们来看看具体实现。
* Okhttp的缓存策略就是根据上述流程图实现的，具体的实现类是CacheStrategy
    * 这两个参数参数的含义如下：
        * networkRequest：网络请求。
        * cacheResponse：缓存响应，基于DiskLruCache实现的文件缓存，可以是请求中url的md5，value是文件中查询到的缓存，这个我们下面会说
* CacheStrategy就是利用这两个参数生成最终的策略，有点像map操作，将networkRequest与cacheResponse这两个值输入，处理之后再将这两个值输出，们的组合结果如下所示：
    * 如果networkRequest为null，cacheResponse为null：only-if-cached(表明不进行网络请求，且缓存不存在或者过期，一定会返回503错误)。
    * 如果networkRequest为null，cacheResponse为non-null：不进行网络请求，而且缓存可以使用，直接返回缓存，不用请求网络。
    * 如果networkRequest为non-null，cacheResponse为null：需要进行网络请求，而且缓存不存在或者过期，直接访问网络。
    * 如果networkRequest为non-null，cacheResponse为non-null：Header中含有ETag/Last-Modified标签，需要在条件请求下使用，还是需要访问网络。

3.2 缓存管理
* 这篇文章我们来分析Okhttp的缓存机制，缓存机制是基于DiskLruCache做的。Cache类封装了缓存的实现，实现了InternalCache接口。
* 在Cache类里还定义一些内部类，这些类封装了请求与响应信息。
    * Cache.Entry：封装了请求与响应等信息，包括url、varyHeaders、protocol、code、message、responseHeaders、handshake、sentRequestMillis与receivedResponseMillis。
    * Cache.CacheResponseBody：继承于ResponseBody，封装了缓存快照snapshot，响应体bodySource，内容类型contentType，内容长度contentLength。