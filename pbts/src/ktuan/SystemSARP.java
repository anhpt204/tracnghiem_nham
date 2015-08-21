package ktuan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SystemSARP {
	public int _maxSpeedkmh;
	public int _minSpeedkmh;
	public int _startWorkingTime;
	public int _terminateRequestTime;
	public int _terminateWorkingTime;
	public int _maxWaitTime;
	public int _Qk;
	public int _alpha;
	public int _beta;
	public int _gamma1;
	public int _gamma2;
	public int _gamma3;
	public double _gamma4;
	public int _maxDeliveryDistanceFactor;
	public double _maxSpeedms;
	public double _minSpeedms;
	@Override
	public String toString() {
		return "SystemSARP [_maxSpeedkmh=" + _maxSpeedkmh + ", _minSpeedkmh="
				+ _minSpeedkmh + ", _startWorkingTime=" + _startWorkingTime
				+ ", _terminateRequestTime=" + _terminateRequestTime
				+ ", _terminateWorkingTime=" + _terminateWorkingTime
				+ ", _maxWaitTime=" + _maxWaitTime + ", _Qk=" + _Qk
				+ ", _alpha=" + _alpha + ", _beta=" + _beta + ", _gamma1="
				+ _gamma1 + ", _gamma2=" + _gamma2 + ", _gamma3=" + _gamma3
				+ ", _gamma4=" + _gamma4 + ", _maxDeliveryDistanceFactor="
				+ _maxDeliveryDistanceFactor + ", _maxSpeedms=" + _maxSpeedms
				+ ", _minSpeedms=" + _minSpeedms + "]";
	}

	public void readConfix_Parameters(String path){
		System.out.println("Start: Read confix parameters");
		File _dataFile= new File(path);
		try {
			BufferedReader br= new BufferedReader(new FileReader(_dataFile.getAbsolutePath()));
			String line = br.readLine().trim();
			while (line!=null){
				if (line.startsWith("#maxSpeedkmh =")){
					_maxSpeedkmh= Integer.parseInt(br.readLine().trim());
					_maxSpeedms= (_maxSpeedkmh*10.0)/36;
				}else 
				if (line.startsWith("#minSpeedkmh")){
					_minSpeedkmh= Integer.parseInt(br.readLine().trim());
					_minSpeedms= (_minSpeedkmh*10.0)/36;
				}else 
				if (line.startsWith("#startWorkingTime")){
					_startWorkingTime= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#terminateRequestTime")){
					_terminateRequestTime= Integer.parseInt(br.readLine().trim());
				}else
				if (line.startsWith("#terminateWorkingTime")){
					_terminateWorkingTime= Integer.parseInt(br.readLine().trim());
				}else
				if (line.startsWith("#maxWaitTime")){
					_maxWaitTime= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#Qk")){
					_Qk= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#alpha")){
					_alpha= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#beta")){
					_beta= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#gamma1")){
					_gamma1= Integer.parseInt(br.readLine().trim());
				}else
				if (line.startsWith("#gamma2")){
					_gamma2= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#gamma3")){
					_gamma3= Integer.parseInt(br.readLine().trim());
				}else 
				if (line.startsWith("#gamma4")){
					_gamma4= Double.parseDouble(br.readLine().trim());
				}else 
				if (line.startsWith("#maxDeliveryDistanceFactor")){
					_maxDeliveryDistanceFactor= Integer.parseInt(br.readLine().trim());
					break;
				}
				line = br.readLine().trim();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finish: Read confix parameters");
	}
	
}
