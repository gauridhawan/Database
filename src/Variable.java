public class Variable{
    int index;
    LockType lockType;
    int value;
    String name;
    int currentSite;

    Variable(String name){
        this.name = name;
        this.index = Integer.parseInt(name.substring(1,name.length()));
    }

    int getSites(){
        if(index % 2 == 0){
            return -1;
        }
        else{
            return 1 + index%10;
        }
    }
}