# Exercise 2, Step 3

The outcome of [the previous step](../step-2) was the addition of Read
Models to inform our users about the state of the system.  When
combined with the outcome of step 1, we now have a complete API of
Commands and Read Models.

The goal of this step is simply connect the "slices" via data-flow
arrows:

1. We connect Interface to Command to Event to connect a
   state change slice.
2. We connect Event to Read Model to Interface to connect a state-view
   slice.

When we're done, the information flow should resemble a sine-wave.

We'll draw data flows for the other two types of slices, external
state import and internal state export, as we encounter them in the
next two steps.
