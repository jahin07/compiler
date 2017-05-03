package cop5556sp17;

import java.util.*;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	
	public int skipWhiteSpace(int pos, int length)
	{
		while(Character.isWhitespace(chars.charAt(pos)))
		{
			
			if(chars.charAt(pos)==10) //Whenever a newline is encountered, the code will not increment the pos
				return pos;
			else
			{
				pos++;
				if(pos==length) //To prevent OutOfBounds Exception, if the input contains paces at the end
					return pos;
			}
		}
			
		return pos;
	}
	//Creating states for DFA
	public static enum State {
		START, AFTER_EQ, IN_DIGIT, IN_IDENT, AFTER_OR, AFTER_NOT, AFTER_LESS, AFTER_GREAT, AFTER_SLASH, AFTER_MINUS, AFTER_STAR, AFTER_COMMENT;
	}
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public HashMap<String, Kind> resWords = new HashMap<String, Kind>();
	public ArrayList<Integer> linePosStart = new ArrayList<Integer>();
	StringBuilder dict = new StringBuilder();
	
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}		
	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() 
		{
			//TODO IMPLEMENT THIS
			if(chars.substring(pos,pos+length).length() >0)
				return chars.substring(pos,pos+length); //Returning the text containing the token from the non-empty input string
			return null;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos()
		{
			//TODO IMPLEMENT THIS
			int arrayIndex;
			arrayIndex = Collections.binarySearch(linePosStart, pos); //Returns the index of pos if present else returns (-insertion_point - 1)
			int arrayLine;
			arrayLine = arrayIndex < 0 ? Math.abs(arrayIndex+2): arrayIndex; //Converting to positive index if pos is not present in the array
			
			LinePos lp = new LinePos(arrayLine, (pos - linePosStart.get(arrayLine)-1));
			
			return lp;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException
		{
			//TODO IMPLEMENT THIS
			
			return Integer.parseInt(chars.substring(pos,pos+length));
			
			//return 0;
		}
		
		@Override
		public int hashCode() 
		{
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj) 
			{
				return true;
			}
			if (obj == null) 
			{
				return false;
			}
			if (!(obj instanceof Token)) 
			{
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) 
			{
				return false;
			}
			if (kind != other.kind) 
			{
				return false;
			}
			if (length != other.length) 
			{
				return false;
			}
			if (pos != other.pos) 
			{
				return false;
			}
			return true;
		}

		private Scanner getOuterType() 
		{
			return Scanner.this;
		}
		
	}


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		//Adding the reserved words to a dictionary (HashMap)
		resWords.put("integer", Kind.KW_INTEGER);
		resWords.put("boolean", Kind.KW_BOOLEAN);
		resWords.put("image", Kind.KW_IMAGE);
		resWords.put("url", Kind.KW_URL);
		resWords.put("file", Kind.KW_FILE);
		resWords.put("frame", Kind.KW_FRAME);
		resWords.put("while", Kind.KW_WHILE);
		resWords.put("if", Kind.KW_IF);
		resWords.put("sleep", Kind.OP_SLEEP);
		resWords.put("screenheight", Kind.KW_SCREENHEIGHT);
		resWords.put("screenwidth", Kind.KW_SCREENWIDTH);
		resWords.put("gray", Kind.OP_GRAY);
		resWords.put("convolve", Kind.OP_CONVOLVE);
		resWords.put("blur", Kind.OP_BLUR);
		resWords.put("scale", Kind.KW_SCALE);
		resWords.put("width", Kind.OP_WIDTH);
		resWords.put("height", Kind.OP_HEIGHT);
		resWords.put("xloc", Kind.KW_XLOC);
		resWords.put("yloc", Kind.KW_YLOC);
		resWords.put("hide", Kind.KW_HIDE);
		resWords.put("show", Kind.KW_SHOW);
		resWords.put("move", Kind.KW_MOVE);
		resWords.put("true", Kind.KW_TRUE);
		resWords.put("false", Kind.KW_FALSE);


	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int length = chars.length();
		int startPos = 0;
		linePosStart.add(-1); //Initial starting element
		State state = State.START;
		int ch;
		while(pos<=length)
		{
			ch = pos < length?chars.charAt(pos):-1;
			switch(state)
			{
				case START:
				{
					if(pos<length)
						pos = skipWhiteSpace(pos, length);
					ch = pos < length ? chars.charAt(pos) : -1;
					startPos = pos;
					switch(ch)
					{
						case -1: 
						{
							tokens.add(new Token(Kind.EOF,pos,0));
							pos++;
						}
						break;
						case '+':
						{
							tokens.add(new Token(Kind.PLUS, startPos, 1));
							pos++;
						}
						break;
						case '*':
						{
							tokens.add(new Token(Kind.TIMES, startPos, 1));
							pos++;
						}
						break;
						case ';':
						{
							tokens.add(new Token(Kind.SEMI, startPos, 1));
							pos++;
						}
						break;
						case ',':
						{
							tokens.add(new Token(Kind.COMMA, startPos, 1));
							pos++;
						}
						break;
						case '(':
						{
							tokens.add(new Token(Kind.LPAREN, startPos, 1));
							pos++;
						}
						break;
						case ')':
						{
							tokens.add(new Token(Kind.RPAREN, startPos, 1));
							pos++;
						}
						break;
						case '{':
						{
							tokens.add(new Token(Kind.LBRACE, startPos, 1));
							pos++;
						}
						break;
						case '}':
						{
							tokens.add(new Token(Kind.RBRACE, startPos, 1));
							pos++;
						}
						break;
						case '&':
						{
							tokens.add(new Token(Kind.AND, startPos, 1));
							pos++;
						}
						break;
						case '%':
						{
							tokens.add(new Token(Kind.MOD, startPos, 1));
							pos++;
						}
						break;
						case '|':
						{
							state = State.AFTER_OR;
							pos++;
						}
						break;
						case '!':
						{
							state = State.AFTER_NOT;
							pos++;
						}
						break;
						case '<':
						{
							state = State.AFTER_LESS;
							pos++;
						}
						break;
						case '>':
						{
							state = State.AFTER_GREAT;
							pos++;
						}
						break;
						case '/':
						{
							state = State.AFTER_SLASH;
							pos++;
						}
						break;
						case '-':
						{
							state = State.AFTER_MINUS;
							pos++;
						}
						break;
						case '=':
						{
							state = State.AFTER_EQ;
							pos++;
						}
						break;
						case '0': 
						{
							tokens.add(new Token(Kind.INT_LIT,startPos, 1));
							pos++;
						}
						break;
						case 10:
						{
							linePosStart.add(pos);
							state = State.START;
							pos++;
						}
						break;
						default: 
						{
				            if (Character.isDigit(ch)) 
				            {
				            	dict.append(Character.getNumericValue(ch));
				            	state = State.IN_DIGIT;
				            	pos++;
				            } 
				            else if (Character.isJavaIdentifierStart(ch)) 
				            {
				                dict.append((char)ch);
				            	state = State.IN_IDENT;
				                pos++;
				            } 
				            else 
				            {
				            	throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos);
				            }
				        }
					}
				}
				break;
				
				case IN_IDENT: 
				{
				      if (Character.isJavaIdentifierPart(ch))
				      {
				          dict.append((char)ch);
				    	  pos++;
				      }
				      else
				      {
				    	  
				    	  if(resWords.containsKey(dict.toString())) //Checking for reserved words
				    		  tokens.add(new Token(resWords.get(dict.toString()), startPos, pos - startPos));
				    	  else
				    		  tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
				    	  
				    	  state = State.START;
				    	  dict.setLength(0); //Clearing the buffer
				    	  
				      }
				}
				break;
				
				case IN_DIGIT:
				{
					if (Character.isDigit(ch))
					{
						dict.append(Character.getNumericValue(ch));
						pos++;
					}
					else
					{
						try{
							Integer.parseInt(dict.toString()); //Converting to integer
							tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
						}
						catch(NumberFormatException e)
						{
							throw new IllegalNumberException("Number too large for Integer range");						
						}
						state = State.START;
						dict.setLength(0); //Clearing the buffer
					}
					
				}
				break;
				
				case AFTER_EQ:
				{
					if(ch =='=')
					{
						pos++;
						tokens.add(new Token(Kind.EQUAL,startPos, pos-startPos));
						state = State.START;
					}
					else 
		            {
		            	throw new IllegalCharException("illegal char " +(char)ch+" at pos "+pos); //Single '=' symbol not allowed
		            }
				}
				break;
				case AFTER_OR:
				{
					if(ch=='-')
					{
						int ch2 = (pos+1) < length?chars.charAt(pos+1):-1;
						if(ch2 =='>') //For ARROW and BARARROW token
						{
							pos++;
							state=State.AFTER_MINUS;
						}
						else
						{
							tokens.add(new Token(Kind.OR,startPos, pos-startPos));
							state = State.START;
						}
						
					}
					else
					{
						
						tokens.add(new Token(Kind.OR,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_NOT:
				{
					if(ch=='=')
					{
						pos++;
						tokens.add(new Token(Kind.NOTEQUAL,startPos, pos-startPos));
						state = State.START;
					}
					else
					{
						tokens.add(new Token(Kind.NOT,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_GREAT:
				{
					if(ch=='=')
					{
						pos++;
						tokens.add(new Token(Kind.GE,startPos, pos-startPos));
						state = State.START;
					}
					else
					{
						tokens.add(new Token(Kind.GT,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_LESS:
				{
					if(ch=='=')
					{
						pos++;
						tokens.add(new Token(Kind.LE,startPos, pos-startPos));
						state = State.START;
					}
					else if(ch=='-')
					{
						pos++;
						tokens.add(new Token(Kind.ASSIGN,startPos, pos-startPos));
						state = State.START;
					}
					else
					{
						tokens.add(new Token(Kind.LT,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_SLASH:
				{
					if(ch=='*')
					{
						pos++;
						state = State.AFTER_COMMENT; //Start of a comment
					}
					else
					{
						tokens.add(new Token(Kind.DIV,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_MINUS:
				{
					if(ch=='>')
					{
						pos++;
						if(pos-startPos==2)
						{
							tokens.add(new Token(Kind.ARROW,startPos, pos-startPos));
						}
						else if(pos-startPos==3)
						{
							tokens.add(new Token(Kind.BARARROW,startPos, pos-startPos));
						}
						state = State.START;
					}
					else
					{
						tokens.add(new Token(Kind.MINUS,startPos, pos-startPos));
						state = State.START;
					}
				}
				break;
				case AFTER_COMMENT:
				{
					if(ch=='*') 
					{
						pos++;
						ch = pos < length ? chars.charAt(pos) : -1;
						if(ch=='/') //Comment Ended with */
						{
							pos++;
							state = State.START;
						}
						else if(ch==-1) //Comment ended with EOF
						{
							state = State.START;
						}
						else
						{
							state = State.AFTER_COMMENT;
						}
					}
					else if(ch==-1) //Comment ended with EOF
					{
						state = State.START;
					}
					else if(ch==10) //Line number within a comment
					{
						linePosStart.add(pos);
						pos++;
						state = State.AFTER_COMMENT;
						
					}
					else
					{
						pos++;
						state = State.AFTER_COMMENT;
					}
				}
			}
		}
		return this;  
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) 
	{
		//TODO IMPLEMENT THIS
		return t.getLinePos(); //Calling the previous getLinePos() method with an object of Token
	}


}
