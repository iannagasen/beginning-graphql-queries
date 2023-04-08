package com.agasen.graphql;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class BatchController {

  @QueryMapping
  Collection<Customer> customers() {
    return List.of(new Customer(1, "A"), new Customer(2, "B"));
  }

  // this implementation gets called everytime you are querying a customer
  // n + 1 problem
  // where n is the number of records and 1 is the original query
  // to solve this problem, we need to batch all the request
  // @SchemaMapping(typeName = "Customer")
  // Account account(Customer customer) {
  // System.out.println("Getting account for customer # " + customer.id());
  // return new Account(customer.id());
  // }

  // to solve the n+1 problem use @BatchMapping
  // now we are getting all the accounts, then we are using all the customers in 1
  // processing
  // Map<Customer, Account> --- For Every customer it needs an Account(1-1)
  @BatchMapping
  Map<Customer, Account> account(List<Customer> customers) {
    System.out.println("account() was called");
    return customers.stream()
        .collect(Collectors.toMap(
            customer -> customer,
            customer -> new Account(customer.id())));
  }
}
