###Features

This application supports two types of endpoints:
```
- GET /?accountId=id
- POST /
```
```
{
  "fromAccountId":fromAccountId,
  "toAccountId":toAccountId,
  "sum":sum
}
```

Default properties are located in application.properties file. User could use custom property file, which should be passed as args.


### Build
```
mvn clean install
```
### Run app (default props)
```
java -jar target\revolut-ex-1.0-SNAPSHOT-jar-with-dependencies.jar
```
### Run app (custom props)
```
java -jar target\revolut-ex-1.0-SNAPSHOT-jar-with-dependencies.jar ./application.properties
```