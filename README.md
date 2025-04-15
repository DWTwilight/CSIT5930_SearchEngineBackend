# CSIT5930 Search Engine Backend

---

## API Doc

### Endpoint

**GET** `{domain}/api/v1/s`

### Request

#### Query Parameters

| Field: Type | Required | Description            |
|-------------|----------|------------------------|
| q: string   | Yes      | Search Query(Original) |

### Response

#### Status Codes

| Status | Description           |
|--------|-----------------------|
| 200    | OK                    |
| 400    | Bad Request           |
| 500    | Internal Server Error |

#### Response Body Structure

```yml
response:
  data:
    - SearchResult 1
    - SearchResult 2
  meta:
    SearchResultMeta
```

##### SearchResult Object

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

##### SearchResultMeta Object

| Field: Type    | Description             |
|----------------|-------------------------|
| count: integer | Total matched documents |

### Examples

#### Request Example

```shell
curl --location -g --request GET '{domain}/api/v1/s?q=spring+boot'
```

#### Success Response (HTTP 200)

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

---

## Start the Server

---

### 1. Prerequisite

- docker
- docker compose plugin (optional)

---

### 2. Start Database

```sh 
docker run -d \
  --name se_pgsql \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=test123 \
  -e POSTGRES_DB=se_db \
  -v postgres_data:/var/lib/postgresql/data \
  postgres:17.4-alpine
```

Or use `docker-compose`:

```sh 
sudo docker compose create
sudo docker compose start
```

A postgres db container will be running on port `5432`, its data will be persisted at `/var/lib/postgresql/data`

- username: `postgres`
- password: `test123`
- database name: `se_db`

---

### 3. Start Backend Server

#### **Option A**: Start Server with Docker

```sh 
sudo docker run -it -p 8080:8080 --network="host" --name se-backend dwtwilight/csit5930-search-engine-backend:latest
```

A server container will be running on port 8080, and database tables will be created automatically.

#### **Option B**: Build Native Executable and Run

- Prerequisite
    - Graalvm JDK 21 (https://www.graalvm.org/downloads/#)

```sh 
./gradlew clean nativeCompile # compilation will take up to 10 minutes
./gradlew nativeRun
```

Or build docker image:

```sh 
./gradlew clean nativeCompile # compilation will take up to 10 minutes
sudo docker build -t dwtwilight/csit5930-search-engine-backend:latest .
```

#### **Option C**: Build Jar and Run

- Prerequisite
    - JDK 21

```sh 
./gradlew clean bootRun
```

---

### 4. Migrate Data to DB

```sh 
# enter crawler repo (https://github.com/Yubelo3/SearchEngineDataPreperation.git)
cd ../SearchEngineDataPreperation

# do data crawling first!

# start data migration
# pip install psycopg2-binary
python migrate_db.py
```

---

### 5. Call Search Api