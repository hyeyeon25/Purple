# =========================================================================
#  1단계: 빌드(Build) 환경 (Gradle)
#  - 소스 코드를 컴파일하고 실행 가능한 .jar 파일로 만드는 단계입니다.
# =========================================================================

# Java 17(Temurin JDK) 이미지를 'builder'라는 별명으로 사용합니다. (대문자 AS로 문법 경고 수정)
FROM eclipse-temurin:17-jdk-jammy AS builder

# 컨테이너 내에서 작업을 수행할 디렉토리를 만듭니다.
WORKDIR /workspace/app

# 먼저 Gradle 관련 파일들을 모두 복사합니다.
# settings.gradle은 프로젝트 구조를 정의하는 필수 파일일 가능성이 높습니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
# Gradle Wrapper에 실행 권한을 부여합니다.
RUN chmod +x ./gradlew

# 이제 나머지 모든 소스 코드를 컨테이너 안으로 복사합니다.
COPY src src

# Gradle을 사용해 프로젝트를 빌드합니다. (.jar 파일 생성)
# --no-daemon 옵션을 추가하여 컨테이너 환경에서 빌드 안정성을 높입니다.
RUN ./gradlew build --no-daemon -x test


# =========================================================================
#  2단계: 실행(Runtime) 환경
#  - 1단계에서 만들어진 .jar 파일을 실행시키는 역할만 하는 가벼운 환경입니다.
# =========================================================================

# 훨씬 가벼운 Java 17 실행 환경(JRE) 이미지를 베이스로 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 컨테이너 내에서 임시 파일을 저장할 공간을 지정합니다. Spring Boot가 사용합니다.
VOLUME /tmp

# 1단계('builder')에서 빌드된 결과물(.jar 파일)을 현재 컨테이너로 복사해옵니다.
# Gradle의 빌드 결과물은 보통 build/libs/ 경로에 생성됩니다.
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# 8080 포트를 외부로 개방한다고 명시합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 이 명령어를 최종적으로 실행합니다.
# java -jar /app.jar 명령으로 Spring Boot 애플리케이션을 실행시킵니다.
ENTRYPOINT ["java","-jar","/app.jar"]

