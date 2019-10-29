package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	boolean listing = false;
    	int start = 0;
    	//System.out.println(expr);
    	for(int i=0; i<expr.length(); i++) {
    		//If a letter has been detected, begin listing
    		if(listing) {
    			//Continue listing if another letter is found
    			if(Character.isLetter(expr.charAt(i))) {
        			listing = true;
        		}
    			//Begin to add variable/array to lists
    			else {
    				//Adds an array to arrays list
    				if(expr.charAt(i)=='[') {
    					String s = expr.substring(start,i);
    					for(int j=0; j<arrays.size();j++) {
    						//If array name is already in list, do nothing
    						if(arrays.get(j).name.equals(s)) {
    							listing = false;
    						}
    					}
    					if(listing)
    						arrays.add( new Array(s) );
    					listing = false;
    				}
    				//Adds a variable to vars list
    				else {
    					String s = expr.substring(start, i);
    					for(int j=0; j<vars.size();j++) {
    						//If variable name is already in list, do nothing
    						if(vars.get(j).name.equals(s)) {
    							listing = false;
    						}
    					}
    					if(listing)
    						vars.add( new Variable(s) );
    					listing = false;
    				}
    			}
    		}
    		//If not listing and a character is detected, begin listing
    		else {
	    		if(Character.isLetter(expr.charAt(i))) {
	    			listing = true;
	    			start = i;
	    		}
    		}
    	}
		//Last character of String check
    	if(listing) {
			String s = expr.substring(start, expr.length());
			for(int j=0; j<vars.size();j++) {
				if(vars.get(j).name.equals(s)) {
					listing = false;
				}
			}
			if(listing)
				vars.add(new Variable(s));
			listing = false;
		}
    	
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	
    	//System.out.println(expr);
    	//Get Rid Of Spaces
    	if(expr.contains(" ")) {
    		while(expr.contains(" ")) {
    			expr = expr.substring(0,expr.indexOf(" "))+expr.substring(expr.indexOf(" ")+1,expr.length());
    		}
    	}
    	//Get Rid of Tabs
    	if(expr.contains("\t")) {
    		while(expr.contains("\t")){
    			expr = expr.substring(0,expr.indexOf("\t"))+expr.substring(expr.indexOf("\t")+1,expr.length());
    		}
    	}
    	//Implement Variables
    	for(Variable vari : vars) {
    		expr = expr.replaceAll((vari.name),String.valueOf(vari.value));
    	}
    	//Solve Brackets
    	if(expr.contains("[")) {
    		int parenDex = 0;
			for(int i=expr.indexOf("[");i<expr.indexOf("]");i++) {
				if(expr.charAt(i)=='[') {parenDex=i;}
			}
			//System.out.println(parenDex+1);
			//System.out.println(expr.indexOf("]"));
    		String newExpr = expr.substring(parenDex+1,expr.indexOf("]"));
    		if(!(newExpr.length()==1)) {
        		newExpr = String.valueOf(evaluate(newExpr,vars,arrays));
        		newExpr = String.valueOf(Math.round(Float.parseFloat(newExpr)));
        		expr = expr.substring(0,parenDex+1)+newExpr+expr.substring(expr.indexOf("]"),expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));
    		}

    	}
    	//Implement Arrays
    	for(Array arr : arrays) {
    		if(expr.contains(arr.name)) {
				String start = expr.substring(expr.indexOf(arr.name+"[")+arr.name.length()+1,expr.length());
				start = start.substring(0,start.indexOf("]"));
				int index = (int)evaluate(start,vars,arrays);
				int[] veve = arr.values;
				expr = expr.substring(0,expr.indexOf(arr.name))+String.valueOf(veve[index])+expr.substring(expr.indexOf("]")+1,expr.length());
    		}
    	}
    	//Stop if number is negative
    	if(!expr.contains("*")&&!expr.contains("/")&&!expr.contains("+")) {
    		if(expr.lastIndexOf("-")==0||!expr.contains("-")) {
    			return Float.parseFloat(expr);
    		}
    	}
    	//Solve Parenthesis
    	if(expr.contains("(")) {
    		int parenDex = 0;
			for(int i=expr.indexOf("(");i<expr.indexOf(")");i++) {
				if(expr.charAt(i)=='(') {parenDex=i;}
			}
    		String newExpr = expr.substring(parenDex+1,expr.indexOf(")"));
    		newExpr = String.valueOf(evaluate(newExpr,vars,arrays));
    		expr = expr.substring(0,parenDex)+newExpr+expr.substring(expr.indexOf(")")+1,expr.length());
    		expr = String.valueOf(evaluate(expr,vars,arrays));
    	}
    	//Safely Add-Subtract Negative Numbers
    	if(expr.contains("+-")) {
    		expr = expr.replaceAll("\\+-", "-");
    	}
    	if(expr.contains("--")) {
    		expr = expr.replaceAll("\\--", "+");
    	}
    	//Solve Multiply/Divide + Add/Subtract
    	if(expr.contains("/") || expr.contains("*") || expr.contains("+") || ((expr.contains("-")) && (expr.lastIndexOf("-")!=0) )) {
    		int x=0; int y=0;
    		if(expr.contains("/-")) {
    			x=expr.indexOf("/-")-1;
    			y=expr.indexOf("/-")+2;
    		}
    		else if (expr.contains("*-")){
    			x=expr.indexOf("*-")-1;
    			y=expr.indexOf("*-")+2;
    		}
    		else if(expr.contains("/")) {
    			x=expr.indexOf("/")-1;
    			y=expr.indexOf("/")+1;
    		}
    		else if (expr.contains("*")){
    			x=expr.indexOf("*")-1;
    			y=expr.indexOf("*")+1;
    		}
    		else if( (expr.contains("-")) &&(expr.indexOf("-")==0) && (expr.lastIndexOf("-")!=0) ) {
    			x=expr.indexOf("-")-1;
    			String s = expr.substring(1,expr.length());
    			y=s.indexOf("-")+2;
    		}
    		else if( (expr.contains("-")) &&(expr.lastIndexOf("-")!=0)) {
    			x=expr.indexOf("-")-1;
    			y=expr.indexOf("-")+1;
    		}
    		else {
    			x=expr.indexOf("+")-1;
    			y=expr.indexOf("+")+1;
    		}
    		
    		boolean inBounds = true;
    		while(inBounds) {
    			if(x<0) {break;}
    			if(Character.isDigit(expr.charAt(x))||(expr.charAt(x)=='.')) {x--;}
    			else if((expr.charAt(x)=='-')&&(x==0)) {x--;}
    			else {break;}
    		}
    		x++;
    		
    		while(inBounds) {
    			if(y>expr.length()-1) {break;}
    			if(Character.isDigit(expr.charAt(y))||(expr.charAt(y)=='.')) {y++;}
    			else {break;}
    		}
    		y--;
    		
    		if(expr.contains("/-")) {
        		float a =Float.parseFloat(expr.substring(x,expr.indexOf("/")));
        		float b =Float.parseFloat(expr.substring(expr.indexOf("/")+2,y+1));
        		expr = expr.substring(0,x)+String.valueOf(a/b*-1)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));	
    		}
    		else if(expr.contains("*-")){
        		float a =Float.parseFloat(expr.substring(x,expr.indexOf("*")));
        		float b =Float.parseFloat(expr.substring(expr.indexOf("*")+2,y+1));
        		expr = expr.substring(0,x)+String.valueOf(a*b*-1)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));
    		}   		
    		else if(expr.contains("/")) {
        		float a =Float.parseFloat(expr.substring(x,expr.indexOf("/")));
        		float b =Float.parseFloat(expr.substring(expr.indexOf("/")+1,y+1));
        		expr = expr.substring(0,x)+String.valueOf(a/b)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));	
    		}
    		else if(expr.contains("*")){
        		float a =Float.parseFloat(expr.substring(x,expr.indexOf("*")));
        		float b =Float.parseFloat(expr.substring(expr.indexOf("*")+1,y+1));
        		expr = expr.substring(0,x)+String.valueOf(a*b)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));
    		}
    		else if( (expr.contains("-")) &&(expr.lastIndexOf("-")!=0)) {
    			float a =0; float b=0;    			
    			if(expr.indexOf("-")==0) {
        			String s = expr.substring(1,expr.length());
	        		a =Float.parseFloat(expr.substring(x,s.indexOf("-")+1));
	        		b =Float.parseFloat(expr.substring(s.indexOf("-")+2,y+1));
    			}
    			else {
            		a =Float.parseFloat(expr.substring(x,expr.indexOf("-")));
            		b =Float.parseFloat(expr.substring(expr.indexOf("-")+1,y+1));
    			}
        		expr = expr.substring(0,x)+String.valueOf(a-b)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));
    		}
    		else {
        		float a =Float.parseFloat(expr.substring(x,expr.indexOf("+")));
        		float b =Float.parseFloat(expr.substring(expr.indexOf("+")+1,y+1));
        		expr = expr.substring(0,x)+String.valueOf(a+b)+expr.substring(y+1,expr.length());
        		expr = String.valueOf(evaluate(expr,vars,arrays));
    		}
    		
    	}
    	//Output result
    	return Float.parseFloat(expr);
    }
}