import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
println "here be start"
def filesByModule = [:].withDefault{new HashSet<Module>()}
def filesByJavaName = [:].withDefault{[]}
modules.each { Module module ->
   module.files.findAll{it.javaName() && !it.absolutePath.contains('test') && it.name != 'verify.groovy' && it.name != 'package-info.java'}.each { ArcheologyFile file ->
     filesByModule[file.javaName()] << module.name
     filesByJavaName[file.javaName()] << file
   } 
}
println "SIZE: "+filesByModule.size()
filesByModule = filesByModule.sort { a, b -> -a.value.size() <=> -b.value.size()}
File output = new File("./results/${new Date().dateTimeString.replaceAll(' ','_').replaceAll('/','_')}/index.html")
output.parentFile.mkdirs()
output.delete()
output << "<html><body>\n"
Map<Module, Integer> modulesOfInterest = [:].withDefault{0}
Map<String, Map<String, List<String>>> coincidenceMatrix = [:].withDefault{[:].withDefault{[]}}
filesByModule.each { fileName, modulesUsingFile ->
    if (modulesUsingFile.size() > 1) {
      modulesUsingFile.each { String firstModule ->
          modulesOfInterest [modules.find {it.name == firstModule}] += 1
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
Map<Module, Integer> modulesByPlagiarism = modulesOfInterest.sort {a, b -> -(a.value <=> b.value)} 
modulesByPlagiarism.each { Module headerColumn, Integer plagiarizedClassCount ->
  output << "   <td><span style='-webkit-writing-mode: vertical-rl; white-space:nowrap;'>${headerColumn.name}</span></td>"  
}
output << "</tr>\n"

modulesByPlagiarism.each {Module row, Integer rPlagiarizedClassCount ->
    output << "<tr>\n   <td><span style='white-space:nowrap;'>${row.name} (${rPlagiarizedClassCount}/${row.classFiles.size()})</span></td>"
    modulesByPlagiarism.each { Module column, Integer cPlagiarizedClassCount ->
      String value
      if (row.name == column.name) {
         value = "X" 
      } else {
          value = coincidenceMatrix[row.name][column.name].size()
          if (value == "0") {
              value = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          } else {
              File details = new File(output.parentFile.absolutePath+"/"+row.name+"_"+column.name+".html")
              if (!details.exists()) {
                  details = new File(output.parentFile.absolutePath+"/"+column.name+"_"+row.name+".html")
                  if (!details.exists()) {
                     details << "<html><body>\n"
                     details << "<table border='true'>\n"
                     details << "<tr>\n  <td>class</td><td>lines in ${row.name}</td><td>lines in ${column.name}</td></tr>\n"
                     coincidenceMatrix[row.name][column.name].sort().each { String className ->
                         ArcheologyFile fRow = row.files.find {it.javaName() == className}
                         ArcheologyFile fColumn = column.files.find {it.javaName() == className}
                         details << "<tr><td>${className}</td><td><a href='file://${fRow.canonicalPath}'>${fRow.linesCount}</a></td><td><a href='file://${fColumn.canonicalPath}'>${fColumn.linesCount}</a></td></tr>\n"
                         
                     }
                     details << "</table>\n"   
                     details << "</html></body>\n"
                  }
              }
              value = "<a href='./${details.name}'>${value}</a>"
          }
      }  
      output << "   <td align='right'>${value}</td>\n"
    }
    output << "</tr>\n"

}
output << "</table>\n"
output << "</body></html>\n"
"open ${output.canonicalPath}".execute()

return modules.size()

