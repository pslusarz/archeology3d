import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
import org.sw7d.archeology.tools.NameParser
import org.sw7d.archeology.tools.OutputFileGoldplating

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;


def isGenerated(ArcheologyFile file) {
    file.canonicalPath.contains('thrift') || 
    file.canonicalPath.contains('protobuf/generated') || 
    file.canonicalPath.contains('codegen')
}

List<DataPoint3d> dp = []

//modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
//         !isGenerated(it)}.each { ArcheologyFile file ->
//         
//               
//}

Module ant = modules.find {it.name == 'ant'}
ant.path = new File(ant.path).canonicalPath.replaceAll("apache-data","archeology3d-data/apache")
println ant.path
GroovyDocTool plainTool = new GroovyDocTool(["${ant.path}/src/main", "${ant.path}/src/tests/junit"] as String[]);
//plainTool.add(["org/sw7d/archeology/parsing/NameParserTest.groovy"]);


List<String> files = []

modules.find {it.name == 'ant'}.files.findAll{it.javaName()}.each {
    files << it.javaName().replaceAll(/\./, "/")+"."+it.extension()
}

plainTool.add (files)

GroovyRootDoc root = plainTool.getRootDoc();
GroovyClassDoc[] classDocs = root.classes();
classDocs.each { GroovyClassDoc clazz ->
    println "========================="
    println clazz.qualifiedTypeName()
    println "  interfaces: "+clazz.interfaces()
    println "  superclass: "+  clazz.superclass()?.name()
    println "  imports: "+clazz.importedClasses()
    println "  methods: "+clazz.methods()
    println "  imported packages"+clazz.importedPackages()
    GroovyMethodDoc[] methodDocs = clazz.methods();
    println ""
}

return new DataPointProvider(xLabel: "",
    yLabel: "",
    zLabel: "", dataPoints: dp)
