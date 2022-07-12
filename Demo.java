package javaGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.event.MenuKeyListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.event.MenuKeyEvent;
import java.awt.Window.Type;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.FontFormatException;

import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import java.awt.FlowLayout;
import java.awt.TextArea;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.SystemColor;

/** File filter for fasta file */
class FastaFileFilter extends FileFilter { // fasta file filter for JFileChooser 
    public String getDescription() {return "Fasta file(*.fasta;*.fas;*.fa)";}
    public boolean accept(File file) {    
        String name = file.getName();    
        return file.isDirectory() || name.toLowerCase().endsWith(".fasta") || name.toLowerCase().endsWith(".fas")|| name.toLowerCase().endsWith(".fa");   
    } 
}  

/** File filter for genetic codon table file */
class gentabFileFilter extends FileFilter { // gentab file filter for JFileChooser 
    public String getDescription() {return "Genetic codon table file(*.tab;*.table;*.txt)";}
    public boolean accept(File file) {    
        String name = file.getName();    
        return file.isDirectory() || name.toLowerCase().endsWith(".tab") || name.toLowerCase().endsWith(".table")|| name.toLowerCase().endsWith(".txt");   
    } 
}  

/** File filter for result file */
class resFileFilter extends FileFilter { // gentab file filter for JFileChooser 
    public String getDescription() {return "Text file(*.Ka.txt;*.Ks.txt;*.KaKs.txt;*.txt)";}
    public boolean accept(File file) {    
        String name = file.getName();    
        return file.isDirectory() || name.toLowerCase().endsWith(".Ka.txt") || name.toLowerCase().endsWith(".Ks.txt")|| name.toLowerCase().endsWith(".KaKs.txt")|| name.toLowerCase().endsWith(".txt");   
    } 
}  



public class Demo extends JFrame {

	NG ng = new NG();
	
	private JPanel contentPane;
	private JTable tableSequenceId;
	private JTable tableSequence;
	private JTable tableRes;
	private JRadioButton rdbtnKaKs;
	private JRadioButton rdbtnKa;
	private JRadioButton rdbtnKs;
	private JTabbedPane tabbedPane;
	private JPanel panelRes;
	private JScrollPane scrollPaneSequence;
	private JScrollPane scrollPaneText;
	private JTextPane textPane;
	private Font fontArial;
	private Font fontNotomono;
	private Font fontNotosans;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Demo frame = new Demo();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,e.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/** refresh JTable */
	public void refresh() throws Exception{
		HashMap<String,HashMap<String,Double>> Res = new HashMap<String,HashMap<String,Double>>();
		// which kind of data will be displayed?
		if(     rdbtnKa.isSelected()) Res = ng.getKa();
		else if(rdbtnKs.isSelected()) Res = ng.getKs();
		else                          Res = ng.getKaKs();
		// then we get data and parse.
		int resNum = Res.keySet().size();
		String[] resName = new String[resNum];
		int i = 0;
		for (String key : Res.keySet()) {
			resName[i] = key;
			i++;
		}
		Arrays.sort(resName);
		String[] resColNames = new String[resNum+1];
		resColNames[0] = "sequence ID";
		for(i=0;i<resNum;i++)resColNames[i+1]=resName[i];
        String[][] resData = new String[resNum][resNum+1];
        for(i=0;i<resNum;i++) {
        	resData[i][0]=resName[i];
        	for(int j=0;j<resNum;j++) {
        		if(i==j)  resData[i][j+1]="";
        		else if(Res.get(resName[i]).get(resName[j])==-1) resData[i][j+1]="N/A";
        		else      resData[i][j+1] = String.format("%-6f",Res.get(resName[i]).get(resName[j]));
        	}
        }
        DefaultTableModel resDtm= new DefaultTableModel(resData, resColNames) {
			@Override
            public boolean isCellEditable(int row, int column) {return false;}// set unEditable
        };
        tableRes.setModel(resDtm);
        tableRes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableRes.getColumnModel().getColumn(0).setPreferredWidth(90); // 9*10px
		tableRes.getColumnModel().getColumn(0).setMinWidth(60); //6*10px
	}
	
	
   class Progress extends Thread {
        private JProgressBar progressBar;
        private NG ng;

        public Progress(JProgressBar progressBar, NG ng) {
            this.progressBar = progressBar;
            this.ng = ng;
        }

        public void run() {
        	ng.analysisKaKs();
            progressBar.setIndeterminate(false);
            progressBar.setString("Completed!");
            progressBar.setValue(100);
            try {
				refresh();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
			}
        }
    }
	   
	/** Run analysis and set progress bar style. */
	public void runAnalysis() {
		progressBar.setIndeterminate(true);
		progressBar.setString("Calculating...");
		new Progress(progressBar, ng).start();
	}
	
	/**
	 * Create the frame.
	 * @throws IOException 
	 * @throws FontFormatException 
	 */
	public Demo() throws FontFormatException, IOException {
		fontArial = Font.createFont(Font.TRUETYPE_FONT,Demo.class.getResourceAsStream("/javaGUI/fonts/arial.ttf"));
		fontNotomono = Font.createFont(Font.TRUETYPE_FONT,Demo.class.getResourceAsStream("/javaGUI/fonts/NotoMono-Regular.ttf"));
		fontNotosans = Font.createFont(Font.TRUETYPE_FONT,Demo.class.getResourceAsStream("/javaGUI/fonts/NotoSans-Regular.ttf"));
		setIconImage(Toolkit.getDefaultToolkit().getImage(Demo.class.getResource("/javaGUI/icons/icons8-geometry-100.png")));
		setFont(fontNotosans.deriveFont((float)14));
		setTitle("javaNG - calc Ka/Ks");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 688, 542);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.WHITE);
		menuBar.setBorderPainted(false);
		setJMenuBar(menuBar);
		
		JMenu mnFileMenu = new JMenu("File");
		mnFileMenu.setBackground(Color.WHITE);
		mnFileMenu.setFont(fontArial.deriveFont((float)14.0));
		menuBar.add(mnFileMenu);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.setBackground(Color.WHITE);
		mntmOpen.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-opened-folder-16.png")));
		mntmOpen.setFont(fontArial.deriveFont((float)14.0));
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(".");//
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FastaFileFilter fastaFilter = new FastaFileFilter(); //fasta file filter   
				fc.addChoosableFileFilter(fastaFilter);  
				fc.setFileFilter(fastaFilter);  
				fc.showOpenDialog(null);
				String fpath = fc.getSelectedFile().getPath();
				try {
					ng.initData();
					ng.loadData(fpath);
				}catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
				
		        HashMap<String,String> sequences = ng.getData();
		        int fasnum = sequences.keySet().size();
		        int maxIdLength = 0;
				String[] fasname = new String[fasnum];
				int i = 0;
				for (String key : sequences.keySet()) {
					fasname[i] = key;
					if(key.length()> maxIdLength)maxIdLength=key.length();
					i++;
				}
				int seqlen = sequences.get(fasname[0]).length();
				Arrays.sort(fasname);

		        String[] seqIdColumnNames = new String[]{"ID"};
		        String[][] seqIdData = new String[fasnum][1];
		        for(i=0;i<fasnum;i++)seqIdData[i][0]=fasname[i];
				DefaultTableModel SeqIdDtm= new DefaultTableModel(seqIdData, seqIdColumnNames) {
		            @Override
		            public boolean isCellEditable(int row, int column) {return false;}// set unEditable
		        };
		        
		        String[] seqColumnNames = new String[]{"Sequence"};
		        String[][] seqData = new String[fasnum][1];
		        for(i=0;i<fasnum;i++)seqData[i][0]=sequences.get(fasname[i]);
				DefaultTableModel SeqDtm= new DefaultTableModel(seqData, seqColumnNames) {
					@Override
		            public boolean isCellEditable(int row, int column) {return false;}// set unEditable
		        };
		        
		        // Use JSplitPane to show DNA sequence.
				tableSequenceId.setModel(SeqIdDtm);
				// adjust column width.
				tableSequenceId.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				tableSequenceId.getColumnModel().getColumn(0).setPreferredWidth((maxIdLength+1)*10); //15pt = 10px
				tableSequenceId.getColumnModel().getColumn(0).setMinWidth((maxIdLength+1)*10); //15pt = 10px
				tableSequence.setModel(SeqDtm);
				// adjust column width.
				tableSequence.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				tableSequence.getColumnModel().getColumn(0).setPreferredWidth((seqlen+1)*9); //15pt = 10px
				tableSequence.getColumnModel().getColumn(0).setMinWidth((seqlen+1)*9); //15pt = 10px
				tabbedPane.setSelectedComponent(scrollPaneSequence);
			}
		});
		mnFileMenu.add(mntmOpen);
		
		JMenuItem mntmExport = new JMenuItem("Export");
		mntmExport.setBackground(Color.WHITE);
		mntmExport.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-save-16.png")));
		mntmExport.setFont(fontArial.deriveFont((float)14.0));
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String prefix = "";
				if(     rdbtnKa.isSelected()) prefix = ".Ka.txt";
				else if(rdbtnKs.isSelected()) prefix = ".Ks.txt";
				else                          prefix = ".KaKs.txt";
				JFileChooser fc = new JFileChooser(".");//
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				resFileFilter resFilter = new resFileFilter(); //fasta file filter   
				fc.addChoosableFileFilter(resFilter);  
				fc.setFileFilter(resFilter);  
				fc.showSaveDialog(null);
				String fpath = fc.getSelectedFile().getPath();
				HashMap<String,HashMap<String,Double>> res = ng.getKa();
				if(res.isEmpty()) JOptionPane.showMessageDialog(null,"Please analysis data first!","Error",JOptionPane.ERROR_MESSAGE);
				else {
					try {
						ng.saveKa(fpath+".Ka.txt");
						ng.saveKs(fpath+".Ks.txt");
						ng.saveKaKs(fpath+".KaKs.txt");
						JOptionPane.showMessageDialog(null,"File saved!");
					}catch(Exception e1) {
						JOptionPane.showMessageDialog(null,e1.getMessage(),"Error!",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		mnFileMenu.add(mntmExport);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setBackground(Color.WHITE);
		mntmExit.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-close-window-16.png")));
		mntmExit.setFont(fontArial.deriveFont((float)14.0));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {System.exit(0);}
		});
		
		mnFileMenu.add(mntmExit);
		
		JMenu mnAnalysisMenu = new JMenu("Analysis");
		mnAnalysisMenu.setBackground(Color.WHITE);
		mnAnalysisMenu.setFont(fontArial.deriveFont((float)14.0));
		menuBar.add(mnAnalysisMenu);
		
		JMenuItem mntmKaKs = new JMenuItem("Calculate Ka/Ks");
		mntmKaKs.setBackground(Color.WHITE);
		mntmKaKs.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-sigma-16.png")));
		mntmKaKs.setFont(fontArial.deriveFont((float)14.0));
		mntmKaKs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// if ng.data is empty, we should remind user to load data first.
				if(ng.isInited()==false) {JOptionPane.showMessageDialog(null,"Please Load Data First!");}
				else {
					tabbedPane.setSelectedComponent(panelRes);
					runAnalysis();
				}
			}
		});
		mnAnalysisMenu.add(mntmKaKs);
		
		JMenuItem mntmCodonTab = new JMenuItem("Change codon table");
		mntmCodonTab.setBackground(Color.WHITE);
		mntmCodonTab.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-settings-16.png")));
		mntmCodonTab.setFont(fontArial.deriveFont((float)14.0));
		mntmCodonTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(".");
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				gentabFileFilter gentabFilter = new gentabFileFilter(); //fasta file filter   
				fc.addChoosableFileFilter(gentabFilter);  
				fc.setFileFilter(gentabFilter);  
				fc.showOpenDialog(null);
				String fpath = fc.getSelectedFile().getPath();
				try {
					ng.initGentab();
					ng.loadGenTable(fpath);
				}catch (IOException e1) {
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
				runAnalysis();
			}
		});
		mnAnalysisMenu.add(mntmCodonTab);
		
		JMenuItem mntmIVratio = new JMenuItem("Set i/v ratio");
		mntmIVratio.setBackground(Color.WHITE);
		mntmIVratio.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-settings-16.png")));
		mntmIVratio.setFont(fontArial.deriveFont((float)14.0));
		mntmIVratio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double old_value = ng.getRatio();
				double new_value = old_value;
				String new_value_string = JOptionPane.showInputDialog(null,"Input new transition/transversion ratio:",old_value);
				try {
					new_value = Double.parseDouble(new_value_string);
					if(new_value<0) {
						JOptionPane.showMessageDialog(null,"Warning: please input a positive number.","Warning",JOptionPane.WARNING_MESSAGE);
					}else {
						ng.setRatio(new_value);
						runAnalysis();
					}
				}catch(Exception e1){
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Warning: please input a float number.",JOptionPane.WARNING_MESSAGE);
				}			
			}
		});
		mnAnalysisMenu.add(mntmIVratio);
		
		JMenu mnHelpMenu = new JMenu("Help");
		mnHelpMenu.setBackground(Color.WHITE);
		mnHelpMenu.setFont(fontArial.deriveFont((float)14.0));
		menuBar.add(mnHelpMenu);
		
		JMenuItem mntmHelp = new JMenuItem("User guide");
		mntmHelp.setBackground(Color.WHITE);
		mntmHelp.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-user-manual-16.png")));
		mntmHelp.setFont(fontArial.deriveFont((float)14.0));
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String help = "";
				help += "Usage:\n\n";
				help += "1. Click 'File -> Open' and open a fasta file.\n";
				help += "2. Click 'Analysis -> Calculate Ka/Ks', the program will calculate Ka, Ks and Ka/Ks for sequences in the file.\n";
				help += "It may take a lot of time, so you can have a cup of coffee, Sit back and relax :)\n";
				help += "3. Then you can see the result in 'Result' tab. You can choose which type of result to display: Ka, Ks or Ka/Ks.\n";
				help += "4. Click 'File -> Export' to save data tables.\n";
				help += "5. Click 'Analysis -> Change codon table' to use other non-standard codon table.\n";
				help += "6. Click 'Analysis -> Set i/v ratio' to change transition/transversion ratio(default=0.5).\n\n\n";
				help += "About algorithm:\n\n";
				help += "What is Ka/Ks? The ratio of the number of nonsynonymous substitutions per nonsynonymous site (Ka) to the number of synonymous substitutions per synonymous site (Ks).";
				help += " And it can diagnose the form of sequence evolution.\n\n";
				help += "Generally speaking, if Ka is much greater than Ks(i.e. Ka/Ks >> 1), this is strong evidence that selection has acted to change the protein (positive selection).\n";
				help += "Similarly, if Ka is much smaller than Ks(i.e. Ka/Ks << 1), that is, most of the time selection eliminates deleterious mutations, keeping the protein as it is (purifying selection).\n";
				help += "However, if Ka equals Ks, the evolution of the sequence may be neutral, may be not, becuase there is anaother situation that one part of the gene (one protein domain, say) was under positive selection, but other parts under purifying selection.";
				help += " So we need to use other method to verify the evolution.\n\n\n";
				tabbedPane.setIconAt(2,new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-user-manual-30.png")));
				tabbedPane.setTitleAt(2, "User guide");
				tabbedPane.setEnabledAt(2, true);
				textPane.setText(help);
				textPane.setCaretPosition(0);
				tabbedPane.setSelectedComponent(scrollPaneText);
			}
		});
		mnHelpMenu.add(mntmHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setBackground(Color.WHITE);
		mntmAbout.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-info-16.png")));
		mntmAbout.setFont(fontArial.deriveFont((float)14.0));
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String about = "";
				about += "NG java 2.1\n";
				about += "A single Ka/Ks caculator\n\n";
				about += "Author:  Wanyu Zhang\n";
				about += "Contact: zhangwanyu2000@outlook.com\n";
				about += "Reference: \n";
				about += "1. Hurst, Laurence D. Trends in genetics : TIG vol. 18,9 (2002): 486.\n";
				about += "2. Nei M, Gojobori T. Mol Biol Evol. 1986 Sep;3(5):418-26.\n\n";
				about += "Icons used in this program are obtained from icons8.com under USE-FOR-FREE license.";
				tabbedPane.setIconAt(2,new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-info-30.png")));
				tabbedPane.setTitleAt(2, "About");
				tabbedPane.setEnabledAt(2, true);
				textPane.setText(about);
				tabbedPane.setSelectedComponent(scrollPaneText);
			}
		});
		mnHelpMenu.add(mntmAbout);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setFont(fontNotosans.deriveFont((float)14.0));
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		scrollPaneSequence = new JScrollPane();
		tabbedPane.addTab("Sequence", new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-biotech-30.png")), scrollPaneSequence, "DNA sequences");
		tabbedPane.setBackgroundAt(0, Color.WHITE);
		
		JSplitPane splitPaneSequence = new JSplitPane();
		splitPaneSequence.setContinuousLayout(true);
		splitPaneSequence.setDividerSize(1);
		scrollPaneSequence.setViewportView(splitPaneSequence);
		
		tableSequenceId = new JTable();
		tableSequenceId.setShowHorizontalLines(false);
		tableSequenceId.setFont(fontNotomono.deriveFont((float)15.0));
		splitPaneSequence.setLeftComponent(tableSequenceId);
		
		tableSequence = new JTable();
		tableSequence.setShowHorizontalLines(false);
		tableSequence.setFont(fontNotomono.deriveFont((float)15.0));
		splitPaneSequence.setRightComponent(tableSequence);
		
		panelRes = new JPanel();
		tabbedPane.addTab("Result", new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-data-sheet-30.png")), panelRes, "Calculating result");
		tabbedPane.setBackgroundAt(1, Color.WHITE);
		panelRes.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBarRes = new JToolBar();
		toolBarRes.setBackground(Color.WHITE);
		panelRes.add(toolBarRes, BorderLayout.NORTH);
		
		JLabel LabelResSelect = new JLabel("Display:");
		LabelResSelect.setBackground(Color.WHITE);
		LabelResSelect.setFont(fontArial.deriveFont((float)14.0));
		toolBarRes.add(LabelResSelect);
		
		rdbtnKaKs = new JRadioButton("Ka/Ks");
		rdbtnKaKs.setToolTipText("Display Ka/Ks result in the following table.");
		rdbtnKaKs.setBackground(Color.WHITE);
		rdbtnKaKs.setFont(fontArial.deriveFont((float)14.0));
		rdbtnKaKs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {refresh();} catch (Exception e1) {
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		rdbtnKaKs.setSelected(true);
		toolBarRes.add(rdbtnKaKs);
		
		rdbtnKa = new JRadioButton("Ka");
		rdbtnKa.setToolTipText("Display Ka values in the following table.");
		rdbtnKa.setBackground(Color.WHITE);
		rdbtnKa.setFont(fontArial.deriveFont((float)14.0));
		rdbtnKa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				try {refresh();} catch (Exception e1) {
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBarRes.add(rdbtnKa);
		
		rdbtnKs = new JRadioButton("Ks");
		rdbtnKs.setToolTipText("Display Ks values in the following table.");
		rdbtnKs.setBackground(Color.WHITE);
		rdbtnKs.setFont(fontArial.deriveFont((float)14.0));
		rdbtnKs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {refresh();} catch (Exception e1) {
					JOptionPane.showMessageDialog(null,e1.getMessage(),"Fatal Error!",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		toolBarRes.add(rdbtnKs);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnKaKs);
		group.add(rdbtnKa);
		group.add(rdbtnKs);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setFont(fontArial.deriveFont((float)14.0));
		toolBarRes.add(progressBar);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		panelRes.add(scrollPane, BorderLayout.CENTER);
		
		tableRes = new JTable();
		tableRes.setFont(fontNotomono.deriveFont((float)15.0));
		scrollPane.setViewportView(tableRes);
		
		scrollPaneText = new JScrollPane();
		tabbedPane.addTab("", null, scrollPaneText, null);
		tabbedPane.setEnabledAt(2, false);
		tabbedPane.setBackgroundAt(2, Color.WHITE);
		
		JButton btnCloseTab = new JButton("Close Tab");
		btnCloseTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setIconAt(2,null);
				tabbedPane.setTitleAt(2, "");
				tabbedPane.setEnabledAt(2, false);
				textPane.setText("");
				tabbedPane.setSelectedComponent(panelRes);
			}
		});
		btnCloseTab.setBackground(SystemColor.control);
		btnCloseTab.setIcon(new ImageIcon(Demo.class.getResource("/javaGUI/icons/icons8-cancel-16.png")));
		btnCloseTab.setFont(fontArial.deriveFont((float)14.0));
		scrollPaneText.setColumnHeaderView(btnCloseTab);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setFont(fontNotosans.deriveFont((float)15.0));
		scrollPaneText.setViewportView(textPane);
	      
	}

}









