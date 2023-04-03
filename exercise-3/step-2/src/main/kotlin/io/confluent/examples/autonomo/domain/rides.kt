package io.confluent.examples.autonomo.domain

import java.time.Instant

// ***** Commands *****

sealed interface RideCommand: Command {
    val ride: RideId?
}

data class RequestRide(
    val rider: UserId,
    // TODO
): RideCommand {
    override val ride = null
}

data class ScheduleRide(
    override val ride: RideId,
    // TODO
): RideCommand

data class ConfirmPickup(
    override val ride: RideId,
    // TODO
): RideCommand

data class EndRide(
    override val ride: RideId,
    // TODO
): RideCommand

data class CancelRide(
    override val ride: RideId
): RideCommand

// ***** Event *****

sealed interface RideEvent: Event {
    val ride: RideId
}

data class RideRequested(
    override val ride: RideId,
    // TODO
): RideEvent

data class RideScheduled(
    override val ride: RideId,
    // TODO
): RideEvent

data class RequestedRideCancelled(
    override val ride: RideId,
    // TODO
): RideEvent

data class ScheduledRideCancelled(
    override val ride: RideId,
    // TODO
): RideEvent

data class RiderPickedUp(
    override val ride: RideId,
    // TODO
): RideEvent

data class RiderDroppedOff(
    override val ride: RideId,
    // TODO
): RideEvent

// ***** Read Models *****

sealed interface Ride: ReadModel {
    val id: RideId
}

object InitialRideState: Ride {
    override val id: RideId
        get() = throw IllegalStateException("Rides don't have an ID before they're created")
}

data class RequestedRide(
    override val id: RideId,
    // TODO
): Ride

data class ScheduledRide(
    override val id: RideId,
    // TODO
): Ride

sealed interface CancelledRide: Ride {
    val rider: UserId
    // TODO
}

data class CancelledRequestedRide(
    override val id: RideId,
    // TODO
): CancelledRide

data class CancelledScheduledRide(
    override val id: RideId,
    // TODO
): CancelledRide

data class InProgressRide(
    override val id: RideId,
    // TODO
): Ride

data class CompletedRide(
    override val id: RideId,
    // TODO
): Ride

// ***** Errors *****

class RideCommandError(
    command: RideCommand,
    state: Ride,
    message: String,
): IllegalStateException("Failed to apply RideCommand $command to Ride $state: $message")
