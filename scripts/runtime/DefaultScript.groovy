import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
import org.sw7d.archeology.parsing.NameParser


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
Map<String, List<ArcheologyFile>> wordsWithClasses = [:].withDefault {[]}
modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !isGenerated(it)}.each { ArcheologyFile file ->
         def words = NameParser.getWords(file.javaName()) 
         words.each { String word ->
             wordsWithClasses[word] << file
         }
         
       
}

wordsWithClasses.sort {-it.value.size()}.eachWithIndex { String word, List<ArcheologyFile> files, int i ->
    if (files.size() > 10) {
        String usages = ""
        files.each {
          usages += "${it.javaName()}[${it.module.name}] , "  
        }
        println word + "  "+files.size()+"  "+ usages
        dp << new DataPoint3d(name: word, x: i, y: 0, z:files.size())
    }
}

return new DataPointProvider(xLabel: "rank",
    yLabel: "",
    zLabel: "occurences", dataPoints: dp)

//
////import org.sw7d.archeology.Module
//import org.sw7d.archeology.ArcheologyFile
//import org.sw7d.archeology.data.DataPointProvider
//import org.sw7d.archeology.data.DataPoint3d
//
//
//def isGenerated(ArcheologyFile file) {
//    file.canonicalPath.contains('thrift') || 
//    file.canonicalPath.contains('protobuf/generated') || 
//    file.canonicalPath.contains('codegen')
//}
//
//List<DataPoint3d> dp = []
//
//def moduleColors = [
//      'ant'          : 'Orange',
//      'camel'        : 'Red',
//      'cassandra'    : 'Cyan',
//      'hadoop-common': 'Blue',
//      'httpclient'   : 'Yellow'  
//    ].withDefault {"DarkGray"}
//
//import org.sw7d.archeology.Module
//import org.sw7d.archeology.ArcheologyFile
//import org.sw7d.archeology.data.DataPointProvider
//import org.sw7d.archeology.data.DataPoint3d
//
//
//
//def today = new Date()
// 
//modules.each { Module module ->
//    def javaFiles = module.files.findAll{it.javaName()}?:[]
//    if (!javaFiles) {
//        println "no java files, probably development on non standard branches: "+module.name
//    }
//    int totalCommits = javaFiles.sum {it.commits.size()} ?: 0
//    int commitsPerJavaFile = totalCommits * 5 / (javaFiles.size() ?: 1)
//    Date firstCommit = module.files*.commits.collect().flatten().min() ?: new Date() - 100000
//    Date lastCommit = module.files*.commits.collect().flatten().max() ?: new Date() - 10000
////    String color = "DarkGray" // 2+ years
////    Date today = new Date()
////    if (today - lastCommit <  180) {  // 1/2 year
////        color = "Red"
////    } else if (today - lastCommit  < 360) { // 1 year
////        color = "Orange"
////    } 
////    else if (today - lastCommit < 720) {  // 2 years
////        color = "Yellow"
////    } 
//
//    dp << new DataPoint3d(name: module.name, 
//        x: (today -firstCommit) / 70, 
//        y: commitsPerJavaFile, type: "box", 
//        z: module.files.findAll{it.javaName()}.size() / 100,  
//        color: moduleColors[module.name], size:2)  
//}
//return new DataPointProvider(xLabel: "Age", yLabel: "Commits per file", zLabel: "java classes", dataPoints: dp)