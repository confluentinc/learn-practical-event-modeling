# Practical Event Modeling course exercises

These exercises are designed to follow along with the course modules
to provide hands-on experience with the course material.  Exercise 1
is the fifth module in the course, Exercise 2 is the eighth module,
and Exercise 3 is the eleventh module, just before the course
conclusion.

The first two Exercises are pure Event Modeling, and can be completed
using nothing more than some sticky notes and a pencil, or your
favorite drawing/diagramming application.  The exercise steps will
include links to images of what the Event Model should look like at
the start and end of each step. If you'd prefer to complete these
steps using the third-party Event Modeling tool
[oNote](https://onote.com), each step will also include a link to a
[JSON export](https://docs.onote.com/onote-docs/Latest/models/import-export-models.html)
of the model as of the start and end of each step.

Exercise 3 involves actually implementing in code the system design
that we captured in the Event Model during the first two exercises.
The codebase is written in [Kotlin](https://kotlinlang.org/) for two
reasons:

* To make use of to the excellent
  [Kafka Java Client](https://docs.confluent.io/kafka-clients/java/current/overview.html)
  and [Kafka Streams Library](https://docs.confluent.io/platform/current/streams/overview.html)
  via Kotlin's easy Java interop
* To illustrate some of the functional Domain Modeling techniques for
  implementing the Event Model as described in the course

## How to use this repository

Each exercise step outlined below links to a more comprehensive
description of that step, including Event Model images/exports, and
codebases at various stages of completion.

## Use-case

The exercises in this course as outlined below are built around the
example system described throughout the course: an autonomous vehicle
ride reservation system. Rather than leaving their vehicles sittling
idle for hours, for example while at work, vehicle owners can make
their autonomous vehicles available to pick up and drop off riders
like a traditional ride sharing service.

First, owners must register their vehicles with our service using the
vehicle's VIN number.

Next, owners make vehicles available to our service for a specific
period.

Then, riders can schedule and pay for rides, and can either cancel
prior to pickup, or else confirm pickup and dropoff.

When the owner needs their vehicle, they request its return to take it
out of circulation.

Finally, if they no longer want an account, owners can unregister
their vehicles from our service.

## [Exercise 1](./exercise-1): Build the Storyboard

Exercise 1 is the fifth module in the course, coming after "The Event
Modeling Workshop: Step 2, Envisioning the User Experience".

In this exercise, we'll build our Event Model up to the point of
functioning like a storyboard for a film.  This phase of the Event
Modeling process proceeds in 3 steps:

1. First, we'll begin [step 1](./exercise-1/step-1) with a blank Event
   Model, and then proceed by brainstorming our business Events.  We
   won't worry yet about properly ordering/sequencing these
   Events. When you're done, the model should look something like this
   ([png](./exercise-1/step-1/result.png?raw=1) |
   [json export](./exercise-1/step-1/result.json?raw=1)).
2. Next, in [step 2](./exercise-1/step-2), we'll sequence the
   [Events that we brainstormed](./exercise-1/step-2) into a plausable
   business narrative, sort of like the script of a film. When you're
   done, the model should look something like this ([png](./exercise-1/step-2/result.png?raw=1) |
   [json export](./exercise-1/step-2/result.json?raw=1))
3. Finally, in [step 3](./exercise-1/step-3) we'll add Interfaces above our
   Events to visualize what the user will be experiencing at that
   point in the workflow, similar to how a film's storyboard combines
   visuals with key plot points. When you're done, the model should look something like this
   ([png](./exercise-1/step-3/result.png?raw=1) | [json export](./exercise-1/step-3/result.json?raw=1))

## [Exercise 2](./exercise-2): Completing the Event Model

Exercise 2 is the eighth module in the course, coming after "The Event
Modeling Workshop: Step 4, Identifying and Integrating Event Streams".

In this exercise, we'll complete our Event Model by identifying our
Command and Read Model API, connecting the model components together
with data flow arrows, and identify and integrate our Event Streams:

1. Picking up where we left off in Exercise 1 ([png](./exercise-1/step-3/result.png?raw=1) |
   [json export](./exercise-1/step-3/result.json?raw=1)), we'll identify a
   Command that is the immediate cause of each Event.  When you're
   done, the model should look something like this ([png](./exercise-2/step-1/result.png?raw=1) |
   [json export](./exercise-2/step-1/result.json?raw=1)).
2. Next, we'll aggregate our various Events into Read Models to inform
   our users.  When completed, the model should look something like
   this ([png](./exercise-2/step-2/result.png?raw=1) | [json
   export](./exercise-2/step-2/result.json?raw=1)).
3. Then, we'll simply connect Interface to Command, Command to Event,
   Event to Read Model, and Read Model to Interface with arrows to
   create our sinusoidal information flow, after which the model
   should look like this ([png](./exercise-2/step-3/result.png?raw=1) |
   [json export](./exercise-2/step-3/result.json?raw=1)).
4. With our state changes mostly modelled, we'll gather the Events
   into separate Stream lanes representing our independent causal
   narratives, which will make the model look like this ([png](./exercise-2/step-4/result.png?raw=1) |
   [json export](./exercise-2/step-4/result.json?raw=1)).
5. Finally, we'll design the necessary integrations among those
   Streams by identifying when a Stream need to import Events or Read
   Models from another Stream, or when a Stream needs to directly
   cause change by invoking another Stream's Command interface and
   react to resulting foreign events or record local result events.
   When we've identfied and integrated these specific use cases, the
   model should look like this ([png](./exercise-2/step-5/result.png?raw=1) |
   [json export](./exercise-2/step-5/result.json?raw=1)).

## [Exercise 3](./exercise-3): Implementing an Event Model on the Streaming Data Platform

Exercise 3 is the eleventh module in the course, coming after
"Implementing Event Modelled Systems on the Streaming Data Platform",
and just before the conclusion module for the course.

In this exercise, we'll turn our model into code to run our system on
the Streaming Data Platform. The Event Model will guide our creation
of Domain Transfer Objects, Domain Types, and our Decide and Evolve
functions. Finally, we'll wire these code components into the Kafka
Producer and Streams APIs.

Each step has its own directory containing a README with instructions,
and with tests that initially fail.

1. First, we'll follow the Event Model as a guide to defining some of the
   Command, Event, and Read Model Domain Transfer Object schemas. See
   the [Step 1 README](./exercise-3/step-1/README.md) for details.
2. Then, we'll define a selection of the Domain Types that correspond
   to our DTOs, but which enforce more of the business constraints.  See
   the [Step 2 README](./exercise-3/step-2/README.md) for details.
3. Next, we'll implement our Decide and Evolve functions for one of
   our Streams in terms of our Domain Types, to govern how state
   changes over time for that Stream.  See the [Step 3
   README](./exercise-3/step-3/README.md) for details.
4. Finally, we'll wire up our Decide function to a Kafka Producer, and
   our Evolve function to a Kafka Streams topology.  See the [Step 4
   README](./exercise-3/step-4/README.md) for details.
