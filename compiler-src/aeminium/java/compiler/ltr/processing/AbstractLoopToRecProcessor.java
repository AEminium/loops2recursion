package aeminium.java.compiler.ltr.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.ModelConsistencyChecker;

public abstract class AbstractLoopToRecProcessor<T extends CtElement> extends
		AbstractProcessor<T> {

	protected boolean makeStatic(CtModifiable met) {

		boolean isStatic;
		Set<ModifierKind> mods = met.getModifiers();
		mods.add(ModifierKind.STATIC);
		met.setModifiers(mods);
		isStatic = true;
		return isStatic;
	}

	protected boolean checkStatic(CtModifiable outterMethod) {
		boolean isStatic = false;
		for (ModifierKind m : outterMethod.getModifiers()) {
			if (m == ModifierKind.STATIC)
				isStatic = true;
		}
		return isStatic;
	}
	
	protected CtInvocation<?> createMethodInvocation(CtClass<?> outterClass,
			boolean isStatic, CtExecutableReference<?> method, Set<String> orderedVariables, HashMap<String, CtTypeReference<?>> variableTypes) {
		CodeFactory cf = getFactory().Code();
		List<CtExpression<?>> arguments = new ArrayList<CtExpression<?>>();
		for (String u: orderedVariables) {
			CtExpression<?> p = cf.createVariableAccess(cf.createLocalVariableReference(variableTypes.get(u), u), false);
			arguments.add(p);
		}
				
        CtInvocation<?> repl = cf.createInvocation(null, method, arguments);
		return repl;
	}
	
	protected CtInvocation<?> createMethodInvocation(CtClass<?> outterClass,
			boolean isStatic, CtExecutableReference<?> method,
			Set<String> orderedVariables,
			HashMap<String, CtTypeReference<?>> variableTypes,
			HashMap<String, CtExpression<?>> variableDefaults) {
		
		CodeFactory cf = getFactory().Code();
		List<CtExpression<?>> arguments = new ArrayList<CtExpression<?>>();
		for (String u: orderedVariables) {
			if (variableDefaults.containsKey(u)) {
				arguments.add(variableDefaults.get(u));
			} else {
				CtExpression<?> p = cf.createVariableAccess(cf.createLocalVariableReference(variableTypes.get(u), u), false);
				arguments.add(p);
			}
		}
				
        CtInvocation<?> repl = cf.createInvocation(null, method, arguments);
		return repl;
		
	}
	
	protected void checkConsistency(CtElement target) {
		// Check for consistency and correct it.
		ModelConsistencyChecker checker = new ModelConsistencyChecker(this.getEnvironment(), true);
		checker.enter(target);
	}
}
