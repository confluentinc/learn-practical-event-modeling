# Exercise 3, Step 4

In this final step, we'll adapt our domain functions to the Streaming
Data Platform.

We'll begin by implementing a helper function in the HTTP adapter
(avoiding the actual HTTP stuff) whose job it is to apply the Decide
function and then record its results to the proper Event topic.

We'll then adapt the Evolve function to a Kafka Streams topology using
`KGroupedStream.aggregate`.

## Completion condition

In this step, implement the missing adapters in
[`adapters/http.kt`](./src/main/kotlin/io/confluent/examples/autonomo/adapters/http.kt)
and
[`adapters/kafka.kt`](./src/main/kotlin/io/confluent/examples/autonomo/adapters/kafka.kt).

To see what needs to be done, run the (initially-failing) JUnit5 tests:

``` bash
# Via the command-line
../gradlew test
```

or via your IDE's test runner.  Then implement until the tests turn green!
