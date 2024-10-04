package umontpellier.erl.calculs.Visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.ArrayList;
import java.util.List;

public class MethodInvocationsVisitor extends ASTVisitor {
	private final List<MethodInvocation> methodInvocations = new ArrayList<>();
	private final List<SuperMethodInvocation> superMethodInvocations = new ArrayList<>();
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		methodInvocations.add(methodInvocation);
		return super.visit(methodInvocation);
	}
	
	@Override
	public boolean visit(SuperMethodInvocation superMethodInvocation) {
		superMethodInvocations.add(superMethodInvocation);
		return super.visit(superMethodInvocation);
	}
	
	public List<MethodInvocation> getMethodInvocations(){
		return methodInvocations;
	}
	
	public List<SuperMethodInvocation> getSuperMethodInvocations() {
		return superMethodInvocations;
	}

}
