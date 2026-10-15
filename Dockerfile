FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /usr/app

COPY . /usr/app/

RUN mvn package -q -DskipTests

FROM maven:3.9-eclipse-temurin-21

WORKDIR /usr/app/target

COPY --from=build /usr/app/target/petclinic-clinic-1.0.0.jar .
COPY --from=build /usr/app/target/dependency ./dependency

RUN apt-get update && apt-get install -y wget \
 && ARCH=$(dpkg --print-architecture) \
 && wget -q -O /tmp/gg.deb "https://github.com/grft-dev/graftcode-gateway/releases/latest/download/gg_linux_${ARCH}.deb" \
 && dpkg -i /tmp/gg.deb && rm /tmp/gg.deb \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

EXPOSE 80
EXPOSE 81

CMD ["gg", "--modules", "/usr/app/target/petclinic-clinic-1.0.0.jar"]
