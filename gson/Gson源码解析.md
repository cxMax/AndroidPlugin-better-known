# 手写Json解析工具
> 之前面试遇到一道编程题，当时完全没思路， 因此学习记录Gson源码

## Gson一些类的说明
> 早期的Gson例如1.6版本中，Json数据先通过JsonReader/JsonWriter转换到JsonToken流，再通过Streams工具类转换到JsonElement树，最后由JsonSerializer/JsonDeserializer转换到Java对象。
> Gson在2.0版本中引入了TypeAdapter，并在2.1版本开放接口。TypeAdapter可以直接转换JsonToken流和Java对象，可以不经过JsonElement，性能得到了提高

### JsonToken
> Json流式操作API
> 首先定义JsonToken, 是一个枚举类型，每种类型表示一个原始Json元素
* Type是Java中的接口，表示对象类型。Type有一个实现类Class（普通类型，例如Object、ArrayList）和几个子类接口：
    * GenericArrayType（数组类型，例如String[]）
    * ParameterizedType（泛型类型，例如List<String>）
    * WildcardType（形如? extends ClassA、？super ClassB）
    * TypeVariable（类型变量）

### JsonReader、JsonWriter
> 用于读写Json字符串，在Json字符串和JsonToken之间转换。
* JsonReader.doPeek()
    * 真正的解析过程就在这里面

### JsonElement
* JsonPrimitive：Json基本类型，例如1, "text", true
* JsonObject：Json对象，例如{"key", "val"}
* JsonArray：Json数组，例如[{"key", "val1"}, {"key", "val2"}]
* JsonNull：Json Null元素，即null

### JsonSerializer、JsonDeserializer
> JsonSerializer和JsonDeserializer用于实现JsonElement树到Java对象之间的转换。

### TypeAdapter
> JsonToken转JavaObject
* write：序列化，Java对象 –> JsonToken –> JsonWriter
* read：反序列化，JsonReader –> JsonToken –> Java对象

### TypeAdapters
> TypeAdapters类中包含了一些基本类型的TypeAdapter实现
* 这里面有匿名类TypeAdapter去实现序列化和反序列化
* 序列化和反序列化对应又调用的JsonReader和JsonWriter

### TypeAdapterFactory：创建TypeAdapter
> TypeAdapterFactory用于创建TypeAdapter。传入特定的type，Factory返回相应的TypeAdapter实例

### Gson、GsonBuilder
> 通常使用Gson对象来序列化/反序列化Json数据。

## Gson 关键类的梳理
* Gson 开发者直接使用的类，只对输入和输出负责。
* TypeToken 封装“操作类”（Gson.fromJson(json,People.class、Gson.toJson(new People)) 两处的People都是操作类）的类型。
* TypeAdapter 直接操作序列化与反序列化的过程，所以该抽象类中存在read()和write方法。
* TypeAdapterFactory 用于生产TypeAdapter的工厂类。
* GsonReader和GsonWriter是Gson处理内容的包装流

## Gson.from()关键流程解析
* TypeToken对象的获取
* 根据TypeToken获取TypeAdapter对象
* 由TypeAdapter对象解析json字符串

### 解析单个基础类型(String类型)对象流程
1. TypeToken的获取
    * 获取type的（type、rawType和hashcode）
        * 其中构造函数中的成员变量type为$Gson$Types包装实现序列化之后的操作
    * type相关请直接看TypeToken

2. TypeAdapter的获取
    * TypeAdapter通过TypeAdapterFactory创建
    * 最终的解析输出，在TypeAdapters.STRING read()函数中
    * 到此解析完毕
    
### 解析用户自定义class流程
1. 首先在Gson构造函数中有去注册TypeAdapterFactory， 看这个ReflectiveTypeAdapterFactory

2. 读取用户定义的class中所有的参数， 在ReflectiveTypeAdapterFactory中定义的内部类TypeAdapter中会去便利读取fields， 并且存到内存map中

3. fieldsmap使用的数据结构式LinkedHashMap, entry为双向链表的HashMap

4. BoundField每一个属性的解析构造器， 还是从TypeAdapter已注册的数据类型去取的。序列化和反序列化都在里面



## 参考
* ： https://www.paincker.com/gson-study
* Gson-源码分析 : https://1004145468.github.io/2017/12/16/Gson-%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/