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

modules.each {
    println it.name +" "+it.files.size()
}

Module ant = modules.find {it.name == 'hadoop-common'}

println ant.name
println ant.files.size()
Date current = ant.files*.commits.flatten().min()
Date last = ant.files*.commits.flatten().max()
int x = 0
while (current <= last) {
    List<ArcheologyFile> totalToDate = ant.files.findAll {ArcheologyFile file -> file.commits.find {Date date -> date <= current}}
    List<ArcheologyFile> totalClosed = totalToDate.findAll {ArcheologyFile file -> file.commits.max() <= current}
    //println current.toString() +" total: "+totalToDate.size()
    current += 31
    x += 1
    def pointTotal = new DataPoint3d(x: x, y: 0, z: totalToDate.size(), color: 'Blue', name: current.format('YYYY/MM'), size: 3)
    def pointClosed = new DataPoint3d(x: x, y: 0, z: totalClosed.size(), color: 'Green', name: current.format('YYYY/MM'), size: 3)
    dp << pointTotal
    dp << pointClosed
}


return new DataPointProvider(xLabel: "months",
    yLabel: "n/a",
    zLabel: "class count", 
    dataPoints: dp,
    scale: new Scale3d(x: 5, y:1 , z: 0.05))
