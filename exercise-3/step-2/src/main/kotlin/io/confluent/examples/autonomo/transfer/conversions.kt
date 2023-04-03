package io.confluent.examples.autonomo.transfer

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import io.confluent.examples.autonomo.domain.CancelledRequestedRide
import io.confluent.examples.autonomo.domain.CancelledScheduledRide
import io.confluent.examples.autonomo.domain.Vin
import java.time.Instant
import java.util.*
import io.confluent.examples.autonomo.domain.GeoCoordinates as DomainGeoCoordinates
import io.confluent.examples.autonomo.domain.AddVehicle as DomainAddVehicle
import io.confluent.examples.autonomo.domain.AvailableVehicle as DomainAvailableVehicle
import io.confluent.examples.autonomo.domain.CancelRide as DomainCancelRide
import io.confluent.examples.autonomo.domain.CancelledRide as DomainCancelledRide
import io.confluent.examples.autonomo.domain.CancelledRequestedRide as DomainCancelledRequestedRide
import io.confluent.examples.autonomo.domain.CancelledScheduledRide as DomainCancelledScheduledRide
import io.confluent.examples.autonomo.domain.CompletedRide as DomainCompletedRide
import io.confluent.examples.autonomo.domain.ConfirmPickup as DomainConfirmPickup
import io.confluent.examples.autonomo.domain.ConfirmVehicleReturn as DomainConfirmVehicleReturn
import io.confluent.examples.autonomo.domain.EndRide as DomainEndRide
import io.confluent.examples.autonomo.domain.InProgressRide as DomainInProgressRide
import io.confluent.examples.autonomo.domain.InitialRideState as DomainInitialRideState
import io.confluent.examples.autonomo.domain.InitialVehicleState as DomainInitialVehicleState
import io.confluent.examples.autonomo.domain.InventoryVehicle as DomainInventoryVehicle
import io.confluent.examples.autonomo.domain.MakeVehicleAvailable as DomainMakeVehicleAvailable
import io.confluent.examples.autonomo.domain.MarkVehicleOccupied as DomainMarkVehicleOccupied
import io.confluent.examples.autonomo.domain.MarkVehicleUnoccupied as DomainMarkVehicleUnoccupied
import io.confluent.examples.autonomo.domain.OccupiedReturningVehicle as DomainOccupiedReturningVehicle
import io.confluent.examples.autonomo.domain.OccupiedVehicle as DomainOccupiedVehicle
import io.confluent.examples.autonomo.domain.RemoveVehicle as DomainRemoveVehicle
import io.confluent.examples.autonomo.domain.RequestRide as DomainRequestRide
import io.confluent.examples.autonomo.domain.RequestVehicleReturn as DomainRequestVehicleReturn
import io.confluent.examples.autonomo.domain.RequestedRide as DomainRequestedRide
import io.confluent.examples.autonomo.domain.ReturningVehicle as DomainReturningVehicle
import io.confluent.examples.autonomo.domain.Ride as DomainRide
import io.confluent.examples.autonomo.domain.RequestedRideCancelled as DomainRequestedRideCancelled
import io.confluent.examples.autonomo.domain.ScheduledRideCancelled as DomainScheduledRideCancelled
import io.confluent.examples.autonomo.domain.RideCommand as DomainRideCommand
import io.confluent.examples.autonomo.domain.RideEvent as DomainRideEvent
import io.confluent.examples.autonomo.domain.RideRequested as DomainRideRequested
import io.confluent.examples.autonomo.domain.RideScheduled as DomainRideScheduled
import io.confluent.examples.autonomo.domain.RiderDroppedOff as DomainRiderDroppedOff
import io.confluent.examples.autonomo.domain.RiderPickedUp as DomainRiderPickedUp
import io.confluent.examples.autonomo.domain.ScheduleRide as DomainScheduleRide
import io.confluent.examples.autonomo.domain.ScheduledRide as DomainScheduledRide
import io.confluent.examples.autonomo.domain.Vehicle as DomainVehicle
import io.confluent.examples.autonomo.domain.VehicleAdded as DomainVehicleAdded
import io.confluent.examples.autonomo.domain.VehicleAvailable as DomainVehicleAvailable
import io.confluent.examples.autonomo.domain.VehicleCommand as DomainVehicleCommand
import io.confluent.examples.autonomo.domain.VehicleEvent as DomainVehicleEvent
import io.confluent.examples.autonomo.domain.VehicleOccupied as DomainVehicleOccupied
import io.confluent.examples.autonomo.domain.VehicleRemoved as DomainVehicleRemoved
import io.confluent.examples.autonomo.domain.VehicleReturnRequested as DomainVehicleReturnRequested
import io.confluent.examples.autonomo.domain.VehicleReturned as DomainVehicleReturned
import io.confluent.examples.autonomo.domain.VehicleReturning as DomainVehicleReturning

// Util

typealias RideId = String

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanos.toLong())

fun Instant.toTimestamp(): Timestamp = timestamp { seconds = epochSecond; nanos = nano }

fun DomainGeoCoordinates.toTransfer(): GeoCoordinates = geoCoordinates {
    lat = this@toTransfer.latitude
    lng = this@toTransfer.longitude
}

fun GeoCoordinates.toDomain(): DomainGeoCoordinates = DomainGeoCoordinates(
    this.lat,
    this.lng
)

// ***** Vehicle Commands *****

fun AddVehicle.toDomain() = DomainAddVehicle(
    // TODO
)

fun MakeVehicleAvailable.toDomain() = DomainMakeVehicleAvailable(
    Vin.build(vin)
)

fun MarkVehicleOccupied.toDomain() = DomainMarkVehicleOccupied(
    Vin.build(vin)
)

fun MarkVehicleUnoccupied.toDomain() = DomainMarkVehicleUnoccupied(
    Vin.build(vin)
)

fun RequestVehicleReturn.toDomain() = DomainRequestVehicleReturn(
    Vin.build(vin)
)

fun ConfirmVehicleReturn.toDomain() = DomainConfirmVehicleReturn(
    Vin.build(vin)
)

fun RemoveVehicle.toDomain() = DomainRemoveVehicle(
    UUID.fromString(owner),
    Vin.build(vin)
)

// Partition Key
val VehicleCommand.vin: String
    get() = when(commandCase) {
        VehicleCommand.CommandCase.ADD_VEHICLE -> addVehicle.vin
        VehicleCommand.CommandCase.MAKE_VEHICLE_AVAILABLE -> makeVehicleAvailable.vin
        VehicleCommand.CommandCase.MARK_VEHICLE_OCCUPIED -> markVehicleOccupied.vin
        VehicleCommand.CommandCase.MARK_VEHICLE_UNOCCUPIED -> markVehicleUnoccupied.vin
        VehicleCommand.CommandCase.REQUEST_VEHICLE_RETURN -> requestVehicleReturn.vin
        VehicleCommand.CommandCase.CONFIRM_VEHICLE_RETURN -> confirmVehicleReturn.vin
        VehicleCommand.CommandCase.REMOVE_VEHICLE -> removeVehicle.vin
        VehicleCommand.CommandCase.COMMAND_NOT_SET, null -> throw IllegalStateException("VIN not set on Vehicle Command!!!")
    }


fun VehicleCommand.toDomain(): DomainVehicleCommand = when (commandCase) {
    VehicleCommand.CommandCase.ADD_VEHICLE -> addVehicle.toDomain()
    VehicleCommand.CommandCase.MAKE_VEHICLE_AVAILABLE -> makeVehicleAvailable.toDomain()
    VehicleCommand.CommandCase.MARK_VEHICLE_OCCUPIED -> markVehicleOccupied.toDomain()
    VehicleCommand.CommandCase.MARK_VEHICLE_UNOCCUPIED -> markVehicleUnoccupied.toDomain()
    VehicleCommand.CommandCase.REQUEST_VEHICLE_RETURN -> requestVehicleReturn.toDomain()
    VehicleCommand.CommandCase.CONFIRM_VEHICLE_RETURN -> confirmVehicleReturn.toDomain()
    VehicleCommand.CommandCase.REMOVE_VEHICLE -> removeVehicle.toDomain()
    VehicleCommand.CommandCase.COMMAND_NOT_SET, null -> throw IllegalStateException("Vehicle Command not set")
}

fun DomainVehicleCommand.toTransfer(): VehicleCommand = vehicleCommand {
    when (val command = this@toTransfer) {
        is DomainAddVehicle -> addVehicle = addVehicle {
            TODO()
        }
        is DomainMakeVehicleAvailable -> makeVehicleAvailable = makeVehicleAvailable {
            vin = command.vin.value
        }
        is DomainMarkVehicleOccupied -> markVehicleOccupied = markVehicleOccupied {
            vin = command.vin.value
        }
        is DomainMarkVehicleUnoccupied -> markVehicleUnoccupied = markVehicleUnoccupied {
            vin = command.vin.value
        }
        is DomainRequestVehicleReturn -> requestVehicleReturn = requestVehicleReturn {
            vin = command.vin.value
        }
        is DomainConfirmVehicleReturn -> confirmVehicleReturn = confirmVehicleReturn {
            vin = command.vin.value
        }
        is DomainRemoveVehicle -> removeVehicle = removeVehicle {
            owner = command.owner.toString()
            vin = command.vin.value
        }
    }
}

// ***** Vehicle Events *****

fun VehicleAdded.toDomain() = DomainVehicleAdded(
    // TODO
)

fun VehicleAvailable.toDomain() = DomainVehicleAvailable(
    Vin.build(vin),
    availableAt.toInstant()
)

fun VehicleOccupied.toDomain() = DomainVehicleOccupied(
    Vin.build(vin),
    occupiedAt.toInstant()
)

fun VehicleReturnRequested.toDomain() = DomainVehicleReturnRequested(
    Vin.build(vin),
    returnRequestedAt.toInstant()
)

fun VehicleReturning.toDomain() = DomainVehicleReturning(
    Vin.build(vin),
    returningAt.toInstant()
)

fun VehicleReturned.toDomain() = DomainVehicleReturned(
    Vin.build(vin),
    returnedAt.toInstant()
)

fun VehicleRemoved.toDomain() = DomainVehicleRemoved(
    UUID.fromString(owner),
    Vin.build(vin),
    removedAt.toInstant()
)

fun VehicleError.toDomain(): DomainVehicleEvent = TODO()

// Partition Key
val VehicleEvent.vin: String
    get() = when(eventCase) {
        VehicleEvent.EventCase.VEHICLE_ADDED -> vehicleAdded.vin
        VehicleEvent.EventCase.VEHICLE_AVAILABLE -> vehicleAvailable.vin
        VehicleEvent.EventCase.VEHICLE_OCCUPIED -> vehicleOccupied.vin
        VehicleEvent.EventCase.VEHICLE_RETURN_REQUESTED -> vehicleReturnRequested.vin
        VehicleEvent.EventCase.VEHICLE_RETURNING -> vehicleReturning.vin
        VehicleEvent.EventCase.VEHICLE_RETURNED -> vehicleReturned.vin
        VehicleEvent.EventCase.VEHICLE_REMOVED -> vehicleRemoved.vin
        VehicleEvent.EventCase.VEHICLE_ERROR -> vehicleError.vin
        VehicleEvent.EventCase.EVENT_NOT_SET, null -> throw IllegalStateException("Event doesn't have VIN!")
    }

fun VehicleEvent.toDomain(): DomainVehicleEvent = when (eventCase) {
    VehicleEvent.EventCase.VEHICLE_ADDED -> vehicleAdded.toDomain()
    VehicleEvent.EventCase.VEHICLE_AVAILABLE -> vehicleAvailable.toDomain()
    VehicleEvent.EventCase.VEHICLE_OCCUPIED -> vehicleOccupied.toDomain()
    VehicleEvent.EventCase.VEHICLE_RETURN_REQUESTED -> vehicleReturnRequested.toDomain()
    VehicleEvent.EventCase.VEHICLE_RETURNING -> vehicleReturning.toDomain()
    VehicleEvent.EventCase.VEHICLE_RETURNED -> vehicleReturned.toDomain()
    VehicleEvent.EventCase.VEHICLE_REMOVED -> vehicleRemoved.toDomain()
    VehicleEvent.EventCase.VEHICLE_ERROR -> vehicleError.toDomain()
    VehicleEvent.EventCase.EVENT_NOT_SET, null -> throw IllegalStateException("Vehicle Event not set")
}

fun DomainVehicleEvent.toTransfer(): VehicleEvent = vehicleEvent {
    when (val event = this@toTransfer) {
        is DomainVehicleAdded -> vehicleAdded = vehicleAdded {
            TODO()
        }
        is DomainVehicleAvailable -> vehicleAvailable = vehicleAvailable {
            vin = event.vin.value
            availableAt = event.availableAt.toTimestamp()
        }
        is DomainVehicleOccupied -> vehicleOccupied = vehicleOccupied {
            vin = event.vin.value
            occupiedAt = event.occupiedAt.toTimestamp()
        }
        is DomainVehicleRemoved -> vehicleRemoved = vehicleRemoved {
            owner = event.owner.toString()
            vin = event.vin.value
            removedAt = event.removedAt.toTimestamp()
        }
        is DomainVehicleReturnRequested -> vehicleReturnRequested = vehicleReturnRequested {
            vin = event.vin.value
            returnRequestedAt = event.returnRequestedAt.toTimestamp()
        }
        is DomainVehicleReturned -> vehicleReturned = vehicleReturned {
            vin = event.vin.value
            returnedAt = event.returnedAt.toTimestamp()
        }
        is DomainVehicleReturning -> vehicleReturning = vehicleReturning {
            vin = event.vin.value
            returningAt = event.returningAt.toTimestamp()
        }
    }
}

// ***** Vehicle Read Models *****

fun InitialVehicleState.toDomain(): DomainVehicle = DomainInitialVehicleState

fun Vehicle.toDomain(): DomainVehicle = when(this.status) {
    // TODO: missing cases
    VehicleStatus.Occupied -> DomainOccupiedVehicle(Vin.build(this.vin), UUID.fromString(this.owner))
    VehicleStatus.OccupiedReturning -> DomainOccupiedReturningVehicle(Vin.build(this.vin), UUID.fromString(this.owner))
    VehicleStatus.Returning -> DomainReturningVehicle(Vin.build(this.vin), UUID.fromString(this.owner))
    VehicleStatus.UNRECOGNIZED, null -> throw IllegalStateException("Domain Vehicle status not set")
}

fun VehicleReadModel.toDomain(): DomainVehicle = when (this.readModelCase) {
    VehicleReadModel.ReadModelCase.INITIAL -> initial.toDomain()
    VehicleReadModel.ReadModelCase.VEHICLE -> vehicle.toDomain()
    VehicleReadModel.ReadModelCase.READMODEL_NOT_SET, null -> initial.toDomain()
}

fun DomainVehicle.toTransfer(): VehicleReadModel = vehicleReadModel {
    when (val readModel = this@toTransfer) {
        DomainInitialVehicleState -> initial = InitialVehicleState.getDefaultInstance()
        else -> {
            val vehicleStatus = when (readModel) {
                // TODO: missing cases
                else -> VehicleStatus.UNRECOGNIZED
            }
            vehicle = vehicle {
                vin = readModel.vin.value
                owner = readModel.owner.toString()
                status = vehicleStatus
            }
        }
    }
}

// ***** Ride Commands *****

fun RequestRide.toDomain() = DomainRequestRide(
    UUID.fromString(this.rider),
    this.origin.toDomain(),
    this.destination.toDomain(),
    this.pickupTime.toInstant()
)

fun ScheduleRide.toDomain() = DomainScheduleRide(
    UUID.fromString(this.ride),
    Vin.build(this.vin),
    this.pickupTime.toInstant()
)

fun ConfirmPickup.toDomain() = DomainConfirmPickup(
    UUID.fromString(this.ride),
    Vin.build(this.vin),
    UUID.fromString(this.rider),
    this.pickupLocation.toDomain()
)

fun EndRide.toDomain() = DomainEndRide(
    UUID.fromString(this.ride),
    this.dropOffLocation.toDomain()
)

fun CancelRide.toDomain() = DomainCancelRide(
    UUID.fromString(this.ride)
)

fun RideCommand.toDomain(): DomainRideCommand = when (this.commandCase) {
    RideCommand.CommandCase.REQUEST_RIDE -> requestRide.toDomain()
    RideCommand.CommandCase.SCHEDULE_RIDE -> scheduleRide.toDomain()
    RideCommand.CommandCase.CONFIRM_PICKUP -> confirmPickup.toDomain()
    RideCommand.CommandCase.END_RIDE -> endRide.toDomain()
    RideCommand.CommandCase.CANCEL_RIDE -> cancelRide.toDomain()
    RideCommand.CommandCase.COMMAND_NOT_SET, null -> throw IllegalStateException("Ride Command not set in transfer type")
}

fun DomainRideCommand.toTransfer(): RideCommand = rideCommand {
    when (val command = this@toTransfer) {
        is DomainRequestRide -> requestRide = requestRide {
            rider = command.rider.toString()
            origin = command.origin.toTransfer()
            destination = command.destination.toTransfer()
            pickupTime = command.pickupTime.toTimestamp()
        }
        is DomainScheduleRide -> scheduleRide = scheduleRide {
            ride = command.ride.toString()
            vin = command.vin.value
            pickupTime = command.pickupTime.toTimestamp()
        }
        is DomainCancelRide -> cancelRide = cancelRide { ride = command.ride.toString() }
        is DomainConfirmPickup -> confirmPickup = confirmPickup {
            ride = command.ride.toString()
            vin = command.vin.value
            rider = command.rider.toString()
            pickupLocation = command.pickupLocation.toTransfer()
        }
        is DomainEndRide -> endRide = endRide {
            ride = command.ride.toString()
            dropOffLocation = command.dropOffLocation.toTransfer()
        }
    }
}

// ***** Ride Events *****

fun RideRequested.toDomain() = DomainRideRequested(
    UUID.fromString(this.ride),
    UUID.fromString(this.rider),
    this.origin.toDomain(),
    this.destination.toDomain(),
    this.pickupTime.toInstant(),
    this.requestedAt.toInstant()
)

fun RideScheduled.toDomain() = DomainRideScheduled(
    UUID.fromString(this.ride),
    Vin.build(this.vin),
    this.pickupTime.toInstant(),
    this.scheduledAt.toInstant()
)

fun RideCancelled.toDomain() = if (this.vin.isBlank()) {
    DomainRequestedRideCancelled(
        UUID.fromString(this.ride),
        this.cancelledAt.toInstant()
    )
} else {
    DomainScheduledRideCancelled(
        UUID.fromString(this.ride),
        Vin.build(this.vin),
        this.cancelledAt.toInstant()
    )
}

fun RiderPickedUp.toDomain() = DomainRiderPickedUp(
    UUID.fromString(this.ride),
    Vin.build(this.vin),
    UUID.fromString(this.rider),
    this.pickupLocation.toDomain(),
    this.pickedUpAt.toInstant()
)

fun RiderDroppedOff.toDomain() = DomainRiderDroppedOff(
    UUID.fromString(this.ride),
    Vin.build(this.vin),
    this.dropOffLocation.toDomain(),
    this.droppedOffAt.toInstant()
)

fun RideError.toDomain(): DomainRideEvent = TODO()

// Partition Key
val RideEvent.ride: String
    get() = when(eventCase) {
        RideEvent.EventCase.RIDE_REQUESTED -> rideRequested.ride
        RideEvent.EventCase.RIDE_SCHEDULED -> rideScheduled.ride
        RideEvent.EventCase.RIDE_CANCELLED -> rideCancelled.ride
        RideEvent.EventCase.RIDER_PICKED_UP -> riderPickedUp.ride
        RideEvent.EventCase.RIDER_DROPPED_OFF -> riderDroppedOff.ride
        RideEvent.EventCase.RIDE_ERROR -> rideError.ride
        RideEvent.EventCase.EVENT_NOT_SET, null -> throw IllegalStateException("No ID Set for Ride Event!!!")
    }

fun RideEvent.toDomain(): DomainRideEvent = when (this.eventCase) {
    RideEvent.EventCase.RIDE_REQUESTED -> rideRequested.toDomain()
    RideEvent.EventCase.RIDE_SCHEDULED -> rideScheduled.toDomain()
    RideEvent.EventCase.RIDE_CANCELLED -> rideCancelled.toDomain()
    RideEvent.EventCase.RIDER_PICKED_UP -> riderPickedUp.toDomain()
    RideEvent.EventCase.RIDER_DROPPED_OFF -> riderDroppedOff.toDomain()
    RideEvent.EventCase.RIDE_ERROR -> rideError.toDomain()
    RideEvent.EventCase.EVENT_NOT_SET, null -> throw IllegalStateException("Ride Event not set in transfer type")
}

fun DomainRideEvent.toTransfer(): RideEvent = rideEvent {
    when (val event = this@toTransfer) {
        is DomainRideRequested -> rideRequested = rideRequested {
            ride = event.ride.toString()
            rider = event.rider.toString()
            origin = event.origin.toTransfer()
            destination = event.destination.toTransfer()
            pickupTime = event.pickupTime.toTimestamp()
            requestedAt = event.requestedAt.toTimestamp()
        }
        is DomainRideScheduled -> rideScheduled = rideScheduled {
            ride = event.ride.toString()
            vin = event.vin.value
            pickupTime = event.pickupTime.toTimestamp()
            scheduledAt = event.scheduledAt.toTimestamp()
        }
        is DomainRequestedRideCancelled -> rideCancelled = rideCancelled {
            ride = event.ride.toString()
            cancelledAt = event.cancelledAt.toTimestamp()
        }
        is DomainScheduledRideCancelled -> rideCancelled = rideCancelled {
            ride = event.ride.toString()
            vin = event.vin.value
            cancelledAt = event.cancelledAt.toTimestamp()
        }
        is DomainRiderPickedUp -> riderPickedUp = riderPickedUp {
            ride = event.ride.toString()
            rider = event.rider.toString()
            vin = event.vin.value
            pickupLocation = event.pickupLocation.toTransfer()
            pickedUpAt = event.pickedUpAt.toTimestamp()
        }
        is DomainRiderDroppedOff -> riderDroppedOff = riderDroppedOff {
            ride = event.ride.toString()
            vin = event.vin.value
            dropOffLocation = event.dropOffLocation.toTransfer()
            droppedOffAt = event.droppedOffAt.toTimestamp()
        }
    }
}

// ***** Ride Read Models *****

fun InitialRideState.toDomain() = DomainInitialRideState

fun Ride.toDomain(): DomainRide = when(this.status) {
    RideStatus.Requested -> DomainRequestedRide(
        UUID.fromString(this.id),
        UUID.fromString(this.rider),
        this.pickupTime.toInstant(),
        this.pickupLocation.toDomain(),
        this.dropOffLocation.toDomain(),
        this.requestedAt.toInstant()
    )
    RideStatus.Scheduled -> DomainScheduledRide(
        UUID.fromString(this.id),
        UUID.fromString(this.rider),
        this.pickupTime.toInstant(),
        this.pickupLocation.toDomain(),
        this.dropOffLocation.toDomain(),
        Vin.build(this.vin),
        this.scheduledAt.toInstant(),
    )
    RideStatus.InProgress -> DomainInProgressRide(
        UUID.fromString(this.id),
        UUID.fromString(this.rider),
        this.pickupLocation.toDomain(),
        this.dropOffLocation.toDomain(),
        this.scheduledAt.toInstant(),
        Vin.build(this.vin),
        this.pickupTime.toInstant(),
        this.pickupTime.toInstant()
    )
    RideStatus.Completed -> DomainCompletedRide(
        UUID.fromString(this.id),
        UUID.fromString(this.rider),
        this.pickupTime.toInstant(),
        this.pickupLocation.toDomain(),
        this.dropOffLocation.toDomain(),
        Vin.build(this.vin),
        this.pickedUpAt.toInstant(),
        this.droppedOffAt.toInstant()
    )
    RideStatus.Cancelled -> if (this.scheduledAt == null) {
        DomainCancelledRequestedRide(
            UUID.fromString(this.id),
            UUID.fromString(this.rider),
            this.pickupTime.toInstant(),
            this.pickupLocation.toDomain(),
            this.dropOffLocation.toDomain(),
            this.cancelledAt.toInstant()
        )
    } else {
        DomainCancelledScheduledRide(
            UUID.fromString(this.id),
            UUID.fromString(this.rider),
            this.pickupTime.toInstant(),
            this.pickupLocation.toDomain(),
            this.dropOffLocation.toDomain(),
            Vin.build(this.vin),
            this.scheduledAt.toInstant(),
            this.cancelledAt.toInstant()
        )
    }
    RideStatus.UNRECOGNIZED, null -> throw IllegalStateException("Domain Ride status not set")
}

fun RideReadModel.toDomain(): DomainRide = when (this.readModelCase) {
    RideReadModel.ReadModelCase.INITIAL -> initial.toDomain()
    RideReadModel.ReadModelCase.RIDE -> ride.toDomain()
    RideReadModel.ReadModelCase.READMODEL_NOT_SET, null -> initial.toDomain()
}

fun DomainRide.toTransfer(): RideReadModel = rideReadModel {
    when (val readModel = this@toTransfer) {
        DomainInitialRideState -> initial = InitialRideState.getDefaultInstance()
        is DomainRequestedRide -> ride = ride {
            id = readModel.id.toString()
            rider = readModel.rider.toString()
            pickupTime = readModel.requestedPickupTime.toTimestamp()
            pickupLocation = readModel.pickupLocation.toTransfer()
            dropOffLocation = readModel.dropOffLocation.toTransfer()
            requestedAt = readModel.requestedAt.toTimestamp()
            status = RideStatus.Requested
        }
        is DomainScheduledRide -> ride = ride {
            id = readModel.id.toString()
            rider = readModel.rider.toString()
            pickupTime = readModel.scheduledPickupTime.toTimestamp()
            pickupLocation = readModel.pickupLocation.toTransfer()
            dropOffLocation = readModel.dropOffLocation.toTransfer()
            vin = readModel.vin.value
            scheduledAt = readModel.scheduledAt.toTimestamp()
            status = RideStatus.Scheduled
        }
        is DomainCancelledRide -> ride = ride {
            status = RideStatus.Cancelled
            when (readModel) {
                is CancelledRequestedRide -> {
                    id = readModel.id.toString()
                    rider = readModel.rider.toString()
                    pickupTime = readModel.requestedPickupTime.toTimestamp()
                    pickupLocation = readModel.pickupLocation.toTransfer()
                    dropOffLocation = readModel.dropOffLocation.toTransfer()
                    cancelledAt = readModel.cancelledAt.toTimestamp()
                }
                is CancelledScheduledRide -> {
                    id = readModel.id.toString()
                    rider = readModel.rider.toString()
                    pickupTime = readModel.scheduledPickupTime.toTimestamp()
                    pickupLocation = readModel.pickupLocation.toTransfer()
                    dropOffLocation = readModel.dropOffLocation.toTransfer()
                    vin = readModel.vin.value
                    scheduledAt = readModel.scheduledAt.toTimestamp()
                    cancelledAt = readModel.cancelledAt.toTimestamp()
                }
            }
        }
        is DomainInProgressRide -> ride = ride {
            id = readModel.id.toString()
            rider = readModel.rider.toString()
            pickupLocation = readModel.pickupLocation.toTransfer()
            dropOffLocation = readModel.dropOffLocation.toTransfer()
            scheduledAt = readModel.scheduledAt.toTimestamp()
            vin = readModel.vin.value
            pickupTime = readModel.pickupTime.toTimestamp()
            pickedUpAt = readModel.pickedUpAt.toTimestamp()
            status = RideStatus.InProgress
        }
        is DomainCompletedRide -> ride = ride {
            id = readModel.id.toString()
            rider = readModel.rider.toString()
            pickupTime = readModel.pickupTime.toTimestamp()
            pickupLocation = readModel.pickupLocation.toTransfer()
            dropOffLocation = readModel.dropOffLocation.toTransfer()
            vin = readModel.vin.value
            pickedUpAt = readModel.pickedUpAt.toTimestamp()
            droppedOffAt = readModel.droppedOffAt.toTimestamp()
            status = RideStatus.Completed
        }
    }
}
