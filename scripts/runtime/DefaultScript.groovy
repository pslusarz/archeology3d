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



//import org.sw7d.archeology.Module
//import org.sw7d.archeology.Modules
//import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
//import org.sw7d.archeology.tools.NameParser
//import org.sw7d.archeology.tools.OutputFileGoldplating

//import org.codehaus.groovy.groovydoc.GroovyClassDoc;
//import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;
//import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
//import org.codehaus.groovy.groovydoc.GroovyRootDoc;
//import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;
//import org.sw7d.archeology.Settings




provider = new DataPointProvider(xLabel: "",
    yLabel: "rank",
    zLabel: "versions", 
    scale: new Scale3d(x: 20, y:20 , z: 20))
//(1..200).each {
//   provider.edge (
//       new DataPoint3d(x: it, y: it, z: it, color: 'Blue', name: "point ${it}", size: 5), 
//       new DataPoint3d(x: it+1, y: it+1, z: it+1, color: 'Blue', name: "point ${it+1}", size: 5)
//   )
//
//}
(1..5).each { z ->
(1..10).each { x ->
def point = new DataPoint3d(x:x, y:0, z:z, name: "bottom-$z-0", size: 5)
(1..15).each { y ->
    def next = new DataPoint3d(x:point.x, y:point.y+3, z:point.z, name: "bottom-$z-$y", size: 5)
    provider.edge (point, next)
    point = next
}
}
}

return provider



