public class UtilityArray {
    int eid=-1;
    Integer item;
    int utility=0;
    int remainUtility=0;
    int next_pos=-1;
    int next_eid=-1;

    public UtilityArray() {
    }

    public UtilityArray(int eid, Integer item, int utility, int remainUtility) {
        this.eid = eid;
        this.item = item;
        this.utility = utility;
        this.remainUtility = remainUtility;
    }

    public UtilityArray(int eid, Integer item, int utility, int remainUtility, int next_pos, int next_eid) {
        this.eid = eid;
        this.item = item;
        this.utility = utility;
        this.remainUtility = remainUtility;
        this.next_pos = next_pos;
        this.next_eid = next_eid;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }

    public int getUtility() {
        return utility;
    }

    public void setUtility(int utility) {
        this.utility = utility;
    }

    public int getRemainUtility() {
        return remainUtility;
    }

    public void setRemainUtility(int remainUtility) {
        this.remainUtility = remainUtility;
    }

    public int getNext_pos() {
        return next_pos;
    }

    public void setNext_pos(int next_pos) {
        this.next_pos = next_pos;
    }

    public int getNext_eid() {
        return next_eid;
    }

    public void setNext_eid(int next_eid) {
        this.next_eid = next_eid;
    }
}

