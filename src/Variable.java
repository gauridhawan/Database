

/*
    Author : Kunal Khatri
    this is a class for a variable
    Date : December 29
    Side Effects: none
 */
public class Variable{
    int index;
    LockType lockType;
    int value;
    String name;
    int currentSite;

    /*
        Author : Kunal Khatri
        this creates an object of the variable class
        Inputs: name, value
        Output: void
        Date : December 29
        Side Effects: creates an object
     */
    Variable(String name, int value){
        this.name = name;
        this.value = value;
        this.index = Integer.parseInt(name.substring(1,name.length()));
    }

    /*
        Author : Kunal Khatri
        this creates an object of the variable class
        Inputs: name, value and site
        Output: void
        Date : December 29
        Side Effects: creates an object
     */
    Variable(String name, int value, int currentSite){
        this.name = name;
        this.value = value;
        this.index = Integer.parseInt(name.substring(1,name.length()));
        this.currentSite = currentSite;
    }

    /*
        Author : Kunal Khatri
        this returns the site on which the variable is present
        Inputs: index of the variable
        Output: integer ( -1 if variable present on each site )
        Date : December 29
        Side Effects: none
     */
    static int getSites(int index){
        if(index % 2 == 0){
            return -1;
        }
        else{
            return 1 + index%10;
        }
    }
}