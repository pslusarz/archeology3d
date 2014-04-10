import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d


def isGenerated(ArcheologyFile file) {
    file.canonicalPath.contains('thrift') || 
    file.canonicalPath.contains('protobuf/generated') || 
    file.canonicalPath.contains('codegen')
}

List<DataPoint3d> dp = []

def moduleColors = [
      'ant'          : 'Orange',
      'camel'        : 'Red',
      'cassandra'    : 'Cyan',
      'hadoop-common': 'Blue',
      'httpclient'   : 'Yellow'  
    ]

import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
println "here be start"


//modules.sort {-it.files.findAll{it.javaName()}.size()}.eachWithIndex { Module module, int index ->
//    def javaFiles = module.files.findAll{it.javaName()}?:[]
//    if (!javaFiles) {
//        println "no java files, probably development on non standard branches: "+module.name
//    }
//    int totalCommits = javaFiles.sum {it.commits.size()} ?: 0
//    int commitsPerJavaFile = totalCommits * 5 / (javaFiles.size() ?: 1)
//    Date firstCommit = module.files*.commits.collect().flatten().min() ?: new Date() - 100000
//    Date lastCommit = module.files*.commits.collect().flatten().max() ?: new Date() - 10000
//    String color = "DarkGray" // 7+ years
//    Date today = new Date()
//    if (today - lastCommit <  180) {  // 1 year
//        color = "Red"
//    } else if (today - lastCommit  < 360) { // 2 years
//        color = "Orange"
//    } 
//    else if (today - lastCommit < 720) {  // 3 years
//        color = "Yellow"
//    } 
//
//    dp << new DataPoint3d(name: module.name, 
//        x: (today -firstCommit) / 70, 
//        y: commitsPerJavaFile, type: "box", 
//        z: module.files.findAll{it.javaName()}.size() / 100,  
//        color: color, size:2)  
//}
//return new DataPointProvider(xLabel: "Age", yLabel: "Commits per file", zLabel: "java classes", dataPoints: dp)



modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it)}.each { ArcheologyFile file ->
            
    def color = moduleColors[file.module.name]
    if (color) {
            dp << new DataPoint3d(name: file.javaName(), 
                x: file.popularity * 1000 / file.module.files.size(), 
                y: (file.linesCount) / 10,
                z: file.commits.size()/3,  
                delegate: file, color: color, size: 4)
      }      
    }
return new DataPointProvider(xLabel: "relative popularity",
    yLabel: "size",
    zLabel: "commits", dataPoints: dp)