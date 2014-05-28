package org.sw7d.archeology.tools

import org.junit.Test



import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;


class GroovyDocTest {

    @Test
    public void ttt() {
        GroovyDocTool plainTool = new GroovyDocTool(["src/test/groovy"] as String[]);
        plainTool.add(["org/sw7d/archeology/parsing/NameParserTest.groovy"]);
        GroovyRootDoc root = plainTool.getRootDoc();
        GroovyClassDoc[] classDocs = root.classes();
        boolean seenThisMethod = false;
        for (int i = 0; i < classDocs.length; i++) {
            GroovyClassDoc clazz = root.classes()[i];
            assert "NameParserTest" == clazz.name()
            println clazz.qualifiedTypeName()
            println clazz.interfaces()
            println clazz.superclass()?.name()
            println clazz.importedClasses()
            println clazz.methods()
            println clazz.importedPackages()
            GroovyMethodDoc[] methodDocs = clazz.methods();
            for (int j = 0; j < methodDocs.length; j++) {
                GroovyMethodDoc method = clazz.methods()[j];
                if ("ttt".equals(method.name())) {
                    seenThisMethod = true;
                    break;
                }
            }
        }
        assert seenThisMethod
        
    }
}
