# Nimbus Java API

This project contains reusable libraries originally created for https://github.com/guillaumeprevot/nimbus.

Any feedback would be great !

## Routing API

Here is an example of the routing library available in this project.

```java
// Configure router
Router router = new Router()
	.get("/ping",        (req, res) -> Render.string("pong"))
	.get("/hello/:name", (req, res) -> Render.string("Hello " + req.pathParameter(":name") + "!"));

// Upload support
router.post("/files/upload", (req, res) -> req.upload("file").saveTo(new File(...)));

// WebSocket are also supported
router.websocket("/ws/echo", WebSocket.text((session, message) -> message));
// a more complex example...
router.websocket("/ws/example", new WebSocket()
	.onConnect((session) -> System.out.println("WebSocket connected " + session))
	.onText((session, message) -> session.sendText("ping".equals(message) ? "pong" : message))
	.onError((session, throwable) -> throwable.printStackTrace())
	.onClose((session, statusCode, reason) -> System.out.println("WebSocket closed " + session)));

// Wrap router in Jetty embedded server
JettyServer server = new JettyServer(8443)
	.https("/path/to/keystore/file", "KeystorePassword")
	.multipart("/path/to/upload/temp/folder", ...)
	.session(secretKey, timeout, ...)
	.start(router);

// ...
server.stop();
```

It should look familiar to those who know [Spark](http://sparkjava.com/), [WebMotion](https://github.com/webmotion-framework/webmotion) or [Express.js](https://expressjs.com/).

## Web Server Application

This [application](./src/fr/techgp/nimbus/server/impl/WebServerApplication.java) is a simple web server built using the routing API.

Get started with the following instructions :

```bash
git clone https://github.com/guillaumeprevot/nimbus-java-api.git
cd nimbus-java-api
mvn compile
cd webserver
java -cp ../bin:../lib/* fr.techgp.nimbus.server.impl.WebServerApplication
```

The default behaviour is this :

- run on port `10001` in HTTP and share the `public` folder as root (and only) folder
    - check access to http://localhost:10001/index.html with default configuration
- read configuration from `webserver.conf`
    - use `-Dwebserver.conf=another-file.conf` to change it's location
    - use `-Dwebserver.conf=default.conf:customized.conf` to use both files
- write traces in `webserver.log`
    - use `-Dwebserver.log=another-file.log` to change it's location
    - use `-Dwebserver.log=none` to disable file logging and write to the output
- write process id in `webserver.pid` when application is started
    - use `-Dwebserver.pid=another-file.pid` to change it's location
    - this should make termination easier, like ``kill -9 `cat webserver.pid` ``

You can easily customize your server. For instance, to share 2 folders with HTTPS enabled, you could use this configuration :

```properties
server.port=10001
server.keystore=webserver.pkcs12
server.keystore.password=CHANGEME
static.0.folder=public
static.0.prefix=/public
static.1.folder=/path/to/folder2
static.1.prefix=/public2
```

## JSON API

This project includes a simple, yet efficient JSON library.

```java
// Create an array of 4 values
JSONArray array = JSON.array().add(true).add(2.5).add("text").addNull();
// Create an object of 5 values including an array
JSONObject object = JSON.object()
		.set("text", "text")
		.set("number", 2.5)
		.set("boolean", true)
		.setNull("nullProperty")
		.set("array", array);
// Encode to a JSON string
String json = JSON.encode(object);
// Decode a JSON string
JSONElement result = JSON.decode(json);
result.isObject(); // true
result.asObject().has("number"); // true
result.asObject().get("array").asArray().size(); // 4
```

## CHANGELOG

- 2020-05-17 : initial release
- 2020-05-27 : add new routing API
- 2020-05-30 : add new web server application
- 2020-05-31 : add reusable MIME type manager
- 2020-06-07 : add route implementation using Java "reflection"
- 2020-07-05 : add client session support
- 2020-07-09 : add reusable "utils" package
- 2020-07-11 : version 1.0
- 2020-07-13 : version 1.1 (add session configuration)
- 2020-07-14 : version 1.2 (use JJWT for JSON Web Token)
- 2020-11-09 : version 1.3 (update dependencies and add formula package and utilities)
- 2021-06-26 : version 1.4 (update dependencies)
- 2021-09-04 : version 1.5 (update dependencies and add JSON API)
