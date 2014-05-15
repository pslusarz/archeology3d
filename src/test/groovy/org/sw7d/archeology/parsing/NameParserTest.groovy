package org.sw7d.archeology.parsing
import org.junit.Test

class NameParserTest {
    @Test
    void testOneWord() {
        assert ["Foo"] == NameParser.getWords('org.Foo')
        assert ["Foo"] == NameParser.getWords('org.bar.Foo')
        assert ["Foo"] == NameParser.getWords('(default).Foo')        
    }
    
    @Test
    void testMultipleWords() {
        assert ["Foo", "Bar"] == NameParser.getWords('org.FooBar')
    }
    
    @Test
    void testURI() {
        assert ['DOM', 'Util'] == NameParser.getWords('org.babache.commons.foo.DOMUtil')
        assert ['Collection', 'URI', 'Resolver'] == NameParser.getWords('boo.hoo.CollectionURIResolver')
        assert ['URI'] == NameParser.getWords('com.foo.URI')
    }
    
    @Test
    void testXPath() {
        assert ['Debugger', 'MBean'] == NameParser.getWords('org.borg.DebuggerMBean')
        assert ['ILike', 'XP'] == NameParser.getWords('foo.ILikeXP')
        assert ['IBatis'] == NameParser.getWords('net.flix.IBatis')
    }
    
//    @Test
//    void ttt() {
//        'DeMBean'.eachWithIndex {character, position ->
//            println character+" "+position+" "+NameParser.isDoubleUppercase('DeMBean', position)
//            
//        }
//    }
}

