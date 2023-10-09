package com.sngular.kloadgen.sampler.gui;

import com.sngular.kloadgen.extractor.model.AsyncApiSR;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AsyncApiSRRenderer extends JLabel implements javax.swing.ListCellRenderer<AsyncApiSR> {

    public AsyncApiSRRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends AsyncApiSR> list, final AsyncApiSR value, final int index, final boolean isSelected, final boolean cellHasFocus) {
        if (Objects.nonNull(value)) {
            setText(value.name());

            final Color background;
            final Color foreground;

            // check if this cell represents the current DnD drop location
            final JList.DropLocation dropLocation = list.getDropLocation();
            if (dropLocation != null
                    && !dropLocation.isInsert()
                    && dropLocation.getIndex() == index) {

                background = Color.BLUE;
                foreground = Color.WHITE;

                // check if this cell is selected
            } else if (isSelected) {
                background = Color.RED;
                foreground = Color.WHITE;

                // unselected, and not the DnD drop location
            } else {
                background = Color.WHITE;
                foreground = Color.BLACK;
            }

            setBackground(background);
            setForeground(foreground);
        }
        return this;
    }

}
