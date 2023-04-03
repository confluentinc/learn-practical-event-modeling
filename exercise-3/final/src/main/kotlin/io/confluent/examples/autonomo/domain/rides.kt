package io.confluent.examples.autonomo.domain

import java.time.Instant

// ***** Commands *****

sealed interface RideCommand: Command {
    val ride: RideId?

    fun decide(state: Ride): List<RideEvent>
}

data class RequestRide(
    val rider: UserId,
    val origin: GeoCoordinates,
    val destination: GeoCoordinates,
    val pickupTime: Instant
): RideCommand {
    override val ride = null
    override fun decide(state: Ride): List<RideEvent> = when(state) {
            InitialRideState -> listOf(
                RideRequested(RideId.randomUUID(), rider, origin, destination, pickupTime, Instant.now())
            )
            else -> throw RideCommandError(this, state, "Ride already exists")
        }
}

data class ScheduleRide(
    override val ride: RideId,
    val vin: Vin,
    val pickupTime: Instant,
): RideCommand {
    override fun decide(state: Ride): List<RideEvent> =
        when(state) {
            is RequestedRide -> listOf(RideScheduled(this.ride, this.vin, this.pickupTime, Instant.now()))
            else -> throw RideCommandError(this, state, "Can only schedule a ride when requested")
        }
}

data class ConfirmPickup(
    override val ride: RideId,
    val vin: Vin,
    val rider: UserId,
    val pickupLocation: GeoCoordinates,
): RideCommand {
    override fun decide(state: Ride): List<RideEvent> =
        when(state) {
            is ScheduledRide -> listOf(RiderPickedUp(this.ride, this.vin, this.rider, this.pickupLocation, Instant.now()))
            else -> throw RideCommandError(this, state, "Can only confirm pickup of a scheduled ride")
        }
}

data class EndRide(
    override val ride: RideId,
    val dropOffLocation: GeoCoordinates,
): RideCommand {
    override fun decide(state: Ride): List<RideEvent> =
        when(state) {
            is InProgressRide -> listOf(RiderDroppedOff(this.ride, state.vin, this.dropOffLocation, Instant.now()))
            else -> throw RideCommandError(this, state, "Can only end a ride already in progress")
        }
}

data class CancelRide(
    override val ride: RideId
): RideCommand {
    override fun decide(state: Ride): List<RideEvent> =
        when(state) {
            is RequestedRide -> listOf(RequestedRideCancelled(this.ride, Instant.now()))
            is ScheduledRide -> listOf(ScheduledRideCancelled(this.ride, state.vin, Instant.now()))
            else -> throw RideCommandError(this, state, "Can only cancel a requested or scheduled ride")
        }
}

// ***** Event *****

sealed interface RideEvent: Event {
    val ride: RideId
}

data class RideRequested(
    override val ride: RideId,
    val rider: UserId,
    val origin: GeoCoordinates,
    val destination: GeoCoordinates,
    val pickupTime: Instant,
    val requestedAt: Instant
): RideEvent

data class RideScheduled(
    override val ride: RideId,
    val vin: Vin,
    val pickupTime: Instant,
    val scheduledAt: Instant,
): RideEvent

data class RequestedRideCancelled(
    override val ride: RideId,
    val cancelledAt: Instant
): RideEvent

data class ScheduledRideCancelled(
    override val ride: RideId,
    val vin: Vin,
    val cancelledAt: Instant
): RideEvent

data class RiderPickedUp(
    override val ride: RideId,
    val vin: Vin,
    val rider: UserId,
    val pickupLocation: GeoCoordinates,
    val pickedUpAt: Instant,
): RideEvent

data class RiderDroppedOff(
    override val ride: RideId,
    val vin: Vin,
    val dropOffLocation: GeoCoordinates,
    val droppedOffAt: Instant
): RideEvent

// ***** Read Models *****

sealed interface Ride: ReadModel {
    val id: RideId

    fun evolve(event: RideEvent): Ride
}

object InitialRideState: Ride {
    override val id: RideId
        get() = throw IllegalStateException("Rides don't have an ID before they're created")

    override fun evolve(event: RideEvent): Ride =
        when(event) {
            is RideRequested -> RequestedRide(event.ride, event.rider, event.pickupTime, event.origin, event.destination, event.requestedAt)
            else -> this
        }
}

data class RequestedRide(
    override val id: RideId,
    val rider: UserId,
    val requestedPickupTime: Instant,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val requestedAt: Instant,
): Ride {
    override fun evolve(event: RideEvent): Ride =
        when(event) {
            is RequestedRideCancelled -> CancelledRequestedRide(
                id,
                rider,
                requestedPickupTime,
                pickupLocation,
                dropOffLocation,
                event.cancelledAt
            )
            is RideScheduled -> ScheduledRide(
                id,
                rider,
                event.pickupTime,
                pickupLocation,
                dropOffLocation,
                event.vin,
                event.scheduledAt
            )
            else -> this
        }
}

data class ScheduledRide(
    override val id: RideId,
    val rider: UserId,
    val scheduledPickupTime: Instant,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val vin: Vin,
    val scheduledAt: Instant,
): Ride {
    override fun evolve(event: RideEvent): Ride = when(event) {
        is ScheduledRideCancelled -> CancelledScheduledRide(
            id, rider, scheduledPickupTime, pickupLocation,
            dropOffLocation, vin, scheduledAt, event.cancelledAt
        )
        is RiderPickedUp -> InProgressRide(
            id, rider, event.pickupLocation, dropOffLocation,
            scheduledAt, vin, scheduledPickupTime, event.pickedUpAt
        )
        else -> this
    }
}

sealed interface CancelledRide: Ride {
    val rider: UserId
    val pickupLocation: GeoCoordinates
    val dropOffLocation: GeoCoordinates
    val cancelledAt: Instant
}

data class CancelledRequestedRide(
    override val id: RideId,
    override val rider: UserId,
    val requestedPickupTime: Instant,
    override val pickupLocation: GeoCoordinates,
    override val dropOffLocation: GeoCoordinates,
    override val cancelledAt: Instant
): CancelledRide {
    override fun evolve(event: RideEvent): Ride = this
}

data class CancelledScheduledRide(
    override val id: RideId,
    override val rider: UserId,
    val scheduledPickupTime: Instant,
    override val pickupLocation: GeoCoordinates,
    override val dropOffLocation: GeoCoordinates,
    val vin: Vin,
    val scheduledAt: Instant,
    override val cancelledAt: Instant
): CancelledRide {
    override fun evolve(event: RideEvent): Ride = this
}

data class InProgressRide(
    override val id: RideId,
    val rider: UserId,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val scheduledAt: Instant,
    val vin: Vin,
    val pickupTime: Instant,
    val pickedUpAt: Instant,
): Ride {
    override fun evolve(event: RideEvent): Ride = when(event) {
        is RiderDroppedOff -> CompletedRide(
            id, rider, pickupTime, pickupLocation,
            event.dropOffLocation, vin, pickedUpAt, event.droppedOffAt
        )
        else -> this
    }
}

data class CompletedRide(
    override val id: RideId,
    val rider: UserId,
    val pickupTime: Instant,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val vin: Vin,
    val pickedUpAt: Instant,
    val droppedOffAt: Instant,
): Ride {
    override fun evolve(event: RideEvent): Ride = this
}

// ***** Errors *****

class RideCommandError(
    command: RideCommand,
    state: Ride,
    message: String,
): IllegalStateException("Failed to apply RideCommand $command to Ride $state: $message")