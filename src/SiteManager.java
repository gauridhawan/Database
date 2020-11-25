import java.util.*;

public class SiteManager {
    int numSites;
    int numVariables;
    List<Site> sites;

    SiteManager(int numSites, int numVariables){
        this.numSites = numSites;
        this.numVariables = numVariables;
        sites = new ArrayList<>();
        for(int i = 0; i<= numSites; i++) sites.add(new Site(i));
    }

    public boolean ifSiteExists(int index){
        if(index > numSites) return false;
        return true;
    }

    public void tick(Instruction instruction){
        if(instruction.transactionType == TransactionType.fail){
            this.fail(instruction.site);
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

    public HashMap<String, Integer> getVariableValues(){
        HashMap<String, Integer> ans = new HashMap<>();
        for(Site site : this.sites){
            if(site.siteStatus == SiteStatus.UP){
                List<Variable> variables = site.getAllVariables();
                for(Variable variable : variables){
                    ans.put(variable.name, variable.value);
                }
            }

            if(site.siteStatus == SiteStatus.RECOVERING){
                List<Variable> variables = site.getAllVariables();
                for(Variable variable : variables){
                    if(site.recoveredVariables.contains(variable))
                    ans.put(variable.name, variable.value);
                }
            }
        }
        return ans;
    }

    public Site getSite(int index){
        if(ifSiteExists(index)){
            return sites.get(index);
        }
        return null;
    }

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


    public int getLock(Transaction transaction, int variable, LockType lockType){
        int value = Variable.getSites(variable);
        List<Site> sites = this.getSites(value);

        boolean flag = true;
        int recoveringFlag = 0;
        int allSitesDown = 1;
        int evenIndex = variable%2;
        for(Site site : sites){
            SiteStatus status = site.siteStatus;
            Variable temp = site.dataManager.getVariable("x"+variable);
            if(status == SiteStatus.DOWN){
                continue;
            }
            if(status == SiteStatus.RECOVERING && lockType == LockType.READ){
                if(!this.sites.get(site.index).recoveredVariables.contains(temp)){
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
                    return LockStatus.GOT_LOCK_RECOVERING.getLockStatus();
                }
                else{
                    return LockStatus.GOT_LOCK.getLockStatus();
                }
            }
            flag &= state;
        }
        if(allSitesDown == 1){
            return LockStatus.ALL_SITES_DOWN.getLockStatus();
        }
        else if (!flag){
            return LockStatus.NO_LOCK.getLockStatus();
        }
        return LockStatus.GOT_LOCK.getLockStatus();
    }

    void fail(int id){
        if(this.ifSiteExists(id)){
            sites.get(id).failSite();
        }
    }

    void recover(int id){
        if(this.ifSiteExists(id)){
            sites.get(id).recover();
        }
    }


}
