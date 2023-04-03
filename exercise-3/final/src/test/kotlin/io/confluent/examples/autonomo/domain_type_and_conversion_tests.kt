package io.confluent.examples.autonomo

import io.confluent.examples.autonomo.domain.GeoCoordinates
import io.confluent.examples.autonomo.domain.InvalidVinError
import io.confluent.examples.autonomo.domain.Vin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private const val VALID_VIN = "1FTZX1722XKA76091"

// Note: These test *are* the type of tests that usefully assert important
// aspects of the business domain
class DomainTypeRules {
    @Test
    fun `Valid and invalid values for Vin domain type`() {
        Assertions.assertDoesNotThrow { Vin.build(VALID_VIN) }
        val tooLong = "1FTZX1722XKA76091asdfasdf"
        Assertions.assertThrows(InvalidVinError::class.java) { Vin.build(tooLong) }
        val tooShort = "1FTZX1722XKA7609"
        Assertions.assertThrows(InvalidVinError::class.java) { Vin.build(tooShort) }
    }

    @Test
    fun `Valid and invalid values for GeoCoordinates domain type`() {
        val validLatitudes = listOf(-90.0, -42.0, 0.0, 42.0, 90.0)
        val validLongitudes = listOf(-180.0, -142.0, -42.0, 0.0, 42.0, 142.0, 180.0)
        for (validLatitude in validLatitudes) {
            for (validLongitude in validLongitudes) {
                Assertions.assertDoesNotThrow { GeoCoordinates(validLatitude, validLongitude) }
            }
        }
        val invalidLatitudes = listOf(-90.1, 90.1)
        val invalidLongitudes = listOf(-180.1, 180.1)
        for (invalidLongitude in invalidLongitudes) {
            for (validLatitude in validLatitudes) {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    GeoCoordinates(validLatitude, invalidLongitude)
                }
            }
            for (invalidLatitude in invalidLatitudes) {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    GeoCoordinates(invalidLatitude, invalidLongitude)
                }
            }
        }
        for (invalidLatitude in invalidLatitudes) {
            for (validLongitude in validLongitudes) {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    GeoCoordinates(invalidLatitude, validLongitude)
                }
            }
            for (invalidLongitude in invalidLongitudes) {
                Assertions.assertThrows(IllegalArgumentException::class.java) {
                    GeoCoordinates(invalidLatitude, invalidLongitude)
                }
            }
        }
    }
}
