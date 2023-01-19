package io.confluent.examples.autonomo.domain

import arrow.core.Validated
import kotlinx.coroutines.flow.Flow
import org.valiktor.functions.matches
import org.valiktor.validate
import java.time.Instant
import java.util.*

// Commands

sealed interface VehicleCommand: Command {
    val vin: Vin
}

data class AddVehicle(
    val owner: UserId,
    override val vin: Vin,
): VehicleCommand

data class MakeVehicleAvailable(
    override val vin: Vin
): VehicleCommand

data class MarkVehicleOccupied(
    override val vin: Vin
): VehicleCommand

data class MarkVehicleUnoccupied(
    override val vin: Vin
): VehicleCommand

data class RequestVehicleReturn(
    override val vin: Vin
): VehicleCommand

data class ConfirmVehicleReturn(
    override val vin: Vin
): VehicleCommand

data class RemoveVehicle(
    val owner: UserId,
    override val vin: Vin,
): VehicleCommand

sealed interface RideCommand: Command {
    val ride: RideId?
}

data class RequestRide(
    val rider: UserId,
    val origin: GeoCoordinates,
    val destination: GeoCoordinates,
    val pickupTime: Instant
): RideCommand {
    override val ride = null
}

data class ScheduleRide(
    override val ride: RideId,
    val vin: Vin,
    val pickupTime: Instant,
): RideCommand

data class ConfirmPickup(
    override val ride: RideId,
    val vin: Vin,
    val rider: UserId,
    val pickupLocation: GeoCoordinates,
): RideCommand

data class EndRide(
    override val ride: RideId,
    val dropOffLocation: GeoCoordinates,
): RideCommand

data class CancelRide(
    override val ride: RideId
): RideCommand

// Events

sealed interface VehicleEvent: Event {
    val vin: Vin
}

data class VehicleAdded(
    val owner: UserId,
    override val vin: Vin,
): VehicleEvent

data class VehicleAvailable(
    override val vin: Vin,
    val availableAt: Instant,
): VehicleEvent

data class VehicleOccupied(
    override val vin: Vin,
    val occupiedAt: Instant,
): VehicleEvent

data class VehicleReturnRequested(
    override val vin: Vin,
    val returnRequestedAt: Instant
): VehicleEvent

data class VehicleReturned(
    override val vin: Vin,
    val returnedAt: Instant
): VehicleEvent

data class VehicleRemoved(
    val owner: UserId,
    override val vin: Vin,
    val removedAt: Instant
): VehicleEvent

sealed interface RideEvent: Event {
    val ride: RideId
}
data class RideRequested(
    override val ride: RideId,
    val rider: UserId,
    val origin: GeoCoordinates,
    val destination: GeoCoordinates,
    val pickupTime: Instant
): RideEvent

data class RideScheduled(
    override val ride: RideId,
    val vin: Vin,
    val pickupTime: Instant,
    val scheduledAt: Instant,
): RideEvent

data class RideCancelled(
    override val ride: RideId,
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
    val dropOffLocation: GeoCoordinates,
    val droppedOffAt: Instant
): RideEvent

// Read Models

sealed interface Vehicle: ReadModel {
    val vin: Vin

    suspend fun year(): String?  = null
    suspend fun make(): String?  = null
    suspend fun model(): String? = null
    suspend fun color(): String? = null
}

interface ProspectiveVehicleContext : Vehicle {
    fun isVinUniqueInInventory(vin: Vin): Boolean
}
data class InventoryVehicle(override val vin: Vin): Vehicle
data class AvailableVehicle(override val vin: Vin): Vehicle
data class OccupiedVehicle(override val vin: Vin): Vehicle
data class OccupiedReturningVehicle(override val vin: Vin): Vehicle
data class ReturningVehicle(override val vin: Vin): Vehicle

interface MyVehicles: ReadModel {
    val owner: UserId

    fun vehicles(): Flow<Vehicle>
}

interface AvailableVehicles: ReadModel {
    fun availableVehicles(): Flow<Vehicle>
}

interface VehicleOccupancyToImport: ReadModel

sealed interface Ride {
    val id: RideId
}

data class RequestedRide(
    override val id: RideId,
    val rider: UserId,
    val requestedPickupTime: Instant,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
): Ride {
    fun toCancelledRide(
        cancelledAt: Instant
    ) = CancelledRequestedRide(
        id,
        rider,
        requestedPickupTime,
        pickupLocation,
        dropOffLocation,
        cancelledAt
    )

    fun toScheduledRide(
        vin: Vin,
        pickupTime: Instant,
        scheduledAt: Instant,
    )= ScheduledRide(
        id,
        rider,
        requestedPickupTime,
        pickupLocation,
        dropOffLocation,
        vin,
        pickupTime,
        scheduledAt
    )
}

data class ScheduledRide(
    override val id: RideId,
    val rider: UserId,
    val requestedPickupTime: Instant,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val vin: Vin,
    val scheduledPickupTime: Instant,
    val scheduledAt: Instant,
): Ride {
    fun toCancelledRide(
        cancelledAt: Instant
    ) = CancelledScheduledRide(
        id,
        rider,
        requestedPickupTime,
        pickupLocation,
        dropOffLocation,
        vin,
        scheduledPickupTime,
        scheduledAt,
        cancelledAt
    )

    fun toInProgressRide(
        vin: Vin,
        rider: UserId,
        pickupLocation: GeoCoordinates,
        pickedUpAt: Instant,
    ) = InProgressRide(
        id, rider, pickupLocation, dropOffLocation, scheduledAt, vin, scheduledPickupTime, pickedUpAt
    )
}

interface CancelledRide: Ride {
    val rider: UserId
    val requestedPickupTime: Instant
    val pickupLocation: GeoCoordinates
    val dropOffLocation: GeoCoordinates
    val cancelledAt: Instant
}

data class CancelledRequestedRide(
    override val id: RideId,
    override val rider: UserId,
    override val requestedPickupTime: Instant,
    override val pickupLocation: GeoCoordinates,
    override val dropOffLocation: GeoCoordinates,
    override val cancelledAt: Instant
): CancelledRide

data class CancelledScheduledRide(
    override val id: RideId,
    override val rider: UserId,
    override val requestedPickupTime: Instant,
    override val pickupLocation: GeoCoordinates,
    override val dropOffLocation: GeoCoordinates,
    val vin: Vin,
    val scheduledPickupTime: Instant,
    val scheduledAt: Instant,
    override val cancelledAt: Instant
): CancelledRide

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
    fun riderDroppedOff(
        dropOffLocation: GeoCoordinates,
        droppedOffAt: Instant
    ) = CompletedRide(
        id,
        rider,
        pickupLocation,
        dropOffLocation,
        scheduledAt,
        vin,
        pickupTime,
        pickedUpAt,
        droppedOffAt
    )
}

data class CompletedRide(
    override val id: RideId,
    val rider: UserId,
    val pickupLocation: GeoCoordinates,
    val dropOffLocation: GeoCoordinates,
    val scheduledAt: Instant,
    val vin: Vin,
    val pickupTime: Instant,
    val pickedUpAt: Instant,
    val droppedOffAt: Instant,
): Ride

// Errors

sealed interface Error
data class InvalidVinError(val vin: String): Error

class VehicleCommandError(
    command: VehicleCommand?,
    state: Vehicle?,
    message: String,
):
    Error, IllegalStateException(
    "Failed to apply VehicleCommand $command to Vehicle $state: $message"
)

class VehicleEventError(
    state: Vehicle?,
    event: VehicleEvent?,
    message: String,
):
    Error, IllegalStateException(
    "Failed to apply VehicleEvent $event to Vehicle $state: $message"
)

class RideCommandError(
    command: RideCommand?,
    state: Ride?,
    message: String,
):
    Error, IllegalStateException(
    "Failed to apply RideCommand $command to Ride $state: $message"
)

class RideEventError(
    state: Ride?,
    event: RideEvent?,
    message: String,
):
    Error, IllegalStateException(
    "Failed to apply RideEvent $event to Ride $state: $message"
)

// Domain Types

typealias RideId = UUID
typealias UserId = UUID

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)

@JvmInline
value class Vin private constructor(val value: String) {
    companion object {
        private const val VIN_PATTERN = "^(?=.*[0-9])(?=.*[A-z])[0-9A-z-]{17}\$"

        fun build(value: String): Vin =
            validate(Vin(value)) {
                validate(Vin::value).matches(Regex(VIN_PATTERN))
            }

        fun of(value: String): Validated<InvalidVinError, Vin> =
            valikate {
                build(value)
            }.mapLeft { InvalidVinError(value) }
    }
}
