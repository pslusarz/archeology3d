import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
println "here be start"

//popularity 
//List<DataPoint3d> dp = []
//modules*.files.flatten().findAll{it.javaName()}.sort{-it.popularity}.each { ArcheologyFile javaFile ->
//    dp << new DataPoint3d(name: javaFile.javaName(), delegate: javaFile, x: javaFile.popularity, z: (javaFile.linesCount / 10), y: javaFile.commits.size()*2) 
//}
//
//return new DataPointProvider(zLabel: "Lines of Code /10", yLabel: "Commits *2", xLabel: "Popularity", dataPoints: dp)

//active projects
List<DataPoint3d> dp = []

    Date today = new Date()
    modules*.files.flatten().findAll{it.javaName() && !it.javaName().startsWith('java') && 
         !it.canonicalPath.contains('thrift') && !it.canonicalPath.contains('protobuf/generated') && 
         !it.canonicalPath.contains('codegen')}.sort{-it.linesCount}.eachWithIndex { ArcheologyFile file, int i ->
        if (i < 5000) {
            if (['ant', 'cassandra', 'camel'].contains(file.module.name) ) {
            println file.module.name+":"+file.javaName()+" ${file.canonicalPath}  "+file.commits.size()+"\t\t"+file.linesCount    
            } else {
           println file.module.name+":"+file.javaName()+"  ${file.canonicalPath} "+file.commits.size()+"\t"+file.linesCount+"\t" 
            }
        }
        
    
        
    String color = 'DarkGray'
    if (file.module.name == 'camel') {  
        color = "Red"
    } else if (file.module.name == 'ant') { // 2 years
        color = "Orange"
    } 
    else if (file.module.name == 'httpclient') {  // 3 years
        color = "Yellow"
    } 
    else if (file.module.name == 'cassandra') {  // 3 years
        color = "Cyan"
    } else if (file.module.name == 'hadoop-common') {  // 3 years
        color = "Blue"
    } 
    
//    if (lastCommit -firstCommit <  360) {  // 1 year
//        color = "Red"
//    } else if (lastCommit -firstCommit < 720) { // 2 years
//        color = "Magenta"
//    } 
//    else if (lastCommit -firstCommit < 1080) {  // 3 years
//        color = "Orange"
//    } else if (lastCommit -firstCommit < 1440) { //4 years
//        color = "Yellow"
//    } else if (lastCommit -firstCommit < 1800) { // 5 years
//        color = "Cyan"
//    } else if (lastCommit -firstCommit < 2160) { // 6 years
//        color = "Green"
//    } else if (lastCommit -firstCommit < 2520) { // 6 years
//        color = "Blue"
//    }
//    if (color == "Orange") {
//        println "active in 90 days: " +module.name
//    }

    //follow tomcat and tapestry
//    if (module.name.startsWith('tapestry')) {
//        color = 'Orange'
//    } else if (module.name.startsWith('tomcat')) {
//        color = 'Cyan'
//    } else {color = 'DarkGray'}

      if (color != 'DarkGray') {
            dp << new DataPoint3d(name: file.javaName(), x: file.popularity * 1000 / file.module.files.size(), z: file.commits.size()/3, y: (file.linesCount) / 10, delegate: file, color: color, size: 4)
      }      
    }
    //dp << new DataPoint3d(name: module.repository+": "+module.name, x: (today -firstCommit) / 70, z: module.files.findAll{it.javaName()}.size() / 100, y: commitsPerJavaFile, type: "box", color: color, size:1)  
    //}
return new DataPointProvider(zLabel: "commits", yLabel: "size", xLabel: "relative popularity", dataPoints: dp)