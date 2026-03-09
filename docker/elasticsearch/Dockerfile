FROM docker.elastic.co/elasticsearch/elasticsearch:8.12.2

# 1. nori 분석기 설치
RUN elasticsearch-plugin install --batch analysis-nori

# 2. 사전 파일들이 위치한 config/analysis 폴더를 컨테이너 내부로 복사
COPY ./config/analysis/ /usr/share/elasticsearch/config/analysis/

# 3. 파일 권한 설정 (Elasticsearch 실행 계정이 읽기)
USER root
RUN chown -R elasticsearch:root /usr/share/elasticsearch/config/analysis
USER elasticsearch