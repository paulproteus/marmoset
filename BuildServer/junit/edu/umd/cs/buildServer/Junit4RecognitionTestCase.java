package edu.umd.cs.buildServer;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.SimpleElementValue;
import org.junit.Test;

public class Junit4RecognitionTestCase {
	@Test(timeout = 500)
	public void testTimeExtractor() throws Exception {
		JavaClass jClass = Repository.lookupClass(Junit4RecognitionTestCase.class.getName());
		for(Method m : jClass.getMethods()){
			System.out.println(m);
			for(AnnotationEntry a : m.getAnnotationEntries()) {

				System.out.println(a.getAnnotationType());
				for(ElementValuePair p : a.getElementValuePairs()) {
					System.out.println("  " + p.getNameString());
					ElementValue value = p.getValue();
					System.out.println("  " + value.getClass().getName());
					if (value instanceof SimpleElementValue) {
						SimpleElementValue s = (SimpleElementValue) value;
						System.out.println("  " + s.getElementValueType());
						System.out.println("  " + s.getValueLong());
					}
				}

			}


		}

	}

}
