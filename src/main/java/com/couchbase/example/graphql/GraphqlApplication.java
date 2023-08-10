package com.couchbase.example.graphql;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.json.JsonObject;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@RestController
public class GraphqlApplication {

	@Value("${hostname}")
	private String hostname;

	@Value("${bucket}")
	private String bucket;

	@Value("${username}")
	private String username;

	@Value("${password}")
	private String password;

	public @Bean
	Cluster cluster() {
		String connectionString = "couchbase://" + this.hostname;
        return Cluster.connect(
				connectionString,
				ClusterOptions.clusterOptions(this.username, this.password).environment(env -> {
					// Sets a pre-configured profile called "wan-development" to help avoid
					// latency issues when accessing Capella from a different Wide Area Network
					// or Availability Zone (e.g. your laptop).
					//env.applyProfile("wan-development");
				}));
	}

	public @Bean
	Bucket bucket() {
		Bucket theBucket = cluster().bucket(this.bucket);
		theBucket.waitUntilReady(Duration.ofSeconds(30));
		return theBucket;
	}

	private GraphQL build;

	@Lazy
	public static void main(String[] args) { SpringApplication.run(GraphqlApplication.class, args);
	}

	@RequestMapping(value="/", method= RequestMethod.GET)
	public Object rootEndpoint() {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "Hello World");
		return response;
	}

	@RequestMapping(value="/graphql", method= RequestMethod.POST)
	public Object graphql(@RequestBody String request) {
		JsonObject jsonRequest = JsonObject.fromJson(request);
		ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(jsonRequest.getString("query")).build();
		ExecutionResult executionResult = this.build.execute(executionInput);
		return executionResult.toSpecification();
	}

	@PostConstruct()
	public void init() {
		ClassLoader classLoader = getClass().getClassLoader();
		File schemaFile = new File(Objects.requireNonNull(classLoader.getResource("schema.graphql")).getFile());
		SchemaParser schemaParser = new SchemaParser();
		TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaFile);
		RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
				.type("Query",typeWiring -> typeWiring
						.dataFetcher("pokemons",Database.getPokemonData(cluster()))
				)
				.type("Query",typeWiring -> typeWiring
						.dataFetcher("games",Database.getGamesData(cluster()))
				)
				.type("Query",typeWiring -> typeWiring
						.dataFetcher("game",Database.getGameData(bucket()))
				)
				.type("Pokemon",typeWiring -> typeWiring
						.dataFetcher("game",Database.getGameData(bucket()))
				).build();

		SchemaGenerator schemaGenerator = new SchemaGenerator();
		GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
		this.build = GraphQL.newGraphQL(graphQLSchema).build();
	}
}
