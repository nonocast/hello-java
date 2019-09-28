TCP SOCKET CLIENT
==================
先撸一个echo server
```
const debug = require("debug")('app');
const net = require("net");

const port = 12009;

net
  .createServer(socket => {
    debug('>>> client connected');
    socket.on("data", data => {
      debug(`echoing: ${data.toString()}`);
      socket.write(data.toString());
    });
  })
  .listen(port, () => {
    debug(`server started at port ${port}`);
  });
```

telnet 127.0.0.1 12009, 服务器搞定。

再来看Java如何操作Socket, 这部分和C++/C#大同小异, 直接看代码就行。
```
this.client = new Socket();
this.client.connect(this.endpoint);
InputStream inputStream = this.client.getInputStream();
OutputStream outputStream = this.client.getOutputStream();
```

这里的重点在流的读写操作的抽象概念
- InputStream/OutputStream负责byte读写
- Reader/Writer负责对Stream进行文本读写

先来看二进制读写, 
## InputStream
- available(): Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream.
- close()
- read(): Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. If no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks until input data is available, the end of the stream is detected, or an exception is thrown. [注] socket下的input stream不存在EOF的情况, 所以不会返回-1, 当available为0, read会block, 所以这个是同步方法.
- read(byte[] b)
- read(byte[] b, int off, int len)
- reset()
- skip()

直接操作InputStream
```
InputStream inputStream = this.client.getInputStream();
int len = inputStream.available();
byte[] buffer = new byte[len];
logger.debug("available: {}", len);
int result = inputStream.read(buffer, 0, len+10);
logger.debug("read byte count: {}", result);
logger.debug("body: {}", buffer);
logger.debug("{}", new String(buffer, StandardCharsets.UTF_8));

len = inputStream.available(); // 没有更多数据, 就返回0
logger.debug("now available: {}", len);
inputStream.read(); // BLOCK
logger.debug("WAIT SERVER SEND DATA");
```


然后可以通过decorator包装扩展对stream操作,
```
DataInputStream wrappedInputStream = new DataInputStream(new BufferedInputStream(this.client.getInputStream()));

wrappedInputStream.readChar()
wrappedInputStream.readInt()
```
这个char也有个坑, 一读就是两个byte, 要谨慎, readInt读4个bytes,

```
public final int readInt() throws IOException {
    int ch1 = in.read();
    int ch2 = in.read();
    int ch3 = in.read();
    int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
        throw new EOFException();
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
}
```
这里采用Big-endian (Java统一都是Big-endian)

0x45679812

Address         00  01  02  03
-------------------------------
Little-endian   12  98  67  45
Big-endian      45  67  98  12


## OutputStream
- close()
- flush()
- write(int b)
- write(byte[] b)
- write(byte[], int off, int len)

# 异步Socket
到这里都是同步操作, 同步操作就需要开额外thread, 一堆破事, 所以更好的操作肯定是异步.

> BIO、NIO、AIO的区别
> - BIO – Blocking IO 即阻塞式IO。
> - NIO – Non-Blocking IO, 即非阻塞式IO或异步IO。
> - AIO - Asynchronous IO, 可以理解为NIO 2.0

BIO 傻, NIO 难用, AIO 没人用
所以Netty就要华丽登场

## NETTY



参考阅读:
- [NIO vs BIO该如何选择 | 枫秀](http://fengxiu.club/posts/8300084f/)
- [Netty 系列 | g5niusx的小黑屋](https://www.g5niusx.com/tags.html#netty)
- [彻底理解Netty，这一篇文章就够了 - 掘金](https://juejin.im/post/5bdaf8ea6fb9a0227b02275a)