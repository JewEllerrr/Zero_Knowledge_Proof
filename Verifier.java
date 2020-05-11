import java.math.BigInteger;
import java.util.Random;

public class Verifier {

	private Random random = new Random();

	// open information for both users
	private BigInteger p;
	private BigInteger B;
	private BigInteger A;

	// open (BUT changing every round) information
	private BigInteger R;
	private BigInteger r;
	private BigInteger w;

	// coin flip
	boolean resultCoinFlipping[];

	public String check(Prover peggy, int num_round) {

		BigInteger[] ArrH = peggy.generatingH(num_round);
		BigInteger[] openInfo = peggy.getOpenInfo();
		p = openInfo[0];
		A = openInfo[1];
		B = openInfo[2];
		System.out.println("p: " + p);
		System.out.println("A: " + A);
		System.out.println("B: " + B);
//////////////////////////////////////////Coin Flipping//////////////////////////////////////////////////////
		resultCoinFlipping = new boolean[num_round];
		// generating p (generating q, then p = q*2 + 1, till p isn't prime, generating
		// new q)
		int len = 256;
		BigInteger q, p2;
		boolean[] tmpArr = new boolean[len];
		do {
			q = RandomGeneration(tmpArr);
			p2 = q.multiply(BigInteger.TWO).add(BigInteger.ONE);
		} while (!q.isProbablePrime(10) || !p2.isProbablePrime(10));
		BigInteger[] h_t = peggy.getGenByP(p2, len);
		// check - h and t are real generator by field p?
		BigInteger h = h_t[0];
		BigInteger t = h_t[1];
		if (gcd(p2, h).compareTo(BigInteger.ONE) != 0 || gcd(p2, t).compareTo(BigInteger.ONE) != 0)
			return "Prover tried to cheat in coin flipping";

		BigInteger[] privateKeys = new BigInteger[num_round];
		BigInteger[] openKeys = new BigInteger[num_round];
		BigInteger x;
		for (int i = 0; i < num_round; i++) {
			do {
				x = RandomGeneration(tmpArr);
			} while (gcd(p2.subtract(BigInteger.ONE), x).compareTo(BigInteger.ONE) != 0);
			privateKeys[i] = x;
			if (random.nextBoolean())
				openKeys[i] = h.modPow(privateKeys[i], p2);
			else
				openKeys[i] = t.modPow(privateKeys[i], p2);
		}
		BigInteger[] guesses = peggy.makeGuesses(openKeys);
		BigInteger checker;
		for (int i = 0; i < num_round; i++) {
			checker = guesses[i].modPow(privateKeys[i], p2);
			if (checker.equals(openKeys[i]))
				resultCoinFlipping[i] = true;
			else
				resultCoinFlipping[i] = false;
		}

		try {
			peggy.checkVerifierForHonesty(privateKeys, resultCoinFlipping);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(0);
		}
//////////////////////////////////////////End Coin Flip/////////////////////////////////////////////////////

		BigInteger[] responseFromProver = peggy.takeChallenge();

		int b_j = 0;
		for (int i = 0; i < resultCoinFlipping.length; i++) {
			if (resultCoinFlipping[i]) {
				b_j = i;
				break;
			}
		}

		for (int i = 0; i < resultCoinFlipping.length; i++) {
			if (resultCoinFlipping[i]) {
				if (!(A.modPow(responseFromProver[i], p).equals(ArrH[i].multiply(inverseElement(ArrH[b_j], p)).mod(p))))
					return "Prover cheating!";
			} else {
				if (!(A.modPow(responseFromProver[i], p).equals(ArrH[i].mod(p))))
					return "Prover cheating!";
			}
		}

		BigInteger Z = peggy.getZ();

		if (!(A.modPow(Z, p).equals(B.multiply(inverseElement(ArrH[b_j], p)).mod(p))))
			return "Prover cheating!";

		return "Prover did not cheat";
	}

	private BigInteger gcd(BigInteger a, BigInteger b) {
		if (b.compareTo(BigInteger.ZERO) == 0)
			return a;
		else
			return gcd(b, a.mod(b));
	}

	private BigInteger RandomGeneration(boolean[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = random.nextBoolean();
		}
		BigInteger num = new BigInteger("0");
		for (int i = arr.length - 1; i >= 0; i--) {
			if (arr[i] == true) {
				num = num.add(BigInteger.TWO.pow(i));
			}
		}
		return num;
	}

	private BigInteger inverseElement(BigInteger a, BigInteger b) {
		BigInteger x[] = new BigInteger[2];
		BigInteger y[] = new BigInteger[2];
		BigInteger q, r, xx, yy;
		int sign = 1;
		BigInteger bCopy = b;

		// initializes the coefficients
		x[0] = BigInteger.ONE;
		x[1] = BigInteger.ZERO;
		y[0] = BigInteger.ZERO;
		y[1] = BigInteger.ONE;

		// As long as b != 0 we replace a by b and b by a % b.
		while (!b.equals(BigInteger.ZERO)) {
			r = a.mod(b);
			q = a.divide(b);
			a = b;
			b = r;
			xx = x[1];
			yy = y[1];
			x[1] = (q.multiply(x[1])).add(x[0]);
			y[1] = (q.multiply(y[1])).add(y[0]);
			x[0] = xx;
			y[0] = yy;
			sign = -sign;
		}
		// Final computation of the coefficients
		x[0] = x[0].multiply(new BigInteger(String.valueOf(sign)));
		y[0] = y[0].multiply(new BigInteger(String.valueOf(-sign)));

		if (x[0].compareTo(BigInteger.ZERO) < 0) { // less than 0
			return bCopy.add(x[0]);
		} else { // equal or greater than 0
			return x[0];
		}
	}

}
