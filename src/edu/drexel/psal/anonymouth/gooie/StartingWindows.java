package edu.drexel.psal.anonymouth.gooie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.drexel.psal.JSANConstants;
import edu.drexel.psal.ANONConstants;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;
import edu.drexel.psal.jstylo.generics.ProblemSet;

/**
 * The starting window of Anonymouth that asks for a session name and allows the user to do one of three actions:
 * 	1.Start Anonymouth (Only available if they have previous document set saved, otherwise it will be greyed out, default action if otherwise).
 * 	2.Create a new problem set (Default action if no previous problem set in records, load and start will be greyed out in this case).
 * 	3.Load a new problem set, action always available.
 * 	4.quitButton, action always available.
 * 
 * This is created in an effort to help guide the user along and not throw too much at them at the beginning.
 * @author Marc Barrowclift
 *
 */
public class StartingWindows extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String NAME = "( StartingWindows ) - ";
	
	private GUIMain main;
	private int width = 520, height = 135;
	private FileDialog load;
	private StartingWindows startingWindows;
	private UserStudySessionName userStudySessionName;
	
	//Swing Components
	private JPanel completePanel;	
	//Top
	private JPanel topPanel;
	private JLabel textLabel;
	private JButton startButton;
	private JPanel startPanel;
	private JPanel textPanel;
	//Bottom
	private JPanel bottomPanel;
	private JPanel buttonPanel;
	private JSeparator separator;
	private JButton loadDocSetButton;
	protected JButton newDocSetButton;
		
	//Listeners
	private ActionListener startListener;
	private ActionListener newDocSetListener;
	protected ActionListener loadDocSetListener;
	
	/**
	 * Constructor
	 * @param main - Instance of GUIMain
	 */
	public StartingWindows(GUIMain main) {	
		initGUI();
		initWindow(main);
		initListeners();
		
		load = new FileDialog(this);
		load.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		userStudySessionName = new UserStudySessionName(this);
	}
	
	/**
	 * Initializes the GUI (visible = false)
	 */
	private void initGUI() {
		startingWindows = this;
		
		loadDocSetButton = new JButton("Load Document Set");
		newDocSetButton = new JButton("New Document Set");
		
		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textLabel = new JLabel();
		textLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
		textPanel.add(textLabel);
		startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		startButton = new JButton("Start");
		startButton.setPreferredSize(new Dimension(100, 30));
		startPanel.add(startButton);

		if (ThePresident.canDoQuickStart) {
			textLabel.setText("Start with previously used document set");
		} else {
			textLabel.setText("No previous document set found");
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
		}
		
		topPanel.add(textPanel);
		topPanel.add(Box.createRigidArea(new Dimension(0,5)));
		topPanel.add(startPanel);
		topPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		buttonPanel.add(loadDocSetButton);
		buttonPanel.add(newDocSetButton);
		
		separator = new JSeparator();
		separator.setMaximumSize(new Dimension(480, 0));
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(separator);
		bottomPanel.add(buttonPanel);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
		//Color Separator
		/*
		bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, -5));
		bottomPanel.add(quitButton);
		bottomPanel.add(loadDocSetButton);
		bottomPanel.add(newDocSetButton);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		bottomPanel.setPreferredSize(new Dimension(520, 35));
		bottomPanel.setBackground(new Color(180, 143, 186));
		*/

		completePanel = new JPanel(new BorderLayout());
		completePanel.add(topPanel, BorderLayout.NORTH);
		completePanel.add(bottomPanel, BorderLayout.SOUTH);
		this.add(completePanel);
	}
	
	/**
	 * Sets all the window attributes (like size, location, etc)
	 * @param main - GUIMain instance
	 */
	private void initWindow(GUIMain main) {
		this.setSize(width, height);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
		this.main = main;
		this.setTitle("Anonymouth Start Window");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Initializes the listeners
	 */
	private void initListeners() {
		startListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startingWindows.setVisible(false);
				DriverEditor.processButtonListener.actionPerformed(e);
			}
		};
		startButton.addActionListener(startListener);
		
		loadDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.logln(NAME+"'Load Problem Set' button clicked on the documents tab");

					load.setTitle("Load A Previous Document Set");
					if (PropertiesUtil.prop.getProperty("recentProbSet") != null) {
						Logger.logln(NAME+"Chooser root directory set to: " + PropertiesUtil.prop.getProperty("recentProbSet"));
						load.setDirectory(PropertiesUtil.prop.getProperty("recentProbSet"));
					} else {
						load.setDirectory(JSANConstants.JSAN_PROBLEMSETS_PREFIX);
					}		
					load.setMode(FileDialog.LOAD);
					load.setMultipleMode(false);
					load.setFilenameFilter(ANONConstants.XML);
					load.setLocationRelativeTo(null);
					load.setVisible(true);

					File[] files = load.getFiles();
					if (files.length != 0) {
						String path = files[0].getAbsolutePath();
						loadProblemSet(path);
					} else {
						Logger.logln(NAME+"Load problem set canceled");
					}
				} catch (NullPointerException arg) {
					arg.printStackTrace();
				}
			}
		};
		loadDocSetButton.addActionListener(loadDocSetListener);
		
		newDocSetListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				main.preProcessWindow.showWindow();
			}
		};
		newDocSetButton.addActionListener(newDocSetListener);
	}
	
	/**
	 * Determines whether or not the user has an acceptable document set built and updates components accordingly
	 * @param ready
	 */
	protected void setReadyToStart(boolean ready, boolean loaded) {
		if (ready) {
			if (loaded)
				textLabel.setText("Start with loaded document set");
			else
				textLabel.setText("Start with finished document set");
			textLabel.setForeground(Color.BLACK);
			startButton.setEnabled(true);
			this.getRootPane().setDefaultButton(startButton);
			startButton.requestFocusInWindow();
		} else {
			if (loaded)
				textLabel.setText("Please finish incomplete document set");
			else
				textLabel.setText("No previous document set found");
			textLabel.setForeground(Color.LIGHT_GRAY);
			startButton.setEnabled(false);
			this.getRootPane().setDefaultButton(newDocSetButton);
			newDocSetButton.requestFocusInWindow();
		}
	}
	
	/**
	 * Makes the prepared window visible
	 */
	@SuppressWarnings("unused") //Eclipse lies, it's being used, it just doesn't like my ANONConstants flag
	public void showStartingWindow() {
		if (ANONConstants.IS_USER_STUDY && ThePresident.sessionName.equals("")) {
			userStudySessionName.showSessionWindow();
		} else {
			Logger.logln(NAME+"Displaying Anonymouth Start Window");

			if (ThePresident.canDoQuickStart) {
				this.getRootPane().setDefaultButton(startButton);
			} else {
				this.getRootPane().setDefaultButton(newDocSetButton);
			}
			
			this.setVisible(true);
			
			if (ThePresident.canDoQuickStart) {
				startButton.requestFocusInWindow();
			} else {
				newDocSetButton.requestFocusInWindow();
			}
		}
	}
	
	/**
	 * Loads the problem set from the given path, attempts to load everything, then checks to make sure everything's ready. Updates
	 * Components accordingly
	 * @param path - The absolute path to the problem set we want to load
	 */
	protected void loadProblemSet(String path) {
		Logger.logln(NAME+"Trying to load problem set at: " + path);
		try {
			main.preProcessWindow.ps = new ProblemSet(path);
			main.ppAdvancedWindow.setClassifier(PropertiesUtil.getClassifier());
			main.ppAdvancedWindow.featureChoice.setSelectedItem(PropertiesUtil.getFeature());
			main.preProcessWindow.driver.titles.clear();
			
			boolean probSetReady = main.preProcessWindow.documentsAreReady();
			if (main.preProcessWindow.driver.updateAllComponents() && probSetReady) {
				setReadyToStart(true, true);
				ThePresident.canDoQuickStart = true;
			} else {
				Logger.logln(NAME+"Some issue was detected constructing the saved Document set, will verify " +
						"if there's still enough documents to move forward");
				if (probSetReady) {
					JOptionPane.showMessageDialog(startingWindows,
							"Anonymouth encountered a few problems loading your document set,\n" +
							"some documents may have not been added in the process. Some\n" +
							"possible causes of this may be:\n\n" +
							"   -The document no longer exists in it's original path\n" +
							"   -The document no longer has read permissions\n" +
							"   -The document is not empty, where it wasn't in the past",
							"Problems with Loading Document Set",
							JOptionPane.WARNING_MESSAGE, ThePresident.dialogLogo);
					setReadyToStart(true, true);
					ThePresident.canDoQuickStart = true;
				} else {
					JOptionPane.showMessageDialog(startingWindows,
							"Anonymouth encountered a few problems loading your document set\n" +
							"and now there isn't enough loaded documents to proceed. Some\n" +
							"possible causes of this may be:\n\n" +
							"   -The document no longer exists in it's original path\n" +
							"   -The document no longer has read permissions\n" +
							"   -The document is not empty, where it wasn't in the past",
							"Problems with Loading Document Set",
							JOptionPane.WARNING_MESSAGE, ThePresident.dialogLogo);
					Logger.logln(NAME+"One or more parts of the loaded doc set are insufficient now to begin due to" +
							"loading problems, cannot quick start");
					setReadyToStart(false, true);
					ThePresident.canDoQuickStart = false;
				}
			}
			
			PropertiesUtil.setProbSet(path);
			main.preProcessWindow.driver.updateTitles();
		} catch (Exception exc) {
			Logger.logln(NAME+"Failed loading problem set at path: "+path, LogOut.STDERR);
			setReadyToStart(false, false);
			ThePresident.canDoQuickStart = false;
			PropertiesUtil.setProbSet("");
			main.preProcessWindow.driver.updateTitles();
			revalidate();
			repaint();
		}
	}
}

/**
 * Simple Frame to ask the user for their name so we can name the log files. This is intended for use in user case studies, and (at least
 * from last discussion) should not be shown or used in a release version of Anonymouth. Whether or not to display this can be easily flipped
 * via the constant boolean "IS_USER_STUDY" in ANONConstants.
 * @author Marc Barrowclift
 *
 */
class UserStudySessionName extends JFrame {

	//Constants
	private static final long serialVersionUID = 1L;
	private final String NAME = "( UserStudySessionName ) - ";
	private final int WIDTH = 520, HEIGHT = 135;
	
	//Class instances
	private UserStudySessionName sessionWindow;
	private StartingWindows startingWindows;
	
	//Swing Components
	private JLabel inputMessage;
	private JTextField textBox;
	private JButton continueButton;
	private JPanel textBoxAndNextPanel;
	private JPanel mainSessionNamePanel;
	
	//Listeners
	private ActionListener continueListener;
	private FocusListener textBoxListener;
	
	/**
	 * Constructor
	 * @param startingWindows - StartingWindows instance
	 */
	public UserStudySessionName(StartingWindows startingWindows) {
		this.startingWindows = startingWindows;
		sessionWindow = this;
		
		initGUI();
		initListeners();
	}
	
	/**
	 * Displays the session window prompt to the user with a nice fade in effect
	 */
	protected void showSessionWindow() {
		Logger.logln(NAME+"Displaying Session Window");
		mainSessionNamePanel.getRootPane().setDefaultButton(continueButton);
		continueButton.requestFocusInWindow();
		
		this.setOpacity((float)0/(float)100);
		this.setVisible(true);
		for (int i = 0; i <= 100; i+=2) {
			this.setOpacity((float)i/(float)100);
			
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.setOpacity((float)1.0);
	}
	
	/**
	 * Initializes the swing components and adds them all to the frame
	 */
	private void initGUI() {		
		inputMessage = new JLabel("<html><center>Please enter your name:</center></html>");
		inputMessage.setHorizontalAlignment(SwingConstants.CENTER);
		inputMessage.setHorizontalTextPosition(SwingConstants.CENTER);
		inputMessage.setFont(new Font("Helvetica", Font.BOLD, 24));
		
		textBox = new JTextField();
		textBox.setPreferredSize(new Dimension(150, 23));
		
		continueButton = new JButton("Continue");
		
		textBoxAndNextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		textBoxAndNextPanel.add(textBox);
		textBoxAndNextPanel.add(continueButton);
		
		mainSessionNamePanel = new JPanel(new BorderLayout(0, 0));
		mainSessionNamePanel.setBorder(new EmptyBorder(35, 20, 35, 20));
		mainSessionNamePanel.add(inputMessage, BorderLayout.NORTH);
		mainSessionNamePanel.add(textBoxAndNextPanel, BorderLayout.SOUTH);
		
		this.add(mainSessionNamePanel);
		this.setUndecorated(true);
		this.setSize(WIDTH, HEIGHT);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(false);
	}
	
	/**
	 * Initialzes all listeners used by the frame and adds them to their respective components
	 */
	private void initListeners() {
		textBoxListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				textBox.setText("");
			}
			@Override
			public void focusLost(FocusEvent e) {
				if (textBox.getText().equals("")) {
					textBox.setText("Anonymouth");
				}
			}
		};
		textBox.addFocusListener(textBoxListener);
		
		continueListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String sessionName = textBox.getText();
				if (sessionName.equals("")) {
					sessionName = "Anonymouth";
				}
				sessionName = sessionName.replaceAll("['.?!()<>#\\\\/|\\[\\]{}*\":;`~&^%$@+=,]", "");
				String tempName = sessionName.replaceAll(" ", "_");
				if (tempName != null)
					sessionName = tempName;
				ThePresident.sessionName = sessionName;
				
				Logger.setFilePrefix("Anonymouth_"+ThePresident.sessionName);
				Logger.logFile = true;
				Logger.initLogFile();
				Logger.logln(NAME+"Logger initialized, GUIMain init complete");
				Logger.logln(NAME+"Session name: " + ThePresident.sessionName);
				
				Logger.logln(NAME+"Closing Session Window");
				
				for (int i = 100; i >= 0; i-=2) {
					sessionWindow.setOpacity((float)i/(float)100);
					
					try {
						Thread.sleep(3);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				sessionWindow.setVisible(false);
				
				startingWindows.showStartingWindow();
			}
		};
		continueButton.addActionListener(continueListener);
	}
}