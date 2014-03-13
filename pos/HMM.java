package pos;

import java.util.ArrayList;
import java.util.HashMap;

public class HMM {
	
	class TransitionUnit{
		HashMap<String,Double> state_transition;
		HashMap<String,Double> terminal_transition;
		
		public TransitionUnit(){
			state_transition=new HashMap<String,Double>();
			terminal_transition=new HashMap<String,Double>();
		}
	}
	
	ArrayList<String[]> inputWords;
	HashMap<String,TransitionUnit> expectation_map;
	HashMap<String,TransitionUnit> transition_map;
	final static String start_symbol=Character.toString((char)Character.START_PUNCTUATION);
	final static String end_symbol=Character.toString((char)Character.END_PUNCTUATION);
	
	public HMM(){
		expectation_map=new HashMap<String,TransitionUnit>();
		transition_map=new HashMap<String,TransitionUnit>();
	}
	
//	public void getForwardProbArray(ArrayList<Double> alphaList,HashMap<String,Double> prevMap,String[] line,int i){
//		HashMap<String,Double> newPrevMap=new HashMap<String,Double>();
//		if(i==0){
//			alphaList.add(1.0);
//			newPrevMap.put(start_symbol,1.0);
//			getForwardProbArray(alphaList,newPrevMap,line,i+1);
//		}else if(i<line.length+1){
//			double curAlpha=0;
//			TransitionUnit transSubMap=transition_map.get();
//			for(String prevY:prevMap.keySet()){
//				TransitionUnit tempMap=transition_map.get(prevY);
//				curAlpha+=prevMap.get(prevY)*tempMap.state_transition.get()
//			}
//			
//		}else{
//			//the ending symbol
//			
//		}
//		
//		
//	}
	
	public static void main(String[] args){
		
		
	}
	
	
	
}















