package io.confluent.examples.autonomo.domain

import arrow.core.Validated
import org.valiktor.functions.matches
import org.valiktor.validate
import java.time.Instant
import java.util.*

typealias CommandType = String
typealias EventType = String
typealias ReadModelName = String

// Commands

sealed interface Command {
    val type: CommandType
        get() = "autonomo.command.${this.javaClass.simpleName}"
}

data class AddVehicle(
    // TODO: Implement Command Schema
): Command

data class MakeVehicleAvailable(
    // TODO: Implement Command Schema
): Command

data class RequestRide(
    // TODO: Implement Command Schema
): Command

data class ScheduleRide(
    // TODO: Implement Command Schema
): Command

data class ConfirmPickup(
    // TODO: Implement Command Schema
): Command

data class EndRide(
    // TODO: Implement Command Schema
): Command

data class CancelRide(
    // TODO: Implement Command Schema
): Command

data class MakeVehicleUnavailable(
    // TODO: Implement Command Schema
): Command

data class RemoveVehicle(
    // TODO: Implement Command Schema
): Command

// Events

sealed interface Event {
    val type: EventType
        get() = "autonomo.event.${this.javaClass.simpleName}"
}

sealed interface MyVehiclesEvent: Event
sealed interface AvailableVehiclesEvent: Event
sealed interface RidesEvent: Event
sealed interface Error: Event

data class VehicleAdded(
    // TODO: Implement Event Schema
): MyVehiclesEvent

data class VehicleMadeAvailable(
    // TODO: Implement Event Schema
): AvailableVehiclesEvent

data class RideRequested(
    // TODO: Implement Event Schema
): RidesEvent

data class RideScheduled(
    // TODO: Implement Event Schema
): AvailableVehiclesEvent, RidesEvent

data class RideCancelled(
    // TODO: Implement Event Schema
): AvailableVehiclesEvent, RidesEvent

data class RiderPickedUp(
    // TODO: Implement Event Schema
): RidesEvent

data class RiderDroppedOff(
    // TODO: Implement Event Schema
): AvailableVehiclesEvent, RidesEvent

data class VehicleMadeUnavailable(
    // TODO: Implement Event Schema
): AvailableVehiclesEvent

data class VehicleRemoved(
    // TODO: Implement Event Schema
): MyVehiclesEvent

// Read Models

sealed interface State

sealed interface WriteModel: State

sealed interface ReadModel: State {
    val name: ReadModelName
        get() = "autonomo.read-model.${this.javaClass.simpleName}"
}

interface MyVehicles: ReadModel
interface AvailableVehicles: ReadModel

sealed interface Ride {
    val id: RideId
}

data class RequestedRide(
    override val id: RideId,
    // TODO: Implement Read Model Schema
): Ride {
    fun rideCancelled(
        // TODO: Method params
    ): CancelledRide = TODO("Method implementation")

    fun rideScheduled(
        // TODO: Method params
    ): ScheduledRide = TODO("Method implementation")
}

data class ScheduledRide(
    override val id: RideId,
): Ride {
    fun rideCancelled(
        // TODO: Method params
    ): CancelledScheduledRide = TODO("Method implementation")

    fun riderPickedUp(
        // TODO: Method params
    ): InProgressRide = TODO("Method implementation")
}

interface CancelledRide: Ride {
    // TODO: Properties
}

data class CancelledRequestedRide(
    override val id: RideId,
    // TODO: Additional properties
): CancelledRide

data class CancelledScheduledRide(
    override val id: RideId,
    // TODO: Additional properties
): CancelledRide

data class InProgressRide(
    override val id: RideId,
    // TODO: Additional properties
): Ride {
    fun riderDroppedOff(
        // TODO: Method parameters
    ): CompletedRide = TODO("Method implementation")
}

data class CompletedRide(
    override val id: RideId,
    // TODO: Additional properties
): Ride

// Errors

data class InvalidVinError(val vin: String): Error
object InvalidStateError: Error

// Domain Types

typealias UserId = UUID

const val VIN_PATTERN = "^(?=.*[0-9])(?=.*[A-z])[0-9A-z-]{17}\$"

@JvmInline
value class Vin private constructor(val value: String) {
    companion object {
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

typealias RideId = UUID

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double
)
