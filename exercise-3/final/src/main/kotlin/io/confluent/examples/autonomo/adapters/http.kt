package io.confluent.examples.autonomo.adapters

import io.confluent.examples.autonomo.transfer.*
import io.confluent.examples.autonomo.decide
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import java.net.URI
import java.util.UUID

@Controller("/rides")
class RidesController(
    private val stateService: QueryService,
    private val producer: Producer<RideId, RideEvent>,
    @Value("\${kafka.topics.ride-events}") private val rideEventsTopic: String
) {
    @Get(value = "/{id}", produces = [MediaType.APPLICATION_JSON])
    fun getRideById(id: UUID): HttpResponse<RideReadModel> =
        stateService.getRideById(id)
            ?.let { HttpResponse.ok(it) }
            ?: HttpResponse.notFound()

    @Post(value = "/request", consumes = [MediaType.APPLICATION_JSON])
    fun requestRide(@Body command: RequestRide): HttpResponse<String> {
        val state = rideReadModel { initial = InitialRideState.getDefaultInstance() }
        return processCommand(rideCommand { requestRide = command }, state)
    }

    @Delete(value = "/{id}", consumes = [MediaType.APPLICATION_JSON])
    fun cancelRide(@PathVariable id: UUID, @Body command: CancelRide): HttpResponse<String> {
        stateService.getRideById(id)?.let { state ->
            return processCommand(rideCommand { cancelRide = command }, state)
        }
        return HttpResponse.notFound("No ride with id: $id")
    }

    @Put(value = "/{id}/pickup", consumes = [MediaType.APPLICATION_JSON])
    fun confirmPickup(@PathVariable id: UUID, @Body command: ConfirmPickup): HttpResponse<String> {
        stateService.getRideById(id)?.let { state ->
            return processCommand(rideCommand { confirmPickup = command }, state)
        }
        return HttpResponse.notFound("No ride with id: $id")
    }

    @Put(value = "/{id}/dropoff", consumes = [MediaType.APPLICATION_JSON])
    fun endRide(@PathVariable id: UUID, @Body command: EndRide): HttpResponse<String> {
        stateService.getRideById(id)?.let { state ->
            return processCommand(rideCommand { endRide = command }, state)
        }
        return HttpResponse.notFound("No ride with id: $id")
    }

    private fun processCommand(command: RideCommand, state: RideReadModel): HttpResponse<String> {
        val result = decide(command, state)
        return if (result.isSuccess) {
            val events = result.getOrDefault(listOf())

            if (events.isNotEmpty()) {
                producer.beginTransaction()
                for (event in events) {
                    producer.send(ProducerRecord(rideEventsTopic, event.ride, event))
                }
                producer.commitTransaction()
            }

            HttpResponse
                .accepted<String?>(URI("/rides/${result.getOrDefault(listOf()).firstOrNull()?.ride}"))
                .body("Success")
        } else {
            HttpResponse.badRequest("Failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
        }
    }
}

@Controller("/vehicles")
class VehiclesController(
    private val stateService: QueryService,
    private val producer: Producer<String, VehicleEvent>,
    @Value("\${kafka.topics.vehicle-events}") private val vehicleEventsTopic: String
) {
    @Get(value = "/{vin}", produces = [MediaType.APPLICATION_JSON])
    fun vehicleByVin(@PathVariable vin: String): HttpResponse<VehicleReadModel> =
        stateService.getVehicleByVin(vin)
            ?.let { HttpResponse.ok(it) }
            ?: HttpResponse.notFound()

    @Get(value = "/mine", produces = [MediaType.APPLICATION_JSON])
    fun myVehicles(): HttpResponse<List<VehicleReadModel>> =
        HttpResponse.ok(stateService.getMyVehicles())

    @Get(value = "/available", produces = [MediaType.APPLICATION_JSON])
    fun availableVehicles(): HttpResponse<List<VehicleReadModel>> =
        HttpResponse.ok(stateService.getAvailableVehicles())

    @Post(value = "/mine", consumes = [MediaType.APPLICATION_JSON])
    fun addVehicle(@Body command: AddVehicle): HttpResponse<String> {
        val state = stateService.getVehicleByVin(command.vin)
            ?: vehicleReadModel {initial = InitialVehicleState.getDefaultInstance() }
        return processCommand(vehicleCommand { addVehicle = command }, state)
    }

    @Put(value = "/mine/{vin}/availability", consumes = [MediaType.APPLICATION_JSON])
    fun makeVehicleAvailable(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    makeVehicleAvailable = makeVehicleAvailable { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    @Delete(value = "/mine/{vin}/availability", consumes = [MediaType.APPLICATION_JSON])
    fun requestVehicleReturn(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    requestVehicleReturn = requestVehicleReturn { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    @Delete(value = "/mine/{vin}", consumes = [MediaType.APPLICATION_JSON])
    fun removeVehicle(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    removeVehicle = removeVehicle { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    @Put(value = "/available/{vin}/occupancy", consumes = [MediaType.APPLICATION_JSON])
    fun markVehicleOccupied(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    markVehicleOccupied = markVehicleOccupied { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    @Delete(value = "/available/{vin}/occupancy", consumes = [MediaType.APPLICATION_JSON])
    fun markVehicleUnoccupied(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    markVehicleUnoccupied = markVehicleUnoccupied { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    @Delete(value = "/available/{vin}", consumes = [MediaType.APPLICATION_JSON])
    fun confirmVehicleReturn(@PathVariable vin: String): HttpResponse<String> {
        stateService.getVehicleByVin(vin)?.let { state ->
            return processCommand(
                vehicleCommand {
                    confirmVehicleReturn = confirmVehicleReturn { this.vin = vin }
                },
                state
            )
        }
        return HttpResponse.notFound("No vehicle with VIN: $vin")
    }

    private fun processCommand(command: VehicleCommand, state: VehicleReadModel): HttpResponse<String> {
        val result = decide(command, state)
        return if (result.isSuccess) {
            val events = result.getOrDefault(listOf())

            if (events.isNotEmpty()) {
                producer.beginTransaction()
                for (event in events) {
                    producer.send(ProducerRecord(vehicleEventsTopic, event.vin, event))
                }
                producer.commitTransaction()
            }

            HttpResponse
                .accepted<String?>(URI("/vehicles/${result.getOrDefault(listOf()).firstOrNull()?.vin}"))
                .body("Success")
        } else {
            HttpResponse.badRequest("Failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
        }
    }
}
