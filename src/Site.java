public class Site {
    int index;
    int lastFailedTime;
    SiteStatus siteStatus;
    DataManager dataManager;

    Site(int index, SiteStatus siteStatus){
        this.index = index;
        this.siteStatus = siteStatus;
        this.dataManager = new DataManager(index);
    }

    Site(int index){
        this.index = index;
        this.siteStatus = SiteStatus.UP;
        this.dataManager = new DataManager(index);
    }

}
