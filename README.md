# PBBG

[![Build Status](https://travis-ci.com/yzaoui/pbbg-api.svg?branch=master)](https://travis-ci.com/yzaoui/pbbg-api)

A persistent browser-based game built on Ktor.

## How to run a release JAR
`java -D[variable]=[value] -jar pbbg-[version].jar`

|Variable|Required?|Description|Values|
|---|---|---|---|
|`KTOR_ENV`|Yes|Application environment|`dev`, `prod`|
|`PORT`|Yes|The port on which to deploy the server|Valid port number|
|`JDBC_ADDRESS`|No|Application database address|Valid JDBC address, appended to `jdbc`<br>H2 or PostgreSQL currently supported<br>Default: `h2:./default`|
|`JWT_SECRET`|Yes|Secret used for JWT tokens|Any string|

### Example

`java -DKTOR_ENV=prod -DPORT=8080 -DJWT_SECRET=secret -jar pbbg-0.3.1.jar`
