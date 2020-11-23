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
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLastFailedTime() {
        return lastFailedTime;
    }

    public void setLastFailedTime(int lastFailedTime) {
        this.lastFailedTime = lastFailedTime;
    }

    public SiteStatus getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(SiteStatus siteStatus) {
        this.siteStatus = siteStatus;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    //TODO do something here
    public void writeVariable(int value){

    }
}
