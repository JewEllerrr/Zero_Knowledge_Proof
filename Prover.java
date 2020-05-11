import java.math.BigInteger;
import java.util.Random;

public class Prover {

	// super secret
	private BigInteger x;
	// half secret to prove that we have super secret
	private BigInteger arrR[];

	// open information for both users
	private BigInteger p;
	private BigInteger q;
	private BigInteger A;
	private BigInteger B;

	// coin flip
	private BigInteger h;
	private BigInteger t;
	private BigInteger p2;
	private int num_round;
	boolean[] resultCoinFlipping;
	BigInteger[] openKeysFromV;
	BigInteger myGuess[];

	private int lenKey = 512;
	private int b_j = 0;

	private Random random = new Random();

	public Prover(boolean w) {
		if (w) {
			// true - if we really have a key
			boolean[] tmpArr = new boolean[lenKey];
			do {
				p = RandomGeneration(tmpArr);
			} while (!p.isProbablePrime(10));

			do {
				x = RandomGeneration(tmpArr);
			} while (x.compareTo(p) >= 0 || gcd(p.subtract(BigInteger.ONE), x).compareTo(BigInteger.ONE) != 0);

			do {
				A = RandomGeneration(tmpArr);
			} while (A.compareTo(p) >= 0);

			B = A.modPow(x, p);
		}
		
		else {
			//false - if we trying to "guess a real key" or "impersonate that we have a real key"
			boolean[] tmpArr = new boolean[lenKey];
			p = RandomGeneration(tmpArr);
			x = RandomGeneration(tmpArr);
			A = RandomGeneration(tmpArr);
			B = A.modPow(x, p);
		}

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

	private BigInteger gcd(BigInteger a, BigInteger b) {
		if (b.compareTo(BigInteger.ZERO) == 0)
			return a;
		else
			return gcd(b, a.mod(b));
	}

	public BigInteger[] generatingH(int num_round) {
		arrR = new BigInteger[num_round];
		BigInteger arrH[] = new BigInteger[num_round];
		BigInteger r;
		boolean[] tmpArr = new boolean[lenKey];

		for (int i = 0; i < num_round; i++) {
			do {
				r = RandomGeneration(tmpArr);
			} while (r.compareTo(p.subtract(BigInteger.ONE)) >= 0);
			arrR[i] = r;
			arrH[i] = A.modPow(r, p);
		}
		return arrH;
	}
/////////////////////////////Coin Flip Functions//////////////////////////////////////
	public BigInteger[] getGenByP(BigInteger p2, int lenP2) {
		this.p2 = p2;
		BigInteger arr[] = new BigInteger[2];
		boolean[] tmpArr = new boolean[lenP2];
		do {
			h = RandomGeneration(tmpArr);
			t = RandomGeneration(tmpArr);
		} while (gcd(p2, h).compareTo(BigInteger.ONE) != 0 || gcd(p2, t).compareTo(BigInteger.ONE) != 0);
		arr[0] = h;
		arr[1] = t;
		return arr;
	}

	public BigInteger[] makeGuesses(BigInteger[] openKeys) {
		num_round = openKeys.length;
		myGuess = new BigInteger[num_round];
		for (int i = 0; i < num_round; i++) {
			if (random.nextBoolean())
				myGuess[i] = h;
			else
				myGuess[i] = t;
		}
		openKeysFromV = openKeys;
		return myGuess;
	}

	public void checkVerifierForHonesty(BigInteger[] privateKeys, boolean[] result) throws Exception {

		for (int i = 0; i < privateKeys.length; i++) {
			if (gcd(p2.subtract(BigInteger.ONE), privateKeys[i]).compareTo(BigInteger.ONE) != 0)
				throw new Exception("Verifier tried to cheat in coin flipping");
		}
		
		boolean[] checkerArr = new boolean[num_round];
		BigInteger checker;

		for (int i = 0; i < num_round; i++) {
			checker = myGuess[i].modPow(privateKeys[i], p2);
			if (checker.equals(openKeysFromV[i]))
				checkerArr[i] = true;
			else
				checkerArr[i] = false;

			if (checkerArr[i] != result[i])
				throw new Exception("Verifier tried to cheat in coin flipping");
		}

		resultCoinFlipping = result;
	}
	///////////////////////////////////////////End Coin Flip Functions/////////////////////

	public BigInteger[] takeChallenge() {

		BigInteger[] res = new BigInteger[num_round];

		b_j = 0;
		for (int i = 0; i < resultCoinFlipping.length; i++) {
			if (resultCoinFlipping[i]) {
				b_j = i;
				break;
			}
		}

		for (int i = 0; i < resultCoinFlipping.length; i++) {
			if (resultCoinFlipping[i])
				res[i] = arrR[i].subtract(arrR[b_j]).mod(p.subtract(BigInteger.ONE));
			else
				res[i] = arrR[i];
		}

		return res;
	}

	public BigInteger[] getOpenInfo() {
		BigInteger keys[] = new BigInteger[3];
		keys[0] = getP();
		keys[1] = getA();
		keys[2] = getB();
		return keys;
	}

	public BigInteger getP() {
		return p;
	}

	public BigInteger getB() {
		return B;
	}

	public BigInteger getA() {
		return A;
	}

	public BigInteger getZ() {
		BigInteger z = x.subtract(arrR[b_j]).mod(p.subtract(BigInteger.ONE));
		return z;
	}

}
