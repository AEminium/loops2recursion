package aeminium.java.compiler.ltr.launcher;

import java.util.List;

import spoon.Launcher;
import aeminium.java.compiler.ltr.processing.ForToRecProcessor;
import aeminium.java.compiler.ltr.processing.WhileToRecProcessor;

import com.martiansoftware.jsap.JSAPException;

public class LTRCompilerLauncher extends Launcher {

	public LTRCompilerLauncher(String[] args) throws JSAPException {
		super(args);
	}
	
	@Override
	protected List<String> getProcessorTypes() {
		List<String> l = super.getProcessorTypes();
		l.add(WhileToRecProcessor.class.getName());
		l.add(ForToRecProcessor.class.getName());
		return l;
	}

	public static void main(String[] args) {
		try {
			LTRCompilerLauncher launcher = new LTRCompilerLauncher(args);
			launcher.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	

}
