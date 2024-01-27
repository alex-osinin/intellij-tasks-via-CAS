package com.aosinin.intellij.tasks.cas;

import com.aosinin.intellij.tasks.cas.resource.CASTaskBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.generic.GenericRepositoryEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.Consumer;

import javax.swing.*;

public class CASGenericRepositoryEditor<T extends CASGenericRepository> extends GenericRepositoryEditor<T> {

    private final JButton reloginButton;

    public CASGenericRepositoryEditor(Project project, T repository, Consumer<? super T> changeListener) {
        super(project, repository, changeListener);

        myLoginMethodTypeComboBox.setVisible(false);
        myLoginAnonymouslyJBCheckBox.setVisible(false);
        myShareUrlCheckBox.setVisible(false);
        myUseHttpAuthenticationCheckBox.setVisible(false);

        myLoginURLLabel.setDisplayedMnemonic(0);
        // in order not to rewrite the form, we use an existing label for our needs
        myLoginURLLabel.setText(CASTaskBundle.message("cas.url"));

        reloginButton = new JButton(CASTaskBundle.message("button.relogin"));
        reloginButton.addActionListener(e -> relogin());

        JPanel generalPanel = (JPanel) myTabbedPane.getComponentAt(0);
        generalPanel.add(reloginButton, new GridConstraints(6, 3, 1, 1, GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
    }

    private void relogin() {
        try {
            myRepository.logoutCAS();
            myRepository.loginCAS();
            Messages.showInfoMessage(myProject, CASTaskBundle.message("relogin.success"),
                    TaskBundle.message("dialog.title.connection"));
        } catch (Exception e) {
            Messages.showErrorDialog(myProject, CASTaskBundle.message("relogin.error"),
                    TaskBundle.message("dialog.title.error"));
        }
    }
}