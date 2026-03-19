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

## Swagger API
- http://localhost:8080/api/swagger-ui/index.html#/

---
# Github Convention

### 브랜치 종류

- **main**
    - 배포 대상 브랜치
- **develop**
    - 개발 브랜치
- feat/{feature-name}
    - 추가 기능 개발 브랜치, 추후 develop 브랜치로 병합
- hotfix/
    - develop 브랜치에서 발생한 버그 수정하는 브랜치
- docs/
    - 문서 작업 브랜치

### 예시    
- **배포**
    - 개발 브랜치(merge 용도 + 버전 기록)
    - Ex) main
- **develop**
    - 개발 브랜치
    - Ex) develoop
- feat/{feature-name}
    - 추가 기능 개발 브랜치, 추후 develop 브랜치로 병합
    - Ex) feat/users   
    - Ex) feat/consult   
    - Ex) feat/jira에 등록된 기능 이름 
- hotfix/
    - develop 브랜치에서 발생한 버그 수정하는 브랜치
- docs/
    - 문서 작업 브랜치

---

# Commit Convention

### Type 종류

- feat : 새로운 기능 추가, 기존의 기능을 요구 사항에 맞추어 수정 커밋
- fix : 기능에 대한 버그 수정 커밋
- build : 빌드 관련 수정 / 모듈 설치 또는 삭제에 대한 커밋
- chore : 패키지 매니저 수정, 그 외 기타 수정 ex) .gitignore
- ci : CI 관련 설정 수정
- docs : 문서(주석) 수정
- style : 코드 스타일, 포맷팅에 대한 수정
- refactor : 기능의 변화가 아닌 코드 리팩터링 ex) 변수 이름 변경
- test : 테스트 코드 추가/수정
- release : 버전 릴리즈

### 예시

- Ex) feat/추가한사람이름/패키지위치/설명   
- Ex) fix/추가한사람이름/패키지위치/설명   
- Ex) build/추가한사람이름/패키지위치/설명   
