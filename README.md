# backend-example

Direct:

curl -X POST \
  http://localhost:8080/api/v1/requisitions/direct \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 7e7e89e5-14fd-e27f-58dd-8939f950ce5c' \
  -d '{
    "clientId": "811fc2a1-66c1-4c81-917e-3290c0873246",
    "ticketId": "98372d3e-68d0-451d-bbeb-82c3a52f323e",
    "routeNumber": "101-A",
    "departure": "2020-01-09T11:30:00"
}'


Fanout:

curl -X POST \
  http://localhost:8080/api/v1/requisitions/fanout \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 10afcc95-fb51-483a-a4bb-30c26b327754' \
  -d '{
    "clientId": "7919428b-5961-4450-96a3-8c3392fe7f35",
    "ticketId": "b824e0cb-b292-4876-af95-8d05fffdf5cc",
    "routeNumber": "17-B",
    "departure": "2020-01-09T11:30:00"
}'


Dead Letter:

curl -X POST \
  http://localhost:8080/api/v1/requisitions/dead-letter \
  -H 'cache-control: no-cache' \
  -H 'content-type: application/json' \
  -H 'postman-token: 3b6241d1-d37a-4956-a40a-4e7ffe2bc701' \
  -d '{
    "clientId": "8bab7dc4-6114-4e65-950d-80097f411aa1",
    "ticketId": "00ca0aa8-2a05-4efe-b037-a9ebea78f402",
    "routeNumber": null,
    "departure": "2020-01-09T11:30:00"
}'

