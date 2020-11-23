import java.util.*;

public class SiteManager {
    int numSites;
    int numVariables;
    List<Site> sites;

    SiteManager(int numSites, int numVariables){
        this.numSites = numSites;
        this.numVariables = numVariables;
        sites = new ArrayList<>();
        for(int i = 0; i< numSites; i++) sites.add(new Site(i));
    }

}
