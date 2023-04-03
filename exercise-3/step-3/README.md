# Exercise 3, Step 3

In this step, we'll define our domain functions.  We'll start by
implementing interface methods for decide and evolve:
`Command.decide(state: ReadModel)` and `ReadModel.evolve(event:
Event)`.

Then, we'll encapsulate these methods inside pure functions
implemented in terms of our transfer types.  This will allow the
adapters to invoke these domain functions without having to dig down
into the domain types.

## Completion condition

In this step, implement the missing `decide` and `evolve` interface
methods in
[`domain/rides.kt`](./src/main/kotlin/io/confluent/examples/autonomo/domain/rides.kt)
and
[`domain/vehicles.kt`](./src/main/kotlin/io/confluent/examples/autonomo/domain/vehicles.kt).
Then, complete the domain function implementations in
[`domain_functions.kt`](./src/main/kotlin/io/confluent/examples/autonomo/domain_functions.kt).

To see what needs to be done, run the (initially-failing) JUnit5 tests:

``` bash
# Via the command-line
../gradlew test
```

or via your IDE's test runner.  Then implement until the tests turn green!
