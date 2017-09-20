package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.*;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	int pdcounter = 0;
	int dcounter = 1;
	ArrayList<Dec> decList = new ArrayList<Dec>();
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables

		for(int i = 0; i<decList.size(); i++){
			mv.visitLocalVariable(decList.get(i).getIdent().getText(), decList.get(i).getTypeName().getJVMTypeDesc(), null, startRun, endRun, decList.get(i).getSlot());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return assignStatement;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain c1 = (Chain) binaryChain.getE0();
		Chain c2 = (Chain) binaryChain.getE1();
		Token aop = binaryChain.getArrow();
		if(c1 instanceof FilterOpChain){
			if(aop.kind == ARROW){
				mv.visitInsn(ACONST_NULL);
			}
			else if(aop.kind == BARARROW){
				if(c1.getFirstToken().kind == OP_GRAY || c2.getFirstToken().kind == OP_BLUR)
					mv.visitInsn(DUP);
			}
			c1.visit(this, null);
		}
		else if(c1 instanceof IdentChain){
			c1.visit(this, true);
		}
		else if(c1 instanceof FrameOpChain){
			c1.visit(this, null);
		}
		else{
			c1.visit(this, null);
		}
		if(c2 instanceof FrameOpChain){
			c2.visit(this, null);
		}
		else if(c2 instanceof FilterOpChain){
			if(aop.kind == ARROW){
				mv.visitInsn(ACONST_NULL);
			}
			else if(aop.kind == BARARROW){
				if(c2.getFirstToken().kind == OP_GRAY || c2.getFirstToken().kind == OP_BLUR)
					mv.visitInsn(DUP);
			}
			c2.visit(this, null);
		}
		else if(c2 instanceof IdentChain){
			c2.visit(this, false);
		}
		else{
			c2.visit(this, null);
		}
		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		Expression e1 = (Expression) binaryExpression.getE0().visit(this, null);
		Expression e2 = (Expression) binaryExpression.getE1().visit(this, null);
		Kind op = binaryExpression.getOp().kind;
		if(e1.getTypeName().getJVMTypeDesc() == "I" && e2.getTypeName().getJVMTypeDesc() == "I"){
			switch(op){
			case PLUS:
				mv.visitInsn(IADD);
				break;
			case MINUS:
				mv.visitInsn(ISUB);
				break;
			case TIMES:
				mv.visitInsn(IMUL);
				break;
			case DIV:
				mv.visitInsn(IDIV);
				break;
			case MOD:
				mv.visitInsn(IREM);
				break;
			case LT:{
				Label label1 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, label1);
				mv.visitInsn(ICONST_1);
				Label label2 = new Label();
				mv.visitJumpInsn(GOTO, label2);
				mv.visitLabel(label1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label2);
				}break;
			case GT:{
				Label label3 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, label3);
				mv.visitInsn(ICONST_1);
				Label label4 = new Label();
				mv.visitJumpInsn(GOTO, label4);
				mv.visitLabel(label3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label4);
				}break;
			case LE:{
				Label label5 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, label5);
				mv.visitInsn(ICONST_1);
				Label label6 = new Label();
				mv.visitJumpInsn(GOTO, label6);
				mv.visitLabel(label5);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label6);
			}break;
			case GE:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			case EQUAL:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			case NOTEQUAL:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			}
		}
		else if(e1.getTypeName().getJVMTypeDesc() == "Z" && e2.getTypeName().getJVMTypeDesc() == "Z"){
			switch(op){
			case AND:
				mv.visitInsn(IAND);
				break;
			case OR:
				mv.visitInsn(IOR);
				break;
			case LT:{
				Label label1 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, label1);
				mv.visitInsn(ICONST_1);
				Label label2 = new Label();
				mv.visitJumpInsn(GOTO, label2);
				mv.visitLabel(label1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label2);
				}break;
			case GT:{
				Label label3 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, label3);
				mv.visitInsn(ICONST_1);
				Label label4 = new Label();
				mv.visitJumpInsn(GOTO, label4);
				mv.visitLabel(label3);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label4);
				}break;
			case LE:{
				Label label5 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, label5);
				mv.visitInsn(ICONST_1);
				Label label6 = new Label();
				mv.visitJumpInsn(GOTO, label6);
				mv.visitLabel(label5);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label6);
				}break;
			case GE:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			case EQUAL:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			case NOTEQUAL:{
				Label label7 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, label7);
				mv.visitInsn(ICONST_1);
				Label label8 = new Label();
				mv.visitJumpInsn(GOTO, label8);
				mv.visitLabel(label7);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label8);
				}break;
			}
		}
		else if(e1.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.BufferedImageDesc && e2.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.BufferedImageDesc){
			switch(op){
			case PLUS:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);break;
			case MINUS:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);break;
			case EQUAL:{
				Label label9 = new Label();
				mv.visitJumpInsn(IF_ACMPNE, label9);
				mv.visitInsn(ICONST_1);
				Label label10 = new Label();
				mv.visitJumpInsn(GOTO, label10);
				mv.visitLabel(label9);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label10);
				}break;
			case NOTEQUAL:{
				Label label11 = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, label11);
				mv.visitInsn(ICONST_1);
				Label label12 = new Label();
				mv.visitJumpInsn(GOTO, label12);
				mv.visitLabel(label11);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(label12);
				}break;
			}
		}
		else if(e1.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.BufferedImageDesc && e2.getTypeName().getJVMTypeDesc() == "I"){
			switch(op){
			case TIMES:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);break;
			case DIV:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);break;
			case MOD:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);break;
			}
		}
		else if(e1.getTypeName().getJVMTypeDesc() == "I" && e2.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.BufferedImageDesc){
			if(op == TIMES){
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			}
		}
			return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label startLabel = new Label();
		mv.visitLabel(startLabel);

		List<Dec> decList1 = block.getDecs();
		for(int i = 0;i < decList1.size(); i++){
			decList1.get(i).visit(this, null);
			decList.add(decList1.get(i));
		}

		List<Statement> stmtList = block.getStatements();
		for(int j=0;j<stmtList.size();j++){
			stmtList.get(j).visit(this, null);
			if(stmtList.get(j) instanceof BinaryChain){
				mv.visitInsn(POP);
			}
		}

		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(!booleanLitExpression.getValue())
			mv.visitInsn(ICONST_0);
		else
			mv.visitInsn(ICONST_1);
		return booleanLitExpression;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if(constantExpression.getFirstToken().kind == KW_SCREENWIDTH){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		else if(constantExpression.getFirstToken().kind == KW_SCREENHEIGHT){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
		return constantExpression;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlot(dcounter);
		int dslot = declaration.getSlot();
		if(declaration.getTypeName().equals(TypeName.IMAGE) || declaration.getTypeName().equals(TypeName.FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getSlot());
		}
		dcounter++;
		return declaration;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
			if(filterOpChain.getFirstToken().kind == OP_BLUR){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			}
			else if(filterOpChain.getFirstToken().kind == OP_GRAY){

				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			}
			else if(filterOpChain.getFirstToken().kind == OP_CONVOLVE){
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
			}
		//}
		//else if(op.kind == BARARROW){
			//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			//mv.visitInsn(SWAP);
			//if(filterOpChain.getFirstToken().kind == OP_BLUR){
				/*mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			}
			else if(filterOpChain.getFirstToken().kind == OP_GRAY){
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			}
			else if(filterOpChain.getFirstToken().kind == OP_CONVOLVE){
				mv.visitInsn(ACONST_NULL);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
			}
		}*/
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
		frameOpChain.getArg().visit(this, arg);
		switch(frameOpChain.getFirstToken().kind){
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
			break;
		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
			break;
		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
			break;
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
			break;
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
			break;
		}
		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec d = identChain.getDec();
		String varName = identChain.getFirstToken().getText();
		if(arg.equals(true)){
			if(d instanceof ParamDec){
				if(d.getTypeName().equals(TypeName.URL)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				}
				else if(d.getTypeName().equals(TypeName.FILE)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
				}
				else{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
				}
			}
			else if(d.getTypeName().equals(TypeName.IMAGE) || d.getTypeName().equals(TypeName.FRAME)){
				mv.visitVarInsn(ALOAD, d.getSlot());
			}
			else{
				mv.visitVarInsn(ILOAD, d.getSlot());
			}
		}
		else{
			if(d.getTypeName().getJVMTypeDesc() == "I" || d.getTypeName().getJVMTypeDesc() == "Z"){
				if(d instanceof ParamDec){
					mv.visitInsn(DUP);
					mv.visitIntInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
				}
				else{
					mv.visitInsn(DUP);
					mv.visitVarInsn(ISTORE, d.getSlot());
				}
			}
			else if(d.getTypeName().equals(TypeName.URL)){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
			}
			else if(d.getTypeName().equals(TypeName.IMAGE)){
				//mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, d.getSlot());
			}
			else if(d.getTypeName().equals(TypeName.FILE)){
				mv.visitIntInsn(ALOAD, 0);
				//mv.visitInsn(SWAP);
				mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
			}
			else if(d.getTypeName().equals(TypeName.FRAME)){
				mv.visitIntInsn(ALOAD, d.getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, d.getSlot());
			}
		}
		return identChain;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		String varName = identExpression.getFirstToken().getText();
		Dec d = identExpression.getDec();
		if(d instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
		}
		else if(d.getTypeName().equals(TypeName.IMAGE) || d.getTypeName().equals(TypeName.FRAME)){
			mv.visitVarInsn(ALOAD, d.getSlot());
		}
		else{
			mv.visitVarInsn(ILOAD, d.getSlot());
		}
		return identExpression;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		String varName = identX.getText();
		Dec d = identX.getDec();
		if(d.getTypeName().equals(TypeName.INTEGER) || d.getTypeName().equals(TypeName.BOOLEAN)){
			if(d instanceof ParamDec){
				mv.visitIntInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, varName, d.getTypeName().getJVMTypeDesc());
			}
			else{
				mv.visitVarInsn(ISTORE, d.getSlot());
			}
		}
		else if(d.getTypeName().equals(TypeName.IMAGE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			mv.visitVarInsn(ASTORE, d.getSlot());
		}
		else{
			mv.visitVarInsn(ASTORE, d.getSlot());
		}
		return identX;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this

		Label ifStmt = new Label();
		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, ifStmt);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(ifStmt);
		return ifStatement;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		switch(imageOpChain.getFirstToken().kind){
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
			break;
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
			break;
		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
			break;
		}
		return imageOpChain;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.value);
		return intLitExpression;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		paramDec.setSlot(-1);
		if(paramDec.getTypeName().getJVMTypeDesc() == "I"){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(pdcounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			pdcounter++;
		}
		else if(paramDec.getTypeName().getJVMTypeDesc() == "Z"){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false));
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(pdcounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
			pdcounter++;
		}
		else if(paramDec.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.FileDesc){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(pdcounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), PLPRuntimeImageIO.FileDesc);
			pdcounter++;
		}
		else if(paramDec.getTypeName().getJVMTypeDesc() == PLPRuntimeImageIO.URLDesc){
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(pdcounter);
			//mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), PLPRuntimeImageIO.URLDesc);
			pdcounter++;
		}
		fv.visitEnd();
		return paramDec;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return sleepStatement;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for(int i = 0; i < tuple.getExprList().size(); i++){
			tuple.getExprList().get(i).visit(this, arg);
			//mv.visitInsn(tuple.getExprList().get(i).getFirstToken().intVal());
		}
		return tuple;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label wStmtStart = new Label();
		Label wStmtEnd = new Label();

		mv.visitJumpInsn(GOTO, wStmtStart);
		mv.visitLabel(wStmtEnd);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(wStmtStart);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, wStmtEnd);

		return whileStatement;
	}

}
