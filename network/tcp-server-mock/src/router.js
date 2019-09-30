const debug = require("debug")("app.router");
const _ = require("lodash");

class Router {
  constructor() {
    this.commands = [this.testCommand1, this.testCommand2, this.testCommand3];
  }

  // echo -n 'foo' | nc localhost 12009
  testCommand1() {
    if (
      /^foo/.test(
        this.buffer
          .toString()
          .trim()
          .toLowerCase()
      )
    ) {
      debug(">>> testCommand1 OK");
      return () => {
        return "bar\n";
      };
    }
    return null;
  }

  // echo -ne '\x04\xff\x21\x19\x95' | nc localhost 12009
  // 11 00 21 00 03 0D 26 03 31 80 1E 00 01 00 00 01 71 9A
  testCommand2() {
    let command = Buffer.from("04FF211995", "hex");

    if (command.equals(this.buffer)) {
      return () => {
        return Buffer.from("11002100030D260331801E0001000001719A", "hex");
      };
    }
  }

  // request: 06 00 01 04 FF D4 39
  // echo -ne '\x06\x00\x01\x04\xff\xd4\x39' | nc localhost 12009
  /**
    1. 无标签时，收到1条指令
    07 00 01 01 01 00 1E 4B

    2. 1个标签时，收到3条指令（标签ID：E20000194604005221003DFF）
    15 00 01 03 01 01 0C E2 00 00 19 46 04 00 52 21 00 3D FF 4C 95 DD 15 00 01 03 01 01 0C E2 00 00 19 46 04 00 52 21 00 3D FF 4C 95 DD 07 00 01 01 01 00 1E 4B 

    3. 3个标签时，收到3条指令（标签ID：E20000194604005221003DFF，E20000195911017214409352，E20000194604009714907F8C）
    15 00 01 03 01 01 0C E2 00 00 19 59 11 01 72 14 40 93 52 48 D8 C0 15 00 01 03 01 01 0C E2 00 00 19 46 04 00 52 21 00 3D FF 48 B1 9B 15 00 01 03 01 01 0C E2 00 00 19 46 04 00 97 14 90 7F 8C 49 E4 EC 
    15 00 01 03 01 01 0C E2 00 00 19 59 11 01 72 14 40 93 52 48 D8 C0 15 00 01 03 01 01 0C E2 00 00 19 46 04 00 52 21 00 3D FF 49 38 8A 15 00 01 03 01 01 0C E2 00 00 19 46 04 00 97 14 90 7F 8C 49 E4 EC 
    07 00 01 01 01 00 1E 4B 
   */
  testCommand3() {
    let command = Buffer.from("06000104FFD439", "hex");

    if (command.equals(this.buffer)) {
      return () => {
        this.i = this.i || 0;

        let resps = [
          Buffer.from("0700010101001E4B", "hex"),
          Buffer.from(
            "1500010301010CE20000194604005221003DFF4C95DD1500010301010CE20000194604005221003DFF4C95DD1500010301010CE20000194604005221003DFF4C95DD0700010101001E4B",
            "hex"
          ),
          Buffer.from(
            "1500010301010CE20000194604005221003DFF4C95DD1500010301010CE20000194604005221003DFF4C95DD0700010101001E4B",
            "hex"
          )
        ];

        return resps[this.i++ % 3];
      };
    }
  }

  /**
   * 如果解析到对应的指令则返回处理结果, 如果不完整则返回null
   * return: Buffer
   */
  append(buf) {
    let result = null;

    debug(`append `, buf);
    this.buffer = buf;

    let handler = null;
    for (let tester of this.commands) {
      if ((handler = tester.call(this))) {
        break;
      }
    }

    if (handler) {
      result = handler.call(this);
      debug(`response: `, result);
    } else {
      // miss handler then just echo input buf
      debug(`echoing: ${buf.toString().trim()}`);
      return buf.toString();
    }

    return result;
  }
}

module.exports = new Router();
