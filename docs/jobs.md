# How to use jobs ?

## Example

### Launch on Flink

```bash
sbt "runMain example.PipelineExample \
--runner=FlinkRunner \
--flinkMaster=http://localhost:8081 \
--fasterCopy=true \
--parallelism=2 \
--inputPath=toto \
--outputTable=fakeOutput"
```

## OneDrive Image Processor

### Direct runner

```bash
sbt "runMain com.onedrive.image.processor.OrganizerPipeline \
--clientId=toto \
--clientSecret=tata \
--tenantId=titi \
--accessToken=tutu \
--folderId=oui"
```

### Launch on Flink

```bash
sbt "runMain com.onedrive.image.processor.OrganizerPipeline \
--runner=FlinkRunner \
--flinkMaster=http://localhost:8081 \
--fasterCopy=true \
--parallelism=2 \
--clientId=toto \
--clientSecret=tata \
--tenantId=titi \
--accessToken=tutu \
--folderId=oui"
```

## Dropbox Image Processor

### Direct runner

```bash
sbt "runMain com.onedrive.image.processor.OrganizerPipeline \
--client=dropbox
--accessToken=sl.BqT7oNZ9tWjniQAQfynZqCNRXbHHBcOsGTCIUssSRuUoq_s8rEdJXBhtVBUU9ejDyTdr5_9veHEv169V62C1JcPI9afLi1D28YSX2dI2wIcPluBLa1QKZKVCOEG0n5A-reGOVpjHfFv_ \
--inputFolder=oui \
--outputFolder=oui"
```

### Launch on Flink

```bash
sbt "runMain com.onedrive.image.processor.OrganizerPipeline \
--runner=FlinkRunner \
--flinkMaster=http://localhost:8081 \
--fasterCopy=true \
--parallelism=2 \
--clientId=toto \
--clientSecret=tata \
--tenantId=titi \
--accessToken=tutu \
--folderId=oui"
```