package io.confluent.examples.autonomo.domain

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import java.time.Instant

// Vehicles

// TODO: Docstring!
fun decideVehicle(command: VehicleCommand?, state: Vehicle?): Flow<VehicleEvent> {
    if (command?.vin == state?.vin)
        throw VehicleCommandError(
            command,
            state,
            "This Command doesn't match this Vehicle's VIN"
        )
    return when(command) {
        is AddVehicle -> when(state) {
            is ProspectiveVehicleContext -> if (state.isVinUniqueInInventory(command.vin))
                flowOf(
                    VehicleAdded(
                        command.owner,
                        command.vin,
                    )
                )
            else
                throw VehicleCommandError(command, state,
                    "A vehicle with the provided VIN already exists in the inventory"
                )
            else -> throw VehicleCommandError(command, state,
                "Can't add a duplicate vehicle to the inventory"
            )
        }
        is MakeVehicleAvailable -> when(state) {
            is InventoryVehicle -> flowOf(
                VehicleAvailable(
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only make available a vehicle in inventory"
            )
        }
        is MarkVehicleOccupied -> when(state) {
            is AvailableVehicle -> flowOf(
                VehicleOccupied(
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only occupy an available vehicle"
            )
        }
        is MarkVehicleUnoccupied -> when(state) {
            is OccupiedVehicle, is OccupiedReturningVehicle -> flowOf(
                VehicleAvailable(
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only mark an occupied vehicle as unoccupied"
            )
        }
        is RequestVehicleReturn -> when(state) {
            is AvailableVehicle, is OccupiedVehicle -> flowOf(
                VehicleReturnRequested(
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only request the return of an in-service vehicle"
            )
        }
        is ConfirmVehicleReturn -> when(state) {
            is AvailableVehicle -> flowOf(
                VehicleReturned(
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only confirm the return of an available vehicle"
            )
        }
        is RemoveVehicle -> when(state) {
            is InventoryVehicle -> flowOf(
                VehicleRemoved(
                    command.owner,
                    command.vin,
                    Instant.now()
                )
            )
            else -> throw VehicleCommandError(command, state,
                "Can only remove a vehicle currently in the inventory"
            )
        }
        null -> emptyFlow()
    }
}

// TODO: Docstring!
fun evolveVehicle(state: Vehicle?, event: VehicleEvent?): Vehicle? {
    if (state?.vin != event?.vin)
        throw VehicleEventError(
            state, event,
            "Event doesn't match Vehicle's VIN"
        )
    return when(event) {
        is VehicleAdded -> when(state) {
            null -> InventoryVehicle(event.vin)
            else -> state
        }
        is VehicleAvailable -> when(state) {
            is InventoryVehicle -> AvailableVehicle(event.vin)
            else -> state
        }
        is VehicleOccupied -> when(state) {
            is AvailableVehicle -> OccupiedVehicle(event.vin)
            else -> state
        }
        is VehicleReturnRequested -> when(state) {
            is AvailableVehicle -> ReturningVehicle(event.vin)
            is OccupiedVehicle -> OccupiedReturningVehicle(event.vin)
            else -> state
        }
        is VehicleReturned -> when(state) {
            is AvailableVehicle -> InventoryVehicle(event.vin)
            else -> state
        }
        is VehicleRemoved -> null
        null -> state
    }
}

fun vehicleDecider(initialState: ProspectiveVehicleContext) =
    Decider<VehicleCommand?, Vehicle?, VehicleEvent?>(
        initialState = initialState,
        decide = ::decideVehicle,
        evolve = ::evolveVehicle
    )

// Rides

// TODO: Docstring!
fun decideRide(command: RideCommand?, state: Ride?): Flow<RideEvent> {
    if (command?.ride != state?.id)
        throw RideCommandError(
            command,
            state,
            "This Command doesn't match this Ride's ID"
        )
    return when(command) {
        is RequestRide -> when(state) {
            null -> flowOf(
                RideRequested(
                RideId.randomUUID(),
                command.rider,
                command.origin,
                command.destination,
                command.pickupTime
            )
            )
            else -> throw RideCommandError(
                command, state, "Can't request an already existing ride"
            )
        }
        is ScheduleRide -> when(state) {
            is RequestedRide -> flowOf(
                RideScheduled(
                    command.ride,
                    command.vin,
                    command.pickupTime,
                    Instant.now()
                )
            )
            else -> throw RideCommandError(
                command, state, "Only requested rides can be scheduled"
            )
        }
        is CancelRide -> when(state) {
            is RequestedRide, is ScheduledRide -> flowOf(
                RideCancelled(
                    command.ride,
                    Instant.now()
                )
            )
            else -> throw RideCommandError(
                command, state, "Only requested or scheduled rides can be cancelled"
            )
        }
        is ConfirmPickup -> when(state) {
            is ScheduledRide -> flowOf(
                RiderPickedUp(
                    command.ride,
                    command.vin,
                    command.rider,
                    command.pickupLocation,
                    Instant.now()
                )
            )
            else -> throw RideCommandError(
                command, state, "Only scheduled rides can be picked up"
            )
        }
        is EndRide -> when(state) {
            is InProgressRide -> flowOf(
                RiderDroppedOff(
                    command.ride,
                    command.dropOffLocation,
                    Instant.now()
                )
            )
            else -> throw RideCommandError(
                command, state, "Only in-progress rides can be dropped off"
            )
        }
        null -> emptyFlow()
    }
}

// TODO: Docstring!
fun evolveRide(state: Ride?, event: RideEvent?): Ride? {
    if (state?.id != event?.ride)
        throw RideEventError(
            state, event,
            "Event doesn't match Ride's ID"
        )

    return when(event) {
        is RideRequested -> when(state) {
            null -> RequestedRide(
                event.ride,
                event.rider,
                event.pickupTime,
                event.origin,
                event.destination
            )
            else -> state
        }
        is RideScheduled -> when(state) {
            is RequestedRide -> state.toScheduledRide(
                event.vin,
                event.pickupTime,
                event.scheduledAt
            )
            else -> state
        }
        is RideCancelled -> when(state) {
            is RequestedRide -> state.toCancelledRide(
                event.cancelledAt
            )
            is ScheduledRide -> state.toCancelledRide(
                event.cancelledAt
            )
            else -> state
        }
        is RiderPickedUp -> when(state) {
            is ScheduledRide -> state.toInProgressRide(
                event.vin,
                event.rider,
                event.pickupLocation,
                event.pickedUpAt
            )
            else -> state
        }
        is RiderDroppedOff -> when(state) {
            is InProgressRide -> state.riderDroppedOff(
                event.dropOffLocation,
                event.droppedOffAt
            )
            else -> state
        }
        null -> state
    }
}

fun ridesDecider() = Decider<RideCommand?, Ride?, RideEvent?>(
    initialState = null,
    decide = ::decideRide,
    evolve = ::evolveRide
)
