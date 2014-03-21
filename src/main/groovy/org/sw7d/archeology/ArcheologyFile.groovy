package org.sw7d.archeology
import java.io.File
import java.text.NumberFormat
import java.util.regex.Matcher


class ArcheologyFile extends File {
    public static final textFileExtensions = ['java', 'groovy','html', 'txt', 'xml', 'sql']
    int linesCount = -1
    Set<String>	imports
    String javaPackage = ''
    List<Date> commits = []
    Module module

    public ArcheologyFile(File file, Module module) {	  
        super(file.getAbsolutePath())
        this.module = module
        if (isText()) {
            List<String> lines = file.readLines()
            linesCount = lines.size()
            imports = initImports(lines)
            javaPackage = initJavaPackage(lines)
	  
            if (module.isRepoGit()) {
                String command = "git --no-pager --git-dir=${module.path}/.git --work-tree=${module.path} log --pretty=format:%ad --date=short '${canonicalPath}'"
                Process p = command.execute()
                p.waitFor()
                String[] commitDates = p.in.text.split('\n')
                //println "command: ${command}"
                //println "output ${commitDates}"
                try {
                    commitDates.reverse().each { String rawDate -> 
                        commits << DateFlyweight.dates[rawDate]
                    }} catch (Exception e) {
                    println "=========== something got goofed up ==============="
                    println "${command}"
                    println "${commitDates}"
                }
            }
        }	  
    }
  
    String repoUrl() {
        if (module.repository == 'github') {
            String chunk = canonicalPath.split('github')[1] // servicemix4-bundles/xalan-2.7.1/src/main/java/org/apache/xalan/xsltc/compiler/util/ObjectFactory.java
            String[] chunks = chunk.split("/")
            return "https://github.com/apache/"+chunks[1]+"/blob/trunk/"+chunks[2..-1].join("/")
        
        } else {
            return "file://"+canonicalPath
        }
    }  
  
    boolean isText() {
        textFileExtensions.contains(extension())
    }
  
    String extension() {
        (name.split(/\./) as List)[-1]?.toLowerCase()
    }

    String javaName() {
        if (['groovy', 'java'].contains(extension())) {
            return (javaPackage?:'(default)')+"."+ (name - ('.'+extension()))
        } else {
            return null
        }
    }

    String initJavaPackage(List<String> lines) {
        if (['java', 'groovy'].contains(extension())) {
            for (String line: lines) {
                if (line.trim().startsWith('package ')) {
                    return (line - 'package ' - ";" ).trim()
                }
            }
        }
      ''

    }

    Set<String> initImports(List<String> lines) {
        Set<String> imports = new TreeSet<String>()
        if (['java', 'groovy'].contains(extension())) {
            lines.each {
                if (it.trim().startsWith('import ')) {
                    imports << (it - 'import ' - ";" - 'static ').trim()
                } else {
                    Matcher m = it.trim() =~ /\b(([a-z0-9]+[\.])+[A-Z]+\w+)/
                    if( m.find()) {
                        imports << m[0][0]
                    }
                }
            }
        }
        return imports
    }


	
}
