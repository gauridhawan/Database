# Distributed Replicated Concurrency Control and Recovery

This is the term project for Advanced Database Management Systems course offered by Prof. Dannis Shasha during Fall 2020

# How to Run the reprozip file:

# File structure after unzipping:

# How to Check the output:

- The output always gets printed to stdout
- If the output file path isn't given as a command line argument, the output file gets automatically stored in outputs folder as output.txt

## Output Structure:

- Each read instruction prints the variable along with its value at the time it gets executed.
- Each transaction end call prints whether the transaction committed or aborted.
- In case of a deadlock, we print the transaction which gets aborted.
- dump prints the output for each site in the format mentioned in the project document (every site and all variables on that site sorted in ascending order)




