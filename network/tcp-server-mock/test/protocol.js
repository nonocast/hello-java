const chai = require("chai");
const assert = chai.assert;
const debug = require("debug")("test");
const net = require("net");
const app = require("../src/app");
const util = require("util");
const check = require("check-types");
chai.should();

describe("protocol", () => {
  let useExternalServer = false;

  let getPort = () => {
    return useExternalServer ? 12009 : 12777;
  };

  before(() => {
    if (!useExternalServer) {
      app.port = getPort();
      app.open();
    }
  });

  after(() => {
    if (!useExternalServer) {
      app.close();
    }
  });

  it("test welcome message", done => {
    let client = net.createConnection(getPort(), "127.0.0.1");
    client.setEncoding("utf-8");
    client.on("data", message => {
      message.should.eq("hello world\n");
      client.end();
      done();
    });
  });

  it.skip("test echo", done => {
    let client = net.createConnection(getPort(), "127.0.0.1");
    client.setEncoding("utf-8");

    let message = "foobar";
    client.on("data", message => {
      debug(message);
      if (/hello world/.test(message)) return;
      message.should.eq(message);
      client.end();
      done();
    });
    client.write(message);
  });

  // it("test query command", done => {
  //   let client = net.createConnection(port, "127.0.0.1");
  //   client.on("data", data => {
  //     if (data instanceof Buffer) {
  //       client.end();
  //       done();
  //     }
  //   });

  //   client.write(message);
  // });

  it("test types", () => {
    assert.equal(true, true);
    assert.isTrue(true);

    let b = Buffer.alloc(8);
    assert.isTrue(b instanceof Buffer);
    assert.isTrue(Buffer.isBuffer(b));
    assert.isFalse("abc" instanceof Buffer);
    assert.isFalse(Buffer.isBuffer("abc"));
    assert.isTrue(util.isBuffer(b));

    assert.isTrue(util.isString("abc"));
    assert.isTrue(util.isString(""));
    assert.isFalse(util.isString(null));

    assert.isTrue(check.zero(0));
    assert.isTrue(check.null(null));
  });

  it("test buffer equals", () => {
    let b1 = Buffer.from([0xaa, 0xbb, 0xcc, 0xdd]);
    let b2 = Buffer.from([0xaa, 0xbb, 0xcc, 0xdd]);
    let b3 = Buffer.from("aabbccdd", "hex");
    let b4 = Buffer.from("aa bb cc dd", "hex");

    assert.isFalse(b1 == b2);
    assert.isTrue(b1.equals(b2));
    assert.isTrue(b1.equals(b3));
    assert.isFalse(b1.equals(b4));

    b1.length.should.eq(4);
    b4.length.should.eq(1);
  });
});
