package cop5556sp17;

import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;
import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;

public class ScannerTest {
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
	Token checkNextToken(Scanner scanner, Scanner.Kind kind, String text, int line, int posInLine) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(kind, token.kind);
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		LinePos p = scanner.getLinePos(token);
		assertEquals(line,p.line);
		assertEquals(posInLine, p.posInLine);
		return token;
	}

	// Don't use this with idents or numlits
	Token checkNextToken(Scanner scanner, Scanner.Kind kind, int line, int posInLine) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(kind, token.kind);
		String text = kind.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		LinePos p = scanner.getLinePos(token);
		assertEquals(line,p.line);
		assertEquals(posInLine, p.posInLine);
		return token;
	}
	
	Token CheckEof(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token.kind);
		return token;
	}
	
	@Test
	public void testEmpty() throws IllegalCharException, IllegalNumberException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
	}

	@Test
	public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
		//input string
		String input = ";;;";
		//create and initialize the scanner
		Scanner scanner = new Scanner(input);
		scanner.scan();
		//get the first token and check its kind, position, and contents
		Scanner.Token token = scanner.nextToken();
		assertEquals(SEMI, token.kind);
		assertEquals(0, token.pos);
		String text = SEMI.getText();
		assertEquals(text.length(), token.length);
		assertEquals(text, token.getText());
		//get the next token and check its kind, position, and contents
		Scanner.Token token1 = scanner.nextToken();
		assertEquals(SEMI, token1.kind);
		assertEquals(1, token1.pos);
		assertEquals(text.length(), token1.length);
		assertEquals(text, token1.getText());
		Scanner.Token token2 = scanner.nextToken();
		assertEquals(SEMI, token2.kind);
		assertEquals(2, token2.pos);
		assertEquals(text.length(), token2.length);
		assertEquals(text, token2.getText());
		//check that the scanner has inserted an EOF token at the end
		Scanner.Token token3 = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF,token3.kind);
		
	}
	
	
	/**
	 * This test illustrates how to check that the Scanner detects errors properly. 
	 * In this test, the input contains an int literal with a value that exceeds the range of an int.
	 * The scanner should detect this and throw and IllegalNumberException.
	 * 
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	@Test
	public void testIntOverflowError() throws IllegalCharException, IllegalNumberException{
		String input = "99999999999999";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalNumberException.class);
		scanner.scan();		
	}
	
	//Test1: Test for comments
	@Test
	public void testComments() throws IllegalCharException, IllegalNumberException {
		String input = "/*abc* \n\t\r */prq/*";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "prq", 1, 5);
		CheckEof(scanner);
	}
	
	//Test2: Test for integer and boolean
	@Test
	public void testKeywordsIntBool() throws IllegalCharException, IllegalNumberException {
		String input = "int integer1 integerinteger \ninteger bool boolean Boolean boolean1";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "int", 0, 0);
		checkNextToken(scanner, IDENT, "integer1", 0, 4);
		checkNextToken(scanner, IDENT, "integerinteger", 0, 13);
		checkNextToken(scanner, KW_INTEGER, 1, 0);
		checkNextToken(scanner, IDENT, "bool", 1, 8);
		checkNextToken(scanner, KW_BOOLEAN, 1, 13);
		checkNextToken(scanner, IDENT, "Boolean", 1, 21);
		checkNextToken(scanner, IDENT, "boolean1", 1, 29);
		CheckEof(scanner);
	}
	
	//Test3: Test keywords image, url, file and frame
	@Test
	public void testKwImgUrlFileFrame() throws IllegalCharException, IllegalNumberException {
		String input = "Image \n /* image abc */ image\n URLurl url File \nfile Fram Frameframe frame";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "Image", 0, 0);
		checkNextToken(scanner, KW_IMAGE, 1, 17);
		checkNextToken(scanner, IDENT, "URLurl", 2, 1);
		checkNextToken(scanner, KW_URL, 2, 8);
		checkNextToken(scanner, IDENT, "File", 2, 12);
		checkNextToken(scanner, KW_FILE, 3, 0);
		checkNextToken(scanner, IDENT, "Fram", 3, 5);
		checkNextToken(scanner, IDENT, "Frameframe", 3, 10);
		checkNextToken(scanner, KW_FRAME, 3, 21);
		CheckEof(scanner);
	}
	
	//Test4: Test while, if, true and false
	@Test
	public void testWhileIfTF() throws IllegalCharException, IllegalNumberException {
		String input = "\r\n \twhile1 If iif while if \ntrue false";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "while1", 1, 2);
		checkNextToken(scanner, IDENT, "If", 1, 9);
		checkNextToken(scanner, IDENT, "iif", 1, 12);
		checkNextToken(scanner, KW_WHILE, 1, 16);
		checkNextToken(scanner, KW_IF, 1, 22);
		checkNextToken(scanner, KW_TRUE, 2, 0);
		checkNextToken(scanner, KW_FALSE, 2, 5);
		CheckEof(scanner);
	}
	
	//Test5: Test for blur, yloc,xloc and screenheight
	@Test
	public void testOpWords() throws IllegalCharException, IllegalNumberException {
		String input = "blur yloc !=xloc \nscreenheight zheight";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, OP_BLUR, "blur", 0, 0);
		checkNextToken(scanner, KW_YLOC, 0, 5);
		checkNextToken(scanner, NOTEQUAL, "!=", 0, 10);
		checkNextToken(scanner, KW_XLOC, 0, 12);
		checkNextToken(scanner, KW_SCREENHEIGHT, 1, 0);
		checkNextToken(scanner, IDENT, "zheight", 1, 13);
		CheckEof(scanner);
	}
	
	//Test6: Test for Operators
	@Test
	public void testOperators() throws IllegalCharException, IllegalNumberException {
		String input = "image1 image /* = screenwidth /* |*///x height blur eof";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "image1", 0, 0);
		checkNextToken(scanner, KW_IMAGE, 0, 7);
		checkNextToken(scanner, DIV, "/", 0, 36);
		checkNextToken(scanner, DIV, "/", 0, 37);
		checkNextToken(scanner, IDENT, "x", 0, 38);
		checkNextToken(scanner, OP_HEIGHT, "height", 0, 40);
		checkNextToken(scanner, OP_BLUR, "blur", 0, 47);
		checkNextToken(scanner, IDENT, "eof", 0, 52);
		CheckEof(scanner);
	}
	
	@Test
	public void testComplex() throws IllegalCharException, IllegalNumberException {
		String input = "int main() {\n\tcin << i;\n\tlong a <- 0==;0;\r\n\tlong b <- (a|->0);;\n\t/**?!@#$%^&*()\n+_kjghjvytv**/\nreturn 0;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "int", 0, 0);
		checkNextToken(scanner, IDENT, "main", 0, 4);
		checkNextToken(scanner, LPAREN, "(", 0, 8);
		checkNextToken(scanner, RPAREN, ")", 0, 9);
		checkNextToken(scanner, LBRACE, "{", 0, 11);
		checkNextToken(scanner, IDENT, "cin", 1, 1);
		checkNextToken(scanner, LT, "<", 1, 5);
		checkNextToken(scanner, LT, "<", 1, 6);
		checkNextToken(scanner, IDENT, "i", 1, 8);
		checkNextToken(scanner, SEMI, ";", 1, 9);
		checkNextToken(scanner, IDENT, "long", 2, 1);
		checkNextToken(scanner, IDENT, "a", 2, 6);
		checkNextToken(scanner, ASSIGN, "<-", 2, 8);
		checkNextToken(scanner, INT_LIT, "0", 2, 11);
		checkNextToken(scanner, EQUAL, "==", 2, 12);
		checkNextToken(scanner, SEMI, ";", 2, 14);
		checkNextToken(scanner, INT_LIT, "0", 2, 15);
		checkNextToken(scanner, SEMI, ";", 2, 16);
		checkNextToken(scanner, IDENT, "long", 3, 1);
		checkNextToken(scanner, IDENT, "b", 3, 6);
		checkNextToken(scanner, ASSIGN, "<-", 3, 8);
		checkNextToken(scanner, LPAREN, "(", 3, 11);
		checkNextToken(scanner, IDENT, "a", 3, 12);
		checkNextToken(scanner, BARARROW, "|->", 3, 13);
		checkNextToken(scanner, INT_LIT, "0", 3, 16);
		checkNextToken(scanner, RPAREN, ")", 3, 17);
		checkNextToken(scanner, SEMI, ";", 3, 18);
		checkNextToken(scanner, SEMI, ";", 3, 19);
		checkNextToken(scanner, IDENT, "return", 6, 0);
		checkNextToken(scanner, INT_LIT, "0", 6, 7);
		checkNextToken(scanner, SEMI, ";", 6, 8);
		checkNextToken(scanner, RBRACE, "}", 6, 9);
		CheckEof(scanner);
	}
	
	@Test
	public void testIllegalDollar() throws IllegalCharException, IllegalNumberException {
		String input = "$abc _pqr 003==!\t|->;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		
		checkNextToken(scanner, IDENT, "$abc", 0, 0);
		checkNextToken(scanner, IDENT, "_pqr", 0, 5);
		checkNextToken(scanner, INT_LIT, "0", 0, 10);
		checkNextToken(scanner, INT_LIT, "0", 0, 11);
		checkNextToken(scanner, INT_LIT, "3", 0, 12);
		checkNextToken(scanner, EQUAL, "==", 0, 13);
		checkNextToken(scanner, NOT, "!", 0, 15);
		checkNextToken(scanner, BARARROW, "|->", 0, 17);
		checkNextToken(scanner, SEMI, ";", 0, 20);
		CheckEof(scanner);
	}
	
	@Test 
	public void testIllegalCharException1() throws IllegalCharException, IllegalNumberException {
		String input = "/\\**";
		Scanner scanner = new Scanner(input);
		thrown.expect(IllegalCharException.class);
		scanner.scan();
	}
}
