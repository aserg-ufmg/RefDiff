package lsdSimplified;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import lsd.rule.LSDPredicate;
import metapackage.MetaInfo;


public class Converter {
	//This class was in use to change facts to class level and save them in a file, 
	//Now this is done in the memory
	//No need to build class level file at the beginning
	
	static String tempRes = MetaInfo.srcDir + "temp.rub";

	public static File convertDeltaFacts(File src) {
		File res = new File(tempRes); 
		String line = null; //not declared within while loop
		BufferedWriter writer = null;
		 try {
			BufferedReader input =  new BufferedReader(new FileReader(src));
			writer = new BufferedWriter(new FileWriter(res));
	        while (( line = input.readLine()) != null){
	          if (line.contains("//") || line.contains("include") || line.length()==0)
	          {
	        	  writer.write(line);
	        	  writer.newLine();
	          }
	          else{
	        	  String pred = line.substring(0,line.indexOf("("));
	        	  String arg = line.substring(line.indexOf("(")+1,line.lastIndexOf(")"));
	        	  if (shouldChange(pred))
	        	  {
	        		  writer.write(changeArguments(pred,arg));
	        		  writer.newLine();
	        	  }
	        	  else 
	        	  {
	        		  writer.write(line);
	        		  writer.newLine();
	        	  }
	          }
	        	
	        }
	        writer.close();  
	        return res;
	      }catch (Exception e) {
			System.err.println("error" + line);
			try {
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
			return null;
		}
	}
	
	private static boolean shouldChange(String predicateName) {
		LSDPredicate predicate = LSDPredicate.getPredicate(predicateName);
		if (predicate.isMethodLevel())
			return true;
		return false;
	}

	public static String changeArguments(String predicateName, String arguments) {
		
		StringTokenizer tokenizer = new StringTokenizer(arguments,",",false);
		String arg0 = tokenizer.nextToken(),arg1,arg2;
		try{
		if (predicateName.contains("typeintype") || predicateName.contains("accesses"))
			arg0 = tokenizer.nextToken();
		if (predicateName.contains("inherited"))
		{
			arg0 = tokenizer.nextToken();
			arg0 = tokenizer.nextToken();
		}
		if (arg0.indexOf("#") != -1)
			arg0 = arg0.substring(1,arg0.indexOf("#"));
		else if (arg0.indexOf("\"") != -1)
			arg0 = arg0.substring(1,arg0.lastIndexOf("\""));
		arg1 = arg0.substring(arg0.indexOf("%.") + 2);
		if (arg0.indexOf("%") == 0)
			arg2="null";
		else
			arg2 = arg0.substring(0,arg0.indexOf("%."));
		if (arg2 == null || arg2.length()==0) 
			arg2 = "null";
		arg0 = "changed_type(\""+arg0+"\",\""+arg1+"\",\""+arg2+"\").";
		return arg0;
		}
		catch (Exception e) {
			arg0 = predicateName+"(\""+ arguments+").";
			System.out.println(arg0);
			return arg0;
		}
	}

	public static void clear() {
		File res = new File(tempRes);
		if (res != null)
			res.delete();
		
	}

}
