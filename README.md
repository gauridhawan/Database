# Distributed Replicated Concurrency Control and Recovery

This is the term project for Advanced Database Management Systems course offered by Prof. Dennis Shasha during Fall 2020

# How to Run the reprozip file:
## MacOS
```reprounzip vagrant setup adb_gd_kk.rpz ~/testAdbFinal  ```<br>
``` reprounzip vagrant run ~/testAdbFinal``` 

## Linux
```reprounzip directory setup adb_gd_kk.rpz ~/testAdbFinal  ```<br>
```reprounzip directory run ~/testAdbFinal```<br>

## Input and Output Arguments

### Providing input file 
- To provide input via file, you can change the argument in the .rpz by using the following command <br>
```reprounzip vagrant upload ~/testAdbFinal <input-file-path>:arg3```
  
### Downloading the output 
The output is always printed in the stdout. In case you want to download it in a file, follow the commands given below. 
- To get output in a file(file will be arg4 in the current direcotry), use the following command <br>
```reprounzip vagrant download ~/finalAdbMac arg4 ```

- To print the output  <br>
```reprounzip vagrant download ~/finalAdbMac arg4:```



## Output Structure:

- Each read instruction prints the variable along with its value at the time it gets executed.
- Each transaction end call prints whether the transaction committed or aborted.
- In case of a deadlock, we print the transaction which gets aborted.
- dump prints the output for each site in the format mentioned in the project document (every site and all variables on that site sorted in ascending order)




