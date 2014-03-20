package pos;

import java.io.IOException;

public class BetterTag {
	public static void main(String[] args) throws IOException{
		HMM hmm=new HMM();
		hmm.readFileToTransitionMapSmooth(args[0]);
		hmm.readTestFileToViterbi(args[1]);
	}
}
