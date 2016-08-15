import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;

/**
 * Created by sschwartz on 8/10/16.
 */

// careful there are 2s throughout the program to denote tabs (2 spaces usually), but might not always be the case

/*
* use char num to split fileText into two strings (before and after line wanted)
* then turn first section into array, get length (that gives us line num just before key wanted)
* from there iterate upward to find first key with a smaller indentation and store
* concatenate and return
*/
public class YamlPathFinder extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

        // get contents of file, store in a String
        PsiFile file = e.getData(LangDataKeys.PSI_FILE).getContainingFile();
        String fileText = file.getText();

        // instantiates an Editor?
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        // gets char position of beginning of line chosen by user
        int lineStartPosition = editor.getCaretModel().getCurrentCaret().getVisualLineStart();

        String[] textArray = fileText.split("\n");

        String[] pair = getKey(fileText, lineStartPosition);

        String path = getPath(pair, textArray);

        // copies to the clipboard
        StringSelection selection = new StringSelection(path);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

    }

    public static String[] getKey(String fileText, int charPosition)
    {
        // split into two strings, before and after the desired line
        char[] charArray = fileText.toCharArray();
        String tempText = String.copyValueOf(charArray, 0, charPosition);

        // now have array of lines before the key we want
        String[] tempArray = tempText.split("\n");

        // line number of the key we want
        // if printed, will be one less than expected because lines start at 1, but array starts at 0
        int lineNumber = tempArray.length;

        String[] textArray = fileText.split("\n");
        String line = textArray[lineNumber];

        String[] pair = new String[2];
        pair[0] = line;
        pair[1] = "" +lineNumber;

        return pair;
    }

    /**
     *
     * @param pair the path it returns will lead to this key
     * @param textArray the lines in the file to search through to find the key
     * @return the concatenated path to key
     */
    public static String getPath(String[] pair, String[] textArray)
    {
        String key = pair[0];
        int lineNumber = Integer.parseInt(pair[1]);

        String[] tempArray = Arrays.copyOfRange(textArray, 0, lineNumber);

        int lineIndent = findIndent(textArray[lineNumber]);
        String[] pathBuilder = new String[(lineIndent/2)];
        pathBuilder[pathBuilder.length-1] = pair[0];

        for(int i = tempArray.length - 1; i > 0; i--)
        {
            if(tempArray[i].endsWith(":") && findIndent(tempArray[i]) < lineIndent)
            {
                int currentIndex = (findIndent(tempArray[i])/2) - 1;

                if(pathBuilder[currentIndex] == null)
                {
                    pathBuilder[currentIndex] = tempArray[i];
                }

            }
        }

        String path = strip(pathBuilder[0]);

        for(int i = 1; i < pathBuilder.length; i++)
        {

            path += "." +strip(pathBuilder[i]);
        }

        System.out.println(path);


        return path;
    }

    public static String strip(String string)
    {
        string = string.trim();

        string = string.substring(0, string.indexOf(":"));

        return string;
    }

    public static int findIndent(String string)
    {
        char[] chars = string.toCharArray();

        // -1 to get rid of extra count for the \n character
        int count = -1;

        for(int i = 0; i < chars.length; i++)
        {
            count++;
            if (chars[i] != ' ')
            {
                return count;
            }
        }

        return count;
    }
}
