/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sw7d.archeology.gui

import de.lessvoid.nifty.screen.ScreenController
import de.lessvoid.nifty.screen.Screen
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.controls.ListBox
import de.lessvoid.nifty.NiftyEventSubscriber
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent
import de.lessvoid.nifty.controls.ButtonClickedEvent
import jme3test.helloworld.HelloJME3
/**
 *
 * @author ps
 */
class SelectModuleController implements ScreenController {
    
    GroovyMain app
    ListBox theBox
    
    List<String> selection = []
    
    public void bind(Nifty nifty, Screen screen) {
        System.out.println("bind( " + screen.getScreenId() + ")");
        theBox = nifty.getScreen("start").findNiftyControl("myListBox", ListBox.class);
        app.modules.each {
          theBox.addItem(it.name)
          if (app.selectedModules.contains(it.name)) {
                  theBox.selectItem(it.name)
          }   
        }
        selection.clear()
        selection.addAll(app.modules.collect {it.name})
    }

    public void onStartScreen() {
        System.out.println("onStartScreen");
    }

    public void onEndScreen() {
        System.out.println("onEndScreen");
    }
    
  @NiftyEventSubscriber(id="myListBox")
  public void onMyListBoxSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
    selection = event.getSelection();
    println "DEBUG: selection changed to: "+selection
  }
  
    
  @NiftyEventSubscriber(id="doneButton")
  public void onDoneButtonClicked(final String id, final ButtonClickedEvent event) {
      println "DEBUG: done selecting, final selection: "+selection
      app.doneSelectingModules(selection)
      
  }
  
  @NiftyEventSubscriber(id="selectAllButton")
  public void onSelectAllButtonClicked(final String id, final ButtonClickedEvent event) {
      theBox.items.each {
          theBox.selectItem(it)
      }
      selection = theBox.items
  }
  
  @NiftyEventSubscriber(id="selectNoneButton")
  public void onSelectNoneButtonClicked(final String id, final ButtonClickedEvent event) {
      theBox.items.each {
          theBox.deselectItem(it)
      }
      selection = []
      println "DEBUG: resetting selection"
  }
}

