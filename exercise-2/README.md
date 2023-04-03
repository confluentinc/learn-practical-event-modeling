# Exercise 2

The outcome of [Exercise 1](../exercise-1) was a storyboard of our
system, e.g. the sequence of business Events with Interfaces
highlighting key moments of the user experience.

Using this storyboard as our starting point, we'll complete our Event
Model by identifying our Command and Read Model API, connecting the
model components together with data flow arrows, and identifying and
integrating our Event Streams.  As before, each of the following steps
has its own `README.md` with more detailed instructions:

1. In [step 1](./step-1/README.md), we'll pick up where we left off in
   Exercise 1 ([png](../exercise-1/step-3/result.png?raw=1) | [json
   export](../exercise-1/step-3/result.json?raw=1)) by identifying a
   Command that is the immediate cause of each Event.  When done, the
   model should look something like this
   ([png](./step-1/result.png?raw=1) | [json
   export](./step-1/result.json?raw=1)).
2. Next in [step 2](./step-2/README.md), we'll aggregate our various
   Events into Read Models to inform our users.  When completed, the
   model should look something like this
   ([png](./step-2/result.png?raw=1) | [json
   export](./step-2/result.json?raw=1)).
3. Then in [step 3](./step-3/README.md), we'll simply connect model
   components together with arrows to create our sinusoidal
   information flow for oru state change and state view slices, after
   which the model should look like this
   ([png](./step-3/result.png?raw=1) | [json
   export](./step-3/result.json?raw=1)).
4. With our information flow mostly modelled, in [step
   4](./step-4/README.md) we'll gather the Events into separate Stream
   lanes representing our independent causal narratives, which will
   make the model look like this ([png](./step-4/result.png?raw=1) |
   [json export](./step-4/result.json?raw=1)).
5. Finally in [step 5](./step-5/README.md), we'll design the necessary
   integrations among those Streams by identifying when a Stream needs
   to import Events or Read Models from another Stream (i.e. an
   external state import slice), or when a Stream needs to directly
   cause change by invoking another Stream's Command interface
   (i.e. an internal state export slice) and react to resulting
   foreign events or to record local result events.  When we've
   identfied and integrated these specific use cases, the model should
   look like this ([png](./step-5/result.png?raw=1) | [json
   export](./step-5/result.json?raw=1)).


## Completing this Exercise with [Evident Design](https://app.evidentstack.com/design)

Create a new Event Model in Evident Design for each step, and follow
the instructions in that step by adding elements to the modeling
canvas.  On the first step, you can begin by importing the [outcome of
Exercise 1](../../exercise-1/step-3/result.json?raw=1).  For all
subsequent steps, you can
[import](https://docs.evidentstack.com/design-docs/Latest/models/import-export-models.html#import-event-model-json)
the `result.json` of the previous step to get you to a consistent
starting point before proceeding.

## Completing this Exercise with sticky notes or a drawing app

For each step, add components to your modeling canvas to complete the
described outcome.  At the beginning of the next step, check the PNG
of the prior step result (linked above) and ensure your canvas
resembles that consistent starting point before proceeding.
