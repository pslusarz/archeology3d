package org.sw7d.archeology

class MyObjectInputStream extends ObjectInputStream {

    ClassLoader classLoader

    public MyObjectInputStream() {
        super();
    }

    public MyObjectInputStream(classLoader, input) {
        super(input);
        this.classLoader=classLoader
    }

    public ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException{
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();
        // initially streams descriptor
        Class localClass; // the class in the local JVM that this descriptor represents.
        try {
            localClass = Class.forName(resultClassDescriptor.getName()); 
        } catch (ClassNotFoundException e) {
            return resultClassDescriptor;
        }
        ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
        if (localClassDescriptor != null) {

            final long localSUID = localClassDescriptor.getSerialVersionUID();
            final long streamSUID = resultClassDescriptor.getSerialVersionUID();
            //println resultClassDescriptor.getName() + " local " + localSUID + " stream " + streamSUID
            if (streamSUID != localSUID) { // check for serialVersionUID mismatch.

                
                println resultClassDescriptor.name
                println "Overriding serialized class version mismatch: local serialVersionUID = " + localSUID + " stream serialVersionUID = " + streamSUID
                resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
            }
        }
        return resultClassDescriptor;
    }

    public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException{
        try{
            String name = desc.getName();
            return Class.forName(name, false, classLoader);
        }
        catch(ClassNotFoundException e){
            return super.resolveClass(desc);
        }
    }
}

