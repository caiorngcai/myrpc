##自定义的RPC框架

### 一.工程说明  
  框架层：  
  1.common：定义通信过程中通用的消息类，编码，解码，序列化相关的方法  
  2.registry：server启动时把自己注册到zookeeper上，并对client提供服务器的发现功能
  3.rpc-server:基于netty实现的RPC服务器，处理客户端发过来的消息，反射调用接口的方法，并封装结果返回  
  4.rpc-client:基于netty的RPC客户端，封装用户的远程调用请求，动态代理发消息到服务器端  
  用户层：  
  1.sample-server:启动框架的sever层，实现接口，处理消息  
  2.sample-client:定义接口和协议  
  3.sample-app：创建代理对象，调用用户服务层的接口  
###二.关键技术点讲解
公共层：  
1.使用Objenesis创建对象时，需要一个无参构造器，这个类库一般用于它一般用于：   
           序列化、远程处理和持久化：无需调用代码即可将Java类实例化并存储特定状态。  
           代理、AOP库和Mock对象：可以创建特定Java类的子类而无需考虑super()构造器。  
           容器框架：可以用非标准方式动态实例化Java类。例如Spring引入Objenesis后，Bean不再必须提供无参构造器了。  
 2.netty中的编解码器需要分别继承MessageToByteEncoder，ByteToMessageDecoder，编码器把对象转化为字节数组后，直接写到输出流即可，  
 解码器把字节数组转化成对象后添加到list中  
 


  
  
