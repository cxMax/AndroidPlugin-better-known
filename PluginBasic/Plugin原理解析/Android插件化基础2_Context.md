## reference :
* https://www.jianshu.com/p/e6ce2d03f8f9

## Context :
* Context : 抽象类
* ContextImpl : Context的具体实现类，实现了Context的所有功能
* ContextWrapper : Context的包装类 , mBase为ContextImpl对象
* ContextThemeWrapper :　封装了包含了与主题相关的接口，　为Application或者Activity指定的主题
* Application : 不需要界面，所以直接继承自ContextWrapper
* Service : 不需要界面，所以直接继承自ContextWrapper
* Activity : 需要界面，所以直接继承自ContextThemeWrapper