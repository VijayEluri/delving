package eu.europeana.sip.gui;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * GUI for the AutoCompletionImpl.
 *
 * @author Serkan Demirel <serkan@blackbuilt.nl>
 */
public class AutoCompleteDialog extends JDialog {

    private JScrollPane availableElementsWindow = new JScrollPane();
    private Listener listener;
    private JList jList = new JList();
    private JTextComponent parent;
    private Point lastCaretPosition;

    interface Listener {
        void itemSelected(Object selectedItem);
    }

    public AutoCompleteDialog(Listener listener, JTextComponent parent) {
        this.listener = listener;
        this.parent = parent;
        init();
    }

    private void init() {
        setAlwaysOnTop(true);
        setSize(new Dimension(300, 200));
        setUndecorated(true);
        add(availableElementsWindow);
        jList.addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyReleased(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP:
                                if (0 == jList.getSelectedIndex()) {
                                    parent.requestFocus();
                                    parent.getCaret().setMagicCaretPosition(lastCaretPosition);
                                }
                                break;
                            case KeyEvent.VK_ENTER:
                                selectItem(e);
                                parent.requestFocus();
                                break;
                            case KeyEvent.VK_ESCAPE:
                                setVisible(false);
                                parent.requestFocus();
                                break;
                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_RIGHT:
                                parent.requestFocus();
                                break;
                        }
                    }
                }
        );
    }

    public void updateLocation(Point caretLocation, Point editorLocation) {
        if (null == caretLocation) {
            return;
        }
        Point point = new Point(
                (int) caretLocation.getX() + (int) editorLocation.getX(),
                (int) caretLocation.getY() + (int) editorLocation.getY() + 16 // todo: get caret height
        );
        setLocation(point);
    }

    public void updateElements(List<String> availableElements) {
        if (null == availableElements) {
            setVisible(false);
            parent.requestFocus();
            return;
        }
        if (!isVisible()) {
            setVisible(true);
        }
        jList.setListData(availableElements.toArray());
        jList.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        selectItem(e);
                    }
                }
        );
        availableElementsWindow.getViewport().setView(jList);
        Dimension dimension = new Dimension(
                (int) availableElementsWindow.getViewport().getPreferredSize().getWidth() * 2,
                (int) availableElementsWindow.getViewport().getPreferredSize().getHeight() * 2
        );
        setSize(dimension);
    }

    public void requestFocus(Point lastCaretPosition) {
        this.lastCaretPosition = lastCaretPosition;
        jList.requestFocus();
        if (-1 == jList.getSelectedIndex()) {
            jList.setSelectedIndex(0);
        }
    }

    private void selectItem(InputEvent inputEvent) {
        setVisible(false);
        listener.itemSelected(((JList) inputEvent.getSource()).getSelectedValue());
    }
}
