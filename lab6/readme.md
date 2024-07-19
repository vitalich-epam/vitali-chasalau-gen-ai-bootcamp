# GenAI Bootcamp - Lab 5: Embedding

## Prerequisites
* Requires JDK17
* Requires Docker

## Application Setup

### 1. Run PGVector
`docker-compose up`
* test with opening pgadmin `http://localhost:5050. Login is `pgadmin4@pgadmin.org` and password is `admin`.

### 2. Connect to LLM
Set your own Azure OpenAI key and URL to the system variables
  * `AZURE_OPENAI_API_KEY` = _your key_
  * `AZURE_OPENAI_ENDPOINT` = _your url_
  * `AZURE_OPENAI_DEPLOYMENT_NAME` = _your used model_

### 3. Launch the application
* `./mvnw spring-boot:run`
* test with 
`curl --location 'http://localhost:8080/ai/upload-embedding' \
  --form 'file=@"/_path to file_>"'`
`curl --location 'http://localhost:8080/ai/embedding/search?message=Your%20question&similarity=0.77&limit=10'`
`curl --location 'http://localhost:8080/ai/prompt?message=Your%20question'`


```