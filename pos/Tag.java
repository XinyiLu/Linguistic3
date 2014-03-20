package pos;

import java.io.IOException;

public class Tag {
	public static void main(String[] args) throws IOException{
		HMM hmm=new HMM();
		hmm.readFileToTransitionMap(args[0]);
		hmm.readTestFileToVertibi(args[1]);
	}
}
