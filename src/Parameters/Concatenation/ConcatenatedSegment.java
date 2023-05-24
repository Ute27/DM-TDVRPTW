package Parameters.Concatenation;
import Parameters.*;

public class ConcatenatedSegment {

    Customer from;
    Customer to;
    double[][] bpointsTravelTime;

    public ConcatenatedSegment(Customer from, Customer to, double[][] bpointsTravelTime) {
        this.from = from;
        this.to = to;
        this.bpointsTravelTime = bpointsTravelTime;
    }

    public double[][] getBpointsTravelTime() {
        return bpointsTravelTime;
    }

    public Customer getTo() {
        return to;
    }

    public Customer getFrom() {
        return from;
    }
}
