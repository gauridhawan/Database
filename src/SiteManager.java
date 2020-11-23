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

    public booelan getLocks(Transaction transaction, Variable variable, LockType lockType){
        int value = variable.getSites();
        List<Site> sites = this.getSites(value);

        return false;
    }


}
