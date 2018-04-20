# Lock Service
![travis](https://travis-ci.org/jyane/lock-service.svg?branch=master)

Lock Service provides a lock service via gRPC.

## Proto

```proto
syntax = "proto3";

package jyane.lock;

import "google/protobuf/duration.proto";

message TryAcquireRequest {
  // owner is the owner, length of owner must be less than 256.
  string owner = 1;

  // key is the key, length of key must be less than 256.
  string key = 2;

  // time-to-live in seconds, nanos will be discarded.
  // Muximum duration is 24 hours.
  google.protobuf.Duration duration = 3;
}

message TryAcquireResponse {
  // time-to-live in seconds
  google.protobuf.Duration duration = 1;
}

message ReleaseRequest {
  string owner = 1;
  string key = 2;
}

message ReleaseResponse {
}

service LockService {
  rpc TryAcquire(TryAcquireRequest) returns (TryAcquireResponse) {}
  rpc Release(ReleaseRequest) returns (ReleaseResponse) {}
}
```

## Usage

### Edit the config

```
etcd {
  address = localhost
}
```


### Run etcd by docker

``` sh
sh etcd.sh
```

### Start application

``` sh
sbt app/run
```

### Call service via grpc_cli
grpc_cli is [gRPC Comman Line Tool](https://github.com/grpc/grpc/blob/v1.7.2/doc/command_line_tool.md)

``` sh
# acquire
grpc_cli call localhost:10080 -l jyane.lock.LockService.TryAcquire 'owner: "jyane" key: "test" duration { seconds: 10 }'

# release
grpc_cli call localhost:10080 -l jyane.lock.LockService.Release 'owner: "jyane" key: "test"'
```
