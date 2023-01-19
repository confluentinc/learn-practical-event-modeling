package io.confluent.examples.autonomo.domain

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.toMessage

// Element Types

const val NULL_UUID_STRING = "00000000-0000-0000-0000-000000000000"

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

sealed interface State

sealed interface ReadModel: State {
    val name: ReadModelName
        get() = "autonomo.read-model.${this.javaClass.simpleName}"
}

// Validation
internal inline fun <reified T> valikate(
    validationFn: () -> T
): Validated<List<String>, T> = try {
    validationFn().valid()
} catch (ex: ConstraintViolationException){
    ex.constraintViolations
        .map {
            val message = it.toMessage()
            "\"${message.value}\" of ${T::class.simpleName}.${message.property}: ${message.message}"
        }.invalid()
}
