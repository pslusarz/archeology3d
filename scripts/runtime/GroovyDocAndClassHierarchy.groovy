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


List<DataPoint3d> dp = []

Set<String> paths = new HashSet<String>()
List<String> files = []

modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && !(it.canonicalPath.contains('/test/') || it.canonicalPath.contains('/integration/'))}.each { ArcheologyFile file ->
         String packagePath = file.javaName().replaceAll(/\./, "/")+"."+file.extension()
         paths << file.canonicalPath - packagePath 
         files << packagePath
    dp << new DataPoint3d(z: file.linesCount, x: file.popularity, y: file.commits.size(), name: file.name)           
}

GroovyDocTool plainTool = new GroovyDocTool(paths.toArray() as String[]);
plainTool.add (files)

Map<String,List<GroovyClassDoc>> interfaceImplementations = [:].withDefault{[]}

GroovyRootDoc root = plainTool.getRootDoc();
GroovyClassDoc[] classDocs = root.classes();
classDocs.each { GroovyClassDoc clazz ->
    clazz.interfaces().each {
      interfaceImplementations[it.qualifiedTypeName()] << clazz        
    }
}

interfaceImplementations.sort{it.value.size()}.each { key, value ->
    println key+ "  "+value.size()
    println "    "+value.collect {it.qualifiedTypeName()}
}

String graphName = "autoreports"

String output = """digraph ${graphName} {
    rankdir=LR;
"""

classDocs.each { GroovyClassDoc clazz ->
    clazz.interfaces().each { GroovyClassDoc iface ->
       if (iface.qualifiedTypeName().contains("carfax")) {
         output += "  ${clazz.name().replaceAll(/\./,'_')} -> ${iface.name()} [color=\"red\"]; \n"    
       }
    }
    if (clazz.superclass() && clazz.superclass().qualifiedTypeName().contains("carfax")) {
        output += "  ${clazz.name().replaceAll(/\./,'_')} -> ${clazz.superclass().name()}; \n"
    }
}
output += "}"
println output
def file = new File("graphviz.in")
file.delete()
file.write(output)
"/usr/local/bin/dot -o${graphName}.png -Tpng graphviz.in".execute().waitFor()
"open ${graphName}.png".execute()



return new DataPointProvider(xLabel: "popularity",
    yLabel: "changes",
    zLabel: "lines", dataPoints: dp)
