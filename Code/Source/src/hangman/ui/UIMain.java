package hangman.ui;

import java.util.Map;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;

import hangman.core.ThreadPool;
import hangman.core.GameManager;
import hangman.db.utils.ConnectionPool;


/**
 * Main UI calss.
 * 
 * Contains all the static functions to build the in-game layouts.
 * Some protected to allow only the UI package to use them, while some public so other components will be able 
 * to communicate with the UI (for example, reporting a problem or update progress of process)
 *
 */
public class UIMain {

	private static Shell mainShell;
	private static Text hangmanPic;
	private static Text clueField;
	private static Composite alphabetComposite;
	private static Label currGuess;
	private static Label clueTitle;
	private static Text usrField;
	private static Label passTitle;
	private static Text passField;
	private static Label login;
	private static Label register;
	private static Shell messageShell;
	private static Label nextLocation;
	private static Label currentScore;
	private static Label play;
	private static Label highScores;
	private static Label logout;
	private static Label buildDB;
	private static String fontName;
	private static Text progress;
	private static Label start;
	private static Label dbBuildBack;
	
	
	/**
	 * public fields, used to communicate with the UI Thread
	 */
	public static Display display;
	public static GameManager gameManager;
	public static boolean DEBUG = false;
	public static boolean inParsing=false;

	
	private static final String icon = "icon.png";
	private static Image iconImg = null;
	
	/**
	 * Open the window.
	 * 
	 * @param err
	 */
	public static void open(String err) {
		display = Display.getDefault();
		createContents(err);
		if (mainShell.isDisposed()) {
			while (!messageShell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			return;
		}
		mainShell.open();
		mainShell.layout();
		while (!mainShell.isDisposed() || !messageShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		System.exit(0);
	}
	
	/***
	 * set icon to shell
	 * icon is defined at UIMain.icon
	 * @param shell
	 */
	public static void setIcon(Shell shell)
	{
		try
		{

			if(iconImg == null)
				iconImg = new Image(display, icon);
			shell.setImage(iconImg);
		}
		catch (Exception e)
		{
			System.out.println(icon + " not found, loading default");
		}
	}

	/**
	 * Create some important utils of the UI class such as:
	 * 1) Load the main font
	 * 2) Create a GameManager object to communicate with the DB
	 * 3) Create shells
	 * 4) if(DEBUG==true) Add debug support
	 * 
	 * This is the entry point of the UI Thread.
	 * @param will be displayed if there was an error while performing pre-UI setup
	 * @wbp.parser.entryPoint
	 */
	protected static void createContents(String err) {

		gameManager = new GameManager();

		
		mainShell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
		messageShell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
		setIcon(mainShell); setIcon(messageShell);
		
		mainShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				if(UpdateLayout.shell!=null && !UpdateLayout.shell.isDisposed())UpdateLayout.shell.dispose();
				try {
					ConnectionPool.closePool();
					
				} catch (Exception e) {
					createMessageShell("Error:\nWhile closing connections");
				}
				
			}
		});

		if (Display.getCurrent().loadFont("res/hangmanfont.ttf")) {
			fontName = "DJB CHALK IT UP";
		}
		


		if (DEBUG) {

			mainShell.addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(KeyEvent key) {
					if (key.character == 's')
						createMessageShell("The word is:\n"
								+ gameManager.getRealLocation());
				}

				@Override
				public void keyPressed(KeyEvent arg0) {

				}
			});
		}

		if (err != null) {
			createMessageShell(err);
			mainShell.dispose();
			return;
		}

		createLoginLayout();

	}

	/**
	 * Create the game main layout (actual game page)
	 */
	protected static void createGameMainLayout() {

		setupShell();
		setMainShell();

		WorkerThread w = new WorkerThread(display, 5, gameManager);
		ThreadPool.getPool().executor.execute(w);

		Menu menu = new Menu(mainShell, SWT.BAR);
		mainShell.setMenuBar(menu);

		Composite composite = new Composite(mainShell, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);

		hangmanPic = new Text(composite, SWT.MULTI);
		hangmanPic.setEditable(false);
		hangmanPic.setBounds(0, 0, 220, 276);
		hangmanPic.setFont(new Font(Display.getDefault(), fontName, 20,SWT.NONE));
		hangmanPic.setText(HangmanFormat.getFormat(0));
		hangmanPic.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		clueField = new Text(mainShell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		clueField.setEditable(false);
		clueField.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		clueField.setFont(new Font(Display.getDefault(), fontName, 13, SWT.NONE));
		clueField.setText(gameManager.getNextClue());
		
		Listener scrollBarListener = new Listener (){
		    @Override
		    public void handleEvent(Event event) {
		      Text t = (Text)event.widget;
		      Rectangle r1 = t.getClientArea();
		      Rectangle r2 = t.computeTrim(r1.x, r1.y, r1.width, r1.height);
		      Point p = t.computeSize(SWT.DEFAULT,  SWT.DEFAULT,  true);
		   
		      t.getVerticalBar().setVisible(r2.height <= p.y);
		      if (event.type == SWT.Modify){
		        t.getParent().layout(true);
		        t.showSelection();
		      }
		    }};
	    clueField.addListener(SWT.Resize, scrollBarListener);
	    clueField.addListener(SWT.Modify, scrollBarListener);
		FormData fd_clueField = new FormData();
		fd_clueField.left = new FormAttachment(0, 10);
		fd_clueField.bottom = new FormAttachment(100, -46);
		fd_clueField.top = new FormAttachment(0, 336);
		clueField.setLayoutData(fd_clueField);
		fd_clueField.right = new FormAttachment(100, -129);
		clueField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (!gameManager.isOver())
					return;
				clueField.setEnabled(false);
				UpdateLayout.createUpdateLayout(display,gameManager.updateCurrClue());
			}
		});
		clueField.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent arg0) {
				if (!gameManager.isOver())
					return;
				clueField.setForeground(SWTResourceManager
						.getColor(SWT.COLOR_RED));
			}

			@Override
			public void mouseExit(MouseEvent arg0) {
				if (!gameManager.isOver())
					return;
				clueField.setForeground(SWTResourceManager
						.getColor(SWT.COLOR_WHITE));
			}
		});

		alphabetComposite = new Composite(mainShell, SWT.NONE);
		alphabetComposite.setLayout(new GridLayout(7, false));
		FormData fd_alphabetComposite = new FormData();
		fd_alphabetComposite.bottom = new FormAttachment(0, 223);
		fd_alphabetComposite.right = new FormAttachment(0, 659);
		fd_alphabetComposite.top = new FormAttachment(0, 10);
		fd_alphabetComposite.left = new FormAttachment(0, 262);
		alphabetComposite.setLayoutData(fd_alphabetComposite);

		currGuess = new Label(mainShell, SWT.LEFT | SWT.WRAP);
		currGuess.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_currGuess = new FormData();
		fd_currGuess.top = new FormAttachment(alphabetComposite, 6);
		fd_currGuess.bottom = new FormAttachment(clueField, -50);
		fd_currGuess.left = new FormAttachment(composite, 32);
		fd_currGuess.right = new FormAttachment(100, -20);
		currGuess.setLayoutData(fd_currGuess);
		currGuess
				.setFont(new Font(Display.getDefault(), fontName, 25, SWT.NONE));
		currGuess.setText(gameManager.getCurrentGuess());

		final Label nextClue = new Label(mainShell, SWT.NONE);
		nextClue.addMouseTrackListener(new MouseEffectAdapter(nextClue));
		nextClue.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		nextClue.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		nextClue.setText("Next");
		FormData fd_nextClue = new FormData();
		fd_nextClue.right = new FormAttachment(100, -41);
		fd_nextClue.left = new FormAttachment(clueField, 6);
		nextClue.setLayoutData(fd_nextClue);
		if(!gameManager.haveNextClue())nextClue.setEnabled(false);

		final Label prevClue = new Label(mainShell, SWT.NONE);
		fd_nextClue.bottom = new FormAttachment(prevClue, -13);
		prevClue.setEnabled(false);
		prevClue.addMouseTrackListener(new MouseEffectAdapter(prevClue));
		prevClue.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		prevClue.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		prevClue.setText("Previous");
		FormData fd_prevClue = new FormData();
		fd_prevClue.top = new FormAttachment(0, 376);
		fd_prevClue.left = new FormAttachment(clueField, 6);
		fd_prevClue.right = new FormAttachment(nextClue, 0, SWT.RIGHT);
		fd_prevClue.bottom = new FormAttachment(100, -41);
		prevClue.setLayoutData(fd_prevClue);

		clueTitle = new Label(mainShell, SWT.NONE);
		clueTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		clueTitle
				.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		clueTitle.setText("Clue:");
		FormData fd_clueTitle = new FormData();
		fd_clueTitle.right = new FormAttachment(0, 391);
		fd_clueTitle.bottom = new FormAttachment(clueField, -10);
		fd_clueTitle.left = new FormAttachment(0, 10);
		clueTitle.setLayoutData(fd_clueTitle);

		nextLocation = new Label(mainShell, SWT.NONE);
		fd_nextClue.top = new FormAttachment(nextLocation, 17);
		nextLocation.setVisible(false);
		nextLocation.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		nextLocation.setFont(new Font(Display.getDefault(), fontName, 15,SWT.NONE));
		nextLocation.setAlignment(SWT.CENTER);
		FormData fd_nextLocation = new FormData();
		fd_nextLocation.bottom = new FormAttachment(clueField, -17);
		fd_nextLocation.top = new FormAttachment(currGuess, 6);
		fd_nextLocation.right = new FormAttachment(100, -10);
		nextLocation.setLayoutData(fd_nextLocation);
		nextLocation.setText("Next Location ->");
		nextLocation.addMouseTrackListener(new MouseEffectAdapter(nextLocation));
		nextLocation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				if (gameManager.isOver() && !gameManager.isWin())return;
				WorkerThread w = new WorkerThread(display, 7, gameManager);
				ThreadPool.getPool().executor.execute(w);
				nextLocation.setEnabled(false);
				clueField.setEnabled(false);
				alphabetComposite.setEnabled(false);
				prevClue.setEnabled(false);
				nextClue.setEnabled(false);

			}
		});

		currentScore = new Label(mainShell, SWT.NONE);
		fd_nextLocation.left = new FormAttachment(currentScore, 280);
		currentScore.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		currentScore.setFont(new Font(Display.getDefault(), fontName, 22,SWT.NONE));
		FormData fd_currentScore = new FormData();
		fd_currentScore.bottom = new FormAttachment(clueField, 36, SWT.BOTTOM);
		fd_currentScore.top = new FormAttachment(clueField, 6);
		fd_currentScore.left = new FormAttachment(0, 10);
		fd_currentScore.right = new FormAttachment(0, 218);
		currentScore.setLayoutData(fd_currentScore);
		currentScore.setText("Score: 0");
		
		Label back = new Label(mainShell, SWT.NONE);
		back.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		back.setFont(new Font(Display.getDefault(), fontName, 15,SWT.NONE));
		FormData fd_back = new FormData();
		fd_back.top = new FormAttachment(prevClue, 6);
		fd_back.left = new FormAttachment(nextClue, 0, SWT.LEFT);
		fd_back.bottom = new FormAttachment(100, -9);
		fd_back.right = new FormAttachment(100, -20);
		back.setLayoutData(fd_back);
		back.setText("Main Menu>");
		back.addMouseTrackListener(new MouseEffectAdapter(back));
		
		createAlphaBet();

		prevClue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				clueField.setText(gameManager.getPrevClue());
				nextClue.setEnabled(true);
				if (!gameManager.havePrevClue())
					prevClue.setEnabled(false);
			}
		});
		nextClue.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				clueField.setText(gameManager.getNextClue());
				prevClue.setEnabled(true);
				if (!gameManager.haveNextClue())
					nextClue.setEnabled(false);
			}
		});
		back.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				if(UpdateLayout.shell!=null && !UpdateLayout.shell.isDisposed())UpdateLayout.shell.dispose();
				createMenuLayout();
			}
		});

		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();
	}

	/**
	 * called when the WorkerThread is done loading the next location
	 */
	protected static void moveNext() {

		gameManager.NextLocation();
		nextLocation.setVisible(false);
		currGuess.setText(gameManager.getCurrentGuess());
		currGuess.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		clueField.setText("");
		clueTitle.setText("Clue:");
		clueTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		currentScore.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		updateHangman();
		for (Control control : alphabetComposite.getChildren()) {
			if (gameManager.getLetterState(((Label) control).getText()) == 3)
				((Label) control).setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			else
				((Label) control).setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}

		WorkerThread w = new WorkerThread(display, 5, gameManager);
		ThreadPool.getPool().executor.execute(w);

	}

	/**
	 * Create the alphabet of the main game view
	 */
	private static void createAlphaBet() {
		for (int i = 0; i < 26; i++) {
			final Label temp = new Label(alphabetComposite, SWT.NONE);
			temp.setText(" " + (char) ('a' + i) + " ");
			temp.setFont(new Font(Display.getDefault(), fontName, 23, SWT.NONE));
			if (gameManager.getLetterState(temp.getText()) == 3)temp.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			else temp.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			temp.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
					if (gameManager.isOver())return;
					if (gameManager.guess(temp.getText().charAt(1))) {
						currGuess.setText(gameManager.getCurrentGuess());
						if (gameManager.isWin()) {
							endRound();
							updateClues();
						}
					} else {
						temp.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
						updateHangman();
						if (gameManager.isOver() && !gameManager.isWin()) {
							endGame();
							updateClues();
						}
					}
				}

				private void endGame() {
					currGuess.setText(gameManager.getRealLocation());
					currGuess.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
					
					WorkerThread w = new WorkerThread(display, 6, gameManager);
					ThreadPool.getPool().executor.execute(w);

				}

				private void updateClues() {
					clueTitle.setText("Clue: (Click on a clue to edit it)");
					clueTitle.setForeground(SWTResourceManager
							.getColor(SWT.COLOR_RED));

				}

				private void endRound() {
					currGuess.setText(currGuess.getText() + "  `");
					currGuess.setForeground(SWTResourceManager
							.getColor(SWT.COLOR_GREEN));
					nextLocation.setVisible(true);
					gameManager.addScore();
					currentScore.setText("Score: " + gameManager.getScore());
					currentScore.setForeground(SWTResourceManager
							.getColor(SWT.COLOR_GREEN));

				}
			});

			temp.addMouseTrackListener(new MouseTrackAdapter() {
				@Override
				public void mouseEnter(MouseEvent arg0) {
					temp.setForeground(SWTResourceManager
							.getColor(SWT.COLOR_DARK_GRAY));
				}

				@Override
				public void mouseExit(MouseEvent arg0) {
					if (gameManager.getLetterState(temp.getText()) == 2)
						temp.setForeground(SWTResourceManager
								.getColor(SWT.COLOR_RED));
					else if (gameManager.getLetterState(temp.getText()) == 3)
						temp.setForeground(SWTResourceManager
								.getColor(SWT.COLOR_GREEN));
					else
						temp.setForeground(SWTResourceManager
								.getColor(SWT.COLOR_WHITE));
				}
			});
		}

	}

	/**
	 * Update hangman pic
	 */
	private static void updateHangman() {
		hangmanPic.setText(HangmanFormat.getFormat(gameManager
				.getNumOfMistakes()));
	}

	/**
	 * Creates the layout of the login page
	 */
	protected static void createLoginLayout() {

		setupShell();
		setMainShell();

		Label gameTitle = new Label(mainShell, SWT.NONE);
		gameTitle
				.setFont(new Font(Display.getDefault(), fontName, 40, SWT.NONE));
		gameTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gameTitle.setAlignment(SWT.CENTER);
		gameTitle.setText("Location Hangman ^");
		FormData fd_gameTitle = new FormData();
		fd_gameTitle.top = new FormAttachment(0, 94);
		fd_gameTitle.left = new FormAttachment(0, 10);
		fd_gameTitle.right = new FormAttachment(100, -10);
		fd_gameTitle.bottom = new FormAttachment(0, 163);
		gameTitle.setLayoutData(fd_gameTitle);

		Label usrTitle = new Label(mainShell, SWT.NONE);
		usrTitle.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		usrTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		usrTitle.setAlignment(SWT.RIGHT);
		FormData fd_usrTitle = new FormData();
		fd_usrTitle.top = new FormAttachment(gameTitle, 81);
		fd_usrTitle.left = new FormAttachment(0, 10);
		usrTitle.setLayoutData(fd_usrTitle);
		usrTitle.setText("Username:");

		usrField = new Text(mainShell, SWT.NONE);
		fd_usrTitle.right = new FormAttachment(usrField, -6);
		usrField.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		usrField.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_usrField = new FormData();
		fd_usrField.left = new FormAttachment(0, 331);
		fd_usrField.right = new FormAttachment(100, -148);
		fd_usrField.top = new FormAttachment(gameTitle, 81);
		usrField.setLayoutData(fd_usrField);

		passTitle = new Label(mainShell, SWT.NONE);
		passTitle
				.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		passTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		passTitle.setAlignment(SWT.RIGHT);
		FormData fd_passTitle = new FormData();
		fd_passTitle.top = new FormAttachment(usrTitle, 22);
		fd_passTitle.right = new FormAttachment(usrTitle, 0, SWT.RIGHT);
		fd_passTitle.left = new FormAttachment(0, 10);
		passTitle.setLayoutData(fd_passTitle);
		passTitle.setText("Password:");

		passField = new Text(mainShell, SWT.PASSWORD | SWT.NONE);
		passField
				.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		passField.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_passField = new FormData();
		fd_passField.top = new FormAttachment(passTitle, 0, SWT.TOP);
		fd_passField.left = new FormAttachment(usrField, 0, SWT.LEFT);
		fd_passField.right = new FormAttachment(100, -148);
		passField.setLayoutData(fd_passField);

		login = new Label(mainShell, SWT.NONE);
		fd_passTitle.bottom = new FormAttachment(login, -43);
		login.setAlignment(SWT.CENTER);
		login.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		login.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		login.setText("Login");
		FormData fd_login = new FormData();
		fd_login.right = new FormAttachment(usrTitle, 0, SWT.RIGHT);
		fd_login.bottom = new FormAttachment(100, -27);
		fd_login.top = new FormAttachment(100, -68);
		fd_login.left = new FormAttachment(0, 202);
		login.setLayoutData(fd_login);
		login.addMouseTrackListener(new MouseEffectAdapter(login));

		register = new Label(mainShell, SWT.NONE);
		register.setAlignment(SWT.CENTER);
		register.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		register.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		register.setText("Register");
		FormData fd_register = new FormData();
		fd_register.top = new FormAttachment(login, 0, SWT.TOP);
		fd_register.left = new FormAttachment(usrField, 0, SWT.LEFT);
		fd_register.right = new FormAttachment(100, -225);
		register.setLayoutData(fd_register);
		register.addMouseTrackListener(new MouseEffectAdapter(register));

		buildDB = new Label(mainShell, SWT.NONE);
		buildDB.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		buildDB.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_buildDB = new FormData();
		fd_buildDB.bottom = new FormAttachment(0, 34);
		fd_buildDB.left = new FormAttachment(100, -100);
		fd_buildDB.top = new FormAttachment(0, 10);
		fd_buildDB.right = new FormAttachment(100, -10);
		buildDB.setLayoutData(fd_buildDB);
		buildDB.setText("Build DB");
		buildDB.addMouseTrackListener(new MouseEffectAdapter(buildDB));

		buildDB.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				createDBBuildLayout();
			}
		});

		login.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				WorkerThread w = new WorkerThread(display, 1, gameManager);
				ThreadPool.getPool().executor.execute(w);
				createMessageShell("Loading...");
				register.setEnabled(false);
				login.setEnabled(false);
			}

		});
		register.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				createRegisterLayout();

			}
		});

		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();

	}

	/**
	 * Creates the layout of the db-build page
	 * 
	 */
	protected static void createDBBuildLayout() {
		setupShell();
		setMainShell();
		
		 mainShell.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	if(!inParsing)return;
		        MessageBox messageBox = new MessageBox(mainShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		        messageBox.setText("WARNING");
		        messageBox.setMessage("Are you sure you want to stop parsing?\n(The game will be closed and you will have to restart)");
		        int buttonID = messageBox.open();
		        event.doit= (buttonID==SWT.OK);
		        if(buttonID==SWT.OK){
		        	System.exit(0);
		        }
		      }
		    });
		 
		 inParsing=true;
		 
		Label title = new Label(mainShell, SWT.NONE);
		title.setFont(new Font(Display.getDefault(), fontName, 35, SWT.NONE));
		title.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		title.setAlignment(SWT.CENTER);
		FormData fd_title = new FormData();
		fd_title.top = new FormAttachment(0, 60);
		fd_title.left = new FormAttachment(0, 10);
		fd_title.right = new FormAttachment(0, 669);
		fd_title.bottom = new FormAttachment(0, 108);
		title.setLayoutData(fd_title);
		title.setText("Database Build");
		
		progress = new Text(mainShell,SWT.MULTI | SWT.BORDER | SWT.WRAP); //  | SWT.V_SCROLL
		progress.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		progress.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_progress = new FormData();
		fd_progress.bottom = new FormAttachment(title, 226, SWT.BOTTOM);
		fd_progress.top = new FormAttachment(title, 24);
		fd_progress.left = new FormAttachment(0, 40);
		fd_progress.right = new FormAttachment(0, 631);
		progress.setLayoutData(fd_progress);
		progress.setText("");
		
		start = new Label(mainShell, SWT.NONE);
		start.setAlignment(SWT.CENTER);
		start.setFont(new Font(Display.getDefault(), fontName, 25, SWT.NONE));
		start.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_start = new FormData();
		fd_start.top = new FormAttachment(progress, 6);
		fd_start.right = new FormAttachment(100, -280);
		fd_start.left = new FormAttachment(0, 274);
		start.setLayoutData(fd_start);
		start.setText("Start");
		start.addMouseTrackListener(new MouseEffectAdapter(start));
		start.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				WorkerThread w = new WorkerThread(display, 8, gameManager);
				ThreadPool.getPool().executor.execute(w);
				start.setVisible(false);
				dbBuildBack.setVisible(false);
			}
		});
		
		dbBuildBack = new Label(mainShell, SWT.NONE);
		fd_start.bottom = new FormAttachment(dbBuildBack, -6);
		dbBuildBack.setAlignment(SWT.CENTER);
		dbBuildBack.setFont(new Font(Display.getDefault(), fontName, 25, SWT.NONE));
		dbBuildBack.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_dbBuildBack = new FormData();
		fd_dbBuildBack.right = new FormAttachment(start, 0, SWT.RIGHT);
		fd_dbBuildBack.bottom = new FormAttachment(100, -50);
		fd_dbBuildBack.top = new FormAttachment(0, 384);
		fd_dbBuildBack.left = new FormAttachment(start, 0, SWT.LEFT);
		dbBuildBack.setLayoutData(fd_dbBuildBack);
		dbBuildBack.setText("Back");
		dbBuildBack.addMouseTrackListener(new MouseEffectAdapter(dbBuildBack));
		dbBuildBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				inParsing=false;
				createLoginLayout();
			}
		});
		
		
		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();
	}
	
	/**
	 * update progress, use when updating database from YAGO
	 * @param s update text
	 */
	public static void updateProgress(String s){
		if(progress!=null && !progress.isDisposed())
			progress.setText(s);
	}
	
	/**
	 * Called after parsing and upload of DB was done, allows user to go back to main menu
	 */
	public static void doneParsing(){
		if(dbBuildBack!=null && !dbBuildBack.isDisposed())
			dbBuildBack.setVisible(true);
	}

	/**
	 * Creates the layout of the login page
	 */
	protected static void createRegisterLayout() {

		setupShell();
		setMainShell();

		Label gameTitle = new Label(mainShell, SWT.NONE);
		gameTitle.setFont(new Font(Display.getDefault(), fontName, 40, SWT.NONE));
		gameTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gameTitle.setAlignment(SWT.CENTER);
		gameTitle.setText("Register");
		FormData fd_gameTitle = new FormData();
		fd_gameTitle.top = new FormAttachment(0, 94);
		fd_gameTitle.left = new FormAttachment(0, 10);
		fd_gameTitle.right = new FormAttachment(100, -10);
		fd_gameTitle.bottom = new FormAttachment(0, 163);
		gameTitle.setLayoutData(fd_gameTitle);

		Label usrTitle = new Label(mainShell, SWT.NONE);
		usrTitle.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		usrTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		usrTitle.setAlignment(SWT.RIGHT);
		FormData fd_usrTitle = new FormData();
		fd_usrTitle.top = new FormAttachment(gameTitle, 81);
		fd_usrTitle.left = new FormAttachment(0, 10);
		usrTitle.setLayoutData(fd_usrTitle);
		usrTitle.setText("Enter Username:");

		usrField = new Text(mainShell, SWT.NONE);
		fd_usrTitle.right = new FormAttachment(usrField, -6);
		usrField.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		usrField.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_usrField = new FormData();
		fd_usrField.left = new FormAttachment(0, 331);
		fd_usrField.right = new FormAttachment(100, -148);
		fd_usrField.top = new FormAttachment(gameTitle, 81);
		usrField.setLayoutData(fd_usrField);

		passTitle = new Label(mainShell, SWT.NONE);
		passTitle
				.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		passTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		passTitle.setAlignment(SWT.RIGHT);
		FormData fd_passTitle = new FormData();
		fd_passTitle.top = new FormAttachment(usrTitle, 22);
		fd_passTitle.right = new FormAttachment(usrTitle, 0, SWT.RIGHT);
		fd_passTitle.left = new FormAttachment(0, 10);
		passTitle.setLayoutData(fd_passTitle);
		passTitle.setText("Enter Password:");

		passField = new Text(mainShell, SWT.PASSWORD | SWT.NONE);
		passField
				.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		passField.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_passField = new FormData();
		fd_passField.top = new FormAttachment(passTitle, 0, SWT.TOP);
		fd_passField.left = new FormAttachment(usrField, 0, SWT.LEFT);
		fd_passField.right = new FormAttachment(100, -148);
		passField.setLayoutData(fd_passField);

		final Label back = new Label(mainShell, SWT.NONE);
		back.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		back.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_back = new FormData();
		fd_back.bottom = new FormAttachment(gameTitle, 31, SWT.BOTTOM);
		fd_back.right = new FormAttachment(0, 96);
		fd_back.top = new FormAttachment(gameTitle, 5);
		fd_back.left = new FormAttachment(0, 10);
		back.setLayoutData(fd_back);
		back.addMouseTrackListener(new MouseEffectAdapter(back));
		back.setText("<back");

		register = new Label(mainShell, SWT.NONE);
		register.setAlignment(SWT.CENTER);
		register.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		register.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		register.setText("OK");
		FormData fd_register = new FormData();
		fd_register.bottom = new FormAttachment(100, -40);
		fd_register.left = new FormAttachment(0, 276);
		fd_register.right = new FormAttachment(100, -280);
		register.setLayoutData(fd_register);
		register.addMouseTrackListener(new MouseEffectAdapter(register));

		register.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				WorkerThread w = new WorkerThread(display, 2, gameManager);
				ThreadPool.getPool().executor.execute(w);
				createMessageShell("Loading...");
				back.setEnabled(false);
			}
		});

		back.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				createLoginLayout();
			}
		});

		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();

	}

	/**
	 * Creates the layout of the menu page
	 */
	protected static void createMenuLayout() {

		setupShell();
		setMainShell();

		Label gameTitle = new Label(mainShell, SWT.NONE);
		gameTitle
				.setFont(new Font(Display.getDefault(), fontName, 40, SWT.NONE));
		gameTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gameTitle.setAlignment(SWT.CENTER);
		gameTitle.setText("Welcome " + gameManager.getUserName() + "!");
		FormData fd_gameTitle = new FormData();
		fd_gameTitle.top = new FormAttachment(0, 94);
		fd_gameTitle.left = new FormAttachment(0, 10);
		fd_gameTitle.right = new FormAttachment(100, -10);
		fd_gameTitle.bottom = new FormAttachment(0, 163);
		gameTitle.setLayoutData(fd_gameTitle);

		play = new Label(mainShell, SWT.NONE);
		play.setAlignment(SWT.CENTER);
		play.setFont(new Font(Display.getDefault(), fontName, 30, SWT.NONE));
		play.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_play = new FormData();
		fd_play.bottom = new FormAttachment(gameTitle, 119, SWT.BOTTOM);
		fd_play.top = new FormAttachment(gameTitle, 66);
		fd_play.right = new FormAttachment(gameTitle, 0, SWT.RIGHT);
		fd_play.left = new FormAttachment(0, 10);
		play.setLayoutData(fd_play);
		play.setText("PLAY");
		play.addMouseTrackListener(new MouseEffectAdapter(play));

		highScores = new Label(mainShell, SWT.NONE);
		highScores.setText("High Scores");
		highScores.setFont(new Font(Display.getDefault(), fontName, 30,
				SWT.NONE));
		highScores.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		highScores
				.setFont(SWTResourceManager.getFont(fontName, 30, SWT.NORMAL));
		highScores.setAlignment(SWT.CENTER);
		FormData fd_highScores = new FormData();
		fd_highScores.left = new FormAttachment(gameTitle, 0, SWT.LEFT);
		fd_highScores.top = new FormAttachment(play, 6);
		fd_highScores.right = new FormAttachment(gameTitle, 0, SWT.RIGHT);
		highScores.setLayoutData(fd_highScores);
		highScores.addMouseTrackListener(new MouseEffectAdapter(highScores));

		logout = new Label(mainShell, SWT.NONE);
		logout.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		logout.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_logout = new FormData();
		fd_logout.top = new FormAttachment(gameTitle, 16);
		fd_logout.bottom = new FormAttachment(play, -6);
		fd_logout.left = new FormAttachment(gameTitle, 0, SWT.LEFT);
		fd_logout.right = new FormAttachment(0, 86);
		logout.setLayoutData(fd_logout);
		logout.setText("<logout");
		logout.addMouseTrackListener(new MouseEffectAdapter(logout));

		final Label easy = new Label(mainShell, SWT.NONE);
		easy.setToolTipText("Easy locations\r\nMany clues\r\nKindergarten style");
		easy.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		easy.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_easy = new FormData();
		fd_easy.bottom = new FormAttachment(100, -22);
		fd_easy.left = new FormAttachment(0, 92);
		fd_easy.top = new FormAttachment(100, -52);
		fd_easy.right = new FormAttachment(0, 198);
		easy.setLayoutData(fd_easy);
		easy.setText("Easy");

		final Label normal = new Label(mainShell, SWT.NONE);
		normal.setToolTipText("For normal players like:\r\nHigh School student, University students, DB course project testers etc.");
		normal.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		normal.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		normal.setText("Normal");
		FormData fd_normal = new FormData();
		fd_normal.bottom = new FormAttachment(easy, 0, SWT.BOTTOM);
		fd_normal.top = new FormAttachment(easy, 0, SWT.TOP);
		fd_normal.right = new FormAttachment(easy, 195, SWT.RIGHT);
		fd_normal.left = new FormAttachment(easy, 89);
		normal.setLayoutData(fd_normal);

		final Label hardcore = new Label(mainShell, SWT.NONE);
		normal.setToolTipText("Heard about Abbotsford?");
		hardcore.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		hardcore.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		hardcore.setText("Hardcore");
		FormData fd_hardcore = new FormData();
		fd_hardcore.top = new FormAttachment(100, -52);
		fd_hardcore.left = new FormAttachment(100, -180);
		fd_hardcore.bottom = new FormAttachment(100, -22);
		fd_hardcore.right = new FormAttachment(100, -74);
		hardcore.setLayoutData(fd_hardcore);

		play.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				WorkerThread w = new WorkerThread(display, 4, gameManager);
				ThreadPool.getPool().executor.execute(w);
				createMessageShell("Loading...");
				play.setEnabled(false);
				highScores.setEnabled(false);
				logout.setEnabled(false);

			}
		});
		highScores.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {

				WorkerThread w = new WorkerThread(display, 3, gameManager);
				ThreadPool.getPool().executor.execute(w);
				createMessageShell("Loading...");
				play.setEnabled(false);
				highScores.setEnabled(false);
				logout.setEnabled(false);

			}
		});
		logout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				createLoginLayout();
			}
		});

		easy.addMouseListener(new MouseLevelAdapter(easy, normal, hardcore,
				100000));
		normal.addMouseListener(new MouseLevelAdapter(normal, easy, hardcore,
				65000));
		hardcore.addMouseListener(new MouseLevelAdapter(hardcore, easy, normal,
				20000));

		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();

	}

	/**
	 * Creates the layout of the high-scores page
	 */
	protected static void createHighScoresLayout(Map<Integer, String[]> scores,
			int playerHigh) {

		setupShell();
		setMainShell();

		Label gameTitle = new Label(mainShell, SWT.NONE);
		gameTitle
				.setFont(new Font(Display.getDefault(), fontName, 40, SWT.NONE));
		gameTitle.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gameTitle.setAlignment(SWT.CENTER);
		gameTitle.setText("High Scores");
		FormData fd_gameTitle = new FormData();
		fd_gameTitle.top = new FormAttachment(0, 22);
		fd_gameTitle.left = new FormAttachment(0, 10);
		fd_gameTitle.right = new FormAttachment(100, -10);
		fd_gameTitle.bottom = new FormAttachment(0, 91);
		gameTitle.setLayoutData(fd_gameTitle);

		Text high1 = new Text(mainShell, SWT.MULTI);
		high1.setEditable(false);
		high1.setFont(new Font(Display.getDefault(), fontName, 25, SWT.NONE));
		high1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_high1 = new FormData();
		fd_high1.right = new FormAttachment(gameTitle, -107, SWT.RIGHT);
		fd_high1.left = new FormAttachment(gameTitle, 128, SWT.LEFT);
		fd_high1.top = new FormAttachment(gameTitle, 16);
		high1.setLayoutData(fd_high1);

		String table = "";
		for (int i = 1; i <= 5; i++)
			table = table + i + ") " + scores.get(i)[0] + "\t"
					+ scores.get(i)[1] + "\n";
		high1.setText(table);

		Label myScore = new Label(mainShell, SWT.NONE);
		fd_high1.bottom = new FormAttachment(myScore, -2);
		myScore.setAlignment(SWT.CENTER);
		myScore.setFont(new Font(Display.getDefault(), fontName, 30, SWT.NONE));
		myScore.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_myScore = new FormData();
		fd_myScore.top = new FormAttachment(0, 385);
		fd_myScore.bottom = new FormAttachment(100, -10);
		fd_myScore.right = new FormAttachment(gameTitle, 0, SWT.RIGHT);
		fd_myScore.left = new FormAttachment(0, 10);
		myScore.setLayoutData(fd_myScore);
		myScore.setText("Your High Score is: " + playerHigh);

		final Label back = new Label(mainShell, SWT.NONE);
		back.setFont(new Font(Display.getDefault(), fontName, 20, SWT.NONE));
		back.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		FormData fd_back = new FormData();
		fd_back.right = new FormAttachment(high1, -58);
		fd_back.left = new FormAttachment(0, 10);
		fd_back.bottom = new FormAttachment(myScore, -142);
		fd_back.top = new FormAttachment(gameTitle, 117);
		back.setLayoutData(fd_back);
		back.setText("<back");
		back.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				createMenuLayout();
			}
		});
		back.addMouseTrackListener(new MouseEffectAdapter(back));

		mainShell.layout(true, true);
		mainShell.redraw();
		mainShell.update();

	}

	/**
	 * Used to retrieve the username from the login page
	 * 
	 * @return username
	 */
	protected static String getUserName() {
		if (usrField != null)
			return usrField.getText();
		return "";
	}

	/**
	 * Used to retrieve the password from the login page
	 * 
	 * @return password
	 */
	protected static String getPassword() {
		if (passField != null)
			return passField.getText();
		return "";
	}

	/**
	 * Creates main shell window
	 */
	private static void setMainShell() {
		mainShell.setSize(685, 500);
		mainShell.setText("Location Hangman");
		mainShell.setLayout(new FormLayout());
		Image image = new Image(display, System.getProperty("user.dir")
				+ "/res/back.jpg");
		mainShell.setBackgroundImage(image);
		mainShell.setBackgroundMode(SWT.INHERIT_FORCE);
	
	}

	/**
	 * Creates Message shell
	 * 
	 * Use to show messages to the user like loading progress and error messages
	 */
	public static void createMessageShell(String s) {

		if (messageShell.isDisposed())messageShell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN);
		else {
			for (Control c : messageShell.getChildren())
				c.dispose();
		}
		messageShell.setText("Message");
		setIcon(messageShell);
		messageShell.setSize(500, 100);
		messageShell.setLayout(new FormLayout());
		Image image = new Image(display, System.getProperty("user.dir")+ "/res/back.jpg");
		messageShell.setBackgroundImage(image);
		messageShell.setBackgroundMode(SWT.INHERIT_FORCE);
		if (!mainShell.isDisposed()) {
			Point pt = mainShell.getLocation();
			messageShell.setLocation(pt.x + mainShell.getSize().x / 2, pt.y+ mainShell.getSize().y / 2);
		}

		Label loading = new Label(messageShell, SWT.NONE | SWT.WRAP );
		loading.setFont(new Font(Display.getDefault(), fontName, 15, SWT.NONE));
		loading.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		loading.setText(s);
		if (!mainShell.isDisposed())
			messageShell.setLocation(messageShell.getLocation().x- messageShell.getSize().x / 2,messageShell.getLocation().y- messageShell.getSize().y / 2);

		messageShell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if(mainShell !=null && !mainShell.isDisposed())enableLayout();
			}
		});

		messageShell.open();
		messageShell.layout(true, true);
		messageShell.redraw();
		messageShell.update();

	}

	/**
	 * Clears shell before updating it
	 */
	private static void setupShell() {
		for (Control control : mainShell.getChildren()) {
			control.dispose();
		}
		disposeMessage();
	}

	/**
	 * dispose message shell
	 */
	public static void disposeMessage() {
		if (messageShell != null && !messageShell.isDisposed())
			messageShell.dispose();
	}

	/**
	 * dispose main shell
	 */
	public static void disposeMain() {
		if (mainShell != null && !mainShell.isDisposed())
			mainShell.dispose();
	}

	/**
	 * enables the buttons in the main shell
	 */
	protected static void enableLayout() {
		if(mainShell==null)return;
		for (Control c : mainShell.getChildren()) {
			c.setEnabled(true);
		}
	}

	/**
	 * Mouse adapter, creates the 'hover' effect that labels have
	 */
	private static class MouseEffectAdapter extends MouseTrackAdapter {
		private Label control;

		public MouseEffectAdapter(Label control) {
			this.control = control;
		}

		@Override
		public void mouseEnter(MouseEvent arg0) {
			control.setForeground(SWTResourceManager
					.getColor(SWT.COLOR_DARK_GRAY));
		}

		@Override
		public void mouseExit(MouseEvent arg0) {
			control.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}
	}

	/**
	 * Mouse adapter, for the 3 level buttons located on the main menu screen
	 */
	private static class MouseLevelAdapter extends MouseAdapter {
		private Label changed;
		private Label other;
		private Label other2;
		private int level;

		public MouseLevelAdapter(Label changed, Label other, Label other2,
				int level) {
			this.changed = changed;
			this.other = other;
			this.other2 = other2;
			this.level = level;
		}

		@Override
		public void mouseUp(MouseEvent arg0) {
			gameManager.setLevel(level);
			changed.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
			other.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			other2.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		}

	}
}
