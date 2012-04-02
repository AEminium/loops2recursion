package aeminium.java.compiler.ltr.processing;

import java.util.ArrayList;
import java.util.Set;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.CodeFactory;
import spoon.reflect.factory.ExecutableFactory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.template.Substitution;
import spoon.template.Template;
import aeminium.java.compiler.ltr.template.WhileToRecTemplate;

public class WhileToRecProcessor extends AbstractLambdaProcessor<CtWhile>{
    
	@Override
	public void process(CtWhile target) {
		
		CtClass<?> outterClass = target.getParent(CtClass.class);
		System.out.println("While Found in " + outterClass.getSimpleName());
		
		Template t = new WhileToRecTemplate(target.getLoopingExpression());
		Substitution.insertAllMethods(outterClass, t);
		
		CtMethod<?> met = outterClass.getMethod("aeminium_rec_method");
		
		// Make static work
        boolean isStatic = checkStatic(target.getParent(CtMethod.class));
        if (isStatic) isStatic = makeStatic(met);
		
		
		// Add body to if.
		CtIf iff = (CtIf) met.getBody().getStatements().get(0);
		CtBlock<?> body = (CtBlock<?>) target.getBody();
		body.getStatements().add(createMethodInvocation(outterClass, isStatic));
		iff.setThenStatement(body);
		
        
        // TODO arguments
        
		CtInvocation<?> repl = createMethodInvocation(outterClass, isStatic);
		target.replace(repl);
	}

	private CtInvocation<?> createMethodInvocation(CtClass<?> outterClass,
			boolean isStatic) {
		CodeFactory cf = getFactory().Code();
	    TypeFactory tf = getFactory().Type();
	    ExecutableFactory ef = getFactory().Executable();
		
		CtExecutableReference<?> method = ef.createReference(
                tf.createReference(outterClass),
                isStatic,
                null, // void
                "aeminium_rec_method"); // takes types then
        CtInvocation<?> repl =
                cf.createInvocation(null, method, new ArrayList<CtExpression<?>>());
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
