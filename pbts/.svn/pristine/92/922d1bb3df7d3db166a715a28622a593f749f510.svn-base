package ktuan;

public class WWW_Herictic {
	public static void main(String[] args){
		//version1();
		//version2();
		version3();
	}

	private static void version3() {
		long timeStart= System.currentTimeMillis();
		ChooseDirectory chooseDicDirectory = new ChooseDirectory();
		for(int cs=0;cs<chooseDicDirectory.listForder.length;cs++){
			if (!chooseDicDirectory.listForder[cs].isDirectory()) continue;
			ChooseFolder frame = new ChooseFolder(chooseDicDirectory.listForder[cs]);
			SetSARP_File setFile= frame.getSetSARP_File();
			System.out.println(setFile.toString());
			Herictic herictic= new Herictic(setFile);
			KSimualator sim= new KSimualator();
			sim.solve(setFile);
			long timeFinish= System.currentTimeMillis();
			System.out.println(String.format("Time Process All = %6d <milisecond>", (int)(timeFinish-timeStart)));
		}
	}

	private static void version2() {
		long timeStart= System.currentTimeMillis();
		//ChooseDirectory chooseDicDirectory = new ChooseDirectory();
		ChooseFolder frame = new ChooseFolder();
		SetSARP_File setFile= frame.getSetSARP_File();
		System.out.println(setFile.toString());
		Herictic herictic= new Herictic(setFile);
		long timeFinish= System.currentTimeMillis();
	}

	private static void version1() {
		long timeStart= System.currentTimeMillis();
		Herictic herictic= new Herictic();
		long timeFinish= System.currentTimeMillis();
		System.out.println(String.format("Time Process All = %6d <milisecond>", (int)(timeFinish-timeStart)));
	}
}
