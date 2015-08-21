package ktuan;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class ChooseFolder  extends JFrame {
	
	private static JButton go;
	private JFileChooser chooser;
	private String choosertitle= new String("Choose data file list SARP");
	private File folderSelected;
	public ChooseFolder(){
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle(choosertitle);
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
		// disable the "All files" option.
		//
		chooser.setAcceptAllFileFilterUsed(true);
		//
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			System.out.println("getCurrentDirectory(): "
					+ chooser.getCurrentDirectory());
			System.out.println("getSelectedFile() : "
					+ chooser.getSelectedFile());
			File fileChooser=chooser.getSelectedFile();
			File[] listFile = fileChooser.listFiles();
			if (listFile!=null){
				System.out.println("List file and forder in folder");
				for (int cs=0;cs<listFile.length;cs++){
					System.out.println(listFile[cs].toString());
				}
			}
			folderSelected= chooser.getSelectedFile();
			//
		} else {
			folderSelected=null;
			System.out.println("No Selection ");
		}
		this.dispose();
	}
	
	public ChooseFolder(File file) {
		this.folderSelected=file;
	}

	public Dimension getPreferredSize(){
		return new  Dimension(200, 200);
	}

	public SetSARP_File getSetSARP_File() {
		if (folderSelected== null) return null;
		SetSARP_File res= new SetSARP_File();
		res._fileDepotsAndParkings= folderSelected.getAbsolutePath().concat("\\depot2.txt");
		res._fileGraph= folderSelected.getAbsolutePath().concat("\\reduceGraph.txt");
		res._fileRequests= folderSelected.getAbsolutePath().concat("\\requestsPeopleParcel.txt");
		res._fileParameters= folderSelected.getParent().concat("\\config-parameters.txt");
		res._fileGenOut= folderSelected.getAbsolutePath().concat("\\out.txt");
		res._fileReport= folderSelected.getAbsolutePath().concat("\\report.txt");
		return res;
	}	
}
