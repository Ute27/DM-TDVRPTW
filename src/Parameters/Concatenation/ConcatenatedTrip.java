package Parameters.Concatenation;
import java.util.List;

public class ConcatenatedTrip {
    private final List<ConcatenatedSegment> segmentList;
    double[][] bpointsTravelTime;

    public ConcatenatedTrip(List<ConcatenatedSegment> segmentList, double[][] bpointsTravelTime) {
        this.segmentList = segmentList;
        this.bpointsTravelTime = bpointsTravelTime;
    }

    public int getNoBpoints() {
        return bpointsTravelTime.length;
    }

    public double[][] getBpointsTravelTime() {return bpointsTravelTime;}
    public List<ConcatenatedSegment> getSegmentList() {return segmentList;}

    public double getIdealDepTime() {
        double minimalCost = Integer.MAX_VALUE;
        int minimalIndex = 0;
        for(int bpointIndex = 0; bpointIndex<bpointsTravelTime.length; bpointIndex++) {
            if(bpointsTravelTime[bpointIndex][1]<minimalCost) {
                minimalCost = bpointsTravelTime[bpointIndex][1];
                minimalIndex = bpointIndex;
            }
        }
        return bpointsTravelTime[minimalIndex][0];
    }




}
