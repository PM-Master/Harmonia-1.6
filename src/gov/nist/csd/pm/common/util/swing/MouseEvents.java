package gov.nist.csd.pm.common.util.swing;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/8/11
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public final class MouseEvents {
    public static boolean isDoubleClick(MouseEvent event){
        return isDoubleClick().apply(event);
    }

    public static Predicate<MouseEvent> isDoubleClick(){
        return new Predicate<MouseEvent>(){

            @Override
            public boolean apply(@Nullable MouseEvent input) {
                return input != null && input.getClickCount() == 2;
            }
        };
    }

    public static boolean isSingleClick(MouseEvent event){
        return isSingleClick().apply(event);
    }

    public static Predicate<MouseEvent> isSingleClick(){
        return new Predicate<MouseEvent>(){

            @Override
            public boolean apply(@Nullable MouseEvent input) {
                return input != null && input.getClickCount() == 1;
            }
        };
    }

    public static Predicate<MouseEvent> isLeftButton(){
        return new Predicate<MouseEvent>(){

            @Override
            public boolean apply(@Nullable MouseEvent input) {
                return input != null && input.getButton() == MouseEvent.BUTTON1;
            }
        };
    }

    public static ActionEvent toActionEvent(MouseEvent event, String asCommand){
        return new ActionEvent(event, event.getID(), asCommand, event.getModifiers());
    }
}
