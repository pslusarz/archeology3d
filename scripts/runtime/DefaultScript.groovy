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

List<DataPoint3d> dp = []
modules.sort {-it.files.findAll{it.javaName()}.size()}.eachWithIndex { Module module, int index ->
    def javaFiles = module.files.findAll{it.javaName()}?:[]
    if (!javaFiles) {
        println "no java files found: "+module.name
    }
    int totalCommits = javaFiles.sum {it.commits.size()} ?: 0
    int commitsPerJavaFile = totalCommits * 10 / (javaFiles.size() ?: 1)
    Date lastCommit = module.files*.commits.collect().flatten().max() ?: new Date() - 10000
    String color = "DarkGray"
    Date today = new Date()
    if (lastCommit > today - 90) {
        color = "Red"
    } else if (lastCommit > today - 180) {
        color = "Orange"
    } 
    else if (lastCommit > today - 360) {
        color = "Yellow"
    } else if (lastCommit > today - 720) {
        color = "Blue"
    }
    if (color == "Orange") {
    dp << new DataPoint3d(name: module.repository+": "+module.name, x: index, z: module.files.findAll{it.javaName()}.size() / 50, y: commitsPerJavaFile, type: "bar", color: color, size:3)  
}}
return new DataPointProvider(zLabel: "java files / 30", yLabel: "Commits per file * 5", xLabel: "Rank * 2", dataPoints: dp)