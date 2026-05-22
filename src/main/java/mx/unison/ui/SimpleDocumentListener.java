package mx.unison.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Adaptador funcional para reaccionar a cambios de filtros de texto. */
@FunctionalInterface
interface SimpleDocumentListener extends DocumentListener {
    void update(DocumentEvent e);

    default void insertUpdate(DocumentEvent e) { update(e); }

    default void removeUpdate(DocumentEvent e) { update(e); }

    default void changedUpdate(DocumentEvent e) { update(e); }
}
