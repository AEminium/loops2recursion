package aeminium.java.compiler.ltr.processing.utils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Stack;

import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssert;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtAnnotationType;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtVisitor;

public class VariablesUsedVisitor implements CtVisitor {
	
	CtClass<?> thisClass;
	public HashMap<String, CtTypeReference<?>> usages = new HashMap<String, CtTypeReference<?>>();
	Frame currentFrame = new Frame(null, usages);
	
	public VariablesUsedVisitor(CtClass<?> outterClass) {
		thisClass = outterClass;
	}

	/* General Scan Methods */
	private VariablesUsedVisitor scan(CtElement e) {
		if (e != null) {
			e.accept(this);
		}
		return this;
	}
	
	private VariablesUsedVisitor scan(CtReference ref) {
		if (ref != null) {
			ref.accept(this);
		}
		return this;
	}
	
	/* General Storage Methods */
	
	private void addUsage(String name, CtTypeReference<?> type) {
		currentFrame.addUsage(name, type);
	}
	
	private void addDeclaration(String name, CtTypeReference<?> type, CtExpression<?> defaultExpression) {
		currentFrame.addDeclaration(name, type, defaultExpression);
	}
	
	private void stackPush() {
		currentFrame = new Frame(currentFrame, usages);
	}
	private void stackPop() {
		currentFrame = currentFrame.parent;
	}
	
	@Override
	public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> node) {}

	@Override
	public <A extends Annotation> void visitCtAnnotationType(
			CtAnnotationType<A> node) {}

	@Override
	public void visitCtAnonymousExecutable(CtAnonymousExecutable node) {
		scan(node.getBody());
	}

	@Override
	public <T, E extends CtExpression<?>> void visitCtArrayAccess(
			CtArrayAccess<T, E> node) {
		scan(node.getTarget());
		scan(node.getIndexExpression());
	}

	@Override
	public <T> void visitCtArrayTypeReference(CtArrayTypeReference<T> node) {}

	@Override
	public <T> void visitCtAssert(CtAssert<T> node) {
		scan(node.getAssertExpression());
	}

	@Override
	public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> node) {
		scan(node.getAssigned());
		scan(node.getAssignment());
	}

	@Override
	public <T> void visitCtBinaryOperator(CtBinaryOperator<T> node) {
		scan(node.getLeftHandOperand());
		scan(node.getRightHandOperand());
	}

	@Override
	public <R> void visitCtBlock(CtBlock<R> node) {
		stackPush();
		for (CtStatement s : node.getStatements()) scan(s);
		stackPop();
	}

	@Override
	public void visitCtBreak(CtBreak node) {}

	@Override
	public <S> void visitCtCase(CtCase<S> node) {
		scan(node.getCaseExpression());
		for (CtStatement s : node.getStatements()) scan(s);
	}

	@Override
	public void visitCtCatch(CtCatch node) {
		stackPush();
		scan(node.getBody());
		stackPop();
	}

	@Override
	public <T> void visitCtClass(CtClass<T> node) {
		// Inner classes (?)
		stackPush();
		for (CtConstructor<?> s : node.getConstructors()) scan(s);
		for (CtMethod<?> s : node.getAllMethods()) scan(s);
		for (CtField<?> s : node.getFields()) scan(s);
		stackPop();
	}

	@Override
	public <T> void visitCtCodeSnippetExpression(CtCodeSnippetExpression<T> node) {
		// Ignore
	}

	@Override
	public void visitCtCodeSnippetStatement(CtCodeSnippetStatement node) {
		// Ignore
	}

	@Override
	public <T> void visitCtConditional(CtConditional<T> node) {
		scan(node.getCondition());
		scan(node.getThenExpression());
		scan(node.getElseExpression());
	}

	@Override
	public <T> void visitCtConstructor(CtConstructor<T> node) {
		stackPush();
		for (CtParameter<?> t : node.getParameters()) scan(t);
		scan(node.getBody());
		stackPop();
	}

	@Override
	public void visitCtContinue(CtContinue node) {}

	@Override
	public void visitCtDo(CtDo node) {
		scan(node.getBody());
		scan(node.getLoopingExpression());
	}

	@Override
	public <T extends Enum<?>> void visitCtEnum(CtEnum<T> node) {}

	@Override
	public <T> void visitCtExecutableReference(CtExecutableReference<T> node) {}

	@Override
	public <T> void visitCtField(CtField<T> node) {
		addDeclaration(node.getSimpleName(), node.getType(), node.getDefaultExpression());
		
	}

	@Override
	public <T> void visitCtFieldAccess(CtFieldAccess<T> node) {
		if (node.getTarget() != thisClass) {
			scan(node.getVariable());
		}
	}

	@Override
	public <T> void visitCtFieldReference(CtFieldReference<T> node) {
		if ( node.getQualifiedName() == thisClass.getQualifiedName() + "#" + node.getSimpleName()) {
			addUsage(node.getSimpleName(), node.getType());
		}
	}

	@Override
	public void visitCtFor(CtFor node) {
		stackPush();
		for ( CtStatement s: node.getForInit()) scan(s);
		for ( CtStatement s: node.getForUpdate()) scan(s);
		scan(node.getExpression());
		scan(node.getBody());
		stackPop();
	}

	@Override
	public void visitCtForEach(CtForEach node) {
		stackPush();
		scan(node.getExpression());
		scan(node.getBody());
		stackPop();
	}

	@Override
	public void visitCtIf(CtIf node) {
		scan(node.getCondition());
		scan(node.getThenStatement());
		scan(node.getElseStatement());
	}

	@Override
	public <T> void visitCtInterface(CtInterface<T> node) {}

	@Override
	public <T> void visitCtInvocation(CtInvocation<T> node) {
		scan(node.getTarget());
		for ( CtExpression<?> s: node.getArguments()) scan(s);
	}

	@Override
	public <T> void visitCtLiteral(CtLiteral<T> node) {
		// Ignore
	}

	@Override
	public <T> void visitCtLocalVariable(CtLocalVariable<T> node) {
		addDeclaration(node.getSimpleName(), node.getType(), node.getDefaultExpression());
	}

	@Override
	public <T> void visitCtLocalVariableReference(
			CtLocalVariableReference<T> node) {
		addUsage(node.getSimpleName(), node.getType());
	}

	@Override
	public <T> void visitCtMethod(CtMethod<T> node) {
		stackPush();
		scan(node.getBody());
		stackPop();
	}

	@Override
	public <T> void visitCtNewArray(CtNewArray<T> node) {
		for( CtExpression<Integer> s: node.getDimensionExpressions()) scan(s);
	}

	@Override
	public <T> void visitCtNewClass(CtNewClass<T> node) {
		scan(node.getExecutable());
		scan(node.getTarget());
	}

	@Override
	public <T, A extends T> void visitCtOperatorAssignement(
			CtOperatorAssignment<T, A> node) {
		scan(node.getAssigned());
		scan(node.getAssignment());
		
	}

	@Override
	public void visitCtPackage(CtPackage node) {
		// Ignore
	}

	@Override
	public void visitCtPackageReference(CtPackageReference node) {
		// Ignore
	}

	@Override
	public <T> void visitCtParameter(CtParameter<T> node) {
		addDeclaration(node.getSimpleName(), node.getType(), null);
	}

	@Override
	public <T> void visitCtParameterReference(CtParameterReference<T> node) {
		addUsage(node.getSimpleName(), node.getType());
	}

	@Override
	public <R> void visitCtReturn(CtReturn<R> node) {
		scan(node.getReturnedExpression());
		
	}

	@Override
	public <R> void visitCtStatementList(CtStatementList<R> node) {
		for (CtStatement s : node.getStatements()) scan(s);		
	}

	@Override
	public <S> void visitCtSwitch(CtSwitch<S> node) {
		scan(node.getSelector());
		for (CtCase<?> s : node.getCases()) scan(s);
	}

	@Override
	public void visitCtSynchronized(CtSynchronized node) {
		scan(node.getBlock());
	}

	@Override
	public void visitCtThrow(CtThrow node) {
		scan(node.getThrownExpression());
	}

	@Override
	public void visitCtTry(CtTry node) {
		scan(node.getBody());
		for (CtCatch s : node.getCatchers()) scan(s);
		scan(node.getFinalizer());
		
	}

	@Override
	public void visitCtTypeParameter(CtTypeParameter node) {}

	@Override
	public void visitCtTypeParameterReference(CtTypeParameterReference node) {}

	@Override
	public <T> void visitCtTypeReference(CtTypeReference<T> node) {}

	@Override
	public <T> void visitCtUnaryOperator(CtUnaryOperator<T> node) {
		scan(node.getOperand());
	}

	@Override
	public <T> void visitCtVariableAccess(CtVariableAccess<T> node) {
		addUsage(node.getVariable().getSimpleName(), node.getType());
	}

	@Override
	public void visitCtWhile(CtWhile node) {
		scan(node.getLoopingExpression());
		scan(node.getBody());
	}

}
