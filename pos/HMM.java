package pos;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class HMM {
	
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
		String word;
		double prob;
		
		public VertibiUnit(String w,double p){
			word=w;
			prob=p;
		}
	}
	
	class PrecisionUnit{
		int correct_count;
		int total_count;
		
		public PrecisionUnit(){
			correct_count=0;
			total_count=0;
		}
	}
	
	final static String padding_word="*PADDING*";
	final static String unknown_word="*UNK*";
	private HashMap<String,TransitionUnit> transition_map;
	private HashSet<String> unknown_set;
	private HashSet<String> popular_set;
	
	public HMM(){
		transition_map=new HashMap<String,TransitionUnit>();
		unknown_set=new HashSet<String>();
		popular_set=new HashSet<String>();
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
	
	public void readTestFileToVertibi(String inputFile){
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
				
				ArrayList<HashMap<String,VertibiUnit>> vertibiMap=updateMuList(wordList);
				ArrayList<VertibiUnit> resultList=getMuList(vertibiMap);
				String outputLine="";
				for(int i=1;i<resultList.size()-1;i++){
					VertibiUnit unit=resultList.get(i);
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
	
	public ArrayList<HashMap<String,VertibiUnit>> updateMuList(ArrayList<String> line){
		ArrayList<HashMap<String,VertibiUnit>> list=new ArrayList<HashMap<String,VertibiUnit>>();
		updateMuListHelper(list,line,0);
		return list;
	}
	
	
	public double getTerminalTransitionProb(String yWord,String xWord){
		HashMap<String,DataUnit> terminalTransitionMap=transition_map.get(yWord).terminal_transition;
		return (terminalTransitionMap.containsKey(xWord)?terminalTransitionMap.get(xWord).prob:0.0);
	}
	
	
	public void updateMuListHelper(ArrayList<HashMap<String,VertibiUnit>> list,ArrayList<String> line,int i){
		HashMap<String,VertibiUnit> mapUnit=new HashMap<String,VertibiUnit>();
		if(i==0){
			mapUnit.put(padding_word,new VertibiUnit("",1.0));
		}else if(i<=line.size()){
			String xWord=line.get(i-1);
			//iterate through all possible ys after the previous ones
			HashMap<String,VertibiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				for(String yWord:transitionSubMap.state_transition.keySet()){
					if(yWord.equals(padding_word)){
						continue;
					}
					double tempMu=prevMapUnit.get(prevY).prob*transitionSubMap.state_transition.get(yWord).prob*
							getTerminalTransitionProb(yWord,xWord);
					if((!mapUnit.containsKey(yWord))||mapUnit.get(yWord).prob<tempMu){
						mapUnit.put(yWord,new VertibiUnit(prevY,tempMu));
						assert(tempMu>0);
					}

				}
			}
	
		}else if(i==line.size()+1){
			HashMap<String,VertibiUnit> prevMapUnit=list.get(i-1);
			for(String prevY:prevMapUnit.keySet()){
				TransitionUnit transitionSubMap=transition_map.get(prevY);
				String yWord=padding_word;
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
		getMuListHelper(muList,vertibiMap,padding_word,vertibiMap.size()-1);
		return muList;
	}
	
	public void getMuListHelper(ArrayList<VertibiUnit> list,ArrayList<HashMap<String,VertibiUnit>> vertibiMap,String yWord,int i){
		if(i<0){
			return;
		}
		HashMap<String,VertibiUnit> unitMap=vertibiMap.get(i);
		
		VertibiUnit unit=new VertibiUnit(yWord,unitMap.get(yWord).prob);
		list.add(0,unit);
		getMuListHelper(list,vertibiMap,unitMap.get(yWord).word,i-1);
	}
	
}















