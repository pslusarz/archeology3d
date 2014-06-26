import org.sw7d.archeology.Module
import org.sw7d.archeology.Modules
import org.sw7d.archeology.ArcheologyFile
import org.sw7d.archeology.data.DataPointProvider
import org.sw7d.archeology.data.DataPoint3d
import org.sw7d.archeology.data.Scale3d
import org.sw7d.archeology.tools.NameParser
import org.sw7d.archeology.tools.OutputFileGoldplating

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;
import org.sw7d.archeology.Settings


List<DataPoint3d> dp = []

//Module ant = modules.find {it.name == 'hadoop-common'}
modules.sort{-it.files.size()}.eachWithIndex { Module ant, int index ->
    List<ArcheologyFile> classFiles =  ant.files.findAll {it.javaName() && !it.isGenerated()}
    println ant.name+" "+classFiles.size()
    if (classFiles.size > 0) {
        Date current = classFiles*.commits.flatten().min()
        Date last = classFiles*.commits.flatten().max()
        int x = 0
        while (current <= last) {
            List<ArcheologyFile> totalToDate = classFiles.findAll {ArcheologyFile file -> file.commits.find {Date date -> date <= current}}
            List<ArcheologyFile> totalClosed = totalToDate.findAll {ArcheologyFile file -> file.commits.max() <= current}
            //println current.toString() +" total: "+totalToDate.size()
            current += 7
            x += 1
            def pointTotal = new DataPoint3d(x: x, y: index * 50, z: totalToDate.size(), color: 'Blue', name: ant.name+" "+current.format('YYYY/MM'), size: 5)
            def pointClosed = new DataPoint3d(x: x, y: index * 50, z: totalClosed.size(), color: 'Green', name: ant.name+" "+current.format('YYYY/MM'), size: 5)
            dp << pointTotal
            dp << pointClosed
        }
    }
}
return new DataPointProvider(xLabel: "months",
    yLabel: "projects",
    zLabel: "class count", 
    dataPoints: dp,
    scale: new Scale3d(x: 5, y:1 , z: 0.05))
