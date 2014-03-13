package pos;


import java.io.*;
import java.util.HashMap;


public class VisibleHMM {

	class DataUnit{
		int count;
		double prob;
		
		public DataUnit(){
			count=0;
			prob=0.0;
		}
		
		public DataUnit(int c,double p){
			count=c;
			prob=p;
		}
	}
	
	class TransitionUnit{
		HashMap<String,DataUnit> state_transition;
		HashMap<String,DataUnit> terminal_transition;
		
		public TransitionUnit(){
			state_transition=new HashMap<String,DataUnit>();
			terminal_transition=new HashMap<String,DataUnit>();
		}
	}
	
	
	final static String start_symbol=Character.toString((char)Character.START_PUNCTUATION);
	final static String end_symbol=Character.toString((char)Character.END_PUNCTUATION);
	final static String unknown_word="*U*";
	private HashMap<String,TransitionUnit> transition_map;
	
	
	public VisibleHMM(){
		transition_map=new HashMap<String,TransitionUnit>();
	}
	
	public void readFileToTransitionMap(String fileName){
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"ISO-8859-1"));
			String line=null;
			//each time we read a line, count its words
			while((line=reader.readLine())!=null){
				parseLineAndCountTransitions(line);
			}
			//close the buffered reader
			reader.close();
			updateTransitionMap();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	public void parseLineAndCountTransitions(String line){
		String[] words=line.split(" ");
		
		assert(words.length>0&&words.length%2==0);
		
		//add the first start_symbol to the state count map
		{
			if(transition_map.containsKey(start_symbol)){
				transition_map.put(start_symbol,new TransitionUnit());
			}
			HashMap<String,DataUnit> state_transition_map=transition_map.get(start_symbol).state_transition;
			if(!state_transition_map.containsKey(words[1])){
				state_transition_map.put(words[1],new DataUnit());
			}
			state_transition_map.get(words[1]).count++;
		}
		//count each (y,x) and (y,y') pair
		for(int i=0;i<words.length/2;i++){
			//each x is at position 2*i and each y is at position 2*i+1
			String xWord=words[2*i],yWord=words[2*i+1];
			if(!transition_map.containsKey(yWord)){
				transition_map.put(yWord,new TransitionUnit());
			}
			TransitionUnit transitionSubMap=transition_map.get(yWord);
			String nextY=(i==words.length?end_symbol:words[2*i+3]);
			if(!transitionSubMap.state_transition.containsKey(nextY)){
				transitionSubMap.state_transition.put(nextY,new DataUnit());
			}
			transitionSubMap.state_transition.get(nextY).count++;
			if(!transitionSubMap.terminal_transition.containsKey(xWord)){
				transitionSubMap.terminal_transition.put(xWord,new DataUnit());
			}
			transitionSubMap.terminal_transition.get(xWord).count++;
		}
		
	}
	
	public void updateTransitionMap(){
		for(String yWord:transition_map.keySet()){
			HashMap<String,DataUnit> stateTransitionMap=transition_map.get(yWord).state_transition;
			HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
			
			//count the total number of Nyo(y)
			int totalStateTransitionCount=0;
			for(String nextY:stateTransitionMap.keySet()){
				totalStateTransitionCount+=stateTransitionMap.get(nextY).count;
			}
			
			for(String nextY:stateTransitionMap.keySet()){
				stateTransitionMap.get(nextY).prob=stateTransitionMap.get(nextY).count*1.0/totalStateTransitionCount;
			}
			
			//count the total number of Nyo(x,y)
			int totalTerminalTransitionCount=1;
			for(String xWord:terminalTransitionMap.keySet()){
				totalTerminalTransitionCount+=terminalTransitionMap.get(xWord).count;
			}
			
			for(String xWord:terminalTransitionMap.keySet()){
				terminalTransitionMap.get(xWord).prob=terminalTransitionMap.get(xWord).count*1.0/totalTerminalTransitionCount;
			}
			
			//put the prob of the unknown word
			assert(!terminalTransitionMap.containsKey(unknown_word));
			terminalTransitionMap.put(unknown_word,new DataUnit(1,1.0/totalTerminalTransitionCount));
		}
	}
	
	
	public static void main(String[] args){
		VisibleHMM hmm=new VisibleHMM();
		
	}
}











