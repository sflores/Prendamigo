package test.tmp.david;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LangFilter implements IMethodInterceptor {
	
	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		
		String envLang = context.getCurrentXmlTest().getParameter("lang");
		System.out.println("lang:" + envLang);

		List<IMethodInstance> result = new ArrayList<IMethodInstance>();
		List<String> keptMethods = new ArrayList<String>();

		for (IMethodInstance method : methods) {
			String methodName = method.getMethod().getMethodName();
			Lang methodAnnotation = method.getMethod().getMethod().getAnnotation(Lang.class);
			if (methodAnnotation == null) {
				//include non-annotated methods
				System.out.println("Keeping: " + methodName);
				result.add(method);
				keptMethods.add(methodName);
			} else {
				//get Type level annotation
				List<String> excludedLangs = new ArrayList<String>();
				List<String> includedLangs = new ArrayList<String>();

				//get Method level annotation
				if (methodAnnotation != null) {
					excludedLangs.addAll(Arrays.asList(methodAnnotation.exclude()));					
					includedLangs.addAll(Arrays.asList(methodAnnotation.include()));
				}
				//If lang matches the exclude list, always exclude.
				if (excludedLangs.contains(envLang)) {
					System.out.println("Excluding: " + methodName); 		
				}
				//If config.properties intl doesn't match include or exclude list, do not run
				else if (!includedLangs.isEmpty() && !includedLangs.contains(envLang)){ 
					System.out.println("Excluding: " + methodName);
				}
				//Run for all other cases
				else {
					System.out.println("Keeping: " + methodName);
					result.add(method);
					keptMethods.add(methodName);
				}
			}
		}
		System.out.println("Filter result set:" + keptMethods);
		return result;		
	}

}

