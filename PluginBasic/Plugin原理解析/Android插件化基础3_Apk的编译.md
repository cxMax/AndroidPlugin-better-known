## reference 
* https://www.jianshu.com/p/85c8ce13fcad

## apk文件 :
* AndroidManifest.xml :
* classes.dex : 可以反编译工具把dex文件转换成class文件
* META-INF : 对的资源文件做的SHA1 hash处理, 签名和公钥证书
* res : 资源文件
* resources.arsc : 资源索引表
* lib : so文件

## 编译工具 :
* aapt : android资源打包工具
* javac : 编译java文件,编译成class文件
* dex : .class文件编译成dex文件
* jarsingner : jar文件签名工具
* zipalign : 字节码对齐工具