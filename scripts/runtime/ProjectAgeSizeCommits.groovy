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
modules.sort {-it.files.findAll{it.javaName()}.size()}.eachWithIndex { Module module, int index ->
    def javaFiles = module.files.findAll{it.javaName()}?:[]
    if (!javaFiles) {
        println "no java files found: "+module.name
    }
    int totalCommits = javaFiles.sum {it.commits.size()} ?: 0
    int commitsPerJavaFile = totalCommits * 5 / (javaFiles.size() ?: 1)
    Date firstCommit = module.files*.commits.collect().flatten().min() ?: new Date() - 100000
    Date lastCommit = module.files*.commits.collect().flatten().max() ?: new Date() - 10000
    String color = "DarkGray" // 7+ years
    Date today = new Date()
    if (today - lastCommit <  180) {  // 1 year
        color = "Red"
    } else if (today - lastCommit  < 360) { // 2 years
        color = "Orange"
    } 
    else if (today - lastCommit < 720) {  // 3 years
        color = "Yellow"
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
    dp << new DataPoint3d(name: module.repository+": "+module.name, x: (today -firstCommit) / 70, z: module.files.findAll{it.javaName()}.size() / 100, y: commitsPerJavaFile, type: "box", color: color, size:1)  
    //}
}
return new DataPointProvider(zLabel: "java files / 100", yLabel: "Commits per file * 5", xLabel: "Age (days / 70)", dataPoints: dp)

