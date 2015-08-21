package ktuan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class SetSARP_File {
	public String _fileDepotsAndParkings;
	public String _fileGraph;
	public String _fileRequests;
	public String _fileParameters;
	public String _fileGenOut;
	public String _fileReport;
	@Override
	public String toString() {
		return String.format("SetSARP_File[\n" +
				 "File Depots And Parking = %s\n"
				+"File Graph              = %s\n"
				+"File Requests           = %s\n"
				+"File Parameters         = %s\n"
				+"File GenOut             = %s\n"
				+"File Report             = %s\n"
				+"]", _fileDepotsAndParkings,_fileGraph,_fileRequests,_fileParameters,_fileGenOut,_fileReport);
		/*
		return "SetSARP_File [_fileDepotsAndParkings=" + _fileDepotsAndParkings
				+ ", _fileGraph=" + _fileGraph + ", _fileRequests="
				+ _fileRequests + ", _fileParameters=" + _fileParameters + "]";
		*/		
	}
	public void rewriteAddCapacityParkingsFormFileDepotsAndParkings() {
		System.out.println("Start: rewriteAddCapacityParkingsFormFileDepotsAndParkings");
		File fileIn= new  File(_fileDepotsAndParkings);
		File fileOut=new File(fileIn.getParent().concat("//DepotsAndParkings.txt"));
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					fileIn.getAbsolutePath()));
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileOut.getAbsolutePath()), "utf-8"));
			for (int cs = 1; cs <= 2; cs++) {
				String line = reader.readLine().trim();
				//System.out.println(line);
				writer.write(line + "\n");
				if (line.startsWith("Depots") || line.startsWith("depots")) {
					line = reader.readLine().trim();
					writer.write(line + " -1\n");
					reader.readLine().trim();
				} else if (line.startsWith("Parkings")
						|| line.startsWith("parkings")) {
					line= reader.readLine().trim();
					line = TextManagerSARP.parserSpace(line);
					String[] s = line.split("\t");
					for (int i = 0; i < s.length; i++) {
						int depotId = Integer.parseInt(s[i]);
						writer.write(new String(depotId+"\t10000000\n"));
						if (depotId == -1) {
							break;
						}
					}
					writer.write("-1\n");
				}
			}
			reader.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finish: rewriteAddCapacityParkingsFormFileDepotsAndParkings");
	}
	
}
