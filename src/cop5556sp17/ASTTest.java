package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.*;


public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}



	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testConstantExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "screenwidth screenheight";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(ConstantExpression.class, ast.getClass());
		assertEquals(ConstantExpression.class, ast.getClass());
	} 
	
	@Test
	public void testBinaryChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc -> show -> ab;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.chain();
		assertEquals(BinaryChain.class, ast.getClass());
		BinaryChain bc = (BinaryChain) ast;
		assertEquals(IdentChain.class, bc.getE1().getClass());
		assertEquals(BinaryChain.class, bc.getE0().getClass());
		assertEquals(ARROW, bc.getArrow().kind);
	}
	
	@Test
	public void testAssignmentStatement() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "snpky <- mad;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(AssignmentStatement.class, ast.getClass());
		AssignmentStatement astmt = (AssignmentStatement) ast;
		assertEquals(astmt.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testBinaryExpr() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "100*abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(TIMES, be.getOp().kind);
	}
	
	@Test
	public void testBlock() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "{ integer i}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.block();
		assertEquals(Block.class, ast.getClass());
		Block b = (Block) ast;
		assertEquals(Block.class, ast.getClass());
		assertEquals(b.getFirstToken().kind, Kind.LBRACE);
	}
	
	@Test
	public void testBooleanLitExpr() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "false";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BooleanLitExpression.class, ast.getClass());
		BooleanLitExpression ble = (BooleanLitExpression) ast;
		assertEquals(BooleanLitExpression.class, ast.getClass());
		assertEquals(ble.getFirstToken().kind, Kind.KW_FALSE);
	}
	
	@Test
	public void testChain() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc->gray(12)->move(a)->scale(false);";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		Chain c = (Chain) ast;
		assertEquals(BinaryChain.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testChain1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " x -> show -> hide ;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		Chain c = (Chain) ast;
		assertEquals(BinaryChain.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testChain2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  x -> move (3,4) -> hide ;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		Chain c = (Chain) ast;
		assertEquals(BinaryChain.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testChain3() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "x -> show |-> move (x,y);";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		Chain c = (Chain) ast;
		assertEquals(BinaryChain.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testChainElem() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "blur(10)|->abc;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		Chain c = (Chain) ast;
		assertEquals(BinaryChain.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.OP_BLUR);
	}
	
	@Test
	public void testConstExpr() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "screenwidth";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.factor();
		assertEquals(ConstantExpression.class, ast.getClass());
		ConstantExpression c = (ConstantExpression) ast;
		assertEquals(ConstantExpression.class, ast.getClass());
		assertEquals(c.getFirstToken().kind, Kind.KW_SCREENWIDTH);
	}
	
	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "integer abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.dec();
		assertEquals(Dec.class, ast.getClass());
		Dec d = (Dec) ast;
		assertEquals(Dec.class, ast.getClass());
		assertEquals(d.getFirstToken().kind, Kind.KW_INTEGER);
	}
	
	@Test
	public void testExpr() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "23";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
		Expression e = (Expression) ast;
		assertEquals(IntLitExpression.class, ast.getClass());
		assertEquals(e.getFirstToken().kind, Kind.INT_LIT);
	}
	
	@Test
	public void testProgram() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc {integer i while(true) {sleep (10);}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		Program p = (Program) ast;
		assertEquals(Program.class, ast.getClass());
		assertEquals(p.getFirstToken().kind, Kind.IDENT);
	}
	
	@Test
	public void testStmt() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while(true) {sleep (10);}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(WhileStatement.class, ast.getClass());
		Statement s = (Statement) ast;
		assertEquals(WhileStatement.class, ast.getClass());
		assertEquals(s.getFirstToken().kind, Kind.KW_WHILE);
	}
	
	@Test
	public void testStmt1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while (true) \n {x -> show |-> move (x,y) ;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(WhileStatement.class, ast.getClass());
		Statement s = (Statement) ast;
		assertEquals(WhileStatement.class, ast.getClass());
		assertEquals(s.getFirstToken().kind, Kind.KW_WHILE);
	}
	
	@Test
	public void testStmt2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "if (true) \n {x -> show |-> move (x,y) ;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(IfStatement.class, ast.getClass());
		Statement s = (Statement) ast;
		assertEquals(IfStatement.class, ast.getClass());
		assertEquals(s.getFirstToken().kind, Kind.KW_IF);
	}
}
