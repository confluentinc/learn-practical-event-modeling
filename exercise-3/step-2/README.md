# Exercise 3, Step 2

In this step, we'll define our domain types. Our transfer types can be
a useful guide here, but beware of `status` fields and enums.  Those
are fine for transfer types, but we'll want separate domain types for
each case. This will allow the compiler to do much of our work for us,
reducing conditionals and testable cases significantly.  Check out the
excellent book [Domain Modeling Made
Functional](https://pragprog.com/titles/swdddf/domain-modeling-made-functional/)
for much more on this subject.

We'll also implement the conversion logic to turn transfer types into
domain types, and vice versa.

## Completion condition

In this step, implement the missing domain types in
[`domain/rides.kt`](./src/main/kotlin/io/confluent/examples/autonomo/domain/rides.kt),
as well as the missing conversion logic in
[`transfer/conversion.kt`](./src/main/kotlin/io/confluent/examples/autonomo/transfer/conversion.kt).

To see what needs to be done, run the (initially-failing) JUnit5 tests:

``` bash
# Via the command-line
../gradlew test
```

or via your IDE's test runner.  Then implement until the tests turn green!
