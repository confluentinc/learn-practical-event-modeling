package io.confluent.examples.autonomo.adapters

import com.fraktalio.fmodel.application.StateRepository

class VehicleRepository<C, S>: StateRepository<C, S> {
    override suspend fun C.fetchState(): S? {
        TODO("Not yet implemented")
    }

    override suspend fun S.save(): S {
        TODO("Not yet implemented")
    }
}