import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
println "here be start"
def filesByModule = [:].withDefault{new HashSet<Module>()}
def filesByJavaName = [:].withDefault{[]}
modules.each { Module module ->
   module.files.findAll{it.javaName() && !it.absolutePath.contains('test')}.each { ArcheologyFile file ->
     filesByModule[file.javaName()] << module.name
     filesByJavaName[file.javaName()] << file
   } 
}
println "SIZE: "+filesByModule.size()
filesByModule = filesByModule.sort { a, b -> -a.value.size() <=> -b.value.size()}
File output = new File("apache-duplicateClassMatrix.htm")
output.delete()
output << "<html><body>\n"
def modulesOfInterest = [] as HashSet
Map<String, Map<String, List<String>>> coincidenceMatrix = [:].withDefault{[:].withDefault{[]}}
filesByModule.each { fileName, modulesUsingFile ->
    if (modulesUsingFile.size() > 1) {
      modulesUsingFile.each { String firstModule ->
          modulesOfInterest << modules.find {it.name == firstModule}
          modulesUsingFile.each { String secondModule ->
            if (firstModule != secondModule) {
                coincidenceMatrix[firstModule][secondModule] << fileName
            }
          }
          
      } 

    }
}
output << "<table border='true'>\n"
output << "<tr>\n  <td>&nbsp;</td>\n"
modulesOfInterest.each { Module headerColumn ->
  output << "   <td><span style='-webkit-writing-mode: vertical-rl; white-space:nowrap;'>${headerColumn.name}</span></td>"  
}
output << "</tr>\n"
modulesOfInterest.each {Module row ->
    output << "<tr>\n   <td><span style='white-space:nowrap;'>${row.name}</span></td>"
    modulesOfInterest.each { Module column ->
      String value
      if (row.name == column.name) {
         value = "X" 
      } else {
          value = coincidenceMatrix[row.name][column.name].size()
          if (value == "0") {
              value = "&nbsp;"
          }
      }  
      output << "   <td>${value}</td>\n"
    }
    output << "</tr>\n"

}
output << "</table>\n"
output << "</body></html>\n"
"open ${output.canonicalPath}".execute()

//Map<String, Map<Integer, String>> chessboard = [:].withDefault{[:].withDefault {'empty'}}
//chessboard['A'][1] = 'white rook'
//println chessboard['A'][1]
//println chessboard['A'][4]
//
//
//Map<String, Map<String, List<String>>> mm = [:].withDefault{[:].withDefault{[]}}
//mm['hi']['mom'] << 'wash that mouth'
//mm['hi']['mom'] << 'clean your room'
//println mm['hi']['mom']
//println mm['hi']['dad']
//println "--------here be end--------"
return modules.size()

