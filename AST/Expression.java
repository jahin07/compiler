package cop5556sp17.AST;


import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	public TypeName val;

	protected Expression(Token firstToken) {
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

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
