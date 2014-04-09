package org.sw7d.archeology.gui

import com.jme3.app.Application
import com.jme3.app.state.AbstractAppState
import com.jme3.app.state.AppStateManager
import de.lessvoid.nifty.controls.ScrollPanel
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.controls.Label
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.tools.SizeValue
import org.sw7d.archeology.ArcheologyFile

class ShowSourceController extends AbstractAppState implements ScreenController {
    Label tf
    ScrollPanel sp
    GroovyMain app

    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")")
        tf = screen.findNiftyControl("sourcecode", Label.class)
        sp = screen.findNiftyControl("sourceScrollPanel", ScrollPanel.class)
    }

    public void onStartScreen() {
        System.out.println("onStartScreen: show source");
        ArcheologyFile af = app.dataPointsByGeometry[app.selected].delegate
        println "file: " + af.getCanonicalPath()
        println "exists? " + af.exists()
        println " path " + af.path
        //String actualFileLocation = ".." + af.path.split(/\.\./)[1]
        //println "presumably this exists: " + actualFileLocation
        File actualFile = new File(af.getCanonicalPath())
        println actualFile.absolutePath + " exists? " + actualFile.exists()
        if (!actualFile.exists()) {
            actualFile = new File(af.canonicalPath.replace('apache-data', 'archeology3d-data/apache')) 
            println "trying another location: "+ actualFile.absolutePath + " exists? " + actualFile.exists()
        }
        if (actualFile.exists()) {
            String text = actualFile.text //new File("src/org/sw7d/archeology/gui/GroovyMain.groovy").text
            tf.text = text
            tf.setHeight(SizeValue.px(tf.element.getRenderer(TextRenderer.class).getFont().height * text.split("\n").size()))//"${text.split("\n").size() * 25}px"
            sp.getElement().layoutElements();
            sp.setUp(0, 5, 0, 50, ScrollPanel.AutoScroll.OFF);
        }
    }

    public void onEndScreen() {
        System.out.println("onEndScreen: show source");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app as GroovyMain;

    }

}

