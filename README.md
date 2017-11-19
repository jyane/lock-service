# Lock Service
![travis](https://travis-ci.org/jyane/lock-service.svg?branch=master)

Lock Service provides lock service via gRPC.

## Usage

### Run etcd by docker

``` sh
sh etcd.sh
```

### Start application

``` sh
sbt run
```

### Call service via grpc_cli
grpc_cli is a [gRPC Comman Line Tool](https://github.com/grpc/grpc/blob/v1.7.2/doc/command_line_tool.md)

``` sh
# acquire
grpc_cli call localhost:10080 -l jyane.lock.LockService.TryAcquire 'owner: "jyane" key: "test" duration { seconds: 10 }'

# release
grpc_cli call localhost:10080 -l jyane.lock.LockService.Release 'owner: "jyane" key: "test"'
```
