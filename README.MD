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
        | created_at  |           | description |
        +-------------+           | price       |
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

Example: 

products
id | name
---------
1  | Laptop
2  | Mouse

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