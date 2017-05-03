package cop5556sp17;
import java.util.*;


import cop5556sp17.AST.Dec;


public class SymbolTable 
{
	
	int  current_scope, next_scope;
	Stack <Integer>scope_stack = new Stack<Integer>();
	HashMap <String, ArrayList<Pair>> hm = new HashMap <String, ArrayList<Pair>>();
    

	/** 
	 * to be called when block entered
	 */
	public void enterScope()
	{
		current_scope = next_scope++; 
		scope_stack.push(current_scope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope()
	{
		
		scope_stack.pop();
		current_scope = scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec)
	{
		ArrayList<Pair> ps = new ArrayList<Pair>();
		Pair p = new Pair(current_scope, dec);
		if(hm.containsKey(ident))
		{
			ps = hm.get(ident);
			for(Pair it: ps)
			{
				if(it.getScope()==current_scope)
					return false;
			}
		}
		ps.add(p);
		hm.put(ident, ps);		
		return true;
	}
	
	public Dec lookup(String ident)
	{
		if(!hm.containsKey(ident))
			return null;
		
		Dec dec=null;
		ArrayList<Pair> ps = hm.get(ident);
		for(int i=ps.size()-1;i>=0;i--)
		{
			int temp_scope = ps.get(i).getScope();
			if(scope_stack.contains(temp_scope))
			{
				dec = ps.get(i).getDec();
				break;
			}
		}
		return dec;
	}
		
	public SymbolTable() 
	{
		this.current_scope = 0;
		this.next_scope = 1;
		scope_stack.push(0);
	}


	@Override
	public String toString() 
	{
		return this.toString();
	}
	
	public class Pair 
	{

		  int scope;
		  Dec dec;
		  public Pair(int s, Dec d)
		  {
			  this.scope = s;
			  this.dec = d;
		  }
		  public int getScope()
		  {
			  return scope;
		  }
		  public Dec getDec()
		  {
			  return dec;
		  }
	}
}
