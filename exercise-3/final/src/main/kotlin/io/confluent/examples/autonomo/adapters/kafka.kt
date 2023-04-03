package io.confluent.examples.autonomo.adapters

import com.google.protobuf.Message
import io.confluent.examples.autonomo.decide
import io.confluent.examples.autonomo.evolve
import io.confluent.examples.autonomo.react
import io.confluent.examples.autonomo.transfer.*
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.query.KeyQuery
import org.apache.kafka.streams.query.StateQueryRequest
import java.util.*


object AutonomoTopology {
    const val RIDES_STORE = "RIDES"
    const val VEHICLES_STORE = "VEHICLES"

    fun build(
        rideEventsTopic: String,
        rideReadModelTopic: String,
        vehicleEventsTopic: String,
        vehicleReadModelTopic: String,
        schemaRegistryUrl: String
    ): Topology {
        val builder = StreamsBuilder()

        val rideEventStream = builder.stream(
            rideEventsTopic,
            Consumed.with(Serdes.UUID(), serde<RideEvent>(schemaRegistryUrl))
        )

        // Rides Stream
        rideEventStream
            .groupByKey()
            .aggregate(
                fun(): RideReadModel = rideReadModel { initial = InitialRideState.getDefaultInstance() },
                fun(_: UUID, event: RideEvent, state: RideReadModel): RideReadModel =
                    evolve(state, event),
                Materialized.`as`(RIDES_STORE)
            )
            .toStream()
            .to(rideReadModelTopic, Produced.with(Serdes.UUID(), serde<RideReadModel>(schemaRegistryUrl)))

        val vehicleEventSerde = serde<VehicleEvent>(schemaRegistryUrl)

        // Vehicles Stream
        val vehicleTable = builder.stream(
            vehicleEventsTopic,
            Consumed.with(Serdes.String(), vehicleEventSerde)
        )
            .groupByKey()
            .aggregate(
                fun(): VehicleReadModel = vehicleReadModel { initial = InitialVehicleState.getDefaultInstance() },
                fun(_: String, event: VehicleEvent, state: VehicleReadModel): VehicleReadModel =
                    evolve(state, event),
                Materialized.`as`(VEHICLES_STORE)
            )

        vehicleTable
            .toStream()
            .to(vehicleReadModelTopic, Produced.with(Serdes.String(), serde<VehicleReadModel>(schemaRegistryUrl)))

        // Rides to Vehicles Saga
        rideEventStream
            .flatMap { _, rideEvent ->
                react(rideEvent).map { command ->
                    KeyValue(command.vin, command)
                }
            }
            .join(vehicleTable) { command, vehicle ->
                decide(command, vehicle)
                    .getOrElse { listOf(
                        vehicleEvent {
                            vehicleError = vehicleError {
                                message = "Command $command failed to apply to vehicle during saga"
                            }
                        }
                    ) }
            }
            .flatMapValues { events -> events }
            .to(vehicleEventsTopic, Produced.with(Serdes.String(), vehicleEventSerde))

        return builder.build()
    }
}

class QueryService(private val streams: KafkaStreams) {
    fun getRideById(rideId: UUID): RideReadModel? {
        val result = streams.query<RideReadModel>(StateQueryRequest
            .inStore(AutonomoTopology.RIDES_STORE)
            .withQuery(KeyQuery.withKey(rideId))
        )
        return if (result.globalResult.isSuccess) {
            result.globalResult.result
        } else {
            null
        }
    }

    fun getVehicleByVin(vin: String): VehicleReadModel? {
        val result = streams.query<VehicleReadModel>(StateQueryRequest
            .inStore(AutonomoTopology.VEHICLES_STORE)
            .withQuery(KeyQuery.withKey(vin))
        )
        return if (result.globalResult.isSuccess) {
            result.globalResult.result
        } else {
            null
        }
    }

    fun getMyVehicles(): List<VehicleReadModel> {
        TODO("Not yet implemented")
    }

    fun getAvailableVehicles(): List<VehicleReadModel> {
        TODO("Not yet implemented")
    }
}

fun <T: Message> serde(schemaRegistryUrl: String): KafkaProtobufSerde<T> {
    val serde = KafkaProtobufSerde<T>()
    val serdeConfig: MutableMap<String, String> = HashMap()
    serdeConfig[SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
    serde.configure(serdeConfig, false)
    return serde
}