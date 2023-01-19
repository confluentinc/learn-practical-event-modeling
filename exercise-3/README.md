# Exercise 3

In this exercise, we'll turn our model ([png](../exercise-2/step-5/result.png?raw=true) |
[json](../exercise-2/step-5/result.json?raw=true)) into code to run our system on
the Streaming Data Platform. The Event Model will guide our creation
of Domain Transfer Objects, Domain Types, and our Decide and Evolve
functions. Finally, we'll wire these code components into the Kafka
Producer and Streams APIs.

![Autonomo Event Model](../exercise-2/step-5/result.png?raw=true)

Each step has its own directory containing a README with instructions,
and with tests that initially fail.

1. First, we'll follow the Event Model as a guide to defining some of the
   Command, Event, and Read Model Domain Transfer Object schemas. See
   the [Step 1 README](./step-1/README.md) for details.
2. Then, we'll define a selection of the Domain Types that correspond
   to our DTOs, but which enforce more of the business constraints.  See
   the [Step 2 README](./step-2/README.md) for details.
3. Next, we'll implement our Decide and Evolve functions for one of
   our Streams in terms of our Domain Types, to govern how state
   changes over time for that Stream.  See the [Step 3
   README](./step-3/README.md) for details.
4. Finally, we'll wire up our Decide function to a Kafka Producer, and
   our Evolve function to a Kafka Streams topology.  See the [Step 4
   README](./step-4/README.md) for details.
