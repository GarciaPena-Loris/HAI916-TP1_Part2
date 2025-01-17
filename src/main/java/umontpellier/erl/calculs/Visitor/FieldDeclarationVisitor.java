package umontpellier.erl.calculs.Visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import java.util.ArrayList;
import java.util.List;

public class FieldDeclarationVisitor extends ASTVisitor {
    List<FieldDeclaration> fields = new ArrayList<>();

    @Override
    public boolean visit(FieldDeclaration node) {
        fields.add(node);
        return super.visit(node);
    }

    public List<FieldDeclaration> getFields() {
        return fields;
    }
}