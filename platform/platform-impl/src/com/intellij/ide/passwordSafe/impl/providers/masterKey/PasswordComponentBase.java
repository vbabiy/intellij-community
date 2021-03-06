/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ide.passwordSafe.impl.providers.masterKey;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author gregsh
 */
public abstract class PasswordComponentBase {
  protected final MasterKeyPasswordSafe mySafe;
  private final String myTitle;

  private JPanel myRootPanel;
  private JLabel myIconLabel;
  protected JLabel myPromptLabel;

  protected JPanel myPasswordPanel;
  protected JPanel myNewPasswordPanel;
  protected JPasswordField myPasswordField;
  protected JPasswordField myNewPasswordField;
  protected JPasswordField myConfirmPasswordField;
  protected JCheckBox myEncryptCheckBox;


  public PasswordComponentBase(@NotNull MasterKeyPasswordSafe safe, @NotNull String title) {
    mySafe = safe;
    myTitle = title;
    myIconLabel.setText("");
    myIconLabel.setIcon(Messages.getQuestionIcon()); // todo add some lock icon
    //myPromptLabel.setUI(new MultiLineLabelUI());
    myPromptLabel.setFont(UIUtil.getLabelFont(UIUtil.FontSize.SMALL));

    if (!safe.isOsProtectedPasswordSupported()) {
      myEncryptCheckBox.setSelected(false);
      myEncryptCheckBox.setVisible(false);
    }
    else {
      myEncryptCheckBox.setSelected(safe.isPasswordEncrypted());
    }
  }

  public JComponent getComponent() {
    return myRootPanel;
  }

  public JComponent getPreferredFocusedComponent() {
    if (myPasswordField.isEnabled()) return myPasswordField;
    else if (myNewPasswordField.isEnabled()) return myNewPasswordField;
    throw new AssertionError("no visible fields found");
  }

  public String getTitle() {
    return myTitle;
  }

  public ValidationInfo doValidate() {
    if (myNewPasswordField.isEnabled() && !Arrays.equals(myNewPasswordField.getPassword(), myConfirmPasswordField.getPassword())) {
      return new ValidationInfo("New passwords do not match", myConfirmPasswordField);
    }
    return null;
  }

  public abstract boolean apply();

  public String getHelpId() {
    return null;
  }

  @Nullable
  protected ValidationInfo validatePassword() {
    if (myPasswordField.isEnabled()) {
      String oldPassword = new String(myPasswordField.getPassword());
      if (!mySafe.setMasterPassword(oldPassword)) {
        return new ValidationInfo("Password is incorrect", myPasswordField);
      }
    }
    return null;
  }
}
