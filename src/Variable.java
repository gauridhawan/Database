public class Variable{
    int index;
    LockType lockType;
    int value;
    String name;
    int currentSite;

    Variable(String name, int value){
        this.name = name;
        this.value = value;
        this.index = Integer.parseInt(name.substring(1,name.length()));
    }

    Variable(String name, int value, int currentSite){
        this.name = name;
        this.value = value;
        this.index = Integer.parseInt(name.substring(1,name.length()));
        this.currentSite = currentSite;
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