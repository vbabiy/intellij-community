package com.intellij.javadoc;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Denis Zhdanov
 * @since 5/30/11 2:08 PM
 */
public class EnterInJavadocParamDescriptionHandler extends EnterHandlerDelegateAdapter {

  private final JavadocHelper myHelper = JavadocHelper.getInstance();

  @Override
  public Result postProcessEnter(@NotNull final PsiFile file, @NotNull Editor editor, @NotNull DataContext dataContext) {
    if (!CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER
        || !CodeStyleSettingsManager.getSettings(file.getProject()).JD_ALIGN_PARAM_COMMENTS)
    {
      return Result.Continue;
    }
    final CaretModel caretModel = editor.getCaretModel();
    final int caretOffset = caretModel.getOffset();
    final Pair<JavadocHelper.JavadocParameterInfo,List<JavadocHelper.JavadocParameterInfo>> pair
      = myHelper.parse(file, editor, caretOffset);
    if (pair.first == null || pair.first.parameterDescriptionStartPosition == null) {
      return Result.Continue;
    }

    final LogicalPosition caretPosition = caretModel.getLogicalPosition();
    final LogicalPosition nameEndPosition = pair.first.parameterNameEndPosition;
    if (nameEndPosition.line == caretPosition.line && caretPosition.column <= nameEndPosition.column) {
      return Result.Continue;
    }
    
    final int descriptionStartColumn = pair.first.parameterDescriptionStartPosition.column;
    final LogicalPosition desiredPosition = new LogicalPosition(caretPosition.line, descriptionStartColumn);
    final Document document = editor.getDocument();
    final CharSequence text = document.getCharsSequence();
    final int offsetAfterLastWs = CharArrayUtil.shiftForward(text, caretOffset, " \t");
    if (editor.offsetToLogicalPosition(offsetAfterLastWs).column < desiredPosition.column) {
      final int lineStartOffset = document.getLineStartOffset(desiredPosition.line);
      final String toInsert = StringUtil.repeat(" ", desiredPosition.column - (offsetAfterLastWs - lineStartOffset));
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          document.insertString(caretOffset, toInsert);
          PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
        }
      });
    } 

    myHelper.navigate(desiredPosition, editor, file.getProject());
    return Result.Stop;
  }
}
