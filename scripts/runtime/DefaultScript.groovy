//import org.sw7d.archeology.Module
//import org.sw7d.archeology.ArcheologyFile
//import org.sw7d.archeology.data.DataPointProvider
//import org.sw7d.archeology.data.DataPoint3d
//import org.sw7d.archeology.data.Scale3d
//
//List<DataPoint3d> dp = []
//
//def moduleColors = [
//      'tuscany-sca-1.x'          : 'Orange',
//      'mahout'        : 'Brown',
//      'cassandra'    : 'Cyan',
//      //'hadoop-common': 'Blue',
//      'camel'   : 'Yellow'  
//    ]
//
//modules.findAll{moduleColors.keySet().contains(it.name)}*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
//         !it.isGenerated()}.each { ArcheologyFile file ->
//            
//    def color = moduleColors[file.module.name]
//    if (color) {
//            dp << new DataPoint3d(name: file.javaName(), 
//                x: file.popularity * 1000 / file.module.files.size(), 
//                y: (file.linesCount),
//                z: file.commits.size(),  
//                delegate: file, color: color, size: 4)
//      }      
//    }
//return new DataPointProvider(xLabel: "relative popularity",
//    yLabel: "size",
//    zLabel: "commits", dataPoints: dp, scale: new Scale3d(x: 2, y: 0.2, z: 1))



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

def moduleNames = [
    "tuscany-sca-1.x"
    ,"hadoop-common"
    ,"cassandra"
    ,"hbase"
    ,"camel"
    ,"lucene-solr"
    ,"mahout"
]
modules.findAll{moduleNames.contains(it.name)}.eachWithIndex { Module ant, int index ->
    println "======================  ${ant.name}   ========================"
    List<ArcheologyFile> classFiles =  ant.files.findAll {it.javaName() && !it.isGenerated() && it.popularity > 25} //(it.popularity > it.module.files.size() * 0.001)}
    println ant.name+" "+classFiles.size()
    if (classFiles.size > 0) {
        Date current = classFiles*.commits.flatten().min()
        Date last = classFiles*.commits.flatten().max()
        int x = 0
        List<ArcheologyFile> previousTotal = []
        List<ArcheologyFile> previousClosed = []
        while (current <= last) {
            List<ArcheologyFile> totalToDate = classFiles.findAll {ArcheologyFile file -> file.commits.find {Date date -> date <= current}}
            List<ArcheologyFile> totalClosed = totalToDate.findAll {ArcheologyFile file -> file.commits.max() <= current}
            println "+++++ ${current.format('YYYY/MM/dd')} +total: ${totalToDate.size() - previousTotal.size()} +closed: ${totalClosed.size() - previousClosed.size()} total: ${totalToDate.size()} closed: ${totalClosed.size()} +++++"
            println "+++ Closed classes: ++++"
//            (totalClosed - previousClosed).each {
//              println "     ${it.javaName()}   ${it.canonicalPath}"        
//            }
            current += 30
            x += 1
            def pointTotal = new DataPoint3d(x: x, y: index * 50, z: totalToDate.size(), color: 'Blue', name: ant.name+" "+current.format('YYYY/MM/dd'), size: 5)
            def pointClosed = new DataPoint3d(x: x, y: index * 50, z: totalClosed.size(), color: 'Green', name: ant.name+" "+current.format('YYYY/MM/dd'), size: 5)
            dp << pointTotal
            dp << pointClosed
            previousTotal = totalToDate
            previousClosed = totalClosed
        }
    }
}
return new DataPointProvider(xLabel: "months",
    yLabel: "projects",
    zLabel: "class count", 
    dataPoints: dp,
    scale: new Scale3d(x: 5, y:1 , z: 0.25))



