import org.sw7d.archeology.Modules

Modules.loadedModules*.files.flatten().groupBy {it.extension()}.sort {-it.value.size()}.each {
    if (it.value.size() > 100) println it.key + "   "+it.value.size()
}
