package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		TypeName val1 = (TypeName) binaryChain.getE0().visit(this,arg);
        TypeName val2 = (TypeName) binaryChain.getE1().visit(this,arg);
        Token binaryToken = binaryChain.getE1().getFirstToken();

        switch(binaryChain.getArrow().kind)
        {
        case ARROW:
        {
        	if(val1.equals(URL) && val2.equals(IMAGE))
        		binaryChain.val = val2;

        	else if(val1.equals(FILE) && val2.equals(IMAGE))
        		binaryChain.val = val2;

        	else if(val1.equals(FRAME) && binaryChain.getE1() instanceof FrameOpChain)
        	{
        		if(binaryToken.kind.equals(KW_XLOC) || binaryToken.kind.equals(KW_YLOC))
        			binaryChain.val = INTEGER;

        		else if(binaryToken.kind.equals(KW_SHOW) || binaryToken.kind.equals(KW_HIDE) || binaryToken.kind.equals(KW_MOVE))
        			binaryChain.val = FRAME;

        		else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        	}
        	else if(val1.equals(IMAGE) && binaryChain.getE1() instanceof ImageOpChain)
        	{
        		if(binaryToken.kind.equals(OP_WIDTH) || binaryToken.kind.equals(OP_HEIGHT))
        			binaryChain.val = INTEGER;

        		else if(binaryToken.kind.equals(KW_SCALE))
        			binaryChain.val = IMAGE;

        		else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        	}
        	else if(val1.equals(IMAGE) && val2.equals(FRAME))
        		binaryChain.val = val2;

        	else if(val1.equals(IMAGE) && val2.equals(FILE))
        		binaryChain.val = NONE;

        	else if(val1.equals(IMAGE) && binaryChain.getE1() instanceof IdentChain && val2.equals(IMAGE)) //modify
        		binaryChain.val = IMAGE;

        	else if(val1.equals(INTEGER) && binaryChain.getE1() instanceof IdentChain && val2.equals(INTEGER)) //add new
        		binaryChain.val = INTEGER;

        	else if(val1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        			binaryChain.val = IMAGE;

        		else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        	}

        	else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());

        }
        break;
        case BARARROW:
        {
        	if(val1.equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)
        	{
        		if(binaryToken.kind.equals(OP_GRAY) || binaryToken.kind.equals(OP_BLUR) || binaryToken.kind.equals(OP_CONVOLVE))
        			binaryChain.val = IMAGE;

        		else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        	}
        	else throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        }
        break;
        default:
        	throw new TypeCheckException("Illegal Type " + binaryChain.firstToken.getText() + " at " + binaryChain.getFirstToken().getLinePos());
        }
		return binaryChain.val;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		TypeName val1 = (TypeName) binaryExpression.getE0().visit(this,arg);
        TypeName val2 = (TypeName) binaryExpression.getE1().visit(this,arg);

        switch(binaryExpression.getOp().kind)
        {
        case TIMES:
        {
        	if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.IMAGE))
        		binaryExpression.val = val2;
        	else if(val1.equals(TypeName.IMAGE) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        break;
        case DIV:
        {
        	if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else if(val1.equals(TypeName.IMAGE) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        break;

        case PLUS:
        case MINUS:
        {
        	if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
     	   else if(val1.equals(TypeName.IMAGE) && val2.equals(TypeName.IMAGE))
     		  binaryExpression.val = val1;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        break;

        case LT:
        case GT:
        case LE:
        case GE:
        {
        	if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = TypeName.BOOLEAN;
        	else if(val1.equals(TypeName.BOOLEAN) && val2.equals(TypeName.BOOLEAN))
        		binaryExpression.val = val1;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        break;
        case EQUAL:
        case NOTEQUAL:
        {
        	if(val1.equals(val2))
        		binaryExpression.val = TypeName.BOOLEAN;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        case AND:
        case OR:
        {
        	if(val1.equals(val2))
        		binaryExpression.val = TypeName.BOOLEAN;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());

        }
        break;
        case MOD:
        {
        	if(val1.equals(TypeName.INTEGER) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else if(val1.equals(TypeName.IMAGE) && val2.equals(TypeName.INTEGER))
        		binaryExpression.val = val1;
        	else
        		throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        break;
        default:
        	throw new TypeCheckException("Illegal Type " + binaryExpression.firstToken.getText() + " at " + binaryExpression.getFirstToken().getLinePos());
        }
        return binaryExpression.val;


	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception
	{
		symtab.enterScope();
		for(Dec d : block.getDecs())
		{
			d.visit(this, null);
		}
		for(Statement s: block.getStatements())
		{
			s.visit(this, null);
		}
		symtab.leaveScope();

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		booleanLitExpression.val = BOOLEAN;
		return booleanLitExpression.val;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception
	{
		filterOpChain.getArg().visit(this, null);
		if(filterOpChain.getArg().getExprList().size()==0)
			filterOpChain.val = IMAGE;
		else
			throw new TypeCheckException("Illegal Type " + filterOpChain.firstToken.getText() + " at " + filterOpChain.firstToken.getLinePos());

		return filterOpChain.val;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception
	{
		frameOpChain.getArg().visit(this, null);
		Kind frameOp = frameOpChain.getFirstToken().kind;
		if(frameOp.equals(KW_SHOW) || frameOp.equals(KW_HIDE))
		{
			if(frameOpChain.getArg().getExprList().size()==0)
				frameOpChain.val = NONE;
			else throw new TypeCheckException("Illegal Type " + frameOpChain.firstToken.getText() + " at " + frameOpChain.firstToken.getLinePos());
		}
		else if(frameOp.equals(KW_XLOC) || frameOp.equals(KW_YLOC))
		{
			if(frameOpChain.getArg().getExprList().size()==0)
				frameOpChain.val = INTEGER;
			else throw new TypeCheckException("Illegal Type " + frameOpChain.firstToken.getText() + " at " + frameOpChain.firstToken.getLinePos());
		}
		else if(frameOp.equals(KW_MOVE))
		{
			if(frameOpChain.getArg().getExprList().size()==2)
				frameOpChain.val = NONE;
			else throw new TypeCheckException("Illegal Type " + frameOpChain.firstToken.getText() + " at " + frameOpChain.firstToken.getLinePos());
		}
		else
			throw new TypeCheckException("Illegal Type " + frameOpChain.firstToken.getText() + " at " + frameOpChain.firstToken.getLinePos());

		return frameOpChain.val;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception
	{
		Dec d = symtab.lookup(identChain.firstToken.getText());
		if( d != null)
		{
			identChain.setType(d.getVal());
			identChain.setDec(d);
		}
		else
			throw new TypeCheckException(identChain.firstToken.getLinePos().toString());

		return identChain.val;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception
	{
		Dec ident = symtab.lookup(identExpression.firstToken.getText());
		if(ident != null)
		{
			identExpression.val = ident.val;
			identExpression.dec = ident;
		}
		return identExpression.val;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception
	{
		if(!ifStatement.getE().visit(this,arg).equals(TypeName.BOOLEAN))
			throw new TypeCheckException("Illegal Type " + ifStatement.firstToken.getText() + " at " + ifStatement.firstToken.getLinePos());

		ifStatement.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception
	{
		intLitExpression.val = INTEGER;
		return intLitExpression.val;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception
	{

		if(!sleepStatement.getE().visit(this,arg).equals(INTEGER))
			throw new TypeCheckException("Illegal Type " + sleepStatement.firstToken.getText() + " at " + sleepStatement.firstToken.getLinePos());

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception
	{

		if(!whileStatement.getE().visit(this,arg).equals(BOOLEAN))
			throw new TypeCheckException("Illegal Type " + whileStatement.firstToken.getText() + " at " + whileStatement.firstToken.getLinePos());

		whileStatement.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception
	{
		declaration.val = Type.getTypeName(declaration.getFirstToken());
		Boolean check = symtab.insert(declaration.getIdent().getText(), declaration);
		if(!check)
			throw new TypeCheckException("Illegal Type " + declaration.firstToken.getText() + " at " + declaration.firstToken.getLinePos());

		return declaration.val;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception
	{

		for(ParamDec pd: program.getParams())
		{
			pd.visit(this, null);
		}
		program.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception
	{
		if(!assignStatement.getVar().visit(this, null).equals(assignStatement.getE().visit(this,arg)))
			throw new TypeCheckException("Illegal Type " + assignStatement.firstToken.getText() + " at " + assignStatement.firstToken.getLinePos());
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception
	{
		Dec ident = symtab.lookup(identX.firstToken.getText());
		if(ident != null)
			identX.dec = ident;
		else
			throw new TypeCheckException("Illegal Type " + identX.firstToken.getText() + " at " + identX.firstToken.getLinePos());
		return identX.getDec().val;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception
	{
		paramDec.val = Type.getTypeName(paramDec.getFirstToken());
		Boolean check = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(!check)
			throw new TypeCheckException("Illegal Type " + paramDec.firstToken.getText() + " at " + paramDec.firstToken.getLinePos());
		return paramDec.val;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		constantExpression.val = INTEGER;
		return constantExpression.val;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception
	{
		imageOpChain.getArg().visit(this, null);
		Kind imageOp = imageOpChain.getFirstToken().kind;
		if(imageOp.equals(OP_WIDTH) || imageOp.equals(OP_HEIGHT))
		{
			if(imageOpChain.getArg().getExprList().size()==0)
				imageOpChain.val = INTEGER;
			else throw new TypeCheckException("Illegal Type " + imageOpChain.firstToken.getText() + " at " + imageOpChain.firstToken.getLinePos());
		}
		else if(imageOp.equals(KW_SCALE))
		{
			if(imageOpChain.getArg().getExprList().size()==1)
				imageOpChain.val = IMAGE;
			else throw new TypeCheckException("Illegal Type " + imageOpChain.firstToken.getText() + " at " + imageOpChain.firstToken.getLinePos());
		}
		else
			throw new TypeCheckException("Illegal Type " + imageOpChain.firstToken.getText() + " at " + imageOpChain.firstToken.getLinePos());

		return imageOpChain.val;

	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception
	{

		for(Expression e: tuple.getExprList())
		{
			if(!e.visit(this,arg).equals(INTEGER))
				throw new TypeCheckException("Illegal Type " + tuple.firstToken.getText() + " at " + tuple.firstToken.getLinePos());
		}

		return null;
	}


}
