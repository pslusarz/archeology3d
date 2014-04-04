package org.sw7d.archeology

class Settings {
    String org
    String getAggregateSerializedFileName() {"${org}-modules.pickle"}
    String getRootDataPath() {
        "../archeology3d-data/${org}/"
    }
    
    File getSerializedModuleFile(File repository, File moduleDir) {
        new File("./serialized-data/${org}/").mkdirs()
        return new File("./serialized-data/${org}/${repository.name}_${moduleDir.name}.bin")
        
    }
    
	
}

