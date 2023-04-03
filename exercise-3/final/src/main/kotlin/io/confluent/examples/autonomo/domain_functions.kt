package io.confluent.examples.autonomo

import io.confluent.examples.autonomo.transfer.*
import io.confluent.examples.autonomo.domain.VehicleEvent as DomainVehicleEvent
import io.confluent.examples.autonomo.domain.RideScheduled as DomainRideScheduled
import io.confluent.examples.autonomo.domain.ScheduledRideCancelled as DomainScheduledRideCancelled
import io.confluent.examples.autonomo.domain.RiderDroppedOff as DomainRiderDroppedOff
import io.confluent.examples.autonomo.domain.MarkVehicleOccupied as DomainMarkVehicleOccupied
import io.confluent.examples.autonomo.domain.MarkVehicleUnoccupied as DomainMarkVehicleUnoccupied
import io.confluent.examples.autonomo.domain.VehicleCommand as DomainVehicleCommand
import io.confluent.examples.autonomo.domain.RideEvent as DomainRideEvent

// ***** Vehicles *****

fun decide(command: VehicleCommand, state: VehicleReadModel): Result<List<VehicleEvent>> =
    runCatching { command.toDomain().decide(state.toDomain()).map(DomainVehicleEvent::toTransfer) }

fun evolve(state: VehicleReadModel, event: VehicleEvent): VehicleReadModel =
    state.toDomain().evolve(event.toDomain()).toTransfer()

// ***** Rides *****

fun decide(command: RideCommand, state: RideReadModel): Result<List<RideEvent>> =
    runCatching { command.toDomain().decide(state.toDomain()).map(DomainRideEvent::toTransfer) }

fun evolve(state: RideReadModel, event: RideEvent): RideReadModel =
    state.toDomain().evolve(event.toDomain()).toTransfer()

fun react(event: RideEvent): List<VehicleCommand> =
    when(val domainEvent = event.toDomain()) {
        is DomainRideScheduled -> listOf(
            DomainMarkVehicleOccupied(domainEvent.vin)
        )
        is DomainScheduledRideCancelled -> listOf(
            DomainMarkVehicleUnoccupied(domainEvent.vin)
        )
        is DomainRiderDroppedOff -> listOf(
            DomainMarkVehicleUnoccupied(domainEvent.vin)
        )
        else -> emptyList()
    }.map(DomainVehicleCommand::toTransfer)
