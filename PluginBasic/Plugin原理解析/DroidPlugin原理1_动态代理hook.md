
## reference : 

* DroidPlugin插件化概要 : http://weishu.me/2016/01/28/understand-plugin-framework-overview/
* 动态代理 : 
    * iInvocationHandler : http://www.cnblogs.com/xiaoluo501395377/p/3383130.html
    * InvocationHandler : https://www.jianshu.com/p/e2917b0b9614
    * cglib : http://www.cnblogs.com/shijiaqi1066/p/3429691.html



## 插件化需解决的问题 : 

* 代码加载 : 
    * 类加载
* 组件声明周期管理 : service , activity
    * 资源加载
    * 插件 和 宿主 资源的问题解决方案

## 插件化实现机制之一 - 代理 : 

* 静态代理 : 
    * 我的理解就是 设计模式 proxy 
* 动态代理 : 
    * JDK提供 : 但有所限制,只支持接口 , 
        * InvocationHandler / Proxy ,
    * cglib实现 : 
        * Enhancer / MethodInterceptor
    * 总结 : 
        * DroidPlugin 就是通过 动态代理 + 反射对关键类/函数进行替换 从而达到hook的目的

                                        
