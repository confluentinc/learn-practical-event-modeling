# Exercise 2

The outcome of [Exercise 1](../exercise-1) was a story board of our
system, e.g. the sequence of business Events with Interfaces
highlighting key moments of the user experience.

Using this story board as our starting point, we'll complete our Event
Model by identifying our Command and Read Model API, connecting the
model components together with data flow arrows, and identify and
integrate our Event Streams:

1. Picking up where we left off in Exercise 1 ([png](../exercise-1/step-3/result.png?raw=1) |
   [json export](../exercise-1/step-3/result.json?raw=1)), we'll identify a
   Command that is the immediate cause of each Event.  When you're
   done, the model should look something like this ([png](./step-1/result.png?raw=1) |
   [json export](./step-1/result.json?raw=1)).
2. Next, we'll aggregate our various Events into Read Models to inform
   our users.  When completed, the model should look something like
   this ([png](./step-2/result.png?raw=1) | [json
   export](./step-2/result.json?raw=1)).
3. Then, we'll simply connect Interface to Command, Command to Event,
   Event to Read Model, and Read Model to Interface with arrows to
   create our sinusoidal information flow, after which the model
   should look like this ([png](./step-3/result.png?raw=1) |
   [json export](./step-3/result.json?raw=1)).
4. With our state changes mostly modelled, we'll gather the Events
   into separate Stream lanes representing our independent causal
   narratives, which will make the model look like this ([png](./step-4/result.png?raw=1) |
   [json export](./step-4/result.json?raw=1)).
5. Finally, we'll design the necessary integrations among those
   Streams by identifying when a Stream need to import Events or Read
   Models from another Stream, or when a Stream needs to directly
   cause change by invoking another Stream's Command interface and
   react to resulting foreign events or record local result events.
   When we've identfied and integrated these specific use cases, the
   model should look like this ([png](./step-5/result.png?raw=1) |
   [json export](./step-5/result.json?raw=1)).
