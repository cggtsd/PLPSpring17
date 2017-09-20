package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	
	//TODO  add fields
	private int  current_scope, next_scope;
	private Map<String, ArrayList<Dec>> symbolTable;
	private Map<String, ArrayList<Integer>> scopeTable;
	private Stack<Integer> scope_stack;
	
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		next_scope++;
		current_scope = next_scope;
		scope_stack.push(current_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scope_stack.pop();
		if(!scope_stack.empty())
			current_scope = scope_stack.peek();
	}
	
	public boolean insert(String id, Dec dec){
		//TODO:  IMPLEMENT THIS
//		if(inCurrentScope(id)){
//			return false;
//		}
		
		ArrayList<Dec> decList = new ArrayList<Dec>();
		ArrayList<Integer> scopeList = new ArrayList<Integer>();

		if(!(symbolTable.get(id) == null)){
			decList = symbolTable.get(id);	
			scopeList = scopeTable.get(id);
			for(int scope: scopeList) {
				if (scope == current_scope) {
					return false;
				}
			}
		}

		decList.add(dec);
		scopeList.add(current_scope);
		
		symbolTable.put(id, decList);	
		scopeTable.put(id, scopeList);	

		return true;
	}
/*	
	public boolean exists(String id) {
		if(scopeTable.get(id) == null)
			return false;
		return true;
	}

	public boolean inCurrentScope(String id) {
		// TODO Auto-generated method stub
		if(scopeTable.get(id) == null)
			return false;
		
		ArrayList<Integer> idScopeList = scopeTable.get(id);
			for(int i = scope_stack.size() -1; i >= 0; i--) {
				for(int j = idScopeList.size() -1; j >= 0; j--){
				if(idScopeList.get(j) == scope_stack.get(i))
					return true;
			}		
			}
		return false;
	}

*/
	public Dec lookup(String id){
		//TODO:  IMPLEMENT THIS
		if(symbolTable.isEmpty())
			return null;

		ArrayList<Dec> decList = symbolTable.get(id);
		ArrayList<Integer> scopeList = scopeTable.get(id);

		if(scopeList == null || decList == null)
			return null;
		
		for(int i = scope_stack.size()-1; i >= 0; i--){
			for(int j = scopeList.size() - 1; j >= 0; j--){
				if(scopeList.get(j) == scope_stack.get(i)){
					return decList.get(j);
				}
			}
		}	
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		symbolTable = new HashMap<String, ArrayList<Dec>>();
		scopeTable = new HashMap<String, ArrayList<Integer>>();
		scope_stack = new Stack<Integer>();
		current_scope = 0;
		next_scope = 0;
		scope_stack.push(current_scope);
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		String res = "";
		for (int i = scope_stack.size() - 1, j = 0; i >= 0; i--, j++) {
		    res += "Scope " + j + ": " + scope_stack.elementAt(i) + "\n";
		}
		return res;
	}
}
