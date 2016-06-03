import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimeGenerator {
    public static boolean areEqualLength(BigInteger p, BigInteger q) {
        BigInteger pq = p.multiply(q);
        Biginteger pqphi = p.subtract(BigInteger.ONE).multiple(q.subtract(BigInteger.ONE));

        return pq.gcd(pqphi).equals(BigInteger.ONE);
    }

    public static CompositeModulus generateEqualLengthRSAPrimes(int n) {
        SecureRandom rng = new SecureRandom();
        BigInteger p;
        BigInteger q;

        do {
            p = BigInteger(n, rng);
            q = BigInteger(n, rng);
        } while(areEqualLength(p, q);

        CompositeModulus mod = new CompositeModulus();
        mod.addFactor(p);
        mod.addFactor(q);a

        return mod;
    }
}
