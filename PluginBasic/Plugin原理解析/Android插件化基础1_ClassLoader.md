## reference :
* https://www.jianshu.com/p/ce71e096ebdf

## ClassLoader :
*作用 :
  * 负责将Class加载到JVM中
  * 审查每个类由谁加载(父类优先的等级加载机制)
  * 将Class字节码重新解析成JVM统一要求的对象格式
* Java的ClassLoader :
  * BootStrap ClassLoader : 负责加载JDK中的核心类库，如：rt.jar，resource.jar,charsets.jar
    * Extension ClassLoader : 加载java的扩展类 , 加载JAVA_HOME/jre/lib/ext/目录下的所有jar
    * App ClassLoader : 加载应用程序classpath目录下所有jar和class文件，一般来说，Java应用的类都是由它们来完成加载的
* JavaClassLoader的规则 :
  * 自底向上检查类是否已经加载
  * 自顶向下尝试加载类
## android类的加载 :
  * PathClassLoader : 只能加载已经安装应用的dex或者Apk文件
  * DexClassLoader : 可以从SD卡上加载包含class.dex的jar和.apk文件
  * PathClassLoader是通过构造函数new DexFile(path)来产生DexFile对象的
  * DexClassLoader则是通过静态方法loadDex(path,outpath,0)得到DexFile对象
## Apk打包 :
  * ODEX : Optimised Dex , 安装应用时 ,DexOpt是第一次加载Dex文件的时候执行 生成ODEX
  * ODEX的用途是分离程序资源和可执行文件，达到快速软件加载速度和开机速度的目的
  * ART , 应用程序会在安装时被编译成OAT文件 , ODEX和dex都会转化为OAT文件
## Apk安装 :
  * 先把apk拷贝到/data/app下， 没错，就是完整的apk
  * 解压apk，把其中的classes.dex 拷贝到/data/dalvik-cache
  * 在/data/data下创建对应的目录，用于存储程序的数据，例如cache, database等
  * 备注：资源文件的读取还是通过apk读取的,并未单独拷贝