import org.sw7d.archeology.Module
import org.sw7d.archeology.ArcheologyFile
println "here be start"
String googleAnalytics = """
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-49239121-1', 'pslusarz.github.io');
  ga('send', 'pageview');

</script>
"""
String lastModified = "<p>Last modified: ${new Date().format('yyyy/MM/dd HH:mm:ss')}</p>"
def filesByModule = [:].withDefault{new HashSet<String>()}
def filesByJavaName = [:].withDefault{[]}
modules.each { Module module ->
    module.files.findAll{
        it.javaName() && !it.absolutePath.toLowerCase().contains('/test/') &&
         !it.absolutePath.contains('/resources/') &&
         it.name != 'verify.groovy' &&
         it.name != 'package-info.java' &&
         it.name != 'postbuild.groovy'
     }.each { ArcheologyFile file ->
     filesByModule[file.javaName()] << module.name
     filesByJavaName[file.javaName()] << file
   } 
}

println "SIZE: "+filesByModule.size()
filesByModule = filesByModule.sort { a, b -> -a.value.size() <=> -b.value.size()}
File output = new File("./results/${new Date().format('yyyy-MM-dd_HH-mm-ss')}/index.html")
output.parentFile.mkdirs()
output.delete()
output << "<html><body>\n"
Map<Module, Integer> modulesOfInterest = [:].withDefault{0}
Map<String, Map<String, List<String>>> coincidenceMatrix = [:].withDefault{[:].withDefault{[]}}
def duplicateClassCount = 0
HashSet<String> dupeModules = new HashSet<String>()
filesByModule.each { fileName, modulesUsingFile ->
    if (modulesUsingFile.size() > 1) {
        duplicateClassCount ++
      modulesUsingFile.each { String firstModule ->
          dupeModules << firstModule
      
          modulesOfInterest [modules.find {it.name == firstModule}] += 1
          modulesUsingFile.each { String secondModule ->
            if (firstModule != secondModule) {
                coincidenceMatrix[firstModule][secondModule] << fileName
            }
          }
          
      } 

    }
}

println "duplicate classes: "+duplicateClassCount+", out of total: "+filesByModule.size()
println "modules infected: "+dupeModules.size()+", out of total: " + modules.size()
println "starting to create html report"
output << "<h1>Conflicting class names in Apache Java projects</h1> \n"
output << "<p>Two classes are conflicting if they have the same name and package. Corpus consists of ${modules.size()} Apache Software Foundation Java projects containing ${String.format('%,d',filesByModule.size())} classes. <br> There are ${String.format('%,d',duplicateClassCount)} with the same names across ${dupeModules.size()} projects.</p>\n"
output << "<table border='true'>\n"
output << "<tr>\n  <td>(distinct duplicates/total project classes)</td>\n"
Map<Module, Integer> modulesByPlagiarism = modulesOfInterest.sort {a, b -> -(a.value <=> b.value)} 
modulesByPlagiarism.each { Module headerColumn, Integer plagiarizedClassCount ->
    output << "   <td><span style='-webkit-writing-mode: vertical-rl; white-space:nowrap;'><a href='https://github.com/apache/${headerColumn.name}'>${headerColumn.name}</a></span></td>"  
}
output << "</tr>\n"

modulesByPlagiarism.each {Module row, Integer rPlagiarizedClassCount ->
    println "now processing: ${row.name}"
    output << "<tr>\n   <td><span style='white-space:nowrap;'><a href='https://github.com/apache/${row.name}'>${row.name}</a> (${rPlagiarizedClassCount}/${row.classFiles.size()})</span></td>"
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
                     details << "<h3>Conflicting class names between <a href='https://github.com/apache/${row.name}'>${row.name}</a> and <a href='https://github.com/apache/${column.name}'>${column.name}</a></h3>"
                     details << "<table border='true'>\n"
                     details << "<tr>\n  <td rowspan=2>class</td><td colspan='2'>lines of code</td><td colspan='2'>commits</td><td colspan='2'>first commit</td><td colspan='2'>last commit</td><td rowspan=2>also present in (lines of code there)</td></tr>\n"
                     details << "<tr>\n  "
                     (1..4).each {
                         details <<  "<td>${row.name}</td><td>${column.name}</td>"
                     }
                     details << "\n</tr>\n"
                       
                     coincidenceMatrix[row.name][column.name].sort().each { String className ->
                         ArcheologyFile fRow = row.files.find {it.javaName() == className}
                         ArcheologyFile fColumn = column.files.find {it.javaName() == className}
                         details << "<tr>\n"
                         details << "  <td>${className}</td>\n"
                         details << "  <td><a href='${fRow.repoUrl()}'>${fRow.linesCount}</a></td>\n"
                         details << "  <td><a href='${fColumn.repoUrl()}'>${fColumn.linesCount}</a></td>\n"
                         details << "  <td>${fRow.commits.size()}</td>\n"
                         details << "  <td>${fColumn.commits.size()}</td>\n"
                         details << "  <td>${fRow.commits.first().format('yyyy-MM-dd')}</td>\n"
                         details << "  <td>${fColumn.commits.first().format('yyyy-MM-dd')}</td>\n"
                         details << "  <td>${fRow.commits.last().format('yyyy-MM-dd')}</td>\n"
                         details << "  <td>${fColumn.commits.last().format('yyyy-MM-dd')}</td>\n"
                         String alsoP = "  <td>"
                         def alsoPresentIn = filesByModule[className].findAll {it != row.name && it != column.name}
                         alsoPresentIn.each { String moduleName ->
                             Module otherModule = modules.find {it.name == moduleName}
                             ArcheologyFile otherFile = otherModule.files.find {it.javaName() == className}
                             alsoP += "${moduleName} (<a href='${otherFile.repoUrl()}'>${otherFile.linesCount}</a>) &nbsp;&nbsp;&nbsp;"
                             
                         }
                         alsoP += "</td>\n"
                         details << alsoP
                         details << "</tr>\n"
                         
                     }
                     details << "</table>\n"
                     details << lastModified
                     details << googleAnalytics
                     details << "</body></html>\n"
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
output << googleAnalytics
output << lastModified
output << "</body></html>\n"

new File("${output.parent}/code.txt") << new File("scripts/runtime/DefaultScript.groovy").text

"open ${output.canonicalPath}".execute()

return modules.size()

