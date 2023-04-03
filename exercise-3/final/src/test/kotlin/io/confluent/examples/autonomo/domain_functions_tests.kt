package io.confluent.examples.autonomo

import io.confluent.examples.autonomo.domain.*
import io.confluent.examples.autonomo.domain.CancelRide
import io.confluent.examples.autonomo.domain.GeoCoordinates
import io.confluent.examples.autonomo.domain.InitialRideState
import io.confluent.examples.autonomo.domain.MakeVehicleAvailable
import io.confluent.examples.autonomo.domain.MarkVehicleUnoccupied
import io.confluent.examples.autonomo.domain.RequestRide
import io.confluent.examples.autonomo.domain.RideId
import io.confluent.examples.autonomo.domain.RideRequested
import io.confluent.examples.autonomo.domain.VehicleAvailable
import io.confluent.examples.autonomo.domain.VehicleReturnRequested
import io.confluent.examples.autonomo.domain.VehicleReturning
import io.confluent.examples.autonomo.transfer.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class DomainFunctionsTests {
    companion object {
        private val VALID_VIN = Vin.build("1FTZX1722XKA76091")
        private val ownerId = UserId.randomUUID()
        private val riderId = UserId.randomUUID()
        private val origin = GeoCoordinates(37.3861, -122.0839)
        private val destination = GeoCoordinates(40.4249, -111.7979)
    }

    @Test
    fun `Decide on Request Ride`() {
        val validInitialState = InitialRideState
        val command  = RequestRide(riderId, origin, destination, Instant.now())
        Assertions.assertDoesNotThrow {
            val result = command.decide(validInitialState)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(RideRequested::class.java, result.first())
        }
        Assertions.assertThrows(RideCommandError::class.java) {
            command.decide(
                RequestedRide(
                    RideId.randomUUID(),
                    riderId,
                    Instant.now(),
                    origin,
                    destination,
                    Instant.now(),
                )
            )
        }
    }

    @Test
    fun `Decide on Cancel Ride`() {
        val rideId = RideId.randomUUID()
        val command  = CancelRide(rideId)

        val requestedRide = RequestedRide(rideId, riderId, Instant.now(), origin, destination, Instant.now())
        Assertions.assertDoesNotThrow {
            val result = command.decide(requestedRide)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(RequestedRideCancelled::class.java, result.first())
        }

        val scheduledRide = ScheduledRide(rideId, riderId, Instant.now(), origin, destination, VALID_VIN, Instant.now())
        Assertions.assertDoesNotThrow {
            val result = command.decide(scheduledRide)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(ScheduledRideCancelled::class.java, result.first())
        }

        val initialRideState = InitialRideState
        Assertions.assertThrows(RideCommandError::class.java) {
            command.decide(initialRideState)
        }
    }

    @Test
    fun `Evolve on Ride Requested`() {
        val rideId = RideId.randomUUID()
        val applicableEvent  = RideRequested(rideId, riderId, origin, destination, Instant.now(), Instant.now())

        val result = InitialRideState.evolve(applicableEvent)
        Assertions.assertInstanceOf(RequestedRide::class.java, result)
        Assertions.assertEquals(rideId, result.id)

        val notApplicableEvent = RequestedRideCancelled(rideId, Instant.now())
        Assertions.assertEquals(InitialRideState, InitialRideState.evolve(notApplicableEvent))
    }

    @Test
    fun `Evolve on Ride Cancelled`() {
        val rideId = RideId.randomUUID()

        val requestedRideCancelled = RequestedRideCancelled(rideId, Instant.now())
        val requestedRide = RequestedRide(rideId, riderId, Instant.now(), origin, destination, Instant.now())
        val requestedRideResult = requestedRide.evolve(requestedRideCancelled)

        Assertions.assertInstanceOf(CancelledRequestedRide::class.java, requestedRideResult)
        Assertions.assertEquals(rideId, requestedRideResult.id)

        val scheduledRideCancelled = ScheduledRideCancelled(rideId, VALID_VIN, Instant.now())
        val scheduledRide = ScheduledRide(rideId, riderId, Instant.now(), origin, destination, VALID_VIN, Instant.now())
        val scheduledRideResult = scheduledRide.evolve(scheduledRideCancelled)

        Assertions.assertInstanceOf(CancelledScheduledRide::class.java, scheduledRideResult)
        Assertions.assertEquals(rideId, scheduledRideResult.id)

        val notApplicableEvent = RideRequested(rideId, riderId, origin, destination, Instant.now(), Instant.now())
        Assertions.assertEquals(requestedRide, requestedRide.evolve(notApplicableEvent))
        Assertions.assertEquals(scheduledRide, scheduledRide.evolve(notApplicableEvent))
    }

    @Test
    fun `Decide on Make Vehicle Available`() {
        val validInitialState = InventoryVehicle(VALID_VIN, ownerId)

        val command  = MakeVehicleAvailable(VALID_VIN)
        Assertions.assertDoesNotThrow {
            val result = command.decide(validInitialState)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(VehicleAvailable::class.java, result.first())
        }
        Assertions.assertThrows(VehicleCommandError::class.java) {
            command.decide(AvailableVehicle(VALID_VIN, ownerId))
        }
    }

    @Test
    fun `Decide on Mark Vehicle Unoccupied`() {
        val command  = MarkVehicleUnoccupied(VALID_VIN)

        val occupiedVehicle = OccupiedVehicle(VALID_VIN, ownerId)
        Assertions.assertDoesNotThrow {
            val result = command.decide(occupiedVehicle)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(VehicleAvailable::class.java, result.first())
        }

        val occupiedReturningVehicle = OccupiedReturningVehicle(VALID_VIN, ownerId)
        Assertions.assertDoesNotThrow {
            val result = command.decide(occupiedReturningVehicle)
            Assertions.assertEquals(result.size, 1)
            Assertions.assertInstanceOf(VehicleReturning::class.java, result.first())
        }

        Assertions.assertThrows(VehicleCommandError::class.java) {
            command.decide(AvailableVehicle(VALID_VIN, ownerId))
        }
    }

    @Test
    fun `Evolve on Vehicle Available`() {
        val vehicleAvailable = VehicleAvailable(VALID_VIN, Instant.now())

        val inventoryVehicle = InventoryVehicle(VALID_VIN, ownerId)
        val inventoryVehicleResult = inventoryVehicle.evolve(vehicleAvailable)
        Assertions.assertInstanceOf(AvailableVehicle::class.java, inventoryVehicleResult)

        val occupiedVehicle = OccupiedVehicle(VALID_VIN, ownerId)
        val occupiedVehicleResult = occupiedVehicle.evolve(vehicleAvailable)
        Assertions.assertInstanceOf(AvailableVehicle::class.java, occupiedVehicleResult)
    }

    @Test
    fun `Evolve on VehicleReturnRequested`() {
        val vehicleReturnRequested = VehicleReturnRequested(VALID_VIN, Instant.now())

        val occupiedVehicle = OccupiedVehicle(VALID_VIN, ownerId)
        val occupiedVehicleResult = occupiedVehicle.evolve(vehicleReturnRequested)
        Assertions.assertInstanceOf(OccupiedReturningVehicle::class.java, occupiedVehicleResult)
    }

    @Test
    fun `decide Domain Function for Rides`() {
        val command = rideCommand {
            requestRide = requestRide {  }
        }
        val state = rideReadModel { initial = io.confluent.examples.autonomo.transfer.InitialRideState.getDefaultInstance() }

        Assertions.assertDoesNotThrow {
            decide(command, state)
        }
    }

    @Test
    fun `evolve Domain Function for Vehicles`() {
        val state = vehicleReadModel {
            vehicle = vehicle {
                vin = VALID_VIN.value
                owner = ownerId.toString()
                status = VehicleStatus.InInventory
            }
        }
        val event = vehicleEvent {
            vehicleAvailable = vehicleAvailable {
                vin = VALID_VIN.value
                availableAt = Instant.now().toTimestamp()
            }
        }

        Assertions.assertDoesNotThrow {
            evolve(state, event)
        }
    }
}