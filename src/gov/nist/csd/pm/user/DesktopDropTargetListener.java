package gov.nist.csd.pm.user;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import gov.nist.csd.pm.common.action.Actions;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.model.PropertyChangeObserver;
import gov.nist.csd.pm.common.util.lang.Strings;
import gov.nist.csd.pm.common.util.swing.MouseEvents;
import gov.nist.csd.pm.common.util.swing.Points;

import javax.annotation.Nullable;
import javax.lang.model.type.NullType;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static gov.nist.csd.pm.common.util.lang.Objects.equalTo;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/7/11
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DesktopDropTargetListener implements DropTargetListener, PropertyChangeObserver {

    /**
	 * @uml.property  name="desktopComponent"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JComponent desktopComponent;
    /**
	 * @uml.property  name="vosGraphSupplier"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.browser.PmGraph"
	 */
    private Supplier<PmGraph> vosGraphSupplier;

    public DesktopDropTargetListener(JComponent desktopComponent, Supplier<PmGraph> vosGraphSupplier){
        this.desktopComponent = desktopComponent;
        this.vosGraphSupplier = vosGraphSupplier;
        MouseAdapter ma = new MouseAdapter() {


            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                super.mouseReleased(mouseEvent);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                super.mouseWheelMoved(mouseWheelEvent);
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseDragged(mouseEvent);
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);
            }
        };
        this.desktopComponent.addMouseMotionListener(ma);
        this.desktopComponent.addMouseListener(ma);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
        checkDropTargetDragEvent(dropTargetDragEvent);
    }



    private static final Predicate<DataFlavor> isAcceptableDataFlavor = new Predicate<DataFlavor>(){

        @Override
        public boolean apply(@Nullable DataFlavor input) {
            String dataFlavorDesc = Objects.toStringHelper(input)
                    .add("In Predicate", "isAcceptableDataFlavor")
                    .add("MIME Type", input.getMimeType())
                    .add("Primary Type", input.getPrimaryType())
                    .add("Subtype", input.getSubType()).toString();
            System.out.println(dataFlavorDesc);
            return PmGraph.pmNodeDataFlavor.match(input);
        }
    };



    private void checkDropTargetDragEvent(DropTargetDragEvent dropTargetDragEvent) {
        Iterables.any(dropTargetDragEvent.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor);
    }

    @Override
    public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
        Iterables.any(dropTargetDragEvent.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor);

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
        Iterables.any(dropTargetDragEvent.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor);

    }

    @Override
    public void dragExit(DropTargetEvent dropTargetEvent) {

    }

    private static Action newModifySourceAction(Function<Object, NullType> modificationFunction){
        return new ModifySourceAction(modificationFunction);
    }

    private static final Function<Object, NullType> nooperation = new Function<Object, NullType>(){

        @Override
        public NullType apply(@Nullable Object input) {
            return null;
        }
    };

    private static class ModifySourceAction extends AbstractAction{

        private final Function<Object, NullType> _modfunction;

        public ModifySourceAction(Function<Object, NullType> modfunction){
            _modfunction = Objects.firstNonNull(modfunction, nooperation);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            _modfunction.apply(actionEvent.getSource());
        }
    }

    private static interface Highlighter{
        public Function<Object, NullType> getHiglightFunction();
        public Function<Object, NullType> getUndoFunction();
    }

    public static Highlighter getDefaultHighlighter(){
        return new Highlighter(){

            @Override
            public Function<Object, NullType> getHiglightFunction() {
                return highlightComponentFunction;
            }

            @Override
            public Function<Object, NullType> getUndoFunction() {
                return dehighlighComponentFunction;
            }
        };
    }

    private static Object resolveComponentFromSource(Object source){
        if(source instanceof EventObject){
            return resolveComponentFromSource(((EventObject)source).getSource());

        }
        if(source instanceof JComponent){
            return source;
        }
        return source;
    }

    private static final Function<Object, NullType> highlightComponentFunction = new Function<Object, NullType>(){

        @Override
        public NullType apply(@Nullable Object input) {
            input = resolveComponentFromSource(input);
            if(input instanceof JComponent){
                JComponent comp = (JComponent) input;
                comp.setBorder(BorderFactory.createLineBorder(Color.white, 3));
            }
            return null;
        }
    };

     private static final Function<Object, NullType> dehighlighComponentFunction = new Function<Object, NullType>(){

        @Override
        public NullType apply(@Nullable Object input) {
            input = resolveComponentFromSource(input);
            if(input instanceof JComponent){
                JComponent comp = (JComponent) input;
                comp.setBorder(BorderFactory.createLineBorder(Color.white, 3));
            }
            return null;
        }
    };

    private static Action newHighlightSourceAction(Container cont, final JComponent label){
        return Actions.compose(
                newModifySourceAction(getDefaultHighlighter().getHiglightFunction()),
                new ModifyAllInContainerBut(cont, label, getDefaultHighlighter().getUndoFunction()));
    }

    public static <T> Predicate<Predicate<T>> appliesPredicateOn(final T value){
        return new Predicate<Predicate<T>>(){

            @Override
            public boolean apply(@Nullable Predicate<T> input) {
                return input.apply(value);
            }
        } ;
    }

     private void filterAndExecuteActions(Map<Predicate<MouseEvent>, Action> actionMap, MouseEvent mouseEvent) {
        Iterable<Predicate<MouseEvent>> results =
        Iterables.filter(actionMap.keySet(), appliesPredicateOn(mouseEvent));

        for(Predicate<MouseEvent> result : results){
            Action action = actionMap.get(result);
            Object nameObj = action.getValue(Action.NAME);
            String name = nameObj == null ? "NO_COMMAND_GIVEN" : nameObj.toString();
            action.actionPerformed(
                    MouseEvents.toActionEvent(mouseEvent, name));
        }
    }

    private static class DragTargetAction extends AbstractAction{

        private final Container _cont;
        private Point _lastPoint = null;
        private Component _draggingComponent;
        private DragTargetAction(Container cont){
            _cont = cont;
        }

        public void completeDrag(){
            _lastPoint = null;
            _draggingComponent = null;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object src = actionEvent.getSource();
            if(src instanceof MouseEvent){
                Point currentPoint = ((MouseEvent)src).getLocationOnScreen();
                if(_lastPoint != null){
                    Point diff = Points.difference(currentPoint, _lastPoint);
                    Point containerPoint = new Point(currentPoint);
                    SwingUtilities.convertPointFromScreen(containerPoint, _cont);
                    if(_draggingComponent == null){
                        _draggingComponent = SwingUtilities.getDeepestComponentAt(_cont, containerPoint.x, containerPoint.y);
                    }
                    _draggingComponent.setLocation(Points.sum(_draggingComponent.getLocation(), diff));

                }

                _lastPoint = currentPoint;
            }
        }
    }

    private void addShortcutAtLocation(final Shortcut shortcut, Point location){
        final JLabel label = new JLabel(shortcut.getName());
        label.setBorder(BorderFactory.createEmptyBorder());
        label.putClientProperty(Strings.namespaced(DesktopDropTargetListener.class, "Shortcut"), Boolean.TRUE);
        label.setOpaque(false);
        label.setBackground(new Color(0));
        label.setIcon(shortcut.getVisualRep());
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
        MouseAdapter ma = new MouseAdapter(){
            Map<Predicate<MouseEvent>, Action> _mouseClickActions = new HashMap(){{
                put(MouseEvents.isDoubleClick(), shortcut.getAction());
                put(MouseEvents.isSingleClick(), newHighlightSourceAction(desktopComponent, label));
            }};
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                filterAndExecuteActions(_mouseClickActions, mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                super.mouseReleased(mouseEvent);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                super.mouseWheelMoved(mouseWheelEvent);
            }
            Action dragTargetAction = new DragTargetAction(DesktopDropTargetListener.this.desktopComponent);
            Map<Predicate<MouseEvent>, Action> _mouseDragActions = new HashMap(){{
                put(MouseEvents.isLeftButton(), dragTargetAction);
            }};

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseDragged(mouseEvent);
                dragTargetAction.actionPerformed(
                        MouseEvents.toActionEvent(
                                mouseEvent,
                                Strings.namespaced(getClass(), "ShortcutDragAction")));
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                super.mouseMoved(mouseEvent);
            }


        };
        label.addMouseMotionListener(ma);
        label.addMouseListener(ma);
        desktopComponent.add(label);
        label.setLocation(location);
        label.setSize(64, 64);
        desktopComponent.repaint(label.getBounds());
    }

    @Override
    public void drop(DropTargetDropEvent dropTargetDropEvent) {
        System.out.println("Dropping: " + dropTargetDropEvent);
        Transferable xfer = dropTargetDropEvent.getTransferable();
        if(xfer.isDataFlavorSupported(PmGraph.pmNodeDataFlavor)){
            try {

                PmNode[] path = (PmNode[])xfer.getTransferData(PmGraph.pmNodeDataFlavor);
                addShortcutAtLocation(
                        DesktopShortcuts.createPmGraphShortcut(path[path.length - 1].getName(), vosGraphSupplier.get(), path),
                        dropTargetDropEvent.getLocation());
                System.out.println("Dropped pmnode array of " + path);
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @Override
    public Set<String> getObservedProperties() {
        return listenerMapping.keySet();
    }

    @Override
    public PropertyChangeListener listenerForProperty(String property) {
        return listenerMapping.get(property);
    }

    /**
	 * @uml.property  name="listenerMapping"
	 * @uml.associationEnd  qualifier="property:java.lang.String java.beans.PropertyChangeListener"
	 */
    private final Map<String, PropertyChangeListener> listenerMapping = new HashMap<String, PropertyChangeListener>(){{
        put(Session.PM_SESSION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                System.out.println("Session updated");
            }
        });
    }};

    private static class ModifyAllInContainerBut extends AbstractAction {

        private final Container _cont;
        private final Component _comp;
        private final Function<Object, NullType> _mod;
        public ModifyAllInContainerBut(Container cont, Component comp, Function<Object, NullType> mod) {
            _cont = cont; _comp = comp; _mod = mod;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            for(Component comp: filter(asList(_cont.getComponents()), not(equalTo(_comp)))){
                _mod.apply(comp);
            };
        }
    }
}
