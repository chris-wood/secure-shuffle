import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.security.SecureRandom;

public class GeneralPaillier {

    BigInteger N;
    BigInteger Np;
    BigInteger N2;
    List<BigInteger> mods;
    BigInteger nsp1;
    BigInteger ns;

    BigInteger d;
    BigInteger p;
    BigInteger q;
    BigInteger lambda;
    BigInteger mu;
    BigInteger g;
    int s;

    public GeneralPaillier(int bitLength, int s) {
        CompositeModulus mod = PrimeGenerator.generateCompositeModulus(bitLength - 1);
        p = mod.factors.get(0);
        q = mod.factors.get(1);
        N = mod.composite;
        // Np = N;

        // Ns = N^{s + 1}
        //    s = 2 in the standard Paillier scheme
        mods = new ArrayList<BigInteger>();
        N2 = N.multiply(N);
        this.s = s;
        mods.add(N);
        mods.add(N2);

        for (int i = 2; i < s; i++) { // if s >= 3
            N2 = N2.multiply(N);
            mods.add(N2);
        }
        // mods[0] = N
        // mods[1] = N^2
        // ...
        // mods[i] = N^{i + 1}

        // g = (1 + n)^j x mod n^{s + 1}
        ns = mods.get(mods.size() - 2);
        nsp1 = mods.get(mods.size() - 1);
        // BigInteger j = randomElementIn(nsp1);
        // BigInteger x = randomElementIn(nsp1);
        // g = N.add(BigInteger.ONE).pow(j).multiply(x).mod(nsp1);
        g = N.add(BigInteger.ONE); // as per security argument in the paper

        // \lambda = lcm(p - 1, q - 1) = (p - 1)(q - 1) / gcd(p - 1, q - 1)
        BigInteger p1 = p.subtract(BigInteger.ONE);
        BigInteger q1 = q.subtract(BigInteger.ONE);
        BigInteger gcd = p1.gcd(q1);
        lambda = p1.multiply(q1).divide(gcd);

        // d = \lambda
        d = lambda.multiply(BigInteger.ONE);

        // \mu = \phi(N)^{-1} mod N = lambda^{-1} mod N
        mu = lambda.modInverse(N);
    }

    public BigInteger factorial(int k) {
        BigInteger f = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            f = f.multiply(new BigInteger("" + k));
        }
        return f;
    }

    public BigInteger L(BigInteger u) {
        return u.subtract(BigInteger.ONE).divide(N);
    }

    public BigInteger extract(BigInteger u) {
        BigInteger i = BigInteger.ZERO;

        for (int j = 1; j < s; j++) {
            // n^{j + 1}, when j = 1, we want n^2, so index at 1
            BigInteger nj = mods.get(j - 1);
            BigInteger njp1 = mods.get(j);

            BigInteger a = u.mod(njp1); // mod n^{j+1}
            BigInteger t1 = L(a);

            System.out.println("Computing with N^" + (j + 1));
            BigInteger t2 = i.multiply(BigInteger.ONE);

            for (int k = 2; k <= j; k++) {

                BigInteger nkm1 = mods.get(k - 1);

                i = i.subtract(BigInteger.ONE);

                t2 = t2.multiply(i).mod(nj); // mod n^j

                // k = 2, n^(k-1) = n^1
                BigInteger numerator = t2.multiply(nkm1); // mod n^{k-1}

                BigInteger fraction = numerator.divide(factorial(k));
                t1 = t1.subtract(fraction).mod(nj); // mod n^j
            }

            i = t1.multiply(BigInteger.ONE);
        }

        return i;
    }

    public BigInteger encrypt(BigInteger m) {
        BigInteger r = randomElementIn(ns);
        while (r.compareTo(BigInteger.ZERO) <= 0 || r.compareTo(ns) >= 0) {
            r = randomElementIn(ns);
        }

        BigInteger gm = g.modPow(m, nsp1);
        BigInteger rn = r.modPow(ns, nsp1);
        BigInteger c = gm.multiply(rn).mod(nsp1);

        System.out.println("n^s = " + ns);
        System.out.println("n^(s+1) = " + nsp1);
        System.out.println("g = " + g);
        System.out.println("r = " + r);
        System.out.println("i = " + m);
        System.out.println("c = " + c);

        return c;
    }

    public BigInteger decrypt(BigInteger ct) {
        BigInteger cd = ct.modPow(d, nsp1);
        BigInteger gd = g.modPow(d, nsp1);

        System.out.println(g + "," + gd);

        // System.out.println("Calling L with " + cl);
        BigInteger jid = extract(cd); //.multiply(mu).mod(N);
        BigInteger jd = extract(gd).modInverse(ns); //.multiply(mu).mod(N);
        BigInteger i = jid.multiply(jd).modInverse(ns);

        return i;
    }

    public BigInteger add(BigInteger x, BigInteger y) {
        return x.multiply(y).mod(nsp1);
    }

    public BigInteger multiply(BigInteger x, BigInteger y) {
        return x.modPow(y, nsp1); // k * m
    }

    public BigInteger randomElementIn(BigInteger max) {
        SecureRandom rng = new SecureRandom();
        BigInteger g = null;
        int numBits = max.bitLength();

        do {
            g = new BigInteger(numBits, rng);
        } while (g.compareTo(max) >= 0);

        return g;
    }

    public BigInteger randomElementInN2() {
        return randomElementIn(N2);
    }

    public BigInteger randomElementInN() {
        return randomElementIn(N);
    }

    public BigInteger randomPlaintextElement() {
        return randomElementIn(Np);
    }

    public static void main(String[] args) {
        GeneralPaillier p = new GeneralPaillier(Integer.parseInt(args[0]), 2);

        BigInteger m1 = p.randomElementInN();
        System.out.println("Encrypting: " + m1);

        BigInteger c1 = p.encrypt(m1);
        System.out.println("... " + c1);

        BigInteger m2 = p.randomElementInN();
        BigInteger c2 = p.encrypt(m2);

        BigInteger c3 = p.add(c1, c2);

        BigInteger m1m = p.decrypt(c1);
        System.out.println("Decryption: " + m1m);
        BigInteger m2m = p.decrypt(c2);
        System.out.println("Decryption: " + m2m);

        BigInteger m3m = p.decrypt(c3);
        System.out.println("Homomorphic decryption: " + m3m);
        System.out.println("Plaintext result:       " + m1.add(m2).mod(p.N));
    }
}
