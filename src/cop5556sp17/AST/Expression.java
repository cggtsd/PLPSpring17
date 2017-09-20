package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {

	public TypeName typeName;

	public Dec dec;

	public TypeName getTypeName() {
		return typeName;
	}

	public Dec getDec() {
		return dec;
	}

	public void setDec(Dec dec) {
		this.dec = dec;
	}

	public void setTypeName(TypeName type) {
		this.typeName = type;
	}

	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
