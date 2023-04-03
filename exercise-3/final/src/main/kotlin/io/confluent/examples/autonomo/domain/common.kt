package io.confluent.examples.autonomo.domain

import java.util.*

// ***** Domain Types *****

typealias RideId = UUID
typealias UserId = UUID

data class GeoCoordinates private constructor (
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0

        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0

        operator fun invoke(latitude: Double, longitude: Double): GeoCoordinates {
            if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                throw InvalidLatitude(latitude)
            }
            if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
                throw InvalidLongitude(longitude)
            }
            return GeoCoordinates(latitude, longitude)
        }
    }
}

class InvalidLatitude(
    latitude: Double,
): IllegalArgumentException("Latitude must be between ${GeoCoordinates.MIN_LATITUDE} " +
        "and ${GeoCoordinates.MAX_LATITUDE}, but was given: $latitude")

class InvalidLongitude(
    longitude: Double,
): IllegalArgumentException("Longitude must be between ${GeoCoordinates.MIN_LONGITUDE} " +
        "and ${GeoCoordinates.MAX_LONGITUDE}, but was given: $longitude")

@JvmInline
value class Vin private constructor(val value: String) {
    companion object {
        private val VIN_PATTERN = Regex("^(?=.*[0-9])(?=.*[A-z])[0-9A-z-]{17}\$")

        fun build(value: String): Vin =
            if (value.matches(VIN_PATTERN)) {
                Vin(value)
            } else {
                throw InvalidVinError(value)
            }
    }
}

class InvalidVinError(
    vinString: String
): IllegalArgumentException("Invalid VIN string: $vinString")

// ***** Model Components *****

typealias CommandType = String

sealed interface Command {
    val type: CommandType
        get() = "autonomo.command.${this.javaClass.simpleName}"
}

typealias EventType = String

sealed interface Event {
    val type: EventType
        get() = "autonomo.event.${this.javaClass.simpleName}"
}

typealias ReadModelName = String

sealed interface ReadModel {
    val name: ReadModelName
        get() = "autonomo.read-model.${this.javaClass.simpleName}"
}