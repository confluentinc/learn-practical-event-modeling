# Practical Event Modeling course hands-on

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
[Evident Design](https://app.evidentstack.com/design), each step will also include a link to a
[JSON export](https://docs.evidentstack.com/design-docs/Latest/models/import-export-models.html)
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
README, containing description of the Exercise and its steps,
including Event Model images/exports, and codebases at various stages
of completion.

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
functioning like a storyboard for a film.

## [Exercise 2](./exercise-2): Completing the Event Model

Exercise 2 is the eighth module in the course, coming after "The Event
Modeling Workshop: Step 4, Identifying and Integrating Event Streams".

In this exercise, we'll complete our Event Model by identifying our
Command and Read Model API, connecting the model components together
with data flow arrows, and identify and integrate our Event Streams.

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
