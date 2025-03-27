## Running the application

### Using docker-compose.yaml
`docker compose up -d`  
the docker image of the application is build on startup if not already present

## Testing the application

In order to call APIs login endpoint /auth/login must be called with username and password (for testing purpose there are three users test1, ..test3 with respective password1, ..password3)
The JWT token returned need to be added as a Bearer Token on all the other APIs

ex:  
`curl --location 'http://localhost:8080/auth/login' \
--header 'Content-Type: application/json' \
--data '{
    "username": "test1",
    "password": "password1"
}'`

## API:

* ### retrieve the details of the order
  GET /api/v1/orders/{id}  
  ex: `curl --location 'http://localhost:8080/api/v1/orders/7' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAxMjE4OCwiZXhwIjoxNzQzMDE1Nzg4LCJyb2xlcyI6WyJVU0VSIl19.cHpMkTV2NvrhpJi2MPYuf-2pQievu1nCCmh2eJy9DBw'` 

* ### retrieve all the orders for the specified user id
  GET /api/v1/orders/user/{id} 
  ex: `curl --location 'http://localhost:8080/api/v1/orders/user/2' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAxNTkyNCwiZXhwIjoxNzQzMDE5NTI0LCJyb2xlcyI6WyJVU0VSIl19.XK_ttPiznSPFwXQBpCDyPzTYqG0_Tkie_rGSQKoAGfg'`

* ### create a new order for the specified user
  PUT /api/v1/orders/user/{id}  
  Body: { "name": "string", "description": "string"}
  ex: `curl --location --request PUT 'http://localhost:8080/api/v1/orders/user/2' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAxNTAxOSwiZXhwIjoxNzQzMDE4NjE5LCJyb2xlcyI6WyJVU0VSIl19.g_MccAc919mbzQlV8QUgs9RfaWXtcRActisdihqXjr4' \
--data '{
    "name": "test user2",
    "description": "descr order"
}'`

* ### delete the specified order
  DELETE /api/v1/orders/{id}
  ex: `curl --location --request DELETE 'http://localhost:8080/api/v1/orders/1' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAwOTg3OSwiZXhwIjoxNzQzMDEzNDc5LCJyb2xlcyI6WyJVU0VSIl19.gwlJxkagpLtbsjwUbWjpQU5tTOEQxB_-i4VfcJ6kkbc'`

* ### add a product to the specified order
  POST /api/v1/orders/{id}/items  
  Body: { "userId": "long", "productId": "long", "quantity": "integer"}

* ### remove the specified product from the order
  DELETE /api/v1/orders/{id}/items/{id}
  ex: `curl --location --request DELETE 'http://localhost:8080/api/v1/orders/1/items/4' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAwOTg3OSwiZXhwIjoxNzQzMDEzNDc5LCJyb2xlcyI6WyJVU0VSIl19.gwlJxkagpLtbsjwUbWjpQU5tTOEQxB_-i4VfcJ6kkbc'`

* ### do a search on database
  GET /api/v1/orders/dbsearch
  ex: `curl --location 'http://localhost:8080/api/v1/orders/dbsearch?ordername=test%20user2' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAxNTc0MSwiZXhwIjoxNzQzMDE5MzQxLCJyb2xlcyI6WyJVU0VSIl19.miF_Q_4AFuadRiMUCWNrrZSSQU9VTZJDTd1cO8B2msw'`
* 
* ### do a search on meilisearch
  GET /api/v1/orders/meilisearch
  ex: `curl --location 'http://localhost:8080/api/v1/orders/meilisearch?ordername=test%20user2' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MSIsImlhdCI6MTc0MzAxNTc0MSwiZXhwIjoxNzQzMDE5MzQxLCJyb2xlcyI6WyJVU0VSIl19.miF_Q_4AFuadRiMUCWNrrZSSQU9VTZJDTd1cO8B2msw'`