Project written using:
Java 17,
Maven,
Junit 5,
Spring boot,
Mockito 5,
Gherkin,
PostgreSQL,
Lombok

Used this project to learn the basics of unit testing, behaviour-driven development and test-driven development.
Also used it as an opportunity to learn more about the Java ecosystem.


Project may require a local installation of maven or an IDE equipped with maven to be build.
I personally used Intellij.

For some functionalities, eq. the blacklist, you need to run an instance of PostgreSQL.

user: postgres
password: test

private_accounts:
id - bigint
name - text
surname - text
pesel - character(11)
coupon - text
balance - double precision
history - double precision[]

black_list
pesel - character(11)
reason - text
