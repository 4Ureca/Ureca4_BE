# 로컬 개발 환경 실행 가이드

## 1. 사전 준비

필수 설치:

- Docker Desktop (Mac/Windows)
- 또는 Linux + Docker Engine + docker compose

설치 확인:

```bash
docker --version
docker compose version
```

---

## 2. 인프라 컨테이너 실행

프로젝트 루트 기준:

```bash
cd docker
docker compose up -d
```

실행 확인:

```bash
docker ps
```

정상적으로 떠 있어야 하는 컨테이너:

- crm-mysql
- crm-mongo
- crm-redis
- crm-es
- crm-kibana
- crm-mongo-ui

---

## 3. 접속 정보

### MySQL

- Host: localhost
- Port: 13306
- Database: crm
- Username: crm
- Password: crm

접속 확인:

```bash
mysql -h 127.0.0.1 -P 13306 -u crm -p
```

---

### MongoDB

- Host: localhost
- Port: 27018

접속 확인:

```bash
mongosh mongodb://localhost:27018
```

Mongo UI:

```
http://localhost:18081
```

---

### Redis

- Host: localhost
- Port: 6380

확인:

```bash
redis-cli -p 6380 ping
```

정상 시:

```
PONG
```

---

### Elasticsearch

```
http://localhost:9201
```

브라우저 또는:

```bash
curl http://localhost:9201
```

정상 시 JSON 응답 출력.

---

### Kibana

```
http://localhost:15601
```
