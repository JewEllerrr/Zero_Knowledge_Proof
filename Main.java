
public class Main {
	
	public static void main(String[] args) {
		int num_round = 52;
		Prover Peggy = new Prover(true);
		Verifier Victor = new Verifier();
		String result = Victor.check(Peggy,num_round);
		System.out.println(result);
	}

}
