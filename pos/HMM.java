package pos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class HMM {
	
	//the data unit in each transition map, contains both the count and the probability of the corresponding pair
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
	
	//class to save the state transition map and the terminal transition map
	class TransitionUnit{
		HashMap<String,DataUnit> state_transition;
		HashMap<String,DataUnit> terminal_transition;
		
		public TransitionUnit(){
			state_transition=new HashMap<String,DataUnit>();
			terminal_transition=new HashMap<String,DataUnit>();
		}
	}
	
	//unit to save its previous word and its corresponding mu
	class ViterbiUnit{
		String word;
		double prob;
		
		public ViterbiUnit(String w,double p){
			word=w;
			prob=p;
		}
	}
	
	final static String padding_word="*PADDING*";
	final static String unknown_word="*UNK*";
	private HashMap<String,TransitionUnit> transition_map;
	
	//these two sets are used to record those words that appear only once in the training data
	private HashSet<String> unknown_set;
	private HashSet<String> popular_set;
	
	public HMM(){
		transition_map=new HashMap<String,TransitionUnit>();
		unknown_set=new HashSet<String>();
		popular_set=new HashSet<String>();
	}
	
	//read the training file and analyze it into transition_map
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
	
	//use better estimate for transition_map
	public void readFileToTransitionMapSmooth(String fileName){
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"ISO-8859-1"));
			String line=null;
			//each time we read a line, count its words
			while((line=reader.readLine())!=null){
				parseLineAndCountTransitions(line);
			}
			//close the buffered reader
			reader.close();
			updateTransitionMapSmooth();
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
	}
	
	//parse each training line and count each state-state and state-terminal pair into transition_map
	public void parseLineAndCountTransitions(String line){
		String[] words=line.split(" ");
		
		assert(words.length>0&&words.length%2==0);
		
		//add the first start_symbol to the state count map
		{
			if(!transition_map.containsKey(padding_word)){
				transition_map.put(padding_word,new TransitionUnit());
			}
			HashMap<String,DataUnit> state_transition_map=transition_map.get(padding_word).state_transition;
			if(!state_transition_map.containsKey(words[1])){
				state_transition_map.put(words[1],new DataUnit());
			}
			state_transition_map.get(words[1]).count++;
		}
		//count each (y,x) and (y,y') pair
		for(int i=0;i<words.length/2;i++){
			
			//each x is at position 2*i and each y is at position 2*i+1
			String xWord=words[2*i],yWord=words[2*i+1];
			
			//add each word to unknown_set if it doesn't contain this word
			//but if it already contains this word, remove from unknown_set
			if(unknown_set.contains(xWord)){
				unknown_set.remove(xWord);
				popular_set.add(xWord);
			}else if(!popular_set.contains(xWord)){
				unknown_set.add(xWord);
			}
			
			if(!transition_map.containsKey(yWord)){
				transition_map.put(yWord,new TransitionUnit());
			}
			TransitionUnit transitionSubMap=transition_map.get(yWord);
			String nextY=((i==words.length/2-1)?padding_word:words[2*i+3]);
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
	
	//calculate the probability of each state-state and state-terminal pair
	public void updateTransitionMap(){
		for(String yWord:transition_map.keySet()){
			HashMap<String,DataUnit> stateTransitionMap=transition_map.get(yWord).state_transition;
			
			//count the total number of Nyo(y)
			int totalStateTransitionCount=0;
			for(String nextY:stateTransitionMap.keySet()){
				totalStateTransitionCount+=stateTransitionMap.get(nextY).count;
			}
			
			for(String nextY:stateTransitionMap.keySet()){
				stateTransitionMap.get(nextY).prob=(stateTransitionMap.get(nextY).count*1.0)/totalStateTransitionCount;
			}
			
			//count the total number of Nyo(x,y)
			HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
			
			int totalTerminalTransitionCount=0;
			int typeCount=0;
			for(String xWord:terminalTransitionMap.keySet()){
				totalTerminalTransitionCount+=terminalTransitionMap.get(xWord).count;
				typeCount++;
			}
			typeCount++;
			for(String xWord:terminalTransitionMap.keySet()){
				terminalTransitionMap.get(xWord).prob=(terminalTransitionMap.get(xWord).count+1.0)/(totalTerminalTransitionCount+typeCount);
			}
			
			terminalTransitionMap.put(unknown_word,new DataUnit(1,1.0/(totalTerminalTransitionCount+typeCount)));
		}
	}
	
	//use better estimate to calculate the probability of each state-state and state-terminal pair
	public void updateTransitionMapSmooth(){
		for(String yWord:transition_map.keySet()){
			HashMap<String,DataUnit> stateTransitionMap=transition_map.get(yWord).state_transition;
			HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
			
			//count the total number of Nyo(y)
			int totalStateTransitionCount=0;
			for(String nextY:stateTransitionMap.keySet()){
				totalStateTransitionCount+=stateTransitionMap.get(nextY).count;
			}
			
			for(String nextY:stateTransitionMap.keySet()){
				stateTransitionMap.get(nextY).prob=(stateTransitionMap.get(nextY).count*1.0)/totalStateTransitionCount;
			}
			
			
			//count the total number of Nyo(x,y)
			int totalTerminalTransitionCount=0,unknown_count=0;
			for(String xWord:terminalTransitionMap.keySet()){
				totalTerminalTransitionCount+=terminalTransitionMap.get(xWord).count;
				if(unknown_set.contains(xWord)){
					unknown_count++;
				}
				
			}
			
			totalTerminalTransitionCount+=unknown_count;
			terminalTransitionMap.put(unknown_word,new DataUnit(unknown_count,0.0));
			
			for(String xWord:terminalTransitionMap.keySet()){
				terminalTransitionMap.get(xWord).prob=(terminalTransitionMap.get(xWord).count*1.0)/(totalTerminalTransitionCount);
			}	
		}
	}
	
	//parse the input file and print out the Viterbi tag sequences for each sentence
	public void readTestFileToViterbi(String inputFile){
		try {
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"ISO-8859-1"));
			String line=null;
			while((line=reader.readLine())!=null){
				String[] words=line.split(" ");
				ArrayList<String> wordList=new ArrayList<String>();
				assert(words.length>0&&words.length%2==0);
				
				for(int i=0;i<words.length;i+=2){
					wordList.add(words[i]);
				}
				
				ArrayList<HashMap<String,ViterbiUnit>> viterbiMap=updateMuList(wordList);
				ArrayList<ViterbiUnit> resultList=getMuList(viterbiMap);
				String outputLine="";
				for(int i=1;i<resultList.size()-1;i++){
					ViterbiUnit unit=resultList.get(i);
					outputLine+=(words[2*(i-1)]+" "+unit.word+" ");
				}
				
				System.out.println(outputLine.substring(0, outputLine.length()-1));
				
			}
			//close the buffered reader
			reader.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	//calculate the correct rate of our estimated tag sequences
	public double getScore(String goldFile,String myOutput){
		double prob=0.0;
		try {
			BufferedReader goldReader=new BufferedReader(new InputStreamReader(new FileInputStream(goldFile),"ISO-8859-1"));
			BufferedReader myReader=new BufferedReader(new InputStreamReader(new FileInputStream(myOutput),"ISO-8859-1"));
			String gline=null,mline=null;
			int totalCount=0,correctCount=0;
			while((gline=goldReader.readLine())!=null&&(mline=myReader.readLine())!=null){
				String[] gwords=gline.split(" ");
				String[] mwords=mline.split(" ");
				assert(gwords.length==mwords.length&&gwords.length%2==0);
				totalCount+=gwords.length/2;
				
				for(int i=1;i<gwords.length;i+=2){
					correctCount+=(gwords[i].equals(mwords[i])?1:0);
				}
				
			}
			//close the buffered reader
			goldReader.close();
			myReader.close();
			prob=correctCount*1.0/totalCount;
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return prob;
	}
	
	//entrance to recursively get the map of possible tag sequences for given sentence
	public ArrayList<HashMap<String,ViterbiUnit>> updateMuList(ArrayList<String> line){
		ArrayList<HashMap<String,ViterbiUnit>> list=new ArrayList<HashMap<String,ViterbiUnit>>();
		updateMuListHelper(list,line,0);
		return list;
	}
	
	//get the tau value give x,y
	public double getTerminalTransitionProb(String yWord,String xWord){
		HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
		return (terminalTransitionMap.containsKey(xWord)?terminalTransitionMap.get(xWord).prob:0.0);
	}
	
	//function to recursively get the Viterbi map of given line
	public void updateMuListHelper(ArrayList<HashMap<String,ViterbiUnit>> list,ArrayList<String> line,int i){
		HashMap<String,ViterbiUnit> mapUnit=new HashMap<String,ViterbiUnit>();
		if(i==0){
			mapUnit.put(padding_word,new ViterbiUnit("",1.0));
		}else if(i<=line.size()){
			String xWord=line.get(i-1);
			//iterate through all possible ys after the previous ones
			HashMap<String,ViterbiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				for(String yWord:transitionSubMap.state_transition.keySet()){
					if(yWord.equals(padding_word)){
						continue;
					}
					double tempMu=prevMapUnit.get(prevY).prob*transitionSubMap.state_transition.get(yWord).prob*
							getTerminalTransitionProb(yWord,xWord);
					if((!mapUnit.containsKey(yWord))||mapUnit.get(yWord).prob<tempMu){
						mapUnit.put(yWord,new ViterbiUnit(prevY,tempMu));
						assert(tempMu>0);
					}

				}
			}
	
		}else if(i==line.size()+1){
			HashMap<String,ViterbiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				String yWord=padding_word;
				if(transitionSubMap.state_transition.containsKey(yWord)){
					double tempMu=prevMapUnit.get(prevY).prob*transitionSubMap.state_transition.get(yWord).prob;
					if((!mapUnit.containsKey(yWord))||mapUnit.get(yWord).prob<tempMu){
						mapUnit.put(yWord,new ViterbiUnit(prevY,tempMu));
					}
				}
			}
			
		}else{
			return;
		}
		
		list.add(mapUnit);
		updateMuListHelper(list,line,i+1);
	}
	
	//entrance to parse the Viterbi map and get the tag sequence
	public ArrayList<ViterbiUnit> getMuList(ArrayList<HashMap<String,ViterbiUnit>> viterbiMap){
		ArrayList<ViterbiUnit> muList=new ArrayList<ViterbiUnit>();
		getMuListHelper(muList,viterbiMap,padding_word,viterbiMap.size()-1);
		return muList;
	}
	
	//function to recursively get the Viterbi sequence given the map
	public void getMuListHelper(ArrayList<ViterbiUnit> list,ArrayList<HashMap<String,ViterbiUnit>> viterbiMap,String yWord,int i){
		if(i<0){
			return;
		}
		HashMap<String,ViterbiUnit> unitMap=viterbiMap.get(i);
		
		ViterbiUnit unit=new ViterbiUnit(yWord,unitMap.get(yWord).prob);
		list.add(0,unit);
		getMuListHelper(list,viterbiMap,unitMap.get(yWord).word,i-1);
	}
	
}















