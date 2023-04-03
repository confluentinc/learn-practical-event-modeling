# Exercise 2, Step 5

The outcome of [the previous step](../step-4) was the identification
of separate Streams and organization of the various Events into
horizontal lanes representing these Streams.  You'll notice gaps in
the data flow that roughly correspond with breaks between Events on
the separate Streams.  These represent points where we'll need to
integrate these Streams using our third and fourth types of slice:
the external state import slice and the internal state export slice.

In this exercse, identify and construct on the modeling canvas the
necessary type of integration:

1. For an external state import slices triggered on external Events,
   we can connect an Event from an external Stream to a Command, and
   that Command to an Event on our local Stream.  For external state
   import slices triggered on the state of an external Read Model, we
   can connect the external Read Model to a job/gear-type Interface to
   a local Command to a local Event.
2. For an internal state export slice, we connect a "TODO-list" Read
   Model to a job/gear-type Interface, the job/gear-type Interface to
   an external Command, and then either record a local Event recording
   the result of that Command (and connect these via an arrow), or
   else we observe the effect of our Command on the external Event
   Stream and perform an external state import.

When finished, the data flow in the model should be complete, and each
Stream should be clearly defined, self-contained, causally complete,
and have defined integration contracts in place with other Streams.
