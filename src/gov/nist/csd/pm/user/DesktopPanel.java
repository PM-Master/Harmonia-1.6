package gov.nist.csd.pm.user;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/7/11
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class DesktopPanel extends JPanel {


    public DesktopPanel() {
        super();
        initialize();
    }

    private void initialize() {
        setBackground(Color.YELLOW);

        setOpaque(false);
    }

    private class PanelDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dropTargetDragEvent) {
            checkDragType(dropTargetDragEvent);
            conditionallyAcceptDrag(dropTargetDragEvent);
        }

        @Override
        public void dragOver(DropTargetDragEvent dropTargetDragEvent) {
            checkDragType(dropTargetDragEvent);
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent) {
            checkDragType(dropTargetDragEvent);
        }

        @Override
        public void dragExit(DropTargetEvent dropTargetEvent) {
        }

        @Override
        public void drop(DropTargetDropEvent dropTargetDropEvent) {
            checkDropType(dropTargetDropEvent);
        }

        private void checkDragType(DropTargetDragEvent dtde) {
            Iterables.any(dtde.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor());
        }

        private void checkDropType(DropTargetDropEvent dte) {
            Iterables.any(dte.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor());
        }

        private final Predicate<DataFlavor> isAcceptableDataFlavor = new Predicate<DataFlavor>(){

            @Override
            public boolean apply(@Nullable DataFlavor input) {
               String printout = Objects.toStringHelper(input)
                       .addValue("In Predicate isAcceptableDataFlavor")
                       .add("MimeType", input.getMimeType())
                       .add("Primary Type", input.getPrimaryType())
                       .add("Sub Type", input.getSubType()).toString();
                System.out.println(printout);
                return false;
            }
        };

        private Predicate<DataFlavor> isAcceptableDataFlavor(){
            return isAcceptableDataFlavor;
        }

        private void conditionallyAcceptDrag(DropTargetDragEvent dtde) {
            if(Iterables.any(dtde.getCurrentDataFlavorsAsList(), isAcceptableDataFlavor())){
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
        }
    }

}
