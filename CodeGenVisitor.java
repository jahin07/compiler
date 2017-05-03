package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.events.EndDocument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;
import java.awt.image.BufferedImage;
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
	static int slotNumber = 0;
	static int paramSlot = 0;
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

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
		paramSlot = 0;
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
		for(Dec dec: program.getB().getDecs())
		{
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getVal().getJVMTypeDesc(), null, startRun, endRun, dec.getSlot());
		}
		//mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);
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
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		if(assignStatement.getE().val.isType(TypeName.IMAGE))
			mv.visitInsn(DUP);
		assignStatement.getVar().visit(this, arg);
		if(assignStatement.getE().val.isType(TypeName.IMAGE))
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			mv.visitVarInsn(ASTORE, assignStatement.getVar().getDec().getSlot());
		}
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		binaryChain.getE0().setLeft(true);
		if(binaryChain.getE0() instanceof FilterOpChain)
			arg = binaryChain.getArrow().kind;
		binaryChain.getE0().visit(this, arg);
		if(binaryChain.getE1().firstToken.kind.equals(OP_HEIGHT) || binaryChain.getE1().firstToken.kind.equals(Kind.OP_WIDTH)
				 || binaryChain.getE1().firstToken.kind.equals(Kind.KW_XLOC) || binaryChain.getE1().firstToken.kind.equals(Kind.KW_YLOC))
			mv.visitInsn(DUP);
		/*switch(binaryChain.getE0().getTypeName())
		{
			case URL:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				break;
			case FILE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
				break;
			default:
				break;
		}*/
		//binaryChain.getE1().setLeft(false);
		if(binaryChain.getE1() instanceof FilterOpChain)
			arg = binaryChain.getArrow().kind;
		binaryChain.getE1().visit(this, arg);
		/*switch(binaryChain.getE1().getTypeName())
		{
			case URL:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				break;
			case FILE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
				break;
			default:
				break;
		}*/
		//mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		Label setTrue = new Label();
		Label endTrue = new Label();
		if (binaryExpression.getE0().getType() == TypeName.INTEGER && binaryExpression.getE1().getType() == TypeName.INTEGER )
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
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
				case AND:
					mv.visitInsn(IAND);
					break;
				case OR:
					mv.visitInsn(IOR);
					break;
				case MOD:
					mv.visitInsn(IREM);
					break;
				case NOTEQUAL:
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case EQUAL:
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				case LE:
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case LT:
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case GE:
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case GT:
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
					break;
				default:
					//throw new
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);
		}
		else if(binaryExpression.getE0().getType() == TypeName.BOOLEAN && binaryExpression.getE1().getType() == TypeName.BOOLEAN)
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
				case AND:
					mv.visitInsn(IAND);
					break;
				case OR:
					mv.visitInsn(IOR);
					break;
				case NOTEQUAL:
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case EQUAL:
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				case LE:
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case LT:
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case GE:
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case GT:
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
					break;
				default:
					//throw new
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);
		}
		else if (binaryExpression.getE0().getType() == TypeName.IMAGE && binaryExpression.getE1().getType() == TypeName.IMAGE )
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
				case PLUS:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
					break;
				case MINUS:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
					break;
				case NOTEQUAL:
					mv.visitJumpInsn(IF_ACMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case EQUAL:
					mv.visitJumpInsn(IF_ACMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				default:
					break;
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);
		}
		else if (binaryExpression.getE0().getType() == TypeName.IMAGE && binaryExpression.getE1().getType() == TypeName.INTEGER )
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
				case DIV:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
					break;
				case TIMES:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
					break;
				case MOD:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
					break;
				default:
					break;
			}
		}
		else if (binaryExpression.getE0().getType() == TypeName.INTEGER && binaryExpression.getE1().getType() == TypeName.IMAGE )
		{
			binaryExpression.getE1().visit(this, arg);
			binaryExpression.getE0().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
				case TIMES:
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
					break;
				default:
					break;
			}
		}
		else if(binaryExpression.getE0().getType() == binaryExpression.getE1().getType())
		{
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			switch (binaryExpression.getOp().kind)
			{
				case NOTEQUAL:
					mv.visitJumpInsn(IF_ACMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case EQUAL:
					mv.visitJumpInsn(IF_ACMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				default:
					break;
			}
			mv.visitJumpInsn(GOTO, endTrue);
			mv.visitLabel(setTrue);
			mv.visitLdcInsn(true);
			mv.visitLabel(endTrue);
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception
	{
		Label startBlock = new Label();
		Label endBlock = new Label();
		mv.visitLabel(startBlock);
		for(Dec dec:block.getDecs())
		{
			if(dec.getVal().isType(FRAME,IMAGE))
			{
				mv.visitInsn(ACONST_NULL);
				mv.visitVarInsn(ASTORE, slotNumber);
			}
			dec.visit(this, arg);
		}
		for(Statement s:block.getStatements())
		{
			if(s instanceof AssignmentStatement && ((AssignmentStatement) s).getVar().getDec() instanceof ParamDec)
			{
				mv.visitVarInsn(ALOAD, 0);
			}
			s.visit(this, arg);
			if(s instanceof BinaryChain)
				mv.visitInsn(POP);
		}
		mv.visitLabel(endBlock);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		if(constantExpression.getFirstToken().kind == KW_SCREENHEIGHT)
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		else
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception
	{
		declaration.setSlot(slotNumber);
		slotNumber++;
		/*if(declaration.getTypeName().isType(TypeName.IMAGE,TypeName.FRAME))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE,slotNumber++);
		}*/
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception
	{
		filterOpChain.getArg().visit(this, arg);
		Kind kind = (Kind) arg;
		if(kind.equals(BARARROW) && filterOpChain.firstToken.kind.equals(OP_GRAY))
		{
			mv.visitInsn(DUP);
			//mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			//mv.visitInsn(SWAP);
		}
		else
		{
			mv.visitInsn(ACONST_NULL);
		}
		switch (filterOpChain.getFirstToken().kind)
		{
			case OP_BLUR:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
				break;
			case OP_CONVOLVE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
				break;
			case OP_GRAY:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception
	{
		frameOpChain.getArg().visit(this, arg);
		switch(frameOpChain.getFirstToken().kind)
		{
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
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception
	{
		if(identChain.getDec() instanceof ParamDec)
		{
			mv.visitVarInsn(ALOAD, 0);
			if(identChain.isLeft())
			{
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getVal().getJVMTypeDesc());
				switch(identChain.getDec().getVal())
				{
					case URL:
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
						break;
					case FILE:
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
						break;
					default:
						break;
				}
			}
			else
			{
				if(identChain.val.isType(TypeName.FILE))
				{
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getVal().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
					mv.visitInsn(DUP);
				}
				else
				{
					if(!identChain.getType().isType(TypeName.INTEGER))
						mv.visitInsn(DUP);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getVal().getJVMTypeDesc());
				}
			}
		}
		else
		{
			if(identChain.isLeft())
			{
				if(identChain.getType().isType(TypeName.INTEGER, TypeName.BOOLEAN))
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				else if(identChain.getType().isType(URL))
				{
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
				}
				else if(identChain.getType().isType(TypeName.FILE))
				{
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
				}
				else
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
			}
			else
			{
				switch(identChain.getType())
				{
					case INTEGER:
						mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
						break;
					case IMAGE:
						mv.visitInsn(DUP);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
						break;
					case FILE:
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
						mv.visitInsn(DUP);
						break;
					case FRAME:
						mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
						//	mv.visitInsn(SWAP);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitInsn(DUP);
						mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
						break;
					default:
						break;
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception
	{
		Dec dec = identExpression.getDec();
		if(dec instanceof ParamDec)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getVal().getJVMTypeDesc());
		}
		else
		{
			if(dec.val.isType(TypeName.INTEGER,TypeName.BOOLEAN))
				mv.visitVarInsn(ILOAD, dec.getSlot());
			else
				mv.visitVarInsn(ALOAD, dec.getSlot());
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception
	{
		Dec dec = identX.getDec();
		if(dec instanceof ParamDec)
		{
			//mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), dec.getVal().getJVMTypeDesc());
		}
		else
		{
			if(dec.val.isType(TypeName.INTEGER,TypeName.BOOLEAN))
				mv.visitVarInsn(ISTORE, dec.getSlot());
			else
				mv.visitVarInsn(ASTORE, dec.getSlot());
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception
	{
		Label afterIf = new Label();
		ifStatement.getE().visit(this, arg); // Expression
		mv.visitJumpInsn(IFEQ, afterIf); //IFEQ AFTER
		Label startIf = new Label();
		mv.visitLabel(startIf);
		ifStatement.getB().visit(this, arg); //BLOCK
		Label endIf = new Label();
		mv.visitLabel(endIf);
		mv.visitLabel(afterIf); //AFTER
		Label endAfterIf = new Label();
		mv.visitLabel(endAfterIf);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception
	{
		imageOpChain.getArg().visit(this, arg);
		switch(imageOpChain.firstToken.kind)
		{
			case OP_WIDTH:
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
				break;
			case OP_HEIGHT:
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
				break;
			case KW_SCALE:
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception
	{
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception
	{
		//For assignment 5, only needs to handle integers and booleans
		FieldVisitor fv;
		paramDec.setSlot(slotNumber++);
		fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getVal().getJVMTypeDesc(), null, null);
		fv.visitEnd();
		mv.visitVarInsn(ALOAD, 0);
		switch(paramDec.val)
		{
			case BOOLEAN:
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramSlot);
				paramSlot++;
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				break;
			case INTEGER:
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramSlot);
				paramSlot++;
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				break;
			case URL:
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramSlot++);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
				break;
			case FILE:
				mv.visitTypeInsn(NEW, "java/io/File");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramSlot);
				paramSlot++;
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);

				break;
			default:
				break;
		}
		mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getVal().getJVMTypeDesc());
		return null;
	}
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception
	{
		sleepStatement.getE().visit(this, arg);
		Label TryStart = new Label();
		Label TryEnd = new Label();
		Label TryHandle = new Label();
		mv.visitTryCatchBlock(TryStart, TryEnd, TryHandle, "java/lang/InterruptedException");
		mv.visitLabel(TryStart);

		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		mv.visitLabel(TryEnd);

		Label CatchEnd = new Label();
		mv.visitJumpInsn(GOTO, CatchEnd);
		mv.visitLabel(TryHandle);

		mv.visitVarInsn(ASTORE, slotNumber);
		Label CatchStart = new Label();
		mv.visitLabel(CatchStart);

		mv.visitVarInsn(ALOAD, slotNumber);
		slotNumber++;
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/InterruptedException", "printStackTrace", "()V", false);
		mv.visitLabel(CatchEnd);

		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception
	{
		for(Expression expr:tuple.getExprList())
			expr.visit(this, arg);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception
	{
		Label whileGuard = new Label();
		Label whileBody = new Label();
		mv.visitJumpInsn(GOTO, whileGuard); //GOTO GUARD
		mv.visitLabel(whileBody);
		whileStatement.getB().visit(this, arg); //BODY
		Label endWhileBody = new Label();
		mv.visitLabel(endWhileBody);
		mv.visitLabel(whileGuard);
		whileStatement.getE().visit(this, arg); //GUARD
		Label endWhileGuard = new Label();
		mv.visitLabel(endWhileGuard);
		mv.visitJumpInsn(IFNE, whileBody); //IFNE BODY
		return null;
	}

}
