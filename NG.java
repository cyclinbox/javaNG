//: NG.java
/** Nei-Gojoborit algorithm solve Ka/Ks problem 
* @author Zhang Wanyu
* @author zhangwanyu2000@outlook.com
* @version 1.0
* */
package javaGUI;

import java.util.Arrays;
import java.util.ArrayList;
import java.io.*;
import java.util.HashMap; // Use hashmap to implement the dictionary function

import javax.swing.JOptionPane;

public class NG{
	/** <b>data</b> is a HashMap object which storage sequences and their titles. */
	private HashMap<String,String> data = new HashMap<String,String>();
	/** <b>gentab</b> is a HashMap object which storage genetic codon table. */
	private HashMap<Character,HashMap<Character,HashMap<Character,Character>>> gentab = new HashMap<Character,HashMap<Character,HashMap<Character,Character>>>();
	
	/** ratio of transition/transversion(i/v ratio). default = 0.5 */
	private Double ivratio = 0.5;
	
	/** a HashMap which storage Ka result */
	private HashMap<String,HashMap<String,Double>> Ka = new HashMap<String,HashMap<String,Double>>();

	/** a HashMap which storage Ks result */
	private HashMap<String,HashMap<String,Double>> Ks = new HashMap<String,HashMap<String,Double>>();

	/** a HashMap which storage KaKs result */
	private HashMap<String,HashMap<String,Double>> KaKs = new HashMap<String,HashMap<String,Double>>();
	
	/** object new NG() isInited? */
	//private Boolean isInited = false;

	/** calculate average */
	private double mean(double[] vec){
		double avg=0.0;
		for(int i=0;i<vec.length;i++){
			avg += vec[i]/(double)vec.length;
		}
		return avg;
	}

	/** calculate average */
	private double mean(int[] vec){
		double avg=0.0;
		for(int i=0;i<vec.length;i++){
			avg += (double)vec[i]/(double)vec.length;
		}
		return avg;
	}

	/** codon translator
	 * @param acid ternary char array of a codon(lower case).
	 * @param gentab genetic codon table(storaged in a HashMap).
	 * @return the amino acid of this codon(upper case).
	 * */
	//public char transcodon(char[] acid, HashMap<Character,HashMap<Character,HashMap<Character,Character>>> gentab)
	private char transcodon(char[] acid)
	{
		//System.out.println(acid);//debug
		//System.out.println(gentab);//debug
		char aminoa;
		if(acid[0]!='-'&&acid[1]!='-'&&acid[2]!='-') aminoa = gentab.get(acid[0]).get(acid[1]).get(acid[2]);
		else aminoa = '-';
		return aminoa;
}

	/** calc S in Ka/Ks method. Notice: N = len(seq)-S 
	 * @param seq a DNA sequence(char[]) which need to be count(lowercase).
	 * @return parameter S in Ka/Ks method.
	 * */
	private double calcS(char[] seq){
		//System.out.println("ivratio="+ivratio);//debug
		double f = 0.0;// this is a variable which is related to f_i in Ka/Ks method.
		double p = ivratio/(1+ivratio); // proportion of transition event.
		for(int i=0;i<seq.length-2;i+=3){
			char[] codon = (char[]) Arrays.copyOfRange(seq,i,i+3);
			char   aa0   = transcodon(codon);
			for(int j=0;j<3;j++){
				char[] nucli = {'a','g','t','c'};
				// we define variable m as the nucleotide index of codon[j] in the array nucli.
				// example: for codon ATG and j=1,codon[j]=T(=nucli[2]), therefore m=2.
				int m;
				for(m=0;m<4;m++) if(codon[j]==nucli[m])break;
				// we use this loop to check the mutation on this nucleotide is synonymous or non-synonymous.
				for(int k=0;k<4;k++){
					if(nucli[k]==codon[j]) continue;
					char[] codon1 = (char[])codon.clone();
					codon1[j] = nucli[k];
					char aa1  = transcodon(codon1);
					if(aa1==aa0) {
						if((m<2)==(k<2)) f += 3*p;//this is means that the mutation on this nucleotide is a transition.
						else f += 3*(1-p)/2;// transversion.
					}
				}
			}
		}
		double S = f/3; 
		//System.out.println("S="+S);//debug
		return S;
	}

	/** calc s_d and n_d in Ka/Ks method for each codon.
	 * @param codon1 paired codon in the first sequence.
	 * @param codon2 paired codon in the second sequence.
	 * @return a double array, storage s_d and n_d.
	 * */
	private double[] codondiff(char[] codon1, char[] codon2){
		ArrayList<Integer> diffsite =new ArrayList<Integer>();
		for(int i=0;i<3;i++)
			if(codon1[i]!=codon2[i]) diffsite.add(i);
		double[] res = new double[2];
		if(diffsite.size()==0)
			res = new double[]{0,0};
		else if(diffsite.size()==1){
			if(transcodon(codon1)==transcodon(codon2)) res = new double[]{1,0};
			else res = new double[]{0,1};
		}else if(diffsite.size()==2){ //two nucleotides are different. there are 2 pathways to reach the result, and what we need to do is calc means of s_d and n_d in 2 ways
			int[] s_dl = new int[2];
			int[] n_dl = new int[2];
			for(int i=0;i<2;i++){
				int s_d = 0;
				int n_d = 0;
				char[] codon3 = (char[])codon1.clone();
				codon3[diffsite.get(i)] = codon2[diffsite.get(i)];
				if(transcodon(codon1)==transcodon(codon3)) s_d++;
				else n_d++;
				if(transcodon(codon3)==transcodon(codon2)) s_d++;
				else n_d++;
				s_dl[i] = s_d;
				n_dl[i] = n_d;
			}
			res = new double[]{mean(s_dl),mean(n_dl)};
		}else{
			int[] s_dl = new int[6];
			int[] n_dl = new int[6];
			int k=0; // count for s_dl[] and n_dl[]
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					if(i==j)  continue;
					int s_d = 0;
					int n_d = 0;
					char[] codon3 = (char[])codon1.clone();
					codon3[diffsite.get(i)] = codon2[diffsite.get(i)];
					char[] codon4 = (char[])codon3.clone();
					codon4[diffsite.get(j)] = codon2[diffsite.get(j)];
					if(transcodon(codon1)==transcodon(codon3)) s_d++;
					else n_d++;
					if(transcodon(codon3)==transcodon(codon4)) s_d++;
					else n_d++;
					if(transcodon(codon4)==transcodon(codon2)) s_d++;
					else n_d++;
					s_dl[k] = s_d;
					n_dl[k] = n_d;
					k++;
				}
			}
			res = new double[]{mean(s_dl),mean(n_dl)};
		}
		return res;
	}

	/** calc Sd and Nd in Ka/Ks method for each sequence.
	 * @param seq1 first sequence.
	 * @param seq2 second sequence.
	 * @return a double array, storage Sd and Nd.
	 * */
	private double[] calcSdNd(char[]seq1,char[]seq2){
		double Sd = 0;
		double Nd = 0;
		for(int i=0;i<seq1.length-2;i+=3){
			//System.out.println("i="+i+";i+3="+(i+3));
			char[] codon1 = (char[]) Arrays.copyOfRange(seq1,i,i+3);
			char[] codon2 = (char[]) Arrays.copyOfRange(seq2,i,i+3);
			double[] res  = codondiff(codon1,codon2);
			Sd += res[0];
			Nd += res[1];
		}
		double[] result = new double[]{Sd,Nd};
		return result;
	}

	/** calc Ka and Ks in Ka/Ks method.
	 * @param seq1 first sequence.
	 * @param seq2 second sequence.
	 * @return a double array, storage Ka,Ks and Ka/Ks.
	 * */
	private double[] calcKaKs(char[]seq1,char[]seq2){
		double S1 = calcS(seq1);
		double S2 = calcS(seq2);
		double S  = (S1+S2)/2;
		double N  = seq1.length-S;
		double[] res = calcSdNd(seq1,seq2);
		double Sd = res[0];
		double Nd = res[1];
		//System.out.println("for Ks: Sd="+Sd+",S="+S+"; for Ka: Nd="+Nd+",N="+N);//debug
		double Ka = Nd/N;
		double Ks = Sd/S;
		//if(Ks==0) Ks += 0.0000001; //I don't know why I write this line. May be I just want to make some big news?
		double KaKs;// = Ka/Ks;
		if(Ks==0)KaKs=-1; // I don't know how to let a double variant in java equal to null. But I know Ka/Ks>0. So if Ka/Ks<0, it must be a "null".
		else KaKs = Ka/Ks;
		double[] result = new double[]{Ka,Ks,KaKs};
		return result;
	}
	
	/** read sequence from fasta file.
	 * @param fpath fasta file path.
	 * @return a HashMap<String,String>, storage sequences' title(key) and sequences(value).
	 * */
	private HashMap<String,String> readfasta(String fpath) throws IOException{
		HashMap<String,String> fas = new HashMap<String, String>();
		File file = new File(fpath);
		if (!file.exists()) throw new FileNotFoundException("Fasta sequence file NOT FOUND!");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String seqtitle = "";
		String sequence = "";
		while ((line=br.readLine())!=null){
			if(line.contains(">")){
				if(sequence.length()>0) fas.put(seqtitle,sequence);
				seqtitle = line.substring(1).trim();
				sequence = "";
			}else{
				sequence += line.trim();
			}
		}
		br.close();
		fas.put(seqtitle,sequence);
		return fas;
	}

	/** read genetic codon table from table file.
	 * @param fpath fasta file path.
	 * @return a HashMap<Character,HashMap>, storage codon table.
	 * */
	private HashMap<Character,HashMap<Character,HashMap<Character,Character>>> readGenTable(String fpath) throws IOException{
		HashMap<Character,HashMap<Character,HashMap<Character,Character>>> gentab = new HashMap<Character,HashMap<Character,HashMap<Character,Character>>>();
		File file = new File(fpath);
		if (!file.exists()) throw new FileNotFoundException("Genetic codon Table NOT FOUND!");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line=br.readLine())!=null){
			char[] a = line.toLowerCase().toCharArray();//all char are lowercase, except symbol '-'
			if(gentab.containsKey(a[0])==false){
				HashMap<Character,HashMap<Character,Character>> gentab_1 = new HashMap<Character,HashMap<Character,Character>>();
				gentab.put(a[0],gentab_1);
			}
			if(gentab.get(a[0]).containsKey(a[1])==false){
				HashMap<Character,Character> gentab_2 = new HashMap<Character,Character>();
				gentab.get(a[0]).put(a[1],gentab_2);
			}
			if(gentab.get(a[0]).get(a[1]).containsKey(a[2])==false){
				char aminoa;
				if(a[4]!=45) aminoa = (char)((int)a[4]-32);//convert to UpperCase
				else aminoa = a[4];//if this codon is a stop codon, then aminoa = '-'
				gentab.get(a[0]).get(a[1]).put(a[2],aminoa);
			}
		}
		br.close();
		return gentab;
	}

	/** read genetic codon table.
	 * notice: this function will read gentic codon table file from jar archive.
	 * @return a HashMap<Character,HashMap>, storage codon table.
	 * */
	private HashMap<Character,HashMap<Character,HashMap<Character,Character>>> readGenTable() throws IOException{
		HashMap<Character,HashMap<Character,HashMap<Character,Character>>> gentab = new HashMap<Character,HashMap<Character,HashMap<Character,Character>>>();
		InputStream asset = NG.class.getResourceAsStream("geneticTable.tab");
		Reader reader = new InputStreamReader(asset);
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line=br.readLine())!=null){
			char[] a = line.toLowerCase().toCharArray();//all char are lowercase, except symbol '-'
			if(gentab.containsKey(a[0])==false){
				HashMap<Character,HashMap<Character,Character>> gentab_1 = new HashMap<Character,HashMap<Character,Character>>();
				gentab.put(a[0],gentab_1);
			}
			if(gentab.get(a[0]).containsKey(a[1])==false){
				HashMap<Character,Character> gentab_2 = new HashMap<Character,Character>();
				gentab.get(a[0]).put(a[1],gentab_2);
			}
			if(gentab.get(a[0]).get(a[1]).containsKey(a[2])==false){
				char aminoa;
				if(a[4]!=45) aminoa = (char)((int)a[4]-32);//convert to UpperCase
				else aminoa = a[4];//if this codon is a stop codon, then aminoa = '-'
				gentab.get(a[0]).get(a[1]).put(a[2],aminoa);
			}
		}
		br.close();
		return gentab;
	}	
	
	private void saveData(HashMap<String,HashMap<String,Double>> Res,String fpath) throws IOException{
		BufferedWriter writer = null;
		File file = new File(fpath);
        if(!file.exists()){
            try   {file.createNewFile();} 
            catch (IOException e) {e.printStackTrace();}
        }
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
    		int      resNum  = Res.keySet().size();
    		String[] resName = new String[resNum];
    		int i = 0;
    		for (String key : Res.keySet()) {
    			resName[i] = key;
    			i++;
    		}
    		Arrays.sort(resName);
    		writer.write("sequence ID");
    		for(i=0;i<resNum;i++)writer.write("\t"+resName[i]);
    		for(i=0;i<resNum;i++) {
    			writer.write("\n"+resName[i]);
             	for(int j=0;j<resNum;j++) {
             		if(i==j)  writer.write("\t");
             		else if(Res.get(resName[i]).get(resName[j])==-1)  writer.write("\tnull"); 
             		else  writer.write("\t"+String.valueOf(Res.get(resName[i]).get(resName[j])));
             	}
             }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {if(writer != null) writer.close();} 
            catch (IOException e) {e.printStackTrace();}
        }
	}
	
	/** public method: load sequence from fasta file.
	 * @param fpath fasta file path.
	 * */
	public void loadData(String fpath)throws IOException {
		if(gentab.isEmpty())gentab = readGenTable();
		data = readfasta(fpath);
	}

	/** public method: load genetic codon table from table file.
	 * @param fpath fasta file path.
	 * */
	public void loadGenTable(String fpath)throws IOException{
		gentab = readGenTable(fpath);
	}
	
	/** public method: load genetic codon table from jar archive.
	 * @param fpath fasta file path.
	 * */
	public void loadGenTable()throws IOException{
		gentab = readGenTable();
	}
	
	
	/** public method: set transition/transversion ratio.
	 * @param ratio custom transition/transversion ratio.
	 * */
	public void setRatio(double ratio) {
		ivratio = ratio;
	}

	/** public method: analysis Ka, Ks, and Ka/Ks. */
	public void analysisKaKs(){
		int fasnum = data.keySet().size();
		String[] fasname = new String[fasnum];
		int i = 0;
		for (String key : data.keySet()) {
			fasname[i] = key;
			i++;
		}
		for(i=0;i<fasnum-1;i++){
			for(int j=i+1;j<fasnum;j++){
				String seq1  = data.get(fasname[i]);
				String seq2  = data.get(fasname[j]);
				double[] res = calcKaKs(seq1.toLowerCase().toCharArray(),seq2.toLowerCase().toCharArray());
				if(Ka.containsKey(fasname[i])==false){
					HashMap<String,Double> Ka_1 = new HashMap<String,Double>();
					HashMap<String,Double> Ks_1 = new HashMap<String,Double>();
					HashMap<String,Double> KaKs_1 = new HashMap<String,Double>();
					Ka.put(fasname[i],Ka_1);
					Ks.put(fasname[i],Ks_1);
					KaKs.put(fasname[i],KaKs_1);
				}
				if(Ka.containsKey(fasname[j])==false){
					HashMap<String,Double> Ka_1 = new HashMap<String,Double>();
					HashMap<String,Double> Ks_1 = new HashMap<String,Double>();
					HashMap<String,Double> KaKs_1 = new HashMap<String,Double>();
					Ka.put(fasname[j],Ka_1);
					Ks.put(fasname[j],Ks_1);
					KaKs.put(fasname[j],KaKs_1);
				}
				Ka.get(fasname[i]).put(fasname[j],res[0]);
				Ka.get(fasname[j]).put(fasname[i],res[0]);
				Ks.get(fasname[i]).put(fasname[j],res[1]);
				Ks.get(fasname[j]).put(fasname[i],res[1]);
				KaKs.get(fasname[i]).put(fasname[j],res[2]);
				KaKs.get(fasname[j]).put(fasname[i],res[2]);
			}
		}
	}
	
	/** return Ka */
	public HashMap<String,HashMap<String,Double>> getKa(){return Ka;}

	/** return Ks */
	public HashMap<String,HashMap<String,Double>> getKs(){return Ks;}
	
	/** return Ka/Ks */
	public HashMap<String,HashMap<String,Double>> getKaKs(){return KaKs;}
	
	/** return data(DNA sequences) */
	public HashMap<String,String>  getData(){return data;}

	/** return i/v ratio */
	public double  getRatio(){return ivratio;}
	
	
	public void saveKa(String fpath)throws IOException {saveData(Ka,fpath);}
	
	public void saveKs(String fpath)throws IOException {saveData(Ks,fpath);}
	
	public void saveKaKs(String fpath)throws IOException {saveData(KaKs,fpath);}
	
	/** init data(make it empty) before load new data */
	public void initData() {
		data = new HashMap<String,String>();
		Ka   = new HashMap<String,HashMap<String,Double>>();
		Ks   = new HashMap<String,HashMap<String,Double>>();
		KaKs = new HashMap<String,HashMap<String,Double>>();
	}
	
	/** init gentab(make it empty) before load new genetic codon table */
	public void initGentab() {gentab = new HashMap<Character,HashMap<Character,HashMap<Character,Character>>>();}
	
	/** if data is empty, return false; else, return true. */
	public Boolean isInited() {
		if(data.isEmpty())return false;
		else return true;
	}
	
	/** Constructor of NG object. When call it, it will load genetic codon table first */
	public void NG() throws IOException {loadGenTable();}	
}


