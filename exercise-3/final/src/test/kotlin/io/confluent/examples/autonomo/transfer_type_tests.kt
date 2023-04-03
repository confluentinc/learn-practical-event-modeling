package io.confluent.examples.autonomo

import io.confluent.examples.autonomo.transfer.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

private const val VALID_VIN = "1FTZX1722XKA76091"
private val ownerId = UUID.randomUUID().toString()

// Note: I wouldn't recommend actually writing tests like this for a production app/service,
// as they are very low value and basically only test the Kotlin language
// and Protobufs library.  They are included to guide learners in completing
// Exercise 3, Steps 1 and 2.
class TransferTypeConstructors {
    @Test
    fun `Constructing Vehicle Command transfer types`() {
        Assertions.assertDoesNotThrow {
            vehicleCommand {
                addVehicle = addVehicle {
                    owner = UUID.randomUUID().toString()
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                makeVehicleAvailable = makeVehicleAvailable {
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                markVehicleOccupied = markVehicleOccupied {
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                markVehicleUnoccupied = markVehicleUnoccupied {
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                requestVehicleReturn = requestVehicleReturn {
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                confirmVehicleReturn = confirmVehicleReturn {
                    vin = VALID_VIN
                }
            }

            vehicleCommand {
                removeVehicle = removeVehicle {
                    owner = UUID.randomUUID().toString()
                    vin = VALID_VIN
                }
            }
        }
    }

    @Test
    fun `Constructing Vehicle Event transfer types`() {
        Assertions.assertDoesNotThrow {
            vehicleEvent {
                vehicleAdded = vehicleAdded {
                    owner = UUID.randomUUID().toString()
                    vin = VALID_VIN
                }
            }

            vehicleEvent {
                vehicleAvailable = vehicleAvailable {
                    vin = VALID_VIN
                    availableAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleOccupied = vehicleOccupied {
                    vin = VALID_VIN
                    occupiedAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleReturnRequested = vehicleReturnRequested {
                    vin = VALID_VIN
                    returnRequestedAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleReturned = vehicleReturned {
                    vin = VALID_VIN
                    returnedAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleReturning = vehicleReturning {
                    vin = VALID_VIN
                    returningAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleRemoved = vehicleRemoved {
                    owner = UUID.randomUUID().toString()
                    vin = VALID_VIN
                    removedAt = Instant.now().toTimestamp()
                }
            }

            vehicleEvent {
                vehicleError = vehicleError {
                    vin = VALID_VIN
                    message = "A nice error message"
                }
            }
        }
    }

    @Test
    fun `Constructing Vehicle ReadModel transfer types`() {
        Assertions.assertDoesNotThrow {
            vehicleReadModel { initial = initialVehicleState { } }
            vehicleReadModel {
                vehicle = vehicle {
                    vin = VALID_VIN
                    owner = ownerId
                    status = VehicleStatus.InInventory
                }
            }
        }
    }
}