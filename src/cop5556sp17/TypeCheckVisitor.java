package cop5556sp17;

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

import java.util.List;

import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
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
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Chain c1 = (Chain) binaryChain.getE0().visit(this, arg);
		ChainElem c2 = (ChainElem) binaryChain.getE1().visit(this, arg);
		switch(c1.getTypeName()){
		case URL:
			if(c2.getTypeName().equals(IMAGE)){
				switch(binaryChain.getArrow().kind){
				case ARROW:
					binaryChain.setTypeName(IMAGE);
					return binaryChain;
				default:
					throw new TypeCheckException("Type not image");
				}
			}
		case FILE:
			if(c2.getTypeName().equals(IMAGE)){
				switch(binaryChain.getArrow().kind){
				case ARROW:
					binaryChain.setTypeName(IMAGE);
					return binaryChain;
				default:
					throw new TypeCheckException("Type not image");
				}
			}
		case FRAME:
			if(c2 instanceof FrameOpChain){
				switch(binaryChain.getArrow().kind){
				case ARROW:
					switch(c2.getFirstToken().kind){
					case KW_XLOC:
					case KW_YLOC:
						binaryChain.setTypeName(INTEGER);
						return binaryChain;
					case KW_SHOW:
					case KW_HIDE:
					case KW_MOVE:
						binaryChain.setTypeName(FRAME);
						return binaryChain;
					default:
						throw new TypeCheckException("Only xoc, yloc, show, hide, move allowed for framechain");
					}
				default:
					throw new TypeCheckException("Recieved token other than arrow operator");
				}
			}
		case INTEGER:
			if(c2 instanceof IdentChain && binaryChain.getArrow().kind == ARROW && c2.getTypeName().equals(INTEGER)){
				binaryChain.setTypeName(INTEGER);
			}
		case IMAGE:
			if(c2 instanceof ImageOpChain){
				switch(binaryChain.getArrow().kind){
				case ARROW:
					switch(c2.getFirstToken().kind){
					case OP_WIDTH:
					case OP_HEIGHT:
						binaryChain.setTypeName(INTEGER);
						return binaryChain;
					case KW_SCALE:
						binaryChain.setTypeName(IMAGE);
						return binaryChain;
					default:
						throw new TypeCheckException("not instance of imageopchain");
					}
				default:
					throw new TypeCheckException("Recieved token other than arrow operator");
				}
			}
			else if(c2 instanceof FilterOpChain){
				switch(binaryChain.getArrow().kind){
				case ARROW:
				case BARARROW:
					switch(c2.getFirstToken().kind){
					case OP_GRAY:
					case OP_BLUR:
					case OP_CONVOLVE:
						binaryChain.setTypeName(IMAGE);
						return binaryChain;
					default:
						throw new TypeCheckException("Not instance of filteropchain");
					}
				default:
					throw new TypeCheckException("parser error only arrow bararrow allowed.");
				}
			}
			else if(c2.getTypeName().equals(FILE) && binaryChain.getArrow().kind == ARROW){
				binaryChain.setTypeName(NONE);
				return binaryChain;
			}
			else if(c2.getTypeName().equals(FRAME) && binaryChain.getArrow().kind.equals(ARROW)){
				binaryChain.setTypeName(FRAME);
				return binaryChain;
			}
			else if(c2 instanceof IdentChain && binaryChain.getArrow().kind == ARROW && c2.getTypeName().equals(IMAGE)){
				binaryChain.setTypeName(IMAGE);
				return binaryChain;
			}
			else if(c2 instanceof IdentChain && binaryChain.getArrow().kind == ARROW && c2.getTypeName().equals(INTEGER)){
				binaryChain.setTypeName(IMAGE);
				return binaryChain;
			}
			else{
				throw new TypeCheckException("Exception image");
			}
		default:
			throw new TypeCheckException("Exception binaryChain");
		}
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression val1 = (Expression) binaryExpression.getE0().visit(this, null);
		Expression val2 = (Expression) binaryExpression.getE1().visit(this, null);
		if(val1.getTypeName().equals(val2.getTypeName())){
			if(val1.getTypeName() == INTEGER){
				switch(binaryExpression.getOp().kind){
				case PLUS:
				case MINUS:
				case TIMES:
				case DIV:
				case MOD:
					binaryExpression.setTypeName(INTEGER);
					return binaryExpression;
				case GT:
				case LT:
				case GE:
				case LE:
				case EQUAL:
				case NOTEQUAL:
					binaryExpression.setTypeName(BOOLEAN);
					return binaryExpression;
				default:
					throw new TypeCheckException("Not valid types");
				}
			}
			else if(val1.getTypeName() == IMAGE){
				switch(binaryExpression.getOp().kind){
				case PLUS:
				case MINUS:
					binaryExpression.setTypeName(IMAGE);
					return binaryExpression;
				case EQUAL:
				case NOTEQUAL:
					binaryExpression.setTypeName(BOOLEAN);
					return binaryExpression;
				default:
					throw new TypeCheckException("Invalid types");
				}
			}
			else if(val1.getTypeName() == TypeName.BOOLEAN){
				switch(binaryExpression.getOp().kind){
				case LT:
				case GT:
				case LE:
				case GE:
				case EQUAL:
				case NOTEQUAL:
				case AND:
				case OR:
					binaryExpression.setTypeName(BOOLEAN);
					return binaryExpression;
				default:
					throw new TypeCheckException("Invalid Types");
				}
			}
			else{
				binaryExpression.setTypeName(TypeName.BOOLEAN);
				return binaryExpression;
			}
		}
		else if(binaryExpression.getOp().kind == TIMES){
			if (val1.getTypeName().equals(INTEGER) && val2.getTypeName().equals(IMAGE)) {
				binaryExpression.setTypeName(IMAGE);
			} else if (val1.getTypeName().equals(IMAGE) && val2.getTypeName().equals(INTEGER)) {
				binaryExpression.setTypeName(IMAGE);
			} else {
				throw new TypeCheckException("Incompatible types");
			}

		}
		else if(binaryExpression.getOp().kind == DIV){
			if (val1.getTypeName().equals(INTEGER) && val2.getTypeName().equals(IMAGE)) {
				binaryExpression.setTypeName(IMAGE);
			} else if (val1.getTypeName().equals(IMAGE) && val2.getTypeName().equals(INTEGER)) {
				binaryExpression.setTypeName(IMAGE);
			} else {
				throw new TypeCheckException("Incompatible types");
			}
		}
		else if(binaryExpression.getOp().kind == MOD){
			if (val1.getTypeName().equals(INTEGER) && val2.getTypeName().equals(IMAGE)) {
				binaryExpression.setTypeName(IMAGE);
			} else if (val1.getTypeName().equals(IMAGE) && val2.getTypeName().equals(INTEGER)) {
				binaryExpression.setTypeName(IMAGE);
			} else {
				throw new TypeCheckException("Incompatible types");
			}
		}
		else{
			throw new TypeCheckException("Incompatible Types");
		}
		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		List<Dec> decs = block.getDecs();
		List<Statement> stmts = block.getStatements();
		int i = 0, j = 0;
		while(i<decs.size() || j<stmts.size()) {
			if (i == decs.size()) {
				stmts.get(j).visit(this, null);
				j++;
			} else if (j == stmts.size()) {
				decs.get(i).visit(this, null);
				i++;
			} else {
				int d_line = decs.get(i).getFirstToken().getLinePos().line;
				int s_line = stmts.get(j).getFirstToken().getLinePos().line;
				if (d_line < s_line) {
					decs.get(i).visit(this, null);
					i++;
				} else if (d_line > s_line){
					stmts.get(j).visit(this, null);
					j++;
				} else {
					int d_pos = decs.get(i).getFirstToken().getLinePos().posInLine;
					int s_pos = stmts.get(j).getFirstToken().getLinePos().posInLine;
					if (d_pos < s_pos) {
						decs.get(i).visit(this, null);
						i++;
					} else {
						stmts.get(j).visit(this, null);
						j++;
					}
				}
			}
		}
/*		for(Dec d: block.getDecs()){
			d.visit(this, arg);
		}
		for(Statement s: block.getStatements()){
			s.visit(this, arg);
		}*/
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, arg);
		if(filterOpChain.getArg().getExprList().size() != 0){
			throw new TypeCheckException("FilterOpChain Exception");
		}
		filterOpChain.setTypeName(IMAGE);
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		frameOpChain.getArg().visit(this, arg);

		if(frameOpChain.getArg().getExprList().size() == 0){
			if(frameOpChain.getFirstToken().kind == KW_SHOW || frameOpChain.getFirstToken().kind == KW_HIDE)
				frameOpChain.setTypeName(NONE);
			else if(frameOpChain.getFirstToken().kind == KW_XLOC || frameOpChain.getFirstToken().kind == KW_YLOC)
				frameOpChain.setTypeName(INTEGER);
		}
		else if(frameOpChain.getArg().getExprList().size() == 2){
			if(frameOpChain.getFirstToken().kind == KW_MOVE)
				frameOpChain.setTypeName(NONE);
		}
		else
			throw new TypeCheckException("FrameOpChain Error");
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token ft = identChain.getFirstToken();
		Dec dec = symtab.lookup(ft.getText());
		if(dec == null){
			throw new TypeCheckException("Identifier not found");
		}
		identChain.setTypeName(dec.getTypeName());
		identChain.setDec(dec);
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token ft = identExpression.getFirstToken();
		Dec dec = symtab.lookup(ft.getText());
		if( dec == null){
			throw new TypeCheckException("Identifier not found");
		}
		identExpression.setTypeName(dec.getTypeName());
		identExpression.setDec(dec);
		return identExpression;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression exprType = (Expression) ifStatement.getE().visit(this, arg);
		if(!exprType.getTypeName().equals(TypeName.BOOLEAN)){
			throw new TypeCheckException("If does not return boolean");
		}
		ifStatement.getB().visit(this,arg);
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression exprType = (Expression) sleepStatement.getE().visit(this, arg);
		if(!exprType.getTypeName().equals(TypeName.INTEGER)){
			throw new TypeCheckException("Sleep Statement does not return integer");
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression exprType = (Expression) whileStatement.getE().visit(this, arg);
		if(!exprType.getTypeName().equals(TypeName.BOOLEAN)){
			throw new TypeCheckException("While Statement does not return boolean");
		}
		whileStatement.getB().visit(this,arg);
		return whileStatement;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//declaration.visit(this, arg);
		String decText = declaration.getIdent().getText();
		declaration.setTypeName(Type.getTypeName(declaration.getFirstToken()));
		if(symtab.insert(decText, declaration)){
			return declaration;
		}
		else{
			throw new TypeCheckException("Variable already declared");
		}
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		program.getFirstToken().getText();
		List<ParamDec> params = program.getParams();
		for (int i = 0; i < params.size(); i++) {
			params.get(i).visit(this, arg);
		}
		program.getB().visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = (Expression) assignStatement.getE().visit(this, arg);
		Dec d = symtab.lookup(assignStatement.var.firstToken.getText());
		if(d == null){
			throw new TypeCheckException("Variable not declared");
		}
		IdentLValue ilVal = (IdentLValue) assignStatement.getVar().visit(this, arg);
		if(ilVal.getDec().getTypeName().equals(e.getTypeName())){
			return assignStatement;
		}
		else
			throw new TypeCheckException("Type mismatch");
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ft = identX.getFirstToken().getText();
		Dec dec = symtab.lookup(ft);
		if(dec == null){
			throw new TypeCheckException("Identifier not found");
		}
		identX.setDec(dec);
		return identX;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		//paramDec.visit(this, arg);
		String ft = paramDec.getIdent().getText();
		paramDec.setTypeName(Type.getTypeName(paramDec.getFirstToken()));
		if(symtab.insert(ft, paramDec)){
			return paramDec;
		}
		else{
			throw new TypeCheckException("Variable already declared");
		}
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		imageOpChain.getArg().visit(this, arg);
		if(imageOpChain.getFirstToken().kind == OP_WIDTH
			|| imageOpChain.getFirstToken().kind == OP_HEIGHT){
			if(imageOpChain.getArg().getExprList().size() == 0)
				imageOpChain.setTypeName(INTEGER);
			else
				throw new TypeCheckException("Not 0 args");
		}
		else if(imageOpChain.getFirstToken().kind == KW_SCALE){
			if(imageOpChain.getArg().getExprList().size() == 1)
				imageOpChain.setTypeName(IMAGE);
			else
				throw new TypeCheckException("Not enough args");
		}
		else
			throw new TypeCheckException("Image Op Exception");
		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		for(int i=0; i<tuple.getExprList().size(); i++){
			tuple.getExprList().get(i).visit(this, arg);
			if (!tuple.getExprList().get(i).getTypeName().equals(INTEGER)){
				throw new TypeCheckException("Non numeric arguement received");
			}
		}
		return tuple;
	}

}
