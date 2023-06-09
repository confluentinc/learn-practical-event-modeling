# Exercise 3

In this exercise, we'll turn our model ([png](../exercise-2/step-5/result.png?raw=true) |
[json](../exercise-2/step-5/result.json?raw=true)) into code to run our system on
the Streaming Data Platform. The Event Model will guide our creation
of Domain Transfer Objects, Domain Types, and our Decide and Evolve
functions. Finally, we'll wire these code components into the Kafka
Producer and Streams APIs.

![Autonomo Event Model](../exercise-2/step-5/result.png?raw=true)

## Exercise Instructions

Each step has its own directory containing a README with instructions,
and with tests that initially fail.

1. First, we'll follow the Event Model as a guide to defining some
   Command, Event, and Read Model transfer types using Protocol
   Buffers. See the [Step 1 README](./step-1/README.md) for details.
2. Then, we'll define a selection of the domain types that correspond
   to our transfer types, but which enforce more of the business
   constraints. We'll also define mapping functions to convert between
   transfer and domain types. See the [Step 2
   README](./step-2/README.md) for details.
3. Next, we'll implement Decide and Evolve on our domain types. We'll
   also expose these methods, as well as defining React, as pure
   Domain Functions defined in terms of our transfer types. These
   Domain Functions fully encapsulate the business logic of our
   bounded contexts: Rides and Vehicles. See the [Step 3
   README](./step-3/README.md) for details.
4. Finally, we'll wire up our Decide function to a Kafka Producer, and
   our Evolve function to a Kafka Streams topology.  See the [Step 4
   README](./step-4/README.md) for details.

## Running the Final App

The final app code (no peeking!) is in `final/`.  The app must be
configured to run with a Kafka cluster, and you can do this in one of
two ways.

### Running with Confluent Cloud

To run with a Confluent Cloud Kafka cluster, first sign up for a
[Confluent Cloud](https://confluent.cloud) account.  Then create a
cluster and download the configuration file.  Export the values in the
configuration file as the following environment variables before
running the app:

``` bash
export KAFKA_BOOTSTRAP_SERVERS=[from config file]
export KAFKA_SECURITY_PROTOCOL=SASL_SSL
export KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username="[from config file]" password="[from config file]";'
export KAFKA_SASL_MECHANISM=PLAIN
export KAFKA_SCHEMA_REGISTRY_URL=[confluent cloud schema registry]
./gradlew :final:run
```

### Running with local Kafka via Docker Stack

Run locally using:

``` bash
docker-compose up -d
./gradlew :final:run
```

To clean up the docker-compose stack that runs our Kafka and Schema Registry:

``` bash
docker-compose down
```
