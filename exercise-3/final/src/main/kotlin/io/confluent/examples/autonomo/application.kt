package io.confluent.examples.autonomo

import io.confluent.examples.autonomo.adapters.AutonomoTopology
import io.confluent.examples.autonomo.adapters.QueryService
import io.confluent.examples.autonomo.adapters.serde
import io.confluent.examples.autonomo.transfer.RideEvent
import io.confluent.examples.autonomo.transfer.RideId
import io.confluent.examples.autonomo.transfer.VehicleEvent
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import io.micronaut.runtime.Micronaut.*
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
	run(*args)
}

@Factory
class Beans {
	@Singleton
	fun queryService(
		topologyRunner: TopologyRunner
	): QueryService =
		QueryService(topologyRunner.streams)

	@Singleton
	@Bean(preDestroy = "close")
	fun rideEventProducer(
		@Value("\${kafka.bootstrap.servers}")
		kafkaBootstrapServers: String,
		@Value("\${kafka.schema.registry.url}")
		schemaRegistryUrl: String,
	): KafkaProducer<RideId, RideEvent> {
		val producerConfig = Properties()
		producerConfig[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
		return KafkaProducer(
			producerConfig,
			StringSerializer(),
			serde<RideEvent>(schemaRegistryUrl).serializer()
		)
	}

	@Singleton
	@Bean(preDestroy = "close")
	fun vehicleEventProducer(
		@Value("\${kafka.bootstrap.servers}")
		kafkaBootstrapServers: String,
		@Value("\${kafka.schema.registry.url}")
		schemaRegistryUrl: String,
	): KafkaProducer<String, VehicleEvent> {
		val producerConfig = Properties()
		producerConfig[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
		return KafkaProducer(
			producerConfig,
			StringSerializer(),
			serde<VehicleEvent>(schemaRegistryUrl).serializer()
		)
	}
}

@Singleton
class TopologyRunner(
	@Value("\${kafka.bootstrap.servers}")
	kafkaBootstrapServers: String,
	@Value("\${kafka.schema.registry.url}")
	schemaRegistryUrl: String,
	@Value("\${kafka.streams.application.id}")
	applicationId: String,
	@Value("\${kafka.topics.ride-events}")
	rideEventsTopic: String,
	@Value("\${kafka.topics.rides}")
	rideReadModelTopic: String,
	@Value("\${kafka.topics.vehicle-events}")
	vehicleEventsTopic: String,
	@Value("\${kafka.topics.vehicles}")
	vehicleReadModelTopic: String,
) {
	val streams: KafkaStreams

	init {
		val config = Properties()
		config[StreamsConfig.APPLICATION_ID_CONFIG] = applicationId
		config[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootstrapServers
		config[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE_V2
		config[StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG] = StreamsConfig.OPTIMIZE
		config[StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG)] = "all"

		val topology = AutonomoTopology.build(
			rideEventsTopic,
			rideReadModelTopic,
			vehicleEventsTopic,
			vehicleReadModelTopic,
			schemaRegistryUrl
		)

		this.streams = KafkaStreams(topology, config)
	}

	@PostConstruct
	fun start() {
		streams.start()
	}

	@PreDestroy
	fun stop() {
		streams.close(Duration.ofMillis(10000))
	}
}
