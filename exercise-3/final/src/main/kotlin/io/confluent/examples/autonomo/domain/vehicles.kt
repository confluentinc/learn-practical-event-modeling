package io.confluent.examples.autonomo.domain

import java.time.Instant

// ***** Commands *****

sealed interface VehicleCommand: Command {
    val vin: Vin

    fun decide(state: Vehicle): List<VehicleEvent>
}

data class AddVehicle(
    val owner: UserId,
    override val vin: Vin,
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            InitialVehicleState -> listOf(VehicleAdded(owner, vin))
            else -> throw VehicleCommandError(this, state, "Vehicle already exists")
        }
}

data class MakeVehicleAvailable(
    override val vin: Vin
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is InventoryVehicle -> listOf(VehicleAvailable(vin, Instant.now()))
            else -> throw VehicleCommandError(this, state, "Only vehicles in the inventory can be made available")
        }
}

data class MarkVehicleOccupied(
    override val vin: Vin
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is AvailableVehicle -> listOf(VehicleOccupied(vin, Instant.now()))
            else -> throw VehicleCommandError(this, state, "Only available vehicles can become occupied")
        }
}

data class MarkVehicleUnoccupied(
    override val vin: Vin
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is OccupiedReturningVehicle -> listOf(VehicleReturning(vin, Instant.now()))
            is OccupiedVehicle -> listOf(VehicleAvailable(vin, Instant.now()))
            else -> throw VehicleCommandError(this, state,
                "Only occupied or occupied-returning vehicles can be marked as unoccupied")
        }
}

data class RequestVehicleReturn(
    override val vin: Vin
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is AvailableVehicle -> listOf(VehicleReturning(vin, Instant.now()))
            is OccupiedVehicle -> listOf(VehicleReturnRequested(vin, Instant.now()))
            else -> throw VehicleCommandError(this, state, "Only available or occupied vehicles can be requested for return")
        }
}

data class ConfirmVehicleReturn(
    override val vin: Vin
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is ReturningVehicle -> listOf(VehicleReturned(vin, Instant.now()))
            else -> throw VehicleCommandError(this, state, "Only vehicles being returned can be confirmed as returned")
        }
}

data class RemoveVehicle(
    val owner: UserId,
    override val vin: Vin,
): VehicleCommand {
    override fun decide(state: Vehicle): List<VehicleEvent> =
        when(state) {
            is InventoryVehicle -> listOf(VehicleRemoved(state.owner, vin, Instant.now()))
            else -> throw VehicleCommandError(this, state, "Only vehicles in the inventory can be removed")
        }
}

// ***** Events *****

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

data class VehicleReturning(
    override val vin: Vin,
    val returningAt: Instant
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

// ***** Read Models *****

sealed interface Vehicle: ReadModel {
    val owner: UserId
    val vin: Vin

    suspend fun year(): String?  = null
    suspend fun make(): String?  = null
    suspend fun model(): String? = null
    suspend fun color(): String? = null

    fun evolve(event: VehicleEvent): Vehicle
}

object InitialVehicleState : Vehicle {
    override val owner: UserId
        get() = throw IllegalStateException("Vehicles don't have an Owner before they're created")

    override val vin: Vin
        get() = throw IllegalStateException("Vehicles don't have a VIN before they're created")

    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleAdded -> InventoryVehicle(event.vin, event.owner)
            else -> this
        }
}

data class InventoryVehicle(override val vin: Vin, override val owner: UserId): Vehicle {
    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleAvailable -> AvailableVehicle(this.vin, this.owner)
            is VehicleRemoved -> InitialVehicleState
            else -> this
        }
}

data class AvailableVehicle(override val vin: Vin, override val owner: UserId): Vehicle {
    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleOccupied -> OccupiedVehicle(this.vin, this.owner)
            is VehicleReturning -> ReturningVehicle(this.vin, this.owner)
            else -> this
        }
}

data class OccupiedVehicle(override val vin: Vin, override val owner: UserId): Vehicle {
    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleAvailable -> AvailableVehicle(this.vin, this.owner)
            is VehicleReturnRequested -> OccupiedReturningVehicle(this.vin, this.owner)
            else -> this
        }
}

data class OccupiedReturningVehicle(override val vin: Vin, override val owner: UserId): Vehicle {
    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleReturning -> ReturningVehicle(this.vin, this.owner)
            else -> this
        }
}

data class ReturningVehicle(override val vin: Vin, override val owner: UserId): Vehicle {
    override fun evolve(event: VehicleEvent): Vehicle =
        when(event) {
            is VehicleReturned -> InventoryVehicle(this.vin, this.owner)
            else -> this
        }
}

// ***** Errors *****

class VehicleCommandError(
    command: VehicleCommand,
    state: Vehicle,
    message: String,
): IllegalStateException("Failed to apply VehicleCommand $command to Vehicle $state: $message")
