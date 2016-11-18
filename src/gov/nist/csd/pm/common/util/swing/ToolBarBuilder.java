/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util.swing;

import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.util.Delegate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author  Administrator
 */
public class ToolBarBuilder {

    private static int SPRING_HEIGHT = 2;
    /**
	 * @uml.property  name="toolBar"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JToolBar toolBar = new JToolBar();
    /**
	 * @uml.property  name="toolBarActionListener"
	 */
    final private ActionListener toolBarActionListener;
    /**
	 * @uml.property  name="nullButtonDelegate"
	 * @uml.associationEnd  
	 */
    final private static Delegate<AbstractButton> nullButtonDelegate = new Delegate<AbstractButton>(){

        @Override
        public void delegate(AbstractButton delegateObj) {
            //Does nothing
        }

    };
    /**
	 * @uml.property  name="inGroup"
	 */
    private Boolean inGroup = false;
    /**
	 * @uml.property  name="buttonGroup"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private ButtonGroup buttonGroup = new ButtonGroup();
    
    public ToolBarBuilder(ActionListener toolBarActionListener) {
        super();
        this.toolBarActionListener = toolBarActionListener;
    }
    
    public void reset(){
        toolBar = new JToolBar();
        inGroup = false;
    }
    
    public ToolBarBuilder group(){
        inGroup = !inGroup;
        if(inGroup){
            buttonGroup = new ButtonGroup();
        }
        return this;
    }



    public ToolBarBuilder addButton(String path, String actionCommand, String toolTip){
        return addButton(path, actionCommand, toolTip, actionCommand, nullButtonDelegate);
    }

    public ToolBarBuilder addButton(String path, String actionCommand, String toolTip, Delegate<AbstractButton> delegate){
        return addButton(path, actionCommand, toolTip, actionCommand, delegate);
    }

    public ToolBarBuilder addButton(String path, String actionCommand, String toolTip, String altText, Delegate<AbstractButton> configureDelegate){
        AbstractButton button = null;
        if(inGroup){
            button = makeToggleBarButton(path, actionCommand, toolTip, altText);
            buttonGroup.add(button);
        }
        else {
            button = makeToolBarButton(path, actionCommand, toolTip, altText);
        }
        if(configureDelegate != null) configureDelegate.delegate(button);
        button.addActionListener(toolBarActionListener);
        toolBar.add(button);
        return this;
    }
    
    public ToolBarBuilder addStrut(int size){
        toolBar.add(Box.createHorizontalStrut(size));
        return this;
    }

    public ToolBarBuilder addSpring(int minimumSize, int maximumSize){
        JPanel panel = new JPanel();
        panel.setMinimumSize(new Dimension(minimumSize, SPRING_HEIGHT));
        panel.setMaximumSize(new Dimension(maximumSize, SPRING_HEIGHT));
        panel.setPreferredSize(new Dimension(maximumSize, SPRING_HEIGHT));
        toolBar.add(panel);
        return this;
    }

    public ToolBarBuilder addSpring(){
        toolBar.add(Box.createHorizontalGlue());
        return this;
    }

    public ToolBarBuilder addDivider(){
        toolBar.add(new JToolBar.Separator());
        return this;
    }

    /**
	 * @return
	 * @uml.property  name="toolBar"
	 */
    public JToolBar getToolBar(){
        return toolBar;
    }
    
   

   

    private static AbstractButton makeToggleBarButton(String iconFile, String actionCommand, String toolTip, String altText) {
        JToggleButton button = new JToggleButton();
        configureToolBarButton(button, iconFile, actionCommand, toolTip, altText);
        return button;
    }

    private static AbstractButton makeToolBarButton(String iconFile, String actionCommand, String toolTip, String altText) {
        JButton button = new JButton();
        configureToolBarButton(button, iconFile, actionCommand, toolTip, altText);
        return button;
    }


    private static void configureToolBarButton(AbstractButton jButton, String iconFile, String actionCommand, String toolTip, String altText) {
        
        ImageIcon imageIcon = GraphicsUtil.getImageIcon(iconFile);
        jButton.setActionCommand(actionCommand);

        jButton.setToolTipText(toolTip);
        if (imageIcon == null) {
            jButton.setText(altText);
        } else {
            jButton.setIcon(imageIcon);
            imageIcon.setDescription(altText);
        }
    }
}
