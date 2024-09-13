package fr.profi.mzDBWizard.gui.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiUtil {

    public static <E> void enableAllChildrenOfClass(Container aRoot, Class<E> aClass, boolean enable) {
        final Component[] children = aRoot.getComponents();
        for (final Component c : children) {
            if (aClass.isInstance(c)) {
                c.setEnabled(enable);
            }
            if (c instanceof Container) {
                enableAllChildrenOfClass((Container) c, aClass, enable);
            }
        }
    }

    public static <E> java.util.List<E> getAllChildrenOfClass(Container aRoot, Class<E> aClass) {
        final List<E> result = new ArrayList<>();
        final Component[] children = aRoot.getComponents();
        for (final Component c : children) {
            if (aClass.isInstance(c)) {
                result.add(aClass.cast(c));
            }
            if (c instanceof Container) {
                result.addAll(getAllChildrenOfClass((Container) c, aClass));
            }
        }
        return result;
    }
}
