package org.sw7d.archeology.tools

import org.sw7d.archeology.ArcheologyFile

class NameParser {
    static String[] getWords(String fullyQualifiedClassName) {
        String[] breakUpPackage = fullyQualifiedClassName.split(/\./) 
        String className = breakUpPackage[-1]
        def result = []
        String currentWord = ""
        className.eachWithIndex { character, position ->
            //println "character: "+character+", classname[$position] "+className[position]
            if (isUppercase(character) && 
                currentWord && 
                !(surroundedByUppercase(className, position)) && 
                position != className.length() -1 && 
                !isDoubleUppercaseWithPrevious(className, position)) {
              result << currentWord
              currentWord = ""
            } 
            currentWord += character  
            
            
        }
        result << currentWord
        return result
        
    }
    
    private static boolean surroundedByUppercase(String word, int position) {
        if (position == 0 || position == word.length() -1) {
            return false
        }
        return isUppercase(word[position-1]) && isUppercase(word[position+1])
    }
    
    private static boolean isUppercase(String c) {
        return c in 'A'..'Z'
    }
    
    public static boolean isDoubleUppercaseWithPrevious(String word, int position) {
        if (position == 0) {
            return false
        }
        return !surroundedByUppercase(word, position) && isUppercase(word[position-1]) && !surroundedByUppercase(word, position-1)
    }
	
}

