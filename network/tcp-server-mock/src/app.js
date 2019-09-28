const debug = require("debug")("app");
const net = require("net");
const util = require("util");
const check = require("check-types");
const router = require("./router");

class App {
  constructor() {
    this.port = 12009;
  }

  open() {
    this.close();

    this.server = net
      .createServer(socket => {
        debug("<<< client connected");
        socket.write("hello world\n");

        socket.on("data", data => {
          check.assert(util.isBuffer(data));
          let result = router.append(data);
          if (result && result.length > 0) {
            socket.write(result);
          }
        });

        socket.on("close", () => {
          debug(">>> client closed");
        });

        socket.on("error", error => {
          debug(`# error: ${error.message}`);
        });
      })
      .listen(this.port, () => {
        debug(`Server is listening: ${JSON.stringify(this.server.address())}`);
      });
  }

  close() {
    if (this.server) {
      this.server.close();
      this.server = null;
    }
  }
}

process.on("uncaughtException", error => {
  debug("### uncaughtException ###");
  debug(error.message);
  debug(error.stack);
});

module.exports = new App();