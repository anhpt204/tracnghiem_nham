package ktuan;

public class WWW_SetSARP_File {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChooseFolder frame = new ChooseFolder();
		SetSARP_File setFile= frame.getSetSARP_File();
		System.out.println(setFile.toString());
		setFile.rewriteAddCapacityParkingsFormFileDepotsAndParkings();
	}

}
