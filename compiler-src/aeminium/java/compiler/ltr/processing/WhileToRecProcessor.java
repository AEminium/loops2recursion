package aeminium.java.compiler.ltr.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.factory.ExecutableFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.template.Substitution;
import spoon.template.Template;
import aeminium.java.compiler.ltr.processing.utils.Counter;
import aeminium.java.compiler.ltr.processing.utils.VariablesUsedVisitor;
import aeminium.java.compiler.ltr.template.WhileToRecTemplate;

public class WhileToRecProcessor extends AbstractLambdaProcessor<CtWhile>{
    
	@Override
	public void process(CtWhile target) {
		int id = Counter.getId();
		
		
		CtClass<?> outterClass = target.getParent(CtClass.class);
		System.out.println("While Found in " + outterClass.getSimpleName());
		
		Template t = new WhileToRecTemplate();
		Substitution.insertAllMethods(outterClass, t);
		
		CtMethod<?> met = outterClass.getMethod("aeminium_rec_method");
		met.setSimpleName( met.getSimpleName() + "_" + id );
		
		
		// Which arguments to pass?
		VariablesUsedVisitor varVis = new VariablesUsedVisitor(outterClass);
		target.accept(varVis);
		HashMap<String, CtTypeReference<?>> variableTypes = varVis.usages;
		Set<String> orderedVariables = variableTypes.keySet();
		
		// Add arguments to method definition
		List<CtParameter<?>> parList = new ArrayList<CtParameter<?>>();
		for (String u: orderedVariables) {
			ExecutableFactory ef = getFactory().Executable();
			CtParameter<?> p = ef.createParameter(met, variableTypes.get(u), u);
			parList.add(p);
		}
		met.setParameters(parList);
		
		// Make static work
        boolean isStatic = checkStatic(target.getParent(CtMethod.class));
        if (isStatic) isStatic = makeStatic(met);
		
        // Add cond to if
        CtIf iff = (CtIf) met.getBody().getStatements().get(0);
		iff.setCondition(target.getLoopingExpression());
        
		// Add body to if.
		CtBlock<?> body = (CtBlock<?>) target.getBody();
		body.getStatements().add(createMethodInvocation(outterClass, isStatic, met.getReference(), orderedVariables, variableTypes));
		iff.setThenStatement(body);
		
		CtInvocation<?> repl = createMethodInvocation(outterClass, isStatic, met.getReference(), orderedVariables, variableTypes);
		target.replace(repl);
	}

	private CtInvocation<?> createMethodInvocation(CtClass<?> outterClass,
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

	private boolean makeStatic(CtMethod<?> met) {
		boolean isStatic;
		Set<ModifierKind> mods = met.getModifiers();
		mods.add(ModifierKind.STATIC);
		met.setModifiers(mods);
		isStatic = true;
		return isStatic;
	}

	private boolean checkStatic(CtMethod<?> outterMethod) {
		boolean isStatic = false;
        for (ModifierKind m :  outterMethod.getModifiers()) {
        	if (m == ModifierKind.STATIC) isStatic = true;
        }
		return isStatic;
	}
	
}
