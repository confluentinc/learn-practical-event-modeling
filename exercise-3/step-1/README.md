# Exercise 3, Step 1

In this step, we'll define our transfer types using
[Protocol Buffers](https://protobuf.dev/programming-guides/proto3/).
The build system is configured to compile `src/main/proto/*.proto`
files into Java classes and Kotlin DSL functions.

## Completion condition

In this step, implement the Vehicle transfer types in `src/main/proto/vehicles.proto`

To see what needs to be done, run the (initially-failing) JUnit5 tests:

``` bash
# Via the command-line
../gradlew test
```

or via your IDE's test runner.  Then implement until the tests turn green!