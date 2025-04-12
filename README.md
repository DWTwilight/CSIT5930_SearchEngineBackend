# CSIT5930 Search Engine Backend

## API Doc

### Search

#### Endpoint

**GET** `{domain}/api/v1/s`

#### Request

##### Query Parameters

| Field: Type | Required | Description            |
|-------------|----------|------------------------|
| q: string   | Yes      | Search Query(Original) |

#### Response

##### Status Codes

| Status | Description           |
|--------|-----------------------|
| 200    | OK                    |
| 400    | Bad Request           |
| 500    | Internal Server Error |

##### Response Body Structure

```yml
response:
  data:
    - SearchResult 1
    - SearchResult 2
  meta:
    SearchResultMeta
```

###### SearchResult Object

| Field: Type                 | Description                    |
|-----------------------------|--------------------------------|
| id: long                    | Unique document identifier     |
| score: double               | Relevance score (0-1)          |
| title: string               | Document title                 |
| url: string                 | Full document URL              |
| lastModified: string        | Last modified time             |
| size: long                  | Document size in bytes         |
| freqWords: Map<string,long> | Top frequent words with counts |
| parentLinks: Array[string]  | Parent document links          |
| childLinks: Array[string]   | Child document links           |

###### SearchResultMeta Object

| Field: Type    | Description             |
|----------------|-------------------------|
| count: integer | Total matched documents |

#### Examples

##### Request Example

```shell
curl --location -g --request GET '{domain}/api/v1/s?q=spring+boot'
```

##### Success Response (HTTP 200)

```json
{
  "data": [
    {
      "id": 12345,
      "score": 0.956,
      "title": "Spring Boot Best Practices Guide",
      "url": "https://example.com/docs/springboot",
      "lastModified": "Tue, 16 May 2023 05:03:16 GMT",
      "size": 24576,
      "freqWords": {
        "dependency": 12,
        "autoconfigure": 8
      },
      "parentLinks": [
        "https://example.com/java",
        "https://example.com/backend"
      ],
      "childLinks": [
        "https://example.com/springboot-security",
        "https://example.com/springboot-data"
      ]
    }
  ],
  "meta": {
    "count": 1
  }
}
```

### Error Response (HTTP 400)

```json
{
  "code": "INVALID_PARAMETER",
  "message": "Missing required query parameter 'q'"
}
```

## Start the Server

### Prerequisite

- docker
- docker compose plugin

### Start Database

```sh 
sudo docker compose create
sudo docker compose start
```

A postgres db container will be running on port `5432`, its data will be persisted at `/var/lib/postgresql/data`

- username: `postgres`
- password: `test123`
- database name: `se_db`

### Migrate Data to DB

TODO

### Start Server with Docker

TODO

### Build and Start Server from Source with Gradle Wrapper

#### Prerequisite

- JDK 21

#### Command

```sh 
./gradlew clean bootRun --args='--spring.profiles.active=local'
```