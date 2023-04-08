# beginning-graphql-queries

### 1. Create the schema for this mini project

### 2. Under graphql, create `queries.graphqls`

```graphql
type Query {
  customerById(id: ID): Customer
  customers: [Customer]
  customersReactive: [Customer]
  helloWithName(name: String): String
  hello: String
}

type Customer {
  id: ID
  name: String
  account: Account
}

type Account {
  id: ID
}
```

---

### Create the model using Java Records

#### `Customer.java`

```java
// Notice that we dont create a field for account which is in declared in schema
// WHY??
record Customer(Integer id, String name) {
}

```

#### `Account.java`

```java
record Account(Integer id) {
}
```

---

### 3. Create the controller that will map the **fields and operations** of the schema that we created

#### `GreetingsController.java`

```java
@Controller
class GreetingsController {

	private static final List<Customer> customers = List.of(new Customer(1, "A"), new Customer(2, "B"));

  @QueryMapping
  String hello() {
    return "Hello, you!"
  }

  @QueryMapping
  String helloWithName(@Argument String name) {
    return "Hello " + name + "!";
  }

  @QueryMapping
  Customer customerById(@Argument Integer id) {
    return new Customer(id, Math.random() > 0.5 ? "A" : "B");
  }

  @QueryMapping
  Collection<Customer> customers() {
    return customers;
  }

  @SchemaMapping(typeName = "Customer")
  Account account(Customer customer) {
    return new Account(customer.id());
  }
}
```

Lets examine each Mapping

1. **`hello()`**

```java
@QueryMapping
String hello() {
  return "Hello, you!"
}
```

The `@QueryMapping` is semantically equivalent to:

```java
@SchemaMapping(typeName = "Query", field = "hello")
```

- We use `@QueryMapping` because the typeName is our root type which is `Query`
- By omitting the `field = "hello"` property, we are implicitly telling the Spring to use the function name as the field name. in our case: `hello()`

To query:

```graphql
query {
  hello
}
```

---

2. **`helloWithName()`**

```java
@QueryMapping
String helloWithName(@Argument String name) {
	return "Hello, " + name;
}
```

This will map the operation:

```graphql
type Query {
  ...
  helloWithName(id: ID)
  ...
}
```

Notice in our method that we annotate it by `@Argument` to denote that we need a parameter to query the operation.

To query:

```graphql
query {
  helloWithName(id: 1)
}
```

---

3. **`customers()`**

```java
@QueryMapping
Collection<Customer> customers() {
  return customers;
}
```

This will map the operation:

```graphql
type Query {
  ...
  customer: [Customer]
  ...
}
```

To query, since Customer is an Entity itself, we need to supply what fields do we need

```graphql
query {
  customers {
    id
    name
  }
}
```

or

```graphql
query {
  customers {
    id
  }
}
```

---

4. **`account()`**

```java
@SchemaMapping(typeName = "Customer")
Account account(Customer customer) {
  return new Account(customer.id());
}
```

Now we use the `@SchemaMapping` annotation since the root(or source) of this operation will be from `Customer` a user defined Entity
And there is no actually @CustomerMapping right?

```java
// Need to define using @SchemaMapping(instead of @QueryMappign) since we are querying user-defined type Customer
@SchemaMapping(typeName = "Customer")
...
```

Note also that you did not see `@Argument` annotation from the `Customer` parameter.
This is because when we query this, we will not supply an argument on the account field.

Q: Then, where will it get the Customer object?

A: It will get the customer object from its source which is type Customer. Now think of this as similar to @Embeddable in Hibernate, where we try to embed another columns to the source column or table. This is like Joining 2 tables together

```java
...
@SchemaMapping(typeName = "Customer")
// No @Argument annotation, why?
Account account(Customer customer) {
...
}
```

To Query:

```graphql
query {
  customers {
    name
    id
    account {
      id
    }
  }
}
```

Sample Output:

```json
{
  "data": {
    "customers": [
      {
        "name": "A",
        "id": "1",
        "account": {
          "id": "1"
        }
      },
      {
        "name": "B",
        "id": "2",
        "account": {
          "id": "2"
        }
      }
    ]
  }
}
```

---

---

---

## **The N+1 Problem**

```java
@SchemaMapping(typeName = "Customer")
Account account(Customer customer) {
  System.out.println("Getting account for customer # " + customer.id());
  return new Account(customer.id());
}
```

The problem with this code is when you query customers. It will execute the method 1 by 1 for every customer.

Query:

```graphql
query {
  customers {
    id
    name
    account {
      id
    }
  }
}
```

Logs:

```cmd
Getting account for customer # 1
Getting account for customer # 2
```

Suppose to be a single query. It technically becomes 3, the original (1) query plus 2 (n), the size of the customers. This is known as the n+1 customers. What if there is a network or database call dependency in the code? then it will be cause of speed issue

### **Solution:**

### Use the **`@BatchMapping`** annotation

```java
// with these, we are mapping the customers by 1 batch and process it.
@BatchMapping
Map<Customer, Account> account(List<Customer> customers) {
  System.out.println("account() was called");

  return customers.stream()
    .collect(Collectors.toMap(
      customer -> customer,
      customer -> new Account(customer.id())
    ));
}
```

**Note** that this will follow a following method signature to make it work

```java
Map<ParentType, Type> fieldName(List<ParentType> x)
```
