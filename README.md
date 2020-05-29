# Nimbus Java API

This project contains some reusable libraries originally build for one of my projects called `Nimbus`.

## Routing API

Here is an example of the routing library available in this project.

```java
Router router = new Router()
	.get("/ping",        (req, res) -> Render.string("pong"))
	.get("/hello/:name", (req, res) -> Render.string("Hello " + req.pathParameter(":name") + "!"));

Server server = JettyServer.init(router,
	8443,
	"/path/to/keystore/file",
	"KeystorePassword",
	"/path/to/upload/temp/folder");

server.join();
```

It should look familiar to those who knows [Spark](http://sparkjava.com/), [WebMotion](https://github.com/webmotion-framework/webmotion) or [Express.js](https://expressjs.com/).
