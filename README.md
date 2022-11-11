# Basic Junit5 Framework
This allows you to run Junit5 tests in parallel, you just need to specify the number of parallel threads as `-DthreadCount=` as runtime param.
It also allows you to re-run failed tests immediately. Very useful when your UI/API tests are not stable.

## Requirement
* Java 11
* Maven >3.0

## Building the project:
* You can build the project with maven goals `clean install`

## How To Use

In order to include *junit5-framework* in your Maven project, first add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.nsingla</groupId>
    <artifactId>junit5-framework</artifactId>
    <version>2.0.0</version>
    <scope>test</scope>
</dependency>
```

And also add following repository to your pom:
```xml
<repositories>
    <repository>
        <id>ossrh</id>
        <name>Central Repository OSSRH</name>
        <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
</repositories>
```

## Examples to run tests in parallel
Simply extend your test class with `TestBase.java`

```java

/**
 * This will enable your tests to run in parallel
 */
class YourTestClass extends TestBase {
    
}
 ```

## Examples to retry failed test
At runtime, provide how many times do you want the test to retry by `-DretryCount=`
```java
           /** 
            * Retry test if test failed.
            */
           @RetryFailedTest
           void runFailedTest() throws Exception {
               throw new Exception("Test Failed");
           }
       
           /**
            * Retry parameterized test
            * @throws Exception - error occurred
            */
           @RetryFailedParameterizedTest(name = "runFailedParameterizedTest-[{arguments}]")
           void runFailedParameterizedTest(Argument arg1, Argument arg2) throws Exception {
               throw new Exception("Parameterized Test Failed");
           }
            /**
             * Retry parameterized test
             * @throws Exception - error occurred
             */
            @RetryFailedParameterizedTest(name = "runFailedParameterizedTest-[{0}-{1}]")
            void runFailedParameterizedTest2(Argument arg1, Argument arg2) throws Exception {
                throw new Exception("Parameterized Test Failed");
            }
```