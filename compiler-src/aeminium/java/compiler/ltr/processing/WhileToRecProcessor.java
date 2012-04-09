package aeminium.java.compiler.ltr.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.factory.ExecutableFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.template.Substitution;
import spoon.template.Template;
import aeminium.java.compiler.ltr.processing.utils.Counter;
import aeminium.java.compiler.ltr.processing.utils.VariablesUsedVisitor;
import aeminium.java.compiler.ltr.processing.utils.methodfixer.ThrownTypesMethodFixer;
import aeminium.java.compiler.ltr.template.WhileToRecTemplate;

public class WhileToRecProcessor extends AbstractLoopToRecProcessor<CtWhile>{
    
	boolean isStatic;

	@Override
	public void process(CtWhile target) {
		int id = Counter.getId();
		
		CtClass<?> outterClass = target.getParent(CtClass.class);
		System.out.println("While found in class " + outterClass.getSimpleName() + " in " + target.getPosition());
		
		
		// Add skeleton method
		Template t = new WhileToRecTemplate();
		Substitution.insertAllMethods(outterClass, t);
		
		
		// Rename method
		CtMethod<?> met = outterClass.getMethod("aeminium_rec_while_method");
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
        isStatic = checkStatic(target.getParent(CtMethod.class));
        if (isStatic) isStatic = makeStatic(met);
		
        // Add cond to if
        CtIf iff = (CtIf) met.getBody().getStatements().get(0);
		iff.setCondition(target.getLoopingExpression());
        
		// Add body to if.
		CtInvocation<?> inv = createMethodInvocation(outterClass, isStatic, met.getReference(), orderedVariables, variableTypes);
		CtStatement body = (CtStatement) target.getBody();
		CtBlock<?> block;
		List<CtStatement> stms;
		if (body instanceof CtBlock) {
			block = (CtBlock<?>) body;
			stms = ((CtBlock<?>) body).getStatements();
		} else {
			block = getFactory().Core().createBlock();
			stms = new ArrayList<CtStatement>();
			stms.add(body);
		}
		stms.add(inv);
		block.setStatements(stms);
		iff.setThenStatement(block);
		CtInvocation<?> repl = createMethodInvocation(outterClass, isStatic, met.getReference(), orderedVariables, variableTypes);
		target.replace(repl);
		
		met.setDocComment("Generated from the While cycle in line " + target.getPosition().getLine() + " of the original file.");
		
		new ThrownTypesMethodFixer(met).fix();
		checkConsistency(target);
	}
	
}
