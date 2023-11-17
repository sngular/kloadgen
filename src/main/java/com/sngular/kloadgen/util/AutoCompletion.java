/* This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication, visit
 * http://creativecommons.org/licenses/publicdomain/
 */

package com.sngular.kloadgen.util;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Objects;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import com.sngular.kloadgen.exception.KLoadGenException;

public class AutoCompletion extends PlainDocument {

  private final JComboBox<String> comboBox;

  private final boolean hidePopupOnFocusLoss;

  private final transient KeyListener editorKeyListener;

  private final transient FocusListener editorFocusListener;

  private transient ComboBoxModel<String> model;

  private JTextComponent editor;

  private boolean selecting = false;

  private boolean hitBackspace = false;

  private boolean hitBackspaceOnSelection;

  public AutoCompletion(final JComboBox<String> comboBox) {
    this.comboBox = comboBox;
    model = comboBox.getModel();
    comboBox.addActionListener(e -> {
      if (!selecting) {
        highlightCompletedText(0);
      }
    });
    comboBox.addPropertyChangeListener(e -> {
      if (e.getPropertyName().equals("editor")) {
        configureEditor((ComboBoxEditor) e.getNewValue());
      }
      if (e.getPropertyName().equals("model")) {
        model = (ComboBoxModel) e.getNewValue();
      }
    });
    editorKeyListener = new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (comboBox.isDisplayable()) {
          comboBox.setPopupVisible(true);
        }
        hitBackspace = false;
        switch (e.getKeyCode()) {
          case KeyEvent.VK_BACK_SPACE:
            hitBackspace = true;
            hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
            break;
          case KeyEvent.VK_DELETE:
            e.consume();
            comboBox.getToolkit().beep();
            break;
          default:
            break;
        }
      }
    };
    hidePopupOnFocusLoss = System.getProperty("java.version").startsWith("1.5");
    editorFocusListener = new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        highlightCompletedText(0);
      }

      @Override
      public void focusLost(final FocusEvent e) {
        if (hidePopupOnFocusLoss) {
          comboBox.setPopupVisible(false);
        }
      }
    };
    configureEditor(comboBox.getEditor());
    final Object selected = comboBox.getSelectedItem();
    if (selected != null) {
      setText(selected.toString());
    }
    highlightCompletedText(0);
  }

  private void highlightCompletedText(final int start) {
    editor.setCaretPosition(getLength());
    editor.moveCaretPosition(start);
  }

  private void configureEditor(final ComboBoxEditor newEditor) {
    if (editor != null) {
      editor.removeKeyListener(editorKeyListener);
      editor.removeFocusListener(editorFocusListener);
    }

    if (newEditor != null) {
      editor = (JTextComponent) newEditor.getEditorComponent();
      editor.addKeyListener(editorKeyListener);
      editor.addFocusListener(editorFocusListener);
      editor.setDocument(this);
    }
  }

  private void setText(final String text) {
    try {
      // remove all text and insert the completed string
      super.remove(0, getLength());
      super.insertString(0, text, null);
    } catch (final BadLocationException ex) {
      throw new KLoadGenException(ex.toString());
    }
  }

  public static void main(final String[] args) {
    javax.swing.SwingUtilities.invokeLater(AutoCompletion::createAndShowGUI);
  }

  private static void createAndShowGUI() {
    final JComboBox<String> comboBox = new JComboBox<>(new String[]{"Ester", "Jordi", "Jordina", "Jorge", "Sergi"});
    enable(comboBox);

    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(comboBox);
    frame.pack();
    frame.setVisible(true);
  }

  public static void enable(final JComboBox<String> comboBox) {
    comboBox.setEditable(true);
    new AutoCompletion(comboBox);
  }

  @Override
  public final void remove(final int offs, final int len) throws BadLocationException {
    int editOffs = offs;
    if (selecting) {
      return;
    }
    if (hitBackspace) {

      if (editOffs > 0) {
        if (hitBackspaceOnSelection) {
          editOffs--;
        }
      } else {
        comboBox.getToolkit().beep();
      }
      highlightCompletedText(editOffs);
    } else {
      super.remove(editOffs, len);
    }
  }

  @Override
  public final void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
    int editOffs = offs;
    if (selecting) {
      return;
    }
    super.insertString(editOffs, str, a);
    Object item = lookupItem(getText(0, getLength()));
    if (item != null) {
      setSelectedItem(item);
    } else {
      item = comboBox.getSelectedItem();
      editOffs = editOffs - str.length();
      comboBox.getToolkit().beep();
    }
    setText(Objects.requireNonNull(item).toString());
    highlightCompletedText(editOffs + str.length());
  }

  private Object lookupItem(final String pattern) {
    final Object selectedItem = model.getSelectedItem();
    Object returnObject = null;
    if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
      returnObject = selectedItem;
    } else {
      int i = 0;
      boolean notFound = true;
      while (i < model.getSize() && notFound) {
        final Object currentItem = model.getElementAt(i);
        if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
          returnObject = currentItem;
          notFound = false;
        } else {
          i++;
        }
      }
    }
    return returnObject;
  }

  private void setSelectedItem(final Object item) {
    selecting = true;
    model.setSelectedItem(item);
    selecting = false;
  }

  private boolean startsWithIgnoreCase(final String str1, final String str2) {
    return str1.toUpperCase().startsWith(str2.toUpperCase());
  }
}

