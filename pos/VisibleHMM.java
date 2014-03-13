package pos;


import java.io.*;
import java.util.ArrayList;
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
	
	class VertibiUnit{
		String prevWord;
		double prob;
		
		public VertibiUnit(String word,double p){
			prevWord=word;
			prob=p;
		}
	}
	
	final static String start_symbol=Character.toString((char)Character.START_PUNCTUATION);
	final static String end_symbol=Character.toString((char)Character.END_PUNCTUATION);
	final static String unknown_word="*U*";
	private HashMap<String,TransitionUnit> transition_map;
	
	
	public VisibleHMM(){
		transition_map=new HashMap<String,TransitionUnit>();
	}
	
	public double getTerminalTransitionProb(String yWord,String xWord){
		assert(transition_map.containsKey(yWord));
		HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
		String newWord=(terminalTransitionMap.containsKey(xWord)?xWord:unknown_word);
		return terminalTransitionMap.get(newWord).prob;
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
			if(!transition_map.containsKey(start_symbol)){
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
			String nextY=((i==words.length/2-1)?end_symbol:words[2*i+3]);
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
	
	public void printTransitionMap(){
		for(String yWord:transition_map.keySet()){
			TransitionUnit submap=transition_map.get(yWord);
			System.out.println("Y:\t"+yWord);
			System.out.println("X----------------------------");
			for(String xWord:submap.terminal_transition.keySet()){
				System.out.print(xWord+":"+submap.terminal_transition.get(xWord).prob+"\t");
			}
			System.out.println("\nY--------------------------");
			for(String nextY:submap.state_transition.keySet()){
				System.out.print(nextY+":"+submap.state_transition.get(nextY).prob+"\t");
			}
			System.out.println();
		}
	}
	
	public void readTestFileToVertibi(String inputFile){
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"ISO-8859-1"));
			String line=null;
			//each time we read a line, count its words
			while((line=reader.readLine())!=null){
				ArrayList<HashMap<String,VertibiUnit>> vertibiMap=parseTestLineToVertibi(line);
				ArrayList<VertibiUnit> resultList=getMuList(vertibiMap);
				for(VertibiUnit unit:resultList){
					System.out.print(unit.prevWord+" ");
				}
				System.out.println();
			}
			//close the buffered reader
			reader.close();
			
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<HashMap<String,VertibiUnit>> parseTestLineToVertibi(String line){
		String[] words=line.split(" ");
		ArrayList<String> wordList=new ArrayList<String>();
		assert(words.length>0&&words.length%2==0);
		
		for(int i=0;i<words.length;i+=2){
			wordList.add(words[i]);
		}
		
		return updateMuList(wordList);
	}
	
	public ArrayList<HashMap<String,VertibiUnit>> updateMuList(ArrayList<String> line){
		ArrayList<HashMap<String,VertibiUnit>> list=new ArrayList<HashMap<String,VertibiUnit>>();
		updateMuListHelper(list,line,0);
		return list;
	}
	
	public void updateMuListHelper(ArrayList<HashMap<String,VertibiUnit>> list,ArrayList<String> line,int i){
		HashMap<String,VertibiUnit> mapUnit=new HashMap<String,VertibiUnit>();
		if(i==0){
			mapUnit.put(start_symbol,new VertibiUnit("",1.0));
		}else if(i<line.size()){
			String xWord=line.get(i);
			//iterate through all possible ys after the previous ones
			HashMap<String,VertibiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				for(String yWord:transitionSubMap.state_transition.keySet()){
					if(yWord.equals(end_symbol)){
						continue;
					}
					double tempMu=prevMapUnit.get(prevY).prob*transitionSubMap.state_transition.get(yWord).prob*getTerminalTransitionProb(yWord,xWord);
					if((!mapUnit.containsKey(yWord))||mapUnit.get(yWord).prob<tempMu){
						mapUnit.put(yWord,new VertibiUnit(prevY,tempMu));
					}
				}
			}
	
		}else if(i==line.size()){
			HashMap<String,VertibiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				String yWord=end_symbol;
				if(transitionSubMap.state_transition.containsKey(yWord)){
					double tempMu=prevMapUnit.get(prevY).prob*transitionSubMap.state_transition.get(yWord).prob;
					if((!mapUnit.containsKey(yWord))||mapUnit.get(yWord).prob<tempMu){
						mapUnit.put(yWord,new VertibiUnit(prevY,tempMu));
					}
				}
				
			}
		}else{
			return;
		}
		
		list.add(mapUnit);
		updateMuListHelper(list,line,i+1);
	}
	
	public ArrayList<VertibiUnit> getMuList(ArrayList<HashMap<String,VertibiUnit>> vertibiMap){
		ArrayList<VertibiUnit> muList=new ArrayList<VertibiUnit>();
		getMuListHelper(muList,vertibiMap,end_symbol,vertibiMap.size()-1);
		return muList;
	}
	
	public void getMuListHelper(ArrayList<VertibiUnit> list,ArrayList<HashMap<String,VertibiUnit>> vertibiMap,String yWord,int i){
		if(i<0){
			return;
		}
		HashMap<String,VertibiUnit> unitMap=vertibiMap.get(i);
		assert(unitMap.containsKey(yWord));
		VertibiUnit unit=new VertibiUnit(yWord,unitMap.get(yWord).prob);
		list.add(0,unit);
		getMuListHelper(list,vertibiMap,unitMap.get(yWord).prevWord,i-1);
	}
	
	public static void main(String[] args){
		VisibleHMM hmm=new VisibleHMM();
		hmm.readFileToTransitionMap(args[0]);
		//hmm.printTransitionMap();
		hmm.readTestFileToVertibi(args[1]);
		
	}
}











