import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;

public class CompositeModulus {

    List<BigInteger> factors;
    BigInteger composite;

    public CompositeModulus() {
        composite = BigInteger.ONE;
        factors = new ArrayList<BigInteger>();
    }

    public void addFactor(BigInteger x) {
        composite = composite.multiply(x);
        factors.add(x);
    }
}
