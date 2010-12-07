package test.tmp.david;

import org.testng.annotations.Test;

public class LangFilterTest {
	
	@Test
	public void method1() {
		System.out.println("Executing method1");
	}
	
	@Test
	@Lang(include="en")
	public void method2() {
		System.out.println("Executing method2");
	}

	@Test
	@Lang(exclude="en")
	public void method3() {		
		System.out.println("Executing method3");
	}

	@Test
	@Lang(include="en", exclude="fr")
	public void method4() {		
		System.out.println("Executing method4");
	}

	@Test
	@Lang(include="fr")
	public void method5() {		
		System.out.println("Executing method5");
	}
}

