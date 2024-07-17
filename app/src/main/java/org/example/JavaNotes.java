package org.example;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import javax.swing.KeyStroke;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.Font;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.SwingUtilities;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.undo.UndoManager;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import java.awt.Button;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JComboBox;
import java.awt.GraphicsEnvironment;
import javax.swing.BoxLayout;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.JCheckBox;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import java.nio.file.Files;
import javax.swing.text.Document;
import java.nio.MappedByteBuffer;
import java.lang.Thread;
import java.nio.channels.FileChannel;
import java.util.List;
import java.nio.file.StandardOpenOption;
import java.lang.Runnable;
import java.io.BufferedReader;
import java.io.StringReader;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JButton;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.FlowLayout;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//import com.formdev.flatlaf.intellijthemes.*;


/*
TODO: 
-Fix Bug: sometimes the very end of the file is cutoff.
-maybe add a run button to the menu bar
*/


public class JavaNotes {
	public static JFileChooser chooser = new JFileChooser();
	public static ArrayList<TextEditor> openFiles = new ArrayList<TextEditor>();
	
	public static void main(String[] args) {
		//setup window, textbox and line numbers
		JFrame frame = new JFrame("Java Notes");
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//add supported file types to JFileChooser
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Java File", "java"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Javascript File", "js"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Python File", "py"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("SQL File", "sql"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("C-Sharp File", "cs"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Ruby File", "rb"));
		//chooser.addChoosableFileFilter(new FileNameExtensionFilter("Kotlin File", "kts"));
		
		JTabbedPane tabPane = new JTabbedPane();
		frame.add(tabPane);
		
		Find findMenu = new Find(tabPane, frame);
		Settings settings = new Settings(frame, findMenu.findFrame);
		openFiles.add(new TextEditor("Untitled", settings, null));
		tabPane.addTab(openFiles.get(0).name, new ImageIcon(), openFiles.get(0).scrollPane);
		tabPane.setTabComponentAt(0, new ButtonTab(openFiles.get(0), tabPane));
		
		frame.setJMenuBar(createMenuBar(tabPane, frame, settings, findMenu));
		
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}
	
	public static JMenuBar createMenuBar(JTabbedPane tabPane, JFrame frame, Settings settings, Find findMenu) {
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem newText = new JMenuItem("new");
		newText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		newText.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("new clicked!");
					chooser.setSelectedFile(null);
					TextEditor editor = new TextEditor("Untitled", settings, null);
					openFiles.add(editor);
					tabPane.addTab(editor.name, editor.scrollPane);
					tabPane.setTabComponentAt(openFiles.indexOf(editor), new ButtonTab(editor, tabPane));
					tabPane.setSelectedComponent(editor.scrollPane);
				}
			}
		);
		file.add(newText);
		JMenuItem open = new JMenuItem("open");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		open.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("open clicked!");
					chooser.showOpenDialog(frame);
					TextEditor editor = new TextEditor(chooser.getSelectedFile().getName(), settings, chooser.getSelectedFile());
					//load file
					String content = "";
					try{
						/*FileChannel fileChannel = FileChannel.open(chooser.getSelectedFile().toPath(), StandardOpenOption.READ);
						
						int numberOfThreads = Runtime.getRuntime().availableProcessors();
						long chunkSize = fileChannel.size() / numberOfThreads;
						System.out.println("chunk size = "+chunkSize);
						List<StringThread> threads = new ArrayList<StringThread>();
						for(int i = 0; i < numberOfThreads; i++) {
							long start = i*chunkSize;
							MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, start, chunkSize);
							StringThread stringThread = new StringThread(buffer);
							threads.add(stringThread);
							stringThread.start();
						}
						
						for(StringThread thread: threads) {
							thread.join();
							content += thread.getString();
						}*/
						
						FileChannel fileChannel = new FileInputStream(chooser.getSelectedFile()).getChannel();
						byte[] barray = new byte[(int) chooser.getSelectedFile().length()];
						ByteBuffer bb = ByteBuffer.wrap(barray);
						bb.order(ByteOrder.LITTLE_ENDIAN);
						fileChannel.read(bb);
						content = new String(barray);
						
						openFiles.add(editor);
						tabPane.addTab(editor.name, editor.scrollPane);
						tabPane.setTabComponentAt(openFiles.indexOf(editor), new ButtonTab(editor, tabPane));
						tabPane.setSelectedComponent(editor.scrollPane);
					}catch(IOException ioEx) {
						ioEx.printStackTrace();
					}/*catch(InterruptedException interuptEx) {
						interuptEx.printStackTrace();
					}*/
					
					//make sure each line ends in a \n because the regex patterns rely on it
					String formattedContent = "";
					try(BufferedReader bufferedReader = new BufferedReader(new StringReader(content))){
						String line;
						while((line = bufferedReader.readLine()) != null){ 
							if(line.length() > 0 && line.charAt(line.length()-1) == '\n') formattedContent += line;
							else formattedContent += line + "\n";
							//skip first line addition because it is already added when TextEditor is created
							if(editor.numLines != 1) editor.lineNumbers.getDocument().insertString(editor.lineNumbers.getDocument().getLength(), editor.numLines + "\n", null);
							editor.numLines++;
						}
						bufferedReader.close();
					}catch(IOException bufferError) {bufferError.printStackTrace();} catch(BadLocationException badLocBufferReader) {badLocBufferReader.printStackTrace();}
					
					editor.text.setText(formattedContent);
					editor.undoManager.discardAllEdits();
					
				}
			}
		);
		file.add(open);
		JMenuItem save = new JMenuItem("save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		save.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("save clicked!");
					try(FileWriter writer = new FileWriter(openFiles.get(tabPane.getSelectedIndex()).file)){
						writer.write(openFiles.get(tabPane.getSelectedIndex()).text.getText());
						writer.close();
					}catch(IOException io) {io.printStackTrace();}
				}
			}
		);
		file.add(save);
		JMenuItem saveAs = new JMenuItem("save as");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
		saveAs.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("save as clicked!");
					
					chooser.showSaveDialog(frame);
					openFiles.get(tabPane.getSelectedIndex()).file = chooser.getSelectedFile();
					try(FileWriter writer = new FileWriter(openFiles.get(tabPane.getSelectedIndex()).file)){
						writer.write(openFiles.get(tabPane.getSelectedIndex()).text.getText());
						writer.close();
						openFiles.get(tabPane.getSelectedIndex()).docFilter.loadSytaxHighlighting();
						((ButtonTab) tabPane.getTabComponentAt(tabPane.getSelectedIndex())).setTitle(chooser.getSelectedFile().getName());
					}catch(IOException io) {
						System.out.println("Error Saving File As");
						io.printStackTrace();
					}
					
				}
			}
		);
		file.add(saveAs);
		bar.add(file);
		JMenu edit = new JMenu("Edit");
		JMenuItem undoButton = new JMenuItem("undo");
		undoButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		undoButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("undo clicked!");
					if(openFiles.get(tabPane.getSelectedIndex()).undoManager.canUndo()) openFiles.get(tabPane.getSelectedIndex()).undoManager.undo();
				}
			}
		);
		edit.add(undoButton);
		JMenuItem redoButton = new JMenuItem("redo");
		redoButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		redoButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("redo clicked!");
					if(openFiles.get(tabPane.getSelectedIndex()).undoManager.canRedo()) openFiles.get(tabPane.getSelectedIndex()).undoManager.redo();
				}
			}
		);
		edit.add(redoButton);
		bar.add(edit);
		JMenu tools = new JMenu("Tools");
		JMenuItem find = new JMenuItem("find");
		find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
		find.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					findMenu.open();
				}
			}
		);
		tools.add(find);
		bar.add(tools);
		JMenu settingsButton = new JMenu("Settings");
		settingsButton.addMouseListener(
			new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					System.out.println("settings clicked!");
					settings.open();
				}
				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			}
		);
		bar.add(settingsButton);
		bar.setEnabled(true);
		
		return bar;
	}
	
	private static class Find {
		JFrame findFrame = new JFrame("Find");
		public Find(JTabbedPane tabPane, JFrame frame) {
			StyleContext styleContext = StyleContext.getDefaultStyleContext();
			AttributeSet reset = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Background, Color.white);
			AttributeSet highlight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Background, Color.green);
			findFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			findFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					openFiles.get(tabPane.getSelectedIndex()).text.getStyledDocument().setCharacterAttributes(0, openFiles.get(tabPane.getSelectedIndex()).text.getText().length(), reset, true);
				}
			});
			JPanel findPanel = new JPanel();
			findPanel.setLayout(new BoxLayout(findPanel, BoxLayout.Y_AXIS));
			JTextField querry = new JTextField(25);
			JPanel textPanel = new JPanel();
			textPanel.add(querry);
			findPanel.add(textPanel);
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			Button findButton = new Button("Find");
			findButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openFiles.get(tabPane.getSelectedIndex()).text.getStyledDocument().setCharacterAttributes(0, openFiles.get(tabPane.getSelectedIndex()).text.getText().length(), reset, true);
						Pattern pattern = Pattern.compile(querry.getText());
						Matcher matcher = pattern.matcher(openFiles.get(tabPane.getSelectedIndex()).text.getText());
						while(matcher.find()) {
							openFiles.get(tabPane.getSelectedIndex()).text.getStyledDocument().setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), highlight, false);
						}
					}
				}
			);
			buttonPanel.add(findButton);
			findPanel.add(buttonPanel);
			//empty border for aesthetics
			findPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			
			findFrame.add(findPanel);
			findFrame.pack();
			findFrame.setResizable(false);
			findFrame.setVisible(false);
			findFrame.setLocationRelativeTo(frame);
		}
		
		public void open() {
			findFrame.setVisible(true);
		}
	}
	
	private static class StringThread extends Thread {
		private MappedByteBuffer buffer;
		private String string;
		
		public StringThread(MappedByteBuffer buffer) {
			this.buffer = buffer;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public void run() {
			byte[] bytes = new byte[buffer.remaining()];
			try{
				buffer.get(bytes, buffer.position(), buffer.remaining()); // can also just use 0 instead of buffer.position()
			}catch(BufferUnderflowException bufunderflowex) {
				bufunderflowex.printStackTrace();
			}
			string = new String(bytes);
			System.out.print(string+"/");
		}
	}
	
	private static class ButtonTab extends JPanel {
		private JLabel label;
		private JButton button;
		
		public ButtonTab(TextEditor editor, JTabbedPane tabPane) {
			setOpaque(false);
			label = new JLabel(editor.name);
			add(label);
			//button.setFont(editor.settings.font);
			button = new JButton("<html><font color='red'><b>X</b></font></html>");
			button.setContentAreaFilled(false);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//remove tab
					tabPane.remove(editor.scrollPane);
					openFiles.remove(editor);
				}
			});
			add(button);
		}
		
		public String getTitle() {
			return label.getText();
		}
		
		public void setTitle(String text) {
			label.setText(text);
		}
	}
	
	private static class TextEditor {
		public int numLines = 1;
		public JTextPane text;
		public JScrollPane scrollPane;
		public JTextPane lineNumbers = new JTextPane();
		public String name = "Untitled";
		public UndoManager undoManager;
		public HighLightDocumentFilter docFilter;
		private Settings settings;
		public File file;
		
		public TextEditor(String newName, Settings newSettings, File file) {
			text = new JTextPane();
			name = newName;
			settings = newSettings;
			this.file = file;
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			
			StyledDocument lineStyle = lineNumbers.getStyledDocument();
			SimpleAttributeSet align = new SimpleAttributeSet();
			StyleConstants.setAlignment(align, StyleConstants.ALIGN_RIGHT);
			lineStyle.setParagraphAttributes(0, lineStyle.getLength(), align, false);
			lineNumbers.setBackground(Color.LIGHT_GRAY);
			lineNumbers.setAlignmentY(JTextArea.RIGHT_ALIGNMENT);
			lineNumbers.setEnabled(false);
			
			lineNumbers.setDisabledTextColor(Color.darkGray);
			lineNumbers.setText(numLines+"\n");
			lineNumbers.setFont(settings.font);
			lineNumbers.setVisible(settings.lineNumberIsVisible);
			
			//create undo manager
			undoManager = new UndoManager();
			text.getDocument().addUndoableEditListener(undoManager);
			
			text.setForeground((Color)UIManager.getDefaults().get("Color")); //Color.black
			text.setFont(settings.font);
			
			//set HighLightDocumentFilter as the document filter to enable syntax highlighting support
			docFilter = new HighLightDocumentFilter(text);
			((AbstractDocument) text.getDocument()).setDocumentFilter(docFilter);
			
			//System.out.println("text");
			
			//override what happens when BACKSPACE and ENTER are pressed to maintain correct line count
			text.addKeyListener(
				new KeyAdapter() {
					char previousLeft = ' ';
					
					@Override
					public void keyPressed(KeyEvent e) {
						char key = e.getKeyChar();
						if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
							System.out.println("BACKSPACE PRESSED");
							if(text.getCaret().getDot() == 0) return;
							previousLeft = text.getText().charAt(text.getCaret().getDot()-1);
							if(text.getCaret().getDot() > 0) {
								if(previousLeft == '\n') {
									numLines--;
									lineNumbers.setText("");
									for(int i = 1; i <= numLines; i++) {
										lineNumbers.setText(lineNumbers.getText() + i+'\n');
									}
								}
							}
							
						} else {
							//manually make sure \n char is used when pressing insert (done b/c it doesn't seem to always use a \n for some reason when normally pressing enter)
							if(e.getKeyCode() == KeyEvent.VK_ENTER) {
								int pos = text.getCaretPosition();
								int offset = 1; //offset starts at 1 b/c \n will always be inserted into the text
								e.consume();
								key = '\n';
								System.out.println("Enter Pressed!");
								numLines++;
								lineNumbers.setText(lineNumbers.getText() + numLines + key);
								String s1 = text.getText().substring(0, text.getCaretPosition());
								String s2 = text.getText().substring(text.getCaretPosition(), text.getText().length());
								
								//get num tabs at start of previous line and add it to new line
								Pattern lastLinePattern = Pattern.compile("^.*?$", Pattern.MULTILINE | Pattern.DOTALL);
								Matcher lineMatcher = lastLinePattern.matcher(s1);
								String lastLine = "";
								while(lineMatcher.find()) lastLine = lineMatcher.group();//text.getText().substring(lineMatcher.start(), lineMatcher.end());
								//System.out.println("last line matched: " + lastLine + " | ENDOFLINE");
								
								Pattern tabPattern = Pattern.compile("^\\t*", Pattern.MULTILINE);
								Matcher tabMatcher = tabPattern.matcher(lastLine);
								boolean tabsFound = tabMatcher.find();
								String tabs = "";
								if(tabsFound) {
									tabs = tabMatcher.group();
									//System.out.println("tabs: |" + tabs + "|END");
									offset+=tabs.length();
								}
								
								text.setText(s1 + key + tabs + s2);
								//System.out.println(text.getText().substring(0, text.getCaretPosition()) + " | " + text.getCaretPosition());
								text.setCaretPosition(pos+offset);
							}
							
						}
					}
				}
			);
			
			panel.add(text);
			scrollPane = new JScrollPane(panel);
			scrollPane.setRowHeaderView(lineNumbers);
		}
	}
	
	private static class Settings {
		public static Font font;
		public static boolean lineNumberIsVisible;// = false;
		public static JFrame settings;
		public static JComboBox fontName, fontSize, themeComboBox;
		public static JCheckBox lineNumCheckBox;
		
		public Settings(JFrame frame, JFrame find) {
			lineNumberIsVisible = false;
			settings = new JFrame("JavaNotes Settings");
			
			setupSettingsUI(frame, find);
			loadSettings(frame, find);
		}
		
		public void open() {
			settings.setVisible(true);
		}
		
		public void close() {
			settings.setVisible(false);
		}
		
		public static void loadSettings(JFrame frame, JFrame find) {
			String fontFace = "Courier New";
			int fontSizeTemp = 14;
			try{
				FileInputStream settingsFile = new FileInputStream("Settings.txt");
				Scanner settingsScanner = new Scanner(settingsFile);
				while(settingsScanner.hasNextLine()) {
					Scanner line = new Scanner(settingsScanner.nextLine());
					String setting = line.next();
					switch(setting) {
						case "font:":
							fontFace = line.next();
							fontName.setSelectedItem(fontFace);
							break;
						case "font-size:":
							fontSizeTemp = Integer.parseInt(line.next());
							fontSize.setSelectedItem(""+fontSizeTemp);
							break;
						case "line-numbers:":
							lineNumberIsVisible = Boolean.parseBoolean(line.next());
							lineNumCheckBox.setSelected(lineNumberIsVisible);
							break;
						case "theme:":
								try {
									String theme = "";
									while(line.hasNext()) theme = line.next();
									//System.out.println("loading theme: " + theme);
									UIManager.setLookAndFeel( theme.substring(0, theme.length()-1) );
									SwingUtilities.updateComponentTreeUI(frame);
									SwingUtilities.updateComponentTreeUI(settings);
									SwingUtilities.updateComponentTreeUI(find);
									for(int i = 0; i < themeComboBox.getItemCount(); i++) {
										if(((UIManager.LookAndFeelInfo)themeComboBox.getItemAt(i)).getClassName().equals(theme.substring(0, theme.length()-1))) {
											themeComboBox.setSelectedItem(themeComboBox.getItemAt(i));
											break;
										}
									}
								} catch( Exception ex ) {
									System.err.println( "Failed to initialize LaF" );
									ex.printStackTrace();
								}
							break;
					}
				}
			}catch(IOException ioException) {
				ioException.printStackTrace();
			}
			font = new Font(fontFace, Font.PLAIN, fontSizeTemp);
		}
		
		public static void setupSettingsUI(JFrame frame, JFrame find) {
			settings.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			JPanel panelSettings = new JPanel();
			panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.Y_AXIS));
			
			//ui to change the font
			JPanel panelFontName = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel FontNameLabel = new JLabel("Font name:");
			panelFontName.add(FontNameLabel);
			String[] fontNameList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			fontName = new JComboBox(fontNameList);
			panelFontName.add(fontName);
			panelSettings.add(panelFontName);
			
			//ui to change font-size
			JPanel panelFontSize = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel FontSizeLabel = new JLabel("Font size:");
			panelFontSize.add(FontSizeLabel);
			String[] fontSizeList = {"5", "6", "7", "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28"};
			fontSize = new JComboBox(fontSizeList);
			panelFontSize.add(fontSize);
			panelSettings.add(panelFontSize);
			
			//ui to choose line number visibility
			JPanel panelLineNum = new JPanel(new FlowLayout(FlowLayout.LEFT));
			lineNumCheckBox = new JCheckBox("View Line Numbers", lineNumberIsVisible);
			panelLineNum.add(lineNumCheckBox);
			panelSettings.add(panelLineNum);
			
			//install flatlaf themes so that they show up when UIManager.getInstalledLookAndFeels() is called
			FlatLightLaf.installLafInfo();
			FlatDarkLaf.installLafInfo();
			FlatIntelliJLaf.installLafInfo();
			FlatDarculaLaf.installLafInfo();
			
			//ui to choose look and feel
			JPanel panelThemes = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panelThemes.add(new JLabel("Theme:"));
			themeComboBox = new JComboBox(UIManager.getInstalledLookAndFeels());
			panelThemes.add(themeComboBox);
			//add padding to bottom because some LAFs mess up spacing between settings and the apply button
			panelThemes.setBorder(new EmptyBorder(0, 0, 20, 0));
			panelSettings.add(panelThemes);
			
			//must click apply button otherwise settings are not applied
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			Button apply = new Button("Apply");
			apply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try(FileOutputStream settingsFile = new FileOutputStream("Settings.txt")){
						String output = "font: " + fontName.getSelectedItem() + "\n" + "font-size: " + fontSize.getSelectedItem() + "\n" + "line-numbers: " + lineNumCheckBox.isSelected() + "\n" + "theme: " + themeComboBox.getSelectedItem();
						settingsFile.write(output.getBytes());
						settingsFile.close();
					}catch(FileNotFoundException fnf) {
						fnf.printStackTrace();
					}catch(IOException ioe) {ioe.printStackTrace();}
					
					font = new Font(fontName.getSelectedItem().toString(), Font.PLAIN, Integer.parseInt(fontSize.getSelectedItem().toString()));
					lineNumberIsVisible = lineNumCheckBox.isSelected();
					for(TextEditor editor: openFiles) {
						editor.text.setFont(font);
						editor.lineNumbers.setFont(font);
						editor.lineNumbers.setVisible(lineNumberIsVisible);
					}
					
					try {
						UIManager.setLookAndFeel( ((UIManager.LookAndFeelInfo) themeComboBox.getSelectedItem()).getClassName() );
						SwingUtilities.updateComponentTreeUI(frame);
						SwingUtilities.updateComponentTreeUI(settings);
						SwingUtilities.updateComponentTreeUI(find);
					} catch( Exception ex ) {
						System.err.println( "Failed to initialize LaF" );
						ex.printStackTrace();
					}
				}
			});
			buttonPanel.add(apply);
			panelSettings.add(buttonPanel);
			
			settings.add(panelSettings);
			
			//empty border for aesthetics
			panelSettings.setBorder(new EmptyBorder(10, 10, 10, 10));
			
			settings.pack();
			settings.setResizable(false);
			settings.setLocationRelativeTo(null);
			settings.setVisible(false);
		}
	}
	
	private static final class HighLightDocumentFilter extends DocumentFilter {
		
		Pattern pattern;
		Pattern patternSecondary;
		Pattern patternBold;
		Pattern patternBoldSecondary;
		Pattern number = Pattern.compile("[0-9]");
		Pattern comment;
		Pattern multiLineComment;
		Pattern string;
		Pattern charPattern;
		Pattern multiLineComment2;
		//Pattern lines = Pattern.compile("$", Pattern.MULTILINE);
		StyledDocument doc;
		
		public boolean hasTextStyle = false;
		public JTextPane text;
		
		public HighLightDocumentFilter(JTextPane textNew) {
			text = textNew;
			doc = text.getStyledDocument();
			loadSytaxHighlighting();
			//System.out.println("UIManager Color = " + UIManager.getDefaults());
		}
		
		public void loadSytaxHighlighting() {
			if(chooser.getSelectedFile() == null) {
				hasTextStyle = false;
				return;
			}
			Pattern extensionPattern = Pattern.compile("(?<=\\.).*");
			Matcher fileExtension = extensionPattern.matcher(chooser.getSelectedFile().getPath());
			InputStream filetype;
			
			if(fileExtension.find()) {
				System.out.println(fileExtension.group());
				filetype = HighLightDocumentFilter.class.getResourceAsStream("/Syntax_"+fileExtension.group()+".txt");
			} else {
				hasTextStyle = false;
				return;
			}
			System.out.println(filetype);
			if(filetype == null) {
				hasTextStyle = false;
				return;
			}
			
			hasTextStyle = true;
			
			Scanner scanner = new Scanner(filetype);
			while(scanner.hasNextLine()) {
				Scanner lineScanner = new Scanner(scanner.nextLine());
				String storage = lineScanner.next();//get first word in line
				System.out.println(storage);
				switch(storage) {
					case "keywords:" :
						ArrayList<String> list = new ArrayList<String>();
						while(lineScanner.hasNext()) {
							list.add(lineScanner.next());
						}
						System.out.println(list);
						pattern = buildPattern(list);
						break;
					case "keywordsSecondary:" :
						ArrayList<String> list2 = new ArrayList<String>();
						while(lineScanner.hasNext()) {
							list2.add(lineScanner.next());
						}
						System.out.println(list2);
						patternSecondary = buildPattern(list2);
						break;
					case "keywordsBold:" :
						ArrayList<String> listBold = new ArrayList<String>();
						while(lineScanner.hasNext()) {
							listBold.add(lineScanner.next());
						}
						System.out.println(listBold);
						patternBold = buildPattern(listBold);
						break;
					case "keyWordsBoldSecondary:" :
						ArrayList<String> listBold2 = new ArrayList<String>();
						while(lineScanner.hasNext()) {
							listBold2.add(lineScanner.next());
						}
						System.out.println(listBold2);
						patternBoldSecondary = buildPattern(listBold2);
						break;
					case "comment:" :
						if(lineScanner.hasNext()) {
							comment = Pattern.compile(lineScanner.next(), Pattern.MULTILINE);
						}
						break;
					case "multiLineComment:" :
						if(lineScanner.hasNext()) {
							multiLineComment = Pattern.compile(lineScanner.next(), Pattern.MULTILINE);
						}
						break;
					case "multiLineComment2:" :
						if(lineScanner.hasNext()) {
							multiLineComment2 = Pattern.compile(lineScanner.next(), Pattern.MULTILINE);
						}
						break;
					case "string:" :
						if(lineScanner.hasNext()) {
							string = Pattern.compile(lineScanner.next());
						}
						break;
					case "char:" :
						if(lineScanner.hasNext()) {
							charPattern = Pattern.compile(lineScanner.next());
						}
						break;
				}
			}
		}
		
		StyleContext styleContext = StyleContext.getDefaultStyleContext();
		private final AttributeSet purple = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.MAGENTA);
		private final AttributeSet black = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.black);
		private final AttributeSet blue = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.blue);
		private final AttributeSet green = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.green);
		private final AttributeSet gray = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.gray);
		private final AttributeSet cyan = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.cyan);
		private final AttributeSet orange = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.orange);
		private final AttributeSet red = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.red);
		private final AttributeSet pink = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.pink);
		private final AttributeSet bold = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Bold, true);
		private final AttributeSet highlight = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Background, Color.green);
		
		//create matchers but without initializing them
		Matcher matcher;
		Matcher matcherSecondary;
		Matcher matcherBold;
		Matcher matcherBoldSecondary;
		Matcher matcherNumber;
		Matcher matcherComment;
		Matcher matcherMultiLineComment;
		Matcher matcherString;
		Matcher matcherCharPattern;
		Matcher matcherMultiLineComment2;
		
		@Override
		public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			super.insertString(fb, offset, string, attr);
			
			handleTextChanged();
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String textTemp, AttributeSet attributeSet) throws BadLocationException {
			super.replace(fb, offset, length, textTemp, attributeSet);

			handleTextChanged();
		}
		
		@Override
		public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
			
			handleTextChanged();
		}
		
		public void handleTextChanged() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if(hasTextStyle) updateTextStyles();
				}
			});
		}
		
		public void updateTextStyles() {
			//clear color
			doc.setCharacterAttributes(0, text.getText().length(), black, true);
			
			//get text
			String paneText = text.getText();
			
			//look for tokens and highlight them
			if(pattern != null) {
			matcher = pattern.matcher(paneText);
				while(matcher.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), purple, false);
				}
			}
			if(patternSecondary != null) {
				matcherSecondary = patternSecondary.matcher(paneText);
				while(matcherSecondary.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherSecondary.start(), matcherSecondary.end() - matcherSecondary.start(), pink, false);
				}
			}
			if(patternBoldSecondary != null) {
				matcherBoldSecondary = patternBoldSecondary.matcher(paneText);
				while(matcherBoldSecondary.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherBoldSecondary.start(), matcherBoldSecondary.end() - matcherBoldSecondary.start(), red, false);
					doc.setCharacterAttributes(matcherBoldSecondary.start(), matcherBoldSecondary.end() - matcherBoldSecondary.start(), bold, false);
				}
			}
			if(patternBold != null) {
				matcherBold = patternBold.matcher(paneText);
				while(matcherBold.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherBold.start(), matcherBold.end() - matcherBold.start(), blue, false);
					doc.setCharacterAttributes(matcherBold.start(), matcherBold.end() - matcherBold.start(), bold, false);
				}
			}
			if(number != null) {
				matcherNumber = number.matcher(paneText);
				while(matcherNumber.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherNumber.start(), matcherNumber.end() - matcherNumber.start(), orange, false);
				}
			}
			if(string != null) {
				matcherString = string.matcher(paneText);
				while(matcherString.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherString.start(), matcherString.end() - matcherString.start(), gray, false);
				}
			}
			if(charPattern != null) {
				matcherCharPattern = charPattern.matcher(paneText);
				while(matcherCharPattern.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherCharPattern.start(), matcherCharPattern.end() - matcherCharPattern.start(), gray, false);
				}
			}
			if(comment != null) {
				matcherComment = comment.matcher(paneText);
				while(matcherComment.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherComment.start(), matcherComment.end() - matcherComment.start(), green, false);
				}
			}
			if(multiLineComment != null) {
				matcherMultiLineComment = multiLineComment.matcher(paneText);
				while(matcherMultiLineComment.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherMultiLineComment.start(), matcherMultiLineComment.end() - matcherMultiLineComment.start(), green, false);
				}
			}
			if(multiLineComment2 != null) {
				matcherMultiLineComment2 = multiLineComment2.matcher(paneText);
				while(matcherMultiLineComment2.find()) {
					//change color of recognized tokens
					doc.setCharacterAttributes(matcherMultiLineComment2.start(), matcherMultiLineComment2.end() - matcherMultiLineComment2.start(), cyan, false);
				}
			}
		}
		
		public Pattern buildPattern(ArrayList<String> words) {
			StringBuilder sb = new StringBuilder();
			for(String token : words) {
				sb.append("\\b"); //start a word boundary
				sb.append(token);
				sb.append("\\b|"); //end of word boundary and an or for the next word
			}
			if(sb.length() > 0) {
				sb.deleteCharAt(sb.length()-1); //remove trailing "|"
			}
			
			return Pattern.compile(sb.toString());
		}
	}
}