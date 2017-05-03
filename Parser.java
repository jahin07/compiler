package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import java.util.*;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser 
{

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception 
	{
		public SyntaxException(String message) 
		{
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException 
	{
		public UnimplementedFeatureException() 
		{
			super();
		}
	}

	Scanner scanner;
	Token t;
	Kind op;

	Parser(Scanner scanner) 
	{
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException 
	{
		Program p = null;
		p = program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException 
	{
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token first = t;
		e0 = term();
		while(t.kind.equals(LT) || t.kind.equals(LE) || t.kind.equals(GT) || t.kind.equals(GE) || t.kind.equals(EQUAL) || t.kind.equals(NOTEQUAL))
		{
			Token op = t;
			relOp();
			e1 = term();
			e0 = new BinaryExpression(first,e0,op,e1);
		}
		return e0;
		
	}
	
	void relOp() throws SyntaxException
	{
		match(LT,LE,GT,GE,EQUAL,NOTEQUAL);
	}

	Expression term() throws SyntaxException 
	{
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token first = t;
		e0 = elem();
		
		while(t.kind.equals(PLUS) || t.kind.equals(MINUS) || t.kind.equals(OR))
		{
			Token op = t;
			weakOp();
			e1 = elem();
			e0 = new BinaryExpression(first,e0,op,e1);	
		}
		return e0;
	}
	
	void weakOp() throws SyntaxException
	{	
		match(PLUS,MINUS,OR);
	}

	Expression elem() throws SyntaxException 
	{
		//TODO
		Expression e0 = null;
		Expression e1 = null;
		Token first = t;
		e0 = factor();
		while(t.kind.equals(TIMES) || t.kind.equals(DIV) || t.kind.equals(AND) || t.kind.equals(MOD))
		{
			Token op = t;
			strongOp();
			e1 = factor();
			e0 = new BinaryExpression(first,e0,op,e1);		
		}
		return e0;
	}
	
	void strongOp() throws SyntaxException
	{
		
		match(TIMES,DIV,AND,MOD);
	}

	Expression factor() throws SyntaxException 
	{
		Kind kind = t.kind;
		Expression e = null;
		switch (kind) 
		{
		case IDENT: 
		{
			e = new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: 
		{
			e = new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		{
			e = new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_FALSE: 
		{
			e = new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		{
			e = new ConstantExpression(t);
			consume();
		}
			break;
		case KW_SCREENHEIGHT: 
		{
			e = new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: 
		{
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
					+ " and position "+t.getLinePos().posInLine);
		}
		return e;
	}

	Block block() throws SyntaxException 
	{
		//TODO
		Block b = null;
		ArrayList<Dec> d = new ArrayList<Dec>();
		ArrayList<Statement> s = new ArrayList<Statement>();
		Token op = null;
		op = t;
		match(LBRACE);
		while(t.kind.equals(KW_INTEGER) || t.kind.equals(KW_BOOLEAN) || t.kind.equals(KW_IMAGE) || t.kind.equals(KW_FRAME) //dec
				|| t.kind.equals(OP_SLEEP) || t.kind.equals(KW_WHILE) || t.kind.equals(KW_IF) || t.kind.equals(IDENT) //statement
				|| t.kind.equals(OP_BLUR) || t.kind.equals(OP_GRAY) || t.kind.equals(OP_CONVOLVE)
				|| t.kind.equals(KW_SHOW) || t.kind.equals(KW_HIDE) || t.kind.equals(KW_MOVE) || t.kind.equals(KW_XLOC) || t.kind.equals(KW_YLOC)
				|| t.kind.equals(OP_WIDTH) || t.kind.equals(OP_HEIGHT) || t.kind.equals(KW_SCALE))
		{
			if(t.kind.equals(KW_INTEGER) || t.kind.equals(KW_BOOLEAN) || t.kind.equals(KW_IMAGE) || t.kind.equals(KW_FRAME)) //dec
			{
				d.add(dec());
			}
			else
			{
				s.add(statement());
			}
		}
		
		match(RBRACE);
		b = new Block(op,d,s);
		return b;
	}

	Program program() throws SyntaxException 
	{
		//TODO
		Program p = null;
		ArrayList<ParamDec> pd = new ArrayList<ParamDec>();
		Block b = null;
		Token op = null;
		op = t;
		match(IDENT);
		if(t.kind.equals(LBRACE))
		{
			b = block();
		}
		else if(t.kind.equals(KW_URL) || t.kind.equals(KW_FILE) || t.kind.equals(KW_INTEGER) || t.kind.equals(KW_BOOLEAN))
		{
			pd.add(paramDec());
			while(t.kind.equals(COMMA))
			{
				consume();
				pd.add(paramDec());
			}
			b = block();
		}
		else
			throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
					+ " and position "+t.getLinePos().posInLine);	
		p = new Program(op,pd,b);
		
		return p;
	}

	ParamDec paramDec() throws SyntaxException 
	{
		//TODO
		ParamDec e = null;
		if(t.kind.equals(KW_URL) || t.kind.equals(KW_FILE) || t.kind.equals(KW_INTEGER) || t.kind.equals(KW_BOOLEAN))
		{
			Token type = t;
			consume();
			e = new ParamDec(type,t);
			match(IDENT);
		}
		else
			throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
					+ " and position "+t.getLinePos().posInLine);
		
		return e;
	}

	Dec dec() throws SyntaxException 
	{
		//TODO
		Dec e = null;
		if(t.kind.equals(KW_INTEGER) || t.kind.equals(KW_BOOLEAN) || t.kind.equals(KW_IMAGE) || t.kind.equals(KW_FRAME))
		{
			Token type = t;
			consume();
			e = new Dec(type,t);
			match(IDENT);
		}
		else
			throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
					+ " and position "+t.getLinePos().posInLine);
		
		return e;
	}

	Statement statement() throws SyntaxException 
	{
		//TODO
		Statement s = null;
		Expression e = null;
		Token dt = null;
		if(t.kind.equals(OP_SLEEP))
		{
			dt = t;
			consume();
			e = expression();
			s = new SleepStatement(dt,e);
			match(SEMI);
		}
		else if(t.kind.equals(KW_WHILE))
		{
			
			s = whileStatement();
		}
		else if(t.kind.equals(KW_IF))
		{
			s = ifStatement();
		}
		else if(t.kind.equals(IDENT))
		{
			Scanner.Token tok = scanner.peek();
			if(tok.kind.equals(ASSIGN))
			{
					
				s = assign();
				match(SEMI);
			}
			else if(tok.kind.equals(ARROW) || tok.kind.equals(BARARROW))
			{
				s = chain();
				match(SEMI);
			}
			else
			{
				consume();
				throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
						+ " and position "+t.getLinePos().posInLine);
			}
		}
		else if(t.kind.equals(OP_BLUR) || t.kind.equals(OP_GRAY) || t.kind.equals(OP_CONVOLVE) 
				|| t.kind.equals(KW_SHOW) || t.kind.equals(KW_HIDE) || t.kind.equals(KW_MOVE) || t.kind.equals(KW_XLOC) || t.kind.equals(KW_YLOC)
				|| t.kind.equals(OP_WIDTH) || t.kind.equals(OP_HEIGHT) || t.kind.equals(KW_SCALE))
				{
					s = chain();
					match(SEMI);
				}
		else
			throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line 
					+ " and position "+t.getLinePos().posInLine);
		
		return s;
	}
	
	Statement whileStatement() throws SyntaxException
	{
		Token op = null;
		Expression e = null;
		Block b = null;
		Statement s = null;
		if (t.kind.equals(KW_WHILE)) 
		{
			op = t;
			consume();
			match(LPAREN);
			e = expression();
			match(RPAREN);
			b = block();
			s = new WhileStatement(op,e,b);
		} 
		else
			throw new SyntaxException("illegal token " + t.kind.toString() + " at line " + t.getLinePos().line
					+ " and position " + t.getLinePos().posInLine);
		return s;
		
	}
	
	Statement ifStatement() throws SyntaxException
	{
		Token op = null;
		Expression e = null;
		Block b = null;
		Statement s = null;
		if (t.kind.equals(KW_IF)) 
		{
			op = t;
			consume();
			match(LPAREN);
			e = expression();
			match(RPAREN);
			b = block();
			s = new IfStatement(op,e,b);
		} 
		else
			throw new SyntaxException("illegal token " + t.kind.toString() + " at line " + t.getLinePos().line
					+ " and position " + t.getLinePos().posInLine);
		
		return s;
		
	}
	
	AssignmentStatement assign() throws SyntaxException
	{
		Token op = t;
		IdentLValue il = new IdentLValue(t);
		AssignmentStatement as = null;
		Expression e = null;
		match(IDENT);
		match(ASSIGN);
		e = expression();
		as = new AssignmentStatement(op,il,e);
		return as;
		
	}

	Chain chain() throws SyntaxException 
	{
		// TODO
		Chain c = null;
		ChainElem ce = null;
		Token op = null, arrow = null;
		op = t;
		c = chainElem();
		arrow = t;
		arrowOp();
		
		ce = chainElem();
		c = new BinaryChain(op,c,arrow,ce);
		while (t.kind.equals(BARARROW) || t.kind.equals(ARROW)) 
		{
			arrow = t;
			arrowOp();
			ce = chainElem();
			c = new BinaryChain(op,c,arrow,ce);
		}
		
		return c;
		
	}
	
	void arrowOp() throws SyntaxException
	{
		match(ARROW,BARARROW);
	}

	ChainElem chainElem() throws SyntaxException 
	{
		// TODO
		ChainElem ce = null;
		Token op = null;
		Tuple tup = null;
		if (t.kind.equals(IDENT)) 
		{
			ce = new IdentChain(t);
			consume();
		} 
		else if (t.kind.equals(OP_BLUR) || t.kind.equals(OP_GRAY) || t.kind.equals(OP_CONVOLVE)) 
		{
			op = t;
			consume();
			tup = arg();
			ce = new FilterOpChain(op,tup);
		} 
		else if (t.kind.equals(KW_SHOW) || t.kind.equals(KW_HIDE) || t.kind.equals(KW_MOVE) || t.kind.equals(KW_XLOC)
				|| t.kind.equals(KW_YLOC)) 
		{
			op = t;
			consume();
			tup = arg();
			ce = new FrameOpChain(op,tup);
		} 
		else if (t.kind.equals(OP_WIDTH) || t.kind.equals(OP_HEIGHT) || t.kind.equals(KW_SCALE)) 
		{
			op = t;
			consume();
			tup = arg();
			ce = new ImageOpChain(op,tup);
		} 
		else
			throw new SyntaxException("illegal token " + t.kind.toString() + " at line " + t.getLinePos().line
					+ " and position " + t.getLinePos().posInLine);
		
		return ce;

	}
	
	void filterOp() throws SyntaxException
	{
		
		match(OP_BLUR, OP_GRAY, OP_CONVOLVE);
		
	}
	
	void frameOp() throws SyntaxException
	{
		
		match(KW_SHOW, KW_HIDE, KW_MOVE, KW_XLOC, KW_YLOC);

	}
	
	void imageOp() throws SyntaxException
	{
		match(OP_WIDTH,OP_HEIGHT,KW_SCALE);
		
	}

	Tuple arg() throws SyntaxException 
	{
		// TODO
		Tuple tup = null;
		Expression e = null;
		Token op = t;
		ArrayList<Expression> al = new ArrayList<Expression>();
		if (t.kind.equals(LPAREN)) 
		{
			consume();
			e = expression();
			al.add(e);
			while (t.kind.equals(COMMA)) 
			{
				consume();
				e = expression();
				al.add(e);
			}
			match(RPAREN);
		}
		tup = new Tuple(op,al);
		return tup;

	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException 
	{
		if (t.kind.equals(EOF)) 
		{
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException 
	{
		if (t.kind.equals(kind)) 
		{
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + " expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	//@SuppressWarnings("unused")
	private Token match(Kind... kinds) throws SyntaxException 
	{
		// TODO. Optional but handy
		for(Kind i:kinds)
		{
			if(t.kind.equals(i))
				return consume();			
		}
		throw new SyntaxException("illegal token "+t.kind.toString()+" at line "+t.getLinePos().line + " and position "+t.getLinePos().posInLine);
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException 
	{
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
