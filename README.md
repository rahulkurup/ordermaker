Order-Maker
============

  What is it?
  -----------
  This is a spring boot application that is developed as part of the challenge.
  
  What does it do ?
  ----------------
  This application provides restAPI, which enables clients to deal with products & orders. An In-memory H2 database is used for storage.
  
  How to Build & Run locally
  ---------------------------
  You can run the following command to build the JAR file:  
  `mvn clean package`
        
  You can use any of the following commands to run the application:  
  `mvn spring-boot:run`
  `java -jar target/ordermaker-0.0.1-SNAPSHOT.jar`
  
  How to use it ?
  ---------------

  Rest EndPoints
  ---------------
  
  When the application is running(default is 8080):
 
  You can find all Api documentations in:<br />
  [Api Documentation](http://localhost:8080/v2/api-docs)
  
  
  You can Test all the Rest endpoints using:<br />
  [Swagger UI Link](http://localhost:8080/swagger-ui.html)
  
  You can look at the h2 database console using:
  [H2 database console](http://localhost:8080/h2/login.jsp)
  (use default username and settings)
  
  
 User API Actions
 ----------------
 
| create               	| get                                               	| getAll                                                                       	| update                                                         	|
|----------------------	|---------------------------------------------------	|------------------------------------------------------------------------------	|----------------------------------------------------------------	|
| Create a new product 	| Get a particular product based on productId       	| Get list of all the existing products                                        	| Update a product (Can only change price or name. Id is unique) 	|
|                      	| 404, if productId does not exist                  	| Empty List if none exist                                                     	| 400, if Update tried for non existent productId                	|
|                       |                                                       | Sorted by productId                                                           |                                                                   |
|                      	| should show latest details after a product update 	| List should contain products with latest changes. Product update reflected.  	|                                                                	|
 
 
 
| create             	| get                                                                                                                                                                    	| getAllBetweenTime                                                               	| recalculate                                                                                                                                                                                                                               	|
|--------------------	|------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|---------------------------------------------------------------------------------	|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| Create a new Order 	| Get a particular order based on orderId                                                                                                                                	| Get list of all the existing orders between two times                           	| Check what would be my current order price based on latest product prices                                                                                                                                                                 	|
|                    	| 404, if orderId does not exist                                                                                                                                         	| Empty List if none exist                                                        	| 400, if tried for non existent orderId                                                                                                                                                                                                    	|
|                    	| Contains all product details and total cost of order. If product 1 = 5 Euro, product 2 = 5 Euro. Order cost is 10 Euro                                                 	| start time and end time included                                                	| Just an information Api.  Just help the user know what is the current value of their old order.                                                                                                                                           	|
|                    	| should still show old details even after a product update. ie, If product 1's price and name is changed to x and 50 Euro. This api still shows the old order as it is. 	| The List should contain orders with old changes. Product update not reflected.  	| If I placed an order with many products in the past. After a few days, many products costs were updated.  I am just interested in the data.   what would be the order cost based on updated prices. Did the value go down or increased ?? 	|
|                    	|                                                                                                                                                                        	|                                                                                 	|                                                                                                                                                                                                                                           	|
  
  
  Design
  ------
  
 You can look at the h2 database console using:
 [H2 database console](http://localhost:8080/h2/login.jsp)
 (use default username and settings)
  
  We have three tables and a view
  
 * Products
  
  This table consists of all product data including old ones (as we need it for past orders). We use version number to keep track of product changes.
  
  Whenever a new product is created, the productId is generated from a sequence. The version is set to 1 and it is marked as latest using a boolean column.
  
  When a product data is updated, we insert another row into table with productId and increment version by 1.
   
   We mark the new one as latest and change latest value for old one. This needs to happen atomic (spring @Transactional)
  
   * Active products
   
   It is a view created from product database to provide a snapshot of only latest product info. We use boolean field latest= true to filer out only latest data.
   
   
   * Orders and Order_Products Junction table
   
   We have a many to many relationship between order and products. Thus we use a junction table order_product.
   We have following foreignKey constraints. Thus, order cannot be mapped to a product and version that does not exist.
   
     foreign key (orderId) references ORDERS(orderId),
     foreign key (productId, version) references PRODUCTS(productId, version)
   
   Whenever a new order is created, the orderId is generated from a sequence. We insert the order in order tables.
   Then we insert orderId, productId, version for each product in the order. version is very important as order details should not change
   when a product is updated. Thus, we fetch the latest version of product (latest when order created)
   This two inserts needs to be atomic. We use @Transactional
   
   Thus, we someone updates product version later, we ar not affected because we clearly now which version of product was part of the order.

   When we need to recalculate the order cost, we fetch all products for an order, look at there latest price (not the version we are associated with) and sum it all.


  Design Constraints & Improvements suggested
  -------------------------------------------
  
  * Kept the complexity of API low. For example, The price of product for example is a float. In real world scenario. There would be a currency enum to determine what currency price is in.
    But, here assumed that all prices are in Euro.
  * Designed the recalculate order based on how I understood the requirements. It does not do any data changes. It lets user get updated order price based on new product updates, for a old order.
    Just wanted to demonstrate how it is done.
  * Did not use any logging framework as app has a simple flow now and not to pollute the code. Can add it to help with debug ands monitoring when application gets bigger.
  * Use JPA/ Hibernate instead of JDBC. We can avoid a lot of boilerPlate in persistence layer.
  * As this is a challenge project, used an In-memory Database (H2) for demonstration purpose. This means the data is not persisted after shutdown.
    We can replace H2 with another scalable and dedicated database to handle production traffic. The code  is written in a way that changes would be minimal.
  * Can make API response DTO's more lean. Order for example. It contains a list of product dto's inside rather than a list of product ids.
    Please find the comment in Order class for reason why I decided to go with this approach.
  * The storing of old versions in PRODUCTS table can make it grow bigger fast in case of constant updates. But we use a view to solve this along with a boolean column.
  
   Design Advantages
   ------------------
  * Very Extensive Integration test which covers all the user flows and corner cases.
  * Followed Modularity and good SOLID principles. Domain objects are well separated from DAO objects and request objects. 
  * Unit Test coverage.
  * Good validation of Rest Endpoints inputs and proper status code in responses.
  * Simple and easy understandable design.
  * proper transactional boundary. Row level locking to optimise performance without sacrificing consistency. 
  * Database constraints and and proper indexes for fast retrieval. (please find data.sql file in src/resources for details)
