package test.tmp.david;

public @interface Lang {

	String[] include() default {};

	String[] exclude() default {};
}
