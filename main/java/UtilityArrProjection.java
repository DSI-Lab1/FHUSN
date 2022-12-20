import java.util.List;

public class UtilityArrProjection {
    UtilityArray[]  utilityArray;
    List<UtilityArrPosition> positions;

    public UtilityArrProjection() {
    }

    public UtilityArrProjection(UtilityArray[] utilityArray, List<UtilityArrPosition> positions) {
        this.utilityArray = utilityArray;
        this.positions = positions;
    }

    public List<UtilityArrPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<UtilityArrPosition> positions) {
        this.positions = positions;
    }

    public UtilityArray[] getUtilityArray() {
        return utilityArray;
    }

    public void setUtilityArray(UtilityArray[] utilityArray) {
        this.utilityArray = utilityArray;
    }


}
