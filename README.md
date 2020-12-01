# Distributed Replicated Concurrency Control and Recovery

This is the term project for Advanced Database Management Systems course offered by Prof. Dannis Shasha during Fall 2020

# How to Run the reprozip file:
## MacOS
reprounzip vagrant setup adb.rpz ~/testAdbFinal <br> 
reprounzip vagrant run ~/testAdbFinal

## Linux
reprounzip directory setup adbFinal.rpz ~/adbFinal <br> 
reprounzip directory run ~/adbFinal

## Input Arguments
- To provide input via file, ???
- To get output in a particular file, please provide the 
- To provide input via stdin, don't pass in any argument as inputfile. Note that in this case, you won't be able to store output to a particular file.

### Example
- 


# How to Check the output:

- The output always gets printed to stdout
- If the output file path isn't given as a command line argument, the output file gets automatically stored in outputs folder as output.txt

## Output Structure:

- Each read instruction prints the variable along with its value at the time it gets executed.
- Each transaction end call prints whether the transaction committed or aborted.
- In case of a deadlock, we print the transaction which gets aborted.
- dump prints the output for each site in the format mentioned in the project document (every site and all variables on that site sorted in ascending order)




