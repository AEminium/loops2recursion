package aeminium.java.compiler.ltr.launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;

import spoon.Launcher;
import spoon.support.builder.CtResource;
import spoon.support.builder.support.CtFolderFile;
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

	@Override
	protected List<CtResource> getTemplateSources() {
		List<CtResource> l = super.getTemplateSources();
		try {
			l.add(new CtFolderFile(new File("compiler-src/aeminium/java/compiler/ltr/template")));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
