package ktuan;

public class WWW_KSimulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long timeStart= System.currentTimeMillis();
		ChooseFolder frame = new ChooseFolder();
		SetSARP_File setFile= frame.getSetSARP_File();
		System.out.println(setFile.toString());
		Herictic herictic= new Herictic(setFile);
		KSimualator sim= new KSimualator();
		sim.solve(setFile);
		long timeFinish= System.currentTimeMillis();
	}

}
