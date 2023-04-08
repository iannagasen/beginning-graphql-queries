package com.agasen.graphql;

import java.util.Collection;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class GraphqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlApplication.class, args);
	}

}

record Customer(Integer id, String name) {
}

record Account(Integer id) {
}

@Controller
class GreetingsController {

	private static final List<Customer> customers = List.of(new Customer(1, "A"), new Customer(2, "B"));

	/*
	 * Semantically equal to:
	 * 
	 * @SchemaMapping(typeName = "Query", field = "hello")
	 */
	@QueryMapping
	String hello() {
		return "Hello, Ian";
	}

	@QueryMapping
	String helloWithName(@Argument String name) {
		return "Hello, " + name;
	}

	@QueryMapping
	Customer customerById(@Argument Integer id) {
		return new Customer(id, Math.random() > 0.5 ? "A" : "B");
	}

	@QueryMapping
	Collection<Customer> customers() {
		return customers;
	}

	@QueryMapping
	Flux<Customer> customersReactive() {
		return Flux.fromIterable(customers);
	}

	@SchemaMapping(typeName = "Customer")
	Account account(Customer customer) {
		return new Account(customer.id());
	}
}