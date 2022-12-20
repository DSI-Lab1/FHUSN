public class PairVal {
    int acUtility;
    int remainingUtility;

    public PairVal(int acUtility, int remainingUtility) {
        this.acUtility = acUtility;
        this.remainingUtility = remainingUtility;
    }
    public PairVal() {
    }
    public int getAcUtility() {
        return acUtility;
    }

    public void setAcUtility(int acUtility) {
        this.acUtility = acUtility;
    }

    public int getRemainingUtility() {
        return remainingUtility;
    }

    public void setRemainingUtility(int remainingUtility) {
        this.remainingUtility = remainingUtility;
    }
}
