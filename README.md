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
	.errors(showStackTraces)
	.start(router);

// ...
server.stop();
```

It should look familiar to those who know [Spark](http://sparkjava.com/) (Java), [WebMotion](https://github.com/webmotion-framework/webmotion) (Java), [Express.js](https://expressjs.com/) (JS) or [HttpRouter](https://github.com/julienschmidt/httprouter) (Go).

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

You can easily customize your server. For instance, this configuration :
- enables HTTPS on port 10001 using the specified keystore file
- shares two folders, one relative path as /public and one absolute path as /public2
- enables the /utils/chat.html route, a [POC](./doc/chat-application.jpg) of chat application using router's WebSocket support

```properties
server.port=10001
server.keystore=webserver.pkcs12
server.keystore.password=CHANGEME
static.0.folder=public
static.0.prefix=/public
static.1.folder=/path/to/folder2
static.1.prefix=/public2
utils.chat.enabled=true
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

// Streaming is also supported
// {"text":"value","number":2.5,"boolean":true,"nullProperty":null,"array":[1,2]}
JSONStreamRenderer renderer = new JSONStreamStringRenderer(System.out::print);
JSON.stream(renderer).objectValue()
	.name("text").value("value")
	.name("number").value(2.5)
	.name("boolean").value(true)
	.name("nullProperty").nullValue()
	.name("array").arrayValue()
		.add(1)
		.add(2)
		.end()
	.end();
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
- 2021-09-05 : version 1.6 (add WebSocket support in routing API + chat POC + JSON improvements)
- 2021-09-10 : version 1.7 (add JSON stream API and some minor improvements)
- 2022-02-03 : version 1.8 (update dependencies and upgrade PBKDF2 parameters in CryptoUtils)
- 2022-02-27 : version 1.9 (update dependencies, update to Java 17 and add Render.notImplemented())
- 2022-03-06 : version 1.10 (refactor the code about file upload, simplify and fix a few typos)
- 2022-07-28 : version 1.11 (migration to major versions of Jetty 11 and Servlet API 5, and minor update of JJWT 0.11.5)
- 2022-08-06 : version 1.12 (fix ServletContextHandler path, add new StackTrace configuration, update GSON 2.9.1 and licence)
- 2023-03-29 : version 1.13 (allow for custom "Invalid SNI" error handler, minor fix in StaticRessourceWithCache, update Jetty 11.0.14, FreeMarker 2.3.32 and GSON 2.10.1)
- 2024-02-21 : version 1.14 (update Jetty 11.0.20 and JJWT 0.12.5)
- 2024-11-23 : version 1.15 (fix of MimeTypes.byName('aaa') + update Jetty 11.0.24, FreeMarker 2.3.33, Gson 2.11.0, JJWT 0.12.6)
- 2025-04-05 : version 1.16 (update Jetty 11.0.25, Freemarker 2.3.34 and Gson 2.12.1)
