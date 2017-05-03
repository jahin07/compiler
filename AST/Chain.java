package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	public TypeName val;
	public boolean left;


	public boolean isLeft() {
		return left;
	}
	public void setLeft(boolean left) {
		this.left = left;
	}
	public Chain(Token firstToken) {
		super(firstToken);
	}
	public TypeName getType()
	{
		return val;
	}
	public void setType(TypeName v)
	{
		this.val = v;
	}

}
