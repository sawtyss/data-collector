# User response data collector

Implementation of thread-safe, memory and CPU efficient component for processing of user agent http response codes.

## Testing notes
I have been using the
```
com.gg.UserResponseStoreLoadTest
```
to perform benchmarks on two implementations of the underlying storage layer for varying maximum data sizes.
From the tests it's evident that the circular-buffer approach is better, especially when facing maximum sizes bigger than 100.