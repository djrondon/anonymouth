package edu.drexel.psal.anonymouth.gooie;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import edu.drexel.psal.anonymouth.utils.SpecialCharacterTracker;
import edu.drexel.psal.jstylo.generics.Logger;

/**
 * @author Marc Barrowclift
 * @author Andrew W.E. McDonald
 * 
 * Supported actions:
 * 
 * 1) Adding/removing ellipsis (along with more than one EOS character and variations like "???", "!?", "....", "...", etc)
 * 		-TODO: "This...... another sentence" will break up the sentence into two. It used to break it up into three but
 * 				I modified the regEx "EOS_chars" slightly in SentenceTools to just split into two as if it was "This.... another sentence"
 * 				instead. I sadly can't figure out how to have it keep it a full sentence though while keeping the "This.... another sentence"
 * 				splitting functionality.
 * 2) Adding/removing abbreviations.
 * 3) Adding/removing quotes (handled inherently by a combination of the two checks above and by existing code in SentenceTools)
 * 4) Adding/removing parentheses (handled primarily by SentenceTools)
 */
public class InputFilter extends DocumentFilter {
	
	private final String NAME = "( InputFilter ) - ";
	public final static int UNDOCHARACTERBUFFER = 5;
	public static int currentCharacterBuffer = 0;
	public static boolean isEOS = false; //keeps track of whether or not the current character is an EOS character.
	public static boolean ignoreTranslation = false;
	public static boolean ignoreDeletion = false;
	public static boolean shouldBackup = false;
	private boolean watchForEOS = false; //Lets us know if the previous character(s) were EOS characters.
	private boolean addingAbbreviation = false;
	private String EOS = ".?!"; //Quick and dirty way to identify EOS characters.
	private String[] notEndsOfSentence = {"U.S.","R.N.","M.D.","i.e.","e.x.","e.g.","D.C.","B.C.","B.S.","Ph.D.","B.A.","A.B.","A.D.","A.M.","P.M.","r.b.i.","V.P."}; //we only need to worry about these kinds of abbreviations since SentenceTools takes care of the others
	
	/**
	 * If the user types a character or pastes in text this will get called BEFORE updating the documentPane and firing the listeners.
	 */
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException {	
		if (text.length() == 1) { //If the user is just typing (single character)
			DriverEditor.shouldUpdate = false;
			
			checkAddingEllipses(text);
			checkAddingAbbreviations(text);
			
			if (DriverEditor.startSelection != DriverEditor.endSelection)
				DriverEditor.skipDeletingEOSes = true; 
		} else { //If the user pasted in text of length greater than a single character
			DriverEditor.shouldUpdate = true; //If the user pasted in a massive chunk of text we want to update no matter what.
			Logger.logln(NAME + "User pasted in text, will update");
		}
		
		fb.replace(offset, length, text, attr);
	}
	
	/**
	 * Keeps track of whether or not the user may be typing ellipses and only removeReplaceAndUpdate's when we are sure they have completed
	 * Typing EOS characters and are beginning a new sentence.
	 * @param text - The text the user typed
	 */
	private void checkAddingEllipses(String text) {
		isEOS = EOS.contains(text); //Checks to see if the character is an EOS character.

		if (isEOS && !addingAbbreviation) {
			watchForEOS = true;
			//For whatever reason, startSelection must be subtracted by 1, and refuses to work otherwise.
			DriverEditor.taggedDoc.specialCharTracker.addEOS(SpecialCharacterTracker.replacementEOS[0], DriverEditor.endSelection-1, false);
		} else if (!isEOS && !watchForEOS) { //If the user isn't typing an EOS character and they weren't typing one previously, then it's just a normal character, update.
			DriverEditor.shouldUpdate = true;
		} else if (isEOS && addingAbbreviation) {
			DriverEditor.shouldUpdate = true;
			addingAbbreviation = false;
		}

		//if the user previously entered an EOS character and the new character is not an EOS character, then we should update
		if (watchForEOS && !isEOS) {
			shouldBackup = true;
			watchForEOS = false;
			/**
			 * NOTE: We must NOT call removeReplaceAndUpdate() directly since the currentSentenceString variable that's used for the
			 * call's parameter is not updated yet (for example, the text here in InputFilter my read "TEST.... A sentence", but the
			 * currentSentenceString variable, and the documentPane, only read TEST....A sentence. The quickest and easiest way to fix
			 * this is just have a little flag at the end of the caret listener that calls removeReplaceAndUpdate only when we command
			 * it to from the InputFilter.
			 */
			DriverEditor.shouldUpdate = true;
		}
	}
	
	/**
	 * Keeps track of whether or not the user is entering an abbreviation or not and will only call removeReplaceAndUpdate when we are sure they are in fact
	 * not typing an abbreviation and want to end the sentence.
	 * @param text - The text the user typed
	 */
	private void checkAddingAbbreviations(String text) {
		try {
			String textBeforePeriod = GUIMain.inst.getDocumentPane().getText().substring(DriverEditor.startSelection-2, DriverEditor.startSelection);
			if (textBeforePeriod.substring(1, 2).equals(".") && !EOS.contains(text)) {			
				for (int i = 0; i < notEndsOfSentence.length; i++) {
					if (notEndsOfSentence[i].contains(textBeforePeriod)) {
						DriverEditor.shouldUpdate = false;
						addingAbbreviation = true;
					}
				}
			}
		} catch(StringIndexOutOfBoundsException e) {} //most likely the user is typing at the very beginning of the document, move on.
	}
	
	/**
	 * If the user deletes a character or a section of text this will get called BEFORE updating the documentPane and firing the listeners.
	 */
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
		if (length == 1) { //If the user is just deleting character by character
			DriverEditor.shouldUpdate = false;
			DriverEditor.EOSesRemoved = false;

			checkRemoveEllipses(offset);
			checkRemoveAbbreviations(offset);
		} else { //If the user selected and deleted a section of text greater than a single character
			/**
			 * I know this looks goofy, but without some sort of check to make sure that the document is done processing, this would fire
			 * removeReplaceAndUpdate() in DriverEditor and screw all the highlighting up. There may be a better way to do this...
			 */
			if (GUIMain.processed && !ignoreTranslation) {
				DriverEditor.shouldUpdate = true; //We want to update no matter what since the user is dealing with a chunk of text
				Logger.logln(NAME + "User deleted multiple characters in text, will update");
			} else
				ignoreTranslation = false;
		}

		fb.remove(offset, length);
	}

	/**
	 * Pretty much the same thing as checkAddingEllipses only it's not receiving text, but checking the document pane at the indices given for
	 * the text instead of getting it as a parameter. Essentially checkAddingEllipses but backwards.
	 * @param offset
	 */
	private void checkRemoveEllipses(int offset) {
		isEOS = EOS.contains(GUIMain.inst.getDocumentPane().getText().substring(offset, offset+1)); //checks to see if the deleted character is an EOS character
		
		if (isEOS && EOS.contains(GUIMain.inst.getDocumentPane().getText().substring(offset-1, offset))) { //if it was AND the character before it is ALSO an EOS character...
			watchForEOS = true;
		} else if (!isEOS && !watchForEOS) { //The user deleted a character and didn't delete one previously, nothing to do, update.
			DriverEditor.shouldUpdate = true;
		}
		
		if (watchForEOS && !isEOS) { //if the user previously deleted an EOS character AND the one they just deleted is not an EOS character, we should update.
			watchForEOS = false;
			shouldBackup = true;
			DriverEditor.shouldUpdate = true;
		}
	}
	
	/**
	 * Checks to see if the text we're deleting is an abbreviation, and only updates when ready.
	 * @param offset
	 */
	private void checkRemoveAbbreviations(int offset) {
		try {
			String textBeforeDeletion = GUIMain.inst.getDocumentPane().getText().substring(offset-2, offset+1);

			for (int i = 0; i < notEndsOfSentence.length; i++) {
				if (notEndsOfSentence[i].contains(textBeforeDeletion))
					DriverEditor.shouldUpdate = false;
			}
		} catch(StringIndexOutOfBoundsException e) {} //most likely the user is deleting at the first index of their document, move on
	}
}