package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.glass.events.KeyEvent;

import controller.Controller;
import model.PersonTableListener;

public class MainFrame extends JFrame{
	

	private Toolbar toolbar;
	private FormPanel formPanel;
	private JList ageList;
	private Controller controller;
	private TablePanel tablePanel;
	private PrefsDialog prefsDialog;
	private Preferences prefs;
	private JSplitPane splitPane;
	private JTabbedPane tabPane;
	private MessagePanel messagePanel;
	
	private JFileChooser fileChooser;
	
	public MainFrame() {
		super("Hello World!");
		
		setLayout(new BorderLayout());
		
		toolbar = new Toolbar();
		formPanel = new FormPanel();
		tablePanel = new TablePanel();
		prefsDialog = new PrefsDialog(this);
		tabPane = new JTabbedPane();
		messagePanel = new MessagePanel(this);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tabPane);
		
		splitPane.setOneTouchExpandable(true);
		
		tabPane.addTab("Person Database", tablePanel);
		tabPane.addTab("Messages", messagePanel);
		
		
		prefs = Preferences.userRoot().node("db");
		
		controller = new Controller();
		
		tablePanel.setData(controller.getPeople());
		
		tablePanel.setPersonTableListener(new PersonTableListener() {
			public void rowDeleted(int row) {
				controller.removePerson(row);
			}
		});
		
		tabPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				int tabIndex = tabPane.getSelectedIndex();
				
				if(tabIndex == 1) {
					messagePanel.refresh();
				}
			}
			
		});
		
		prefsDialog.setPrefsListener(new PrefsListener() {
			
			@Override
			public void preferencesSet(String user, String password, int port) {
				prefs.put("user", user);
				prefs.put("password", password);
				prefs.putInt("port", port);
			}
		});
		
		String user = prefs.get("user", "");
		String password = prefs.get("password", "");
		Integer port = prefs.getInt("port", 3306);
		prefsDialog.setDefaults(user, password, port);
		
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new PersonFileFilter());
		
		setJMenuBar(createMenuBar());
		
		
		toolbar.setToolbarListener(new ToolbarListener() {

			@Override
			public void saveEventOcurred() {
				connect();
				
				try {
					controller.save();	
				} catch (Exception e) {
					JOptionPane.showMessageDialog(MainFrame.this, "Unable to save to database.", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
				}
				
				tablePanel.refresh();
			}
			
			public void refreshEventOcurred() {
				connect();
				
				try {
					controller.load();
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(MainFrame.this, "Unable to load from database", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
				}
				
				tablePanel.refresh();
			}
		});
		
		formPanel.setFormListener(new FormListener() {

			@Override
			public void formEventOccured(FormEvent e) {
				controller.addPerson(e);
				tablePanel.refresh();
			}
		});
		
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent arg0) {
				controller.close();
				dispose();
				System.gc();
			}
			
		});


		add(toolbar, BorderLayout.PAGE_START);
		add(splitPane, BorderLayout.CENTER);
		
		
		setMinimumSize(new Dimension(500, 500));
		setSize(600, 600);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	private void connect() {
		try {
			controller.connect();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(MainFrame.this, "Unable to connect to database", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		
		JMenu windowMenu = new JMenu("Window");
		JMenuItem prefsItem = new JMenuItem("Preferences...");
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem exportDataItem = new JMenuItem("Export Data...");
		JMenuItem importDataItem = new JMenuItem("Import Data...");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		fileMenu.add(exportDataItem);
		fileMenu.add(importDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		JMenu showMenu = new JMenu("Show");
		JCheckBoxMenuItem showFormItem = new JCheckBoxMenuItem("Person Form");
		showFormItem.setSelected(true);
		
		showMenu.add(showFormItem);
		windowMenu.add(showMenu);
		windowMenu.add(prefsItem);
		
		prefsItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				prefsDialog.setVisible(true);				
			}
		});
		
		menuBar.add(fileMenu);
		menuBar.add(windowMenu);
		
		showFormItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) ev.getSource();
				
				if(menuItem.isSelected()) {
					splitPane.setDividerLocation((int) formPanel.getMinimumSize().getWidth());
				}
				
				formPanel.setVisible(menuItem.isSelected());
			}
		});
		
		fileMenu.setMnemonic(KeyEvent.VK_F);
		exitItem.setMnemonic(KeyEvent.VK_X);
		
		prefsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));

		
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		
		importDataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		
		importDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.loadFromFile(fileChooser.getSelectedFile());
						tablePanel.refresh();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MainFrame.this,
								"Couldn't load data from file.", "Error",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					System.out.println(fileChooser.getSelectedFile());
				}
			}
		});
		
		exportDataItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
					try {
						controller.saveToFile(fileChooser.getSelectedFile());
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MainFrame.this,
								"Couldn't save data to file.", "Error",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					System.out.println(fileChooser.getSelectedFile());
				}
			}
		});
		
		exitItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ev) {
//				String text = JOptionPane.showInputDialog(MainFrame.this,  
//						"Enter your user name.",
//						"Enter user name",
//						JOptionPane.OK_OPTION|JOptionPane.QUESTION_MESSAGE);
//				
//				System.out.println(text);
				
				int action = JOptionPane.showConfirmDialog(MainFrame.this,  
						"Do you really want to exit the application?",
						"Confirm Exit",
						JOptionPane.OK_CANCEL_OPTION);
				if (action == JOptionPane.OK_OPTION) {
					WindowListener[] listeners = getWindowListeners();
					
					for(WindowListener listener : listeners) {
						listener.windowClosing(new WindowEvent(MainFrame.this, 0));
					}
				}
			}
		});
		
		return menuBar;
	}
}
