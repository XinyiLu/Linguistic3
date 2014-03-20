package pos;

public class Score {
	public static void main(String[] args){
		HMM hmm=new HMM();
		System.out.println(hmm.getScore(args[0], args[1]));
	}
}
