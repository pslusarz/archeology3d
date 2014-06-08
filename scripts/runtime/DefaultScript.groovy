import org.sw7d.archeology.Module
import org.sw7d.archeology.Modules
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
import org.sw7d.archeology.Settings


def isGenerated(ArcheologyFile file) {
    file.canonicalPath.contains('thrift') || 
    file.canonicalPath.contains('protobuf/generated') || 
    file.canonicalPath.contains('codegen')
}

List<DataPoint3d> dp = []

Set<String> paths = new HashSet<String>()
List<String> files = []

modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it) && !(it.canonicalPath.contains('/test/') || it.canonicalPath.contains('/integration/'))}.each { ArcheologyFile file ->
         String packagePath = file.javaName().replaceAll(/\./, "/")+"."+file.extension()
         //println (file.canonicalPath - packagePath)
         paths << file.canonicalPath - packagePath 
         files << packagePath
    dp << new DataPoint3d(z: file.linesCount, x: file.popularity, y: file.commits.size(), name: file.name)           
}



//Module ant = modules.find {it.name == 'ant'}
//ant.path = new File(ant.path).canonicalPath.replaceAll("apache-data","archeology3d-data/apache")
//println ant.path
String common = "/Users/ps/sandbox-jmonkey/archeology3d-data/autoreports/stash/"

GroovyDocTool plainTool = new GroovyDocTool(paths.toArray() as String[]);
//
//
//List<String> files = []
//
//modules.find {it.name == 'ant'}.files.findAll{it.javaName()}.each {
//    files << it.javaName().replaceAll(/\./, "/")+"."+it.extension()
//}
//
plainTool.add (files)
//

Map<String,List<GroovyClassDoc>> interfaceImplementations = [:].withDefault{[]}

GroovyRootDoc root = plainTool.getRootDoc();
GroovyClassDoc[] classDocs = root.classes();
classDocs.each { GroovyClassDoc clazz ->
//    println "========================="
//    println clazz.qualifiedTypeName()
//    println "  interfaces: "+clazz.interfaces()
    clazz.interfaces().each {
      interfaceImplementations[it.qualifiedTypeName()] << clazz        
    }
//    println "  superclass: "+  clazz.superclass()?.name()
//    println "  imports: "+clazz.importedClasses()
//    println "  methods: "+clazz.methods()
//    println "  imported packages"+clazz.importedPackages()
//    GroovyMethodDoc[] methodDocs = clazz.methods();
//    println ""
}

interfaceImplementations.sort{it.value.size()}.each { key, value ->
    println key+ "  "+value.size()
    println "    "+value.collect {it.qualifiedTypeName()}
}



return new DataPointProvider(xLabel: "popularity",
    yLabel: "changes",
    zLabel: "lines", dataPoints: dp)
