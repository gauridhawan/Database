import java.util.*;

/*
    Author : Kunal Khatri
    This is the SiteManager class which is used by the transaction manager class to manage sites
    Date : December 29
*/
public class SiteManager {
    int numSites;
    int numVariables;
    List<Site> sites;

    /*
        Author : Kunal Khatri
        This is the constructor of the Sitemanager class
        Inputs: number of Sites, number of Variables
        Output: Void
        Date : December 29
        Side Effects: None
     */
    SiteManager(int numSites, int numVariables){
        this.numSites = numSites;
        this.numVariables = numVariables;
        sites = new ArrayList<>();
        for(int i = 0; i<= numSites; i++) sites.add(new Site(i));
    }

    /*
        Author : Kunal Khatri
        This function checks if a site with a particular index exists
        Inputs: sideID
        Output: boolean
        Date : December 29
        Side Effects: None
     */
    public boolean ifSiteExists(int index){
        if(index > numSites) return false;
        return true;
    }

    /*
        Author : Kunal Khatri
        This is the method which handles the operations: fail(), recover() and dump()
        Inputs: Instruction, time
        Output: Void
        Date : December 29
        Side Effects: None
     */
    public void tick(Instruction instruction, int time){
        if(instruction.transactionType == TransactionType.fail){
            this.fail(instruction.site, time);
        }
        if(instruction.transactionType == TransactionType.recover){
            this.recover(instruction.site);
        }
        if(instruction.transactionType == TransactionType.dump){
            for(int i = 1; i <= numSites; i++){
                this.sites.get(i).dumpSite();
            }
        }
    }

    /*
        Author : Kunal Khatri
        This is the method which returns the odd variables since they aren't the replicated variables
        Inputs: None
        Output: List of odd variables per site
        Date : December 29
        Side Effects: None
     */
    public HashMap<String, Pair<Site,Integer>> getOddVariableValues(){

        HashMap<String, Pair<Site,Integer>> ans = new HashMap<>();
        for(Site site : this.sites){
            if(site.siteStatus == SiteStatus.DOWN){
                List<Variable> variables = site.getAllVariables();
                for(Variable variable : variables){
                    ans.put(variable.name, new Pair(site,variable.value));
                }
            }
        }
        return ans;
    }

    /*
        Author : Kunal Khatri
        This is the method which returns all the variables
        Inputs: None
        Output: List of all variables per site
        Date : December 29
        Side Effects: None
     */
    public HashMap<String, Pair<Site,Integer>> getVariableValues(){
        HashMap<String, Pair<Site,Integer>> ans = new HashMap<>();
        for(Site site : this.sites){
            if(site.siteStatus == SiteStatus.UP){
                List<Variable> variables = site.getAllVariables();
                for(Variable variable : variables){
                    ans.put(variable.name, new Pair(site,variable.value));
                }
            }

            if(site.siteStatus == SiteStatus.RECOVERING){
                List<Variable> variables = site.getAllVariables();
                for(Variable variable : variables){
                    if(site.recoveredVariables.contains(variable.name))
                    ans.put(variable.name, new Pair(site,variable.value));
                }
            }
        }
        return ans;
    }

    /*
        Author : Kunal Khatri
        This is the method which returns the site with the corresponding index
        Inputs: index of the site
        Output: Site
        Date : December 29
        Side Effects: None
     */
    public Site getSite(int index){
        if(ifSiteExists(index)){
            return sites.get(index);
        }
        return null;
    }

    /*
        Author : Kunal Khatri
        This is the method which returns all the sites with the corresponding variable
        Inputs: index of the variable
        Output: list of sites
        Date : December 29
        Side Effects: None
     */
    public List<Site> getSites(int value){
        List<Site> ans = new ArrayList<>();
        if(value == -1){
            for(int i = 1; i<=numSites; i++) ans.add(getSite(i));
        }
        else{
            ans.add(getSite(value));
        }
        return ans;
    }


    /*
        Author : Kunal Khatri
        This is the method which returns the site and value of the variable if the transaction gets a lock on that variable
        Inputs: Transaction, variable and locktype
        Output: Site on which it got lock and the value of the variable at that site
        Date : December 29
        Side Effects: Adds lock to waiting queue if the transaction doesn't get a lock on the variable
     */
    public Pair<Site, Integer> getLock(Transaction transaction, int variable, LockType lockType){
        int value = Variable.getSites(variable);
        List<Site> sites = this.getSites(value);
        boolean flag = true;
        int recoveringFlag = 0;
        int allSitesDown = 1;
        int evenIndex = variable%2;
        //System.out.println(sites.get(0).recoveredVariables + " "+ sites.get(0).siteStatus);
        for(Site site : sites){
            SiteStatus status = site.siteStatus;
            Variable temp = site.dataManager.getVariable("x"+variable);
            if(status == SiteStatus.DOWN){
                continue;
            }
            if(status == SiteStatus.RECOVERING && lockType == LockType.READ){
                if(!this.sites.get(site.index).recoveredVariables.contains(temp.name)){
                    continue;
                }
                else if(evenIndex == 1){
                    recoveringFlag = 1;
                }
            }
            allSitesDown = 0;
            boolean state = this.sites.get(site.index).getLock(transaction, temp, lockType);
            if(state && lockType == LockType.READ){
                if(recoveringFlag == 1){
                    return new Pair(site,LockStatus.GOT_LOCK_RECOVERING.getLockStatus());
                }
                else{
                    return new Pair(site,LockStatus.GOT_LOCK.getLockStatus());
                }
            }
            flag &= state;
            //System.out.println("Getlock : " + state);
        }
        if(allSitesDown == 1){
            return new Pair(null,LockStatus.ALL_SITES_DOWN.getLockStatus());
        }
        else if (!flag){
            return new Pair(null,LockStatus.NO_LOCK.getLockStatus());
        }
        return new Pair(sites.get(0),LockStatus.GOT_LOCK.getLockStatus());
    }

    /*
        Author : Kunal Khatri
        This is the method which fails a site at a particular time
        Inputs: index of the site and the current time
        Output: void
        Date : December 29
        Side Effects: None
     */
    void fail(int id, int time){
        if(this.ifSiteExists(id)){
            sites.get(id).failSite(time);
        }
    }

    /*
        Author : Kunal Khatri
        This is the method which recovers a site
        Inputs: index of the site
        Output: void
        Date : December 29
        Side Effects: None
     */
    void recover(int id){
        if(this.ifSiteExists(id)){
            sites.get(id).recover();
        }
    }

}
