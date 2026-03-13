Warehouse Management

Database setup

- create mysql database with this same name : `warehouse_management`
- application is configured with this db name in `application.properties`
- redis should also be running before starting server
- rabbitmq should also be running before starting server

example:

```sql
CREATE DATABASE warehouse_management;
```

Current local config

- mysql    : `localhost:3306`
- database : `warehouse_management`
- redis    : `localhost:6379`
- rabbitmq : `localhost:5672`
- rabbitmq user : `guest`
- rabbitmq password : `guest`


Run from terminal

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```


Schema DB

             +-----------+
             |   roles   |
             +-----------+
             | id        |
             | name      |
             +-----------+
                   |
                   | 1
                   |
                   | n
             +-----------+
             |   users   |
             +-----------+
             | id        |
             | name      |
             | email     |
             | password  |
             | role_id   |
             +-----------+


        +-------------+           +-------------+
        | warehouses  |           |  products   |
        +-------------+           +-------------+
        | id          |           | id          |
        | name        |           | sku         |
        | location    |           | name        |
        +-------------+           | description |
                                  | price       |
                                  | reorder_lvl |
                                  +-------------+

                     \               /
                      \             /
                       \           /
                        \         /
                         \       /
                       +-------------+
                       |  inventory  |
                       +-------------+
                       | id          |
                       | product_id  |
                       | warehouse_id|
                       | quantity    |
                       +-------------+


Roles seeded by app

- SUPER_ADMIN
- ADMIN
- MANAGER
- STAFF


Default login

email    : superadmin@warehouse.com
password : super123


Security flow

- login api is public
- register api now needs authenticated SUPER_ADMIN
- product apis need login
- warehouse apis need login
- inventory apis need login
- order apis need login


Role rules used in java validators

- SUPER_ADMIN can create users
- ADMIN and SUPER_ADMIN can create/update product
- ADMIN and SUPER_ADMIN can create/update warehouse
- MANAGER, ADMIN, SUPER_ADMIN can update inventory
- STAFF, MANAGER, ADMIN, SUPER_ADMIN can create orders


User creation

api : POST /auth/register

request:

{
  "name": "Warehouse Staff",
  "email": "staff@warehouse.com",
  "password": "staff123",
  "role": "STAFF"
}

Notes:

- only SUPER_ADMIN can call this api
- ADMIN and SUPER_ADMIN role creation is restricted in register flow
- allowed normal creation roles are STAFF and MANAGER


Product list api response

api : GET /products

response:

{
  "content": [
    {
      "id": 2,
      "sku": "PRD-Book-1",
      "name": "Java",
      "description": "book for java",
      "price": 100.0,
      "reorderLevel": 150
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}

Note:

- client reads product list from content


Frontend

- login page : /
- dashboard  : /dashboard.html#/overview
- product list : #/products/list
- product create : #/products/create
- warehouse list : #/warehouses/list
- warehouse create : #/warehouses/create
- inventory list : #/inventory/list
- inventory create : #/inventory/create
- user create : #/users/create


UI behavior

- forbidden and unauthorized messages are shown in the view layer
- create form errors are shown near the create button
- top flash message is also shown for success and error


Example as in project:

products
id | sku         | name
-----------------------
1  | Product 1   | Laptop
2  | PRD-Book-1  | Java

warehouses
id | name
---------
1  | Chennai
2  | Bangalore

inventory
product_id | warehouse_id | quantity
-------------------------------------
1          | 1            | 50
1          | 2            | 30
2          | 1            | 100
