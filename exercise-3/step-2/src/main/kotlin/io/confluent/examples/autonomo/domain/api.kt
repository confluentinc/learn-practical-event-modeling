package io.confluent.examples.autonomo.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.time.Instant

interface Decider {
    fun addVehicle(
        state: MyVehicles,
        command: AddVehicle
    ): Either<Error, List<VehicleAdded>>

    fun makeVehicleAvailable(
        state: MyVehicles,
        command: MakeVehicleAvailable
    ): Either<Error, List<VehicleMadeAvailable>>

    fun scheduleRide(
        state: AvailableVehicles,
        command: ScheduleRide,
    ): Either<Error, List<RideScheduled>>

    fun confirmPickup(
        state: RidesWriteModel,
        command: ConfirmPickup
    ): Either<Error, List<RiderPickedUp>>

    fun endRide(
        state: RidesWriteModel,
        command: EndRide
    ): Either<Error, List<RiderDroppedOff>>

    fun cancelRide(
        state: RidesWriteModel,
        command: CancelRide
    ): Either<Error, List<RideCancelled>>

    fun makeVehicleUnavailable(
        state: AvailableVehicles,
        command: MakeVehicleUnavailable
    ): Either<Error, List<VehicleMadeUnavailable>>

    fun removeVehicle(
        state: MyVehicles,
        command: RemoveVehicle
    ): Either<Error, List<VehicleRemoved>>
}

interface Evolver {
    val ridesWriteModel: RidesWriteModel

    fun evolveMyVehicles(
        state: MyVehicles,
        event: MyVehiclesEvent,
    ): Either<Error, MyVehicles> = when(event) {
        is VehicleAdded -> TODO()
        is VehicleRemoved -> TODO()
    }

    fun evolveAvailableVehicles(
        state: AvailableVehicles,
        event: AvailableVehiclesEvent,
    ): Either<Error, AvailableVehicles> = when(event) {
        is RideCancelled -> TODO()
        is RideScheduled -> TODO()
        is RiderDroppedOff -> TODO()
        is VehicleMadeAvailable -> TODO()
        is VehicleMadeUnavailable -> TODO()
    }

    fun evolveRide(
        ride: Ride,
        event: RidesEvent,
    ): Either<Error, Ride> = when(event) {
        is RideRequested -> TODO()
        is RideScheduled -> when(ride) {
            is RequestedRide -> TODO()
            else -> InvalidStateError.left()
        }
        is RideCancelled -> when(ride) {
            is RequestedRide -> TODO()
            else -> InvalidStateError.left()
        }
        is RiderPickedUp -> when(ride) {
            is ScheduledRide -> TODO()
            else -> InvalidStateError.left()
        }
        is RiderDroppedOff -> when(ride) {
            is RequestedRide -> TODO()
            else -> InvalidStateError.left()
        }
    }
}

interface RidesReadModel: ReadModel {
    fun rideById(id: RideId): Ride?
}

interface RidesWriteModel: WriteModel, RidesReadModel {
    fun rideRequested(
        // TODO: Method params
    ): RequestedRide = TODO("Method implementation")

    fun rideScheduled(
        // TODO: Method params
    ): ScheduledRide = TODO("Method implementation")

    fun rideCancelled(
        // TODO: Method params
    ): CancelledRide = TODO("Method implementation")

    fun riderPickedUp(
        // TODO: Method params
    ): InProgressRide = TODO("Method implementation")

    fun riderDroppedOff(
        // TODO: Method params
    ): CompletedRide = TODO("Method implementation")
}
