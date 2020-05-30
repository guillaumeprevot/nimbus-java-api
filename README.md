# Nimbus Java API

This project contains some reusable libraries originally build for one of my projects called `Nimbus`.

## Routing API

Here is an example of the routing library available in this project.

```java
Router router = new Router()
	.get("/ping",        (req, res) -> Render.string("pong"))
	.get("/hello/:name", (req, res) -> Render.string("Hello " + req.pathParameter(":name") + "!"));

JettyServer server = new JettyServer(8443)
	.https("/path/to/keystore/file", "KeystorePassword")
	.multipart("/path/to/upload/temp/folder", ...)
	.start(router);

// ...
server.stop();
```

It should look familiar to those who knows [Spark](http://sparkjava.com/), [WebMotion](https://github.com/webmotion-framework/webmotion) or [Express.js](https://expressjs.com/).
