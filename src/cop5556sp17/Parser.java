package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.*;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;


public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
/*	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}*/

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	/**
	 * Check for expression
	 * expression ∷= term ( relOp term)*
	 * relOp ∷=  LT | LE | GT | GE | EQUAL | NOTEQUAL
	 * @return 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException {
		//TODO
		Token r = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = term();
		
		while(t.kind == LE || t.kind == LT || t.kind == GT || t.kind == GE || t.kind == EQUAL || t.kind == NOTEQUAL){
			Token op =t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(r, e0, op, e1);
		}
		return e0;
	}

	
	/**
	 * Check for term
	 * term ∷= elem ( weakOp  elem)*
	 * weakOp  ∷= PLUS | MINUS | OR 
	 * @throws SyntaxException
	 */
	public Expression term() throws SyntaxException {
		//TODO
		Token r = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = elem();
		while(t.kind == PLUS || t.kind == MINUS || t.kind == OR){
			Token op = t;
			consume();
//			match(t.kind);
			e1 = elem();
			e0 = new BinaryExpression(r, e0, op, e1);
		}
		return e0;
	}
	
	/**
	 * Check for element
	 * elem ∷= factor ( strongOp factor)*
	 * strongOp ∷= TIMES | DIV | AND | MOD 
	 * @throws SyntaxException
	 */
	public Expression elem() throws SyntaxException {
		//TODO
		Token r = t;
		Expression e0 = null;
		Expression e1 = null;
		e0 = factor();
		while(t.kind == TIMES || t.kind == DIV || t.kind == AND || t.kind == MOD){
			Token op = t;
			consume();
			//match(t.kind);
			e1 = factor();
			e0 = new BinaryExpression(r, e0, op, e1);
		}
		return e0;
	}
	
	
	/**
	 * Check for factor
	 * factor ∷= IDENT | INT_LIT | KW_TRUE | KW_FALSE
       	| KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	 * @throws SyntaxException
	 */
	public Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e = null;
		switch (kind) {
		case IDENT: {
			Token r = consume();
			e = new IdentExpression(r);
		}
			break;
		case INT_LIT: {
			Token r = consume();
			e = new IntLitExpression(r);
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			Token r = consume();
			e = new BooleanLitExpression(r);
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			Token r = consume();
			e = new ConstantExpression(r);
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e;
	}
	

	/**
	 * Check for block
	 * block ::= { ( dec | statement) * }
	 * dec ::= (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME) IDENT
	 * @throws SyntaxException
	 */
	public Block block() throws SyntaxException {
		//TODO
		Block b = null;
		Token r = match(LBRACE);
		ArrayList<Dec> ald = new ArrayList<>();
		ArrayList<Statement> als = new ArrayList<>();
		while(t.kind != RBRACE){
			if(t.kind == KW_INTEGER || t.kind == KW_BOOLEAN || t.kind == KW_IMAGE || t.kind == KW_FRAME){
				Dec dr = dec();
				ald.add(dr);
			}
			else{
				Statement sr = statement();
				als.add(sr);
			}
		}
		if(t.kind == RBRACE){
			consume();
			b = new Block(r, ald, als);
		}
		else{
			throw new SyntaxException("Illegal token"+t.kind+"in block");
		}
		return b;
	}

	/**
	 * Check for program
	 * program ::=  IDENT block
	 * program ::=  IDENT param_dec ( , param_dec )*   block
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		//TODO
		Program p = null;
		ArrayList<ParamDec> alpd = new ArrayList<>();
		Block b = null;
		ParamDec pdr = null;
		Token r = match(IDENT);
		switch(t.kind){
		case LBRACE:
			b = block();
			p = new Program(r, alpd, b);
			break;
		case KW_INTEGER:
		case KW_FILE:
		case KW_BOOLEAN:
		case KW_URL:
			pdr = paramDec();
			alpd.add(pdr);
			while(t.kind == COMMA){
				consume();
				pdr = paramDec();
				alpd.add(pdr);
			}
			b = block();
			p = new Program(r, alpd, b);
			break;
		default: throw new SyntaxException("Illegal token"+t.kind+"in program");
		}
		return p;
	}
	
	/**
	 *Check for paramDec
	 * paramDec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
	 * @throws SyntaxException
	 */
	public ParamDec paramDec() throws SyntaxException {
		//TODO
		Kind kind = t.kind;
		ParamDec pd = null;
		Token p =null;
		Token r =null;
		switch(kind){
		case KW_URL:
		case KW_FILE:
		case KW_INTEGER:
		case KW_BOOLEAN:
			r = consume();
			p = match(IDENT);
			pd = new ParamDec(r, p);
			break;
		default: throw new SyntaxException("Illegal token"+t.kind+"in paramDec");
		}
		return pd;
	}
	
	/**
	 * Check for dec
	 * dec ::= (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME) IDENT
	 * @throws SyntaxException
	 */
	public Dec dec() throws SyntaxException {
		//TODO
		Kind kind = t.kind;
		Dec d = null;
		switch(kind){
		case KW_INTEGER:
		case KW_BOOLEAN:
		case KW_IMAGE:
		case KW_FRAME:
			Token r = consume();
			Token p = match(IDENT);
			d = new Dec(r, p);
			break;
		default: throw new SyntaxException("Illegal token"+t.kind+"in dec"); 
		}
		return d;
	}
	
	/**
	 * Check for statement
	 * statement ::=   OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
	 * @throws SyntaxException
	 */
	public Statement statement() throws SyntaxException {
		//TODO
		Statement s = null;
		IdentLValue il = null;
		Expression er = null;
		Token r = null;
		Chain c = null;
		Token tr = null;
		Block br = null;
		switch(t.kind){
		case OP_SLEEP:
			r = consume();
			er = expression();
			match(SEMI);
			s = new SleepStatement(r, er);
			break;
		case KW_WHILE: 
			r = consume();
			match(LPAREN);
			er = expression();
			match(RPAREN);
			br = block();
			s = new WhileStatement(r, er, br);
			break;
		case KW_IF:
			r = consume();
			match(LPAREN);
			er = expression();
			match(RPAREN);
			br = block();
			s = new IfStatement(r, er, br);
			break;
		case IDENT: 
			Token next = scanner.peek();
			if(next.kind == ASSIGN){
				r = consume();
				tr = consume();
				er = expression();
				il = new IdentLValue(r);
				s = new AssignmentStatement(r, il, er);
			} else{
				c = chain();
				s = c;
			}
			match(SEMI);
			break;
		case OP_BLUR: case OP_GRAY: case OP_CONVOLVE:
			c = chain();
			match(SEMI);
			s = c;
			break;
		case KW_SHOW: case KW_HIDE: case KW_MOVE: case KW_XLOC: case KW_YLOC:
			c = chain();
			match(SEMI);
			s = c;
			break;
		case OP_WIDTH: case OP_HEIGHT: case KW_SCALE:
			c = chain();
			match(SEMI);
			s = c;
			break;
		default: throw new SyntaxException("Illegal token"+t.kind+"in statement");
		}
		return s;
	}

	/**
	 * Check for chain
	 * chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)*
	 * @throws SyntaxException
	 */
	public Chain chain() throws SyntaxException {
		//TODO
		Chain c = null;
		Token r = t;
		ChainElem cer = chainElem();
		ChainElem cer1 = null;
		Token op = null;
		if(t.kind == ARROW || t.kind == BARARROW){
			op = consume();
		}
		cer1 = chainElem();
		c = new BinaryChain(r, cer, op, cer1);
		while(t.kind == ARROW || t.kind == BARARROW){
			 op = consume();
			cer = chainElem();
			c = new BinaryChain(r, c, op, cer);
		}
	
		return c;
	}
	
	/**
	 * Check for Chain element
	 * chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
	 * @throws SyntaxException
	 */
	public ChainElem chainElem() throws SyntaxException {
		//TODO
		ChainElem ce = null;
		Token r = null;
		Tuple tr = null;
		switch(t.kind){
		case IDENT:
			r = consume();
			ce = new IdentChain(r);
			break;
		case OP_BLUR: case OP_GRAY: case OP_CONVOLVE:
			r = consume();
			tr = arg();
			ce = new FilterOpChain(r, tr);
			break;
		case KW_SHOW: case KW_HIDE: case KW_MOVE: case KW_XLOC: case KW_YLOC:
			r = consume();
			tr = arg();
			ce = new FrameOpChain(r, tr);
			break;
		case OP_WIDTH: case OP_HEIGHT: case KW_SCALE:
			r = consume();
			tr = arg();
			ce = new ImageOpChain(r, tr);
			break;
		default:
			throw new SyntaxException("Illegal token"+t.kind+"in chain element");
		}
		return ce;
	}

	/**
	 * Check for arg
	 * arg ::= ε | ( expression (   ,expression)* )
	 * @throws SyntaxException
	 */
	public Tuple arg() throws SyntaxException {
		//TODO
		Tuple tr = null;
		List<Expression> le = new ArrayList<>();
		Token r = null;
		if(t.kind == LPAREN){
			r = consume();
			Expression er = expression();
			le.add(er);
			while(t.kind == COMMA){
				consume();
				er = expression();
				le.add(er);
			}
			match(Kind.RPAREN);
		}
		tr = new Tuple(r, le);
//		else{
//			consume();
//			throw new SyntaxException("Illegal token" + t.kind);
//		}
		return tr;
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
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
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
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
	@SuppressWarnings("unused")
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
