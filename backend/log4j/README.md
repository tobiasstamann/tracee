> This document contains documentation for the tracee-log4j backend module. Check the [TracEE main documentation](/README.md) to get started.

# tracee-log4j

Backend implementation for [log4j (1.2.4+)](http://logging.apache.org/log4j/1.2/)

## Installation

You need exactly one backend provider on your runtime classpath. Add following to your `pom.xml` to add this module to your dependency tree:

```xml
<dependencies>
...
	<dependency>
		<groupId>io.tracee.backend</groupId>
		<artifactId>tracee-log4j</artifactId>
		<version>RELEASE</version> <!-- You should specify a version instead -->
		<scope>runtime</scope>
	</dependency>
...
<dependencies>
```
