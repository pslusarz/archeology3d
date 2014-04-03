package org.sw7d.archeology

import java.util.List

class Modules extends ArrayList<Module> {
    static String serializedFile = "carfax-modules.pickle"
    
    public static Modules loadedModules

    public Modules initFromFilesystem() {
        String root = '../archeology/'
        println "reading from file system"
        new File("./serialized-data/apache/").mkdirs()
        new File(root).eachDir { File repository ->
            repository.eachDir{ File moduleDir ->
                if (moduleDir.name != '.DS_Store') {
                    Module module
                    File serializedModuleFile = new File("./serialized-data/carfax/${moduleDir.name}.bin")
                    if (!serializedModuleFile.exists()) {
                        module = new Module(name: moduleDir.name, path: moduleDir.absolutePath, repository: repository.name)
                        println "  init from raw folder: " + module.name
                  
                        module.init()  
                        println "     now storing to a serialized file"
                        def output = new FileOutputStream(serializedModuleFile.canonicalPath)
                        output.withObjectOutputStream { oos ->
                            oos << module
                        }  
                    } 
                    println "     now loading from serialized file: ${moduleDir.name}"
                    def fis = new FileInputStream(serializedModuleFile.canonicalPath)
                    def ois = new MyObjectInputStream(Module.class.classLoader, fis)
                    module = ois.readObject()
                    this << module
                }
            }
        }
        //this*.clearLibraryRefs()
        //this*.initLibrary(this)
        initPopularity()
        //initLevels()
        return this

    }
    
    private initPopularity() {
        def javaFiles = files.flatten().findAll{!it.javaName()?.startsWith('java') && (it.extension() == 'java' || it.extension() == 'groovy')}
        javaNames = javaFiles*.javaName()
        namesByPopularity = files*.imports.flatten().findAll{!it?.startsWith('java') && it}.groupBy {it}.sort {a, b -> -a.value.size() <=>-b.value.size()}
        
        println "Now computing popularity"
        namesByPopularity.each { String className, List<String> popularity ->
            if (!hasRequestedMaxDataPoints() || dataPoints.size() < maxDataPoints) {
                ArcheologyFile javaFile = modules.findFirstClassFile(className)
                if (javaFile) {
                    def javaImports = javaFile.imports.findAll{javaNames.contains(it)}
                    javaFile.popularity = popularity.size()
                    javaFile.javaImports = javaImports
                }
            }
        }
    }

    List<ArcheologyFile> findClassFile(String className) {
        List<ArcheologyFile> result = []
        this.each { Module module ->
            ArcheologyFile file = module.classFiles[className]
            if (file) {
                result << file
            }
        }
        result
    }

    ArcheologyFile findFirstClassFile(String className) {
        List<ArcheologyFile> result = findClassFile(className)
        if (result.size() > 0) {
            return result.first()
        } else {
            return null
        }
    }

    //    private void initLevels() {
    //        def changed = true
    //        while (changed) {
    //            changed = false
    //            this.each {
    //                if (it.dependsOn.size() == 0) {
    //                    if (it.level == -1) {
    //                        it.level = 1
    //                        changed = true
    //                    }
    //                } else if (!it.dependsOn.findAll { it.level == -1 }) {
    //                    def newLevel = (it.dependsOn.max { a, b -> a.level <=> b.level }).level + 1
    //                    if (newLevel != it.level) {
    //                        changed = true
    //                        it.level = newLevel
    //                    }
    //                }
    //            }
    //        }
    //    }

    public serialize() {
        println "serializing to $serializedFile"
        def output = new FileOutputStream(serializedFile)
        output.withObjectOutputStream { oos ->
            oos << this
        }
    }

    public static Modules initFromPickle() {
        println "loading from pickle"
        def fis = new FileInputStream(serializedFile)
        def ois = new MyObjectInputStream(Modules.class.classLoader, fis)
        return ois.readObject()
    }

    public static Modules create() {
        if (!loadedModules) {         
            if (!new File(serializedFile).exists()) {
                loadedModules = new Modules().initFromFilesystem().serialize()
            }
            loadedModules = initFromPickle()
        }    
        return loadedModules
    }
}
