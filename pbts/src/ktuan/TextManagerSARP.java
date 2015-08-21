package ktuan;

public class TextManagerSARP {

	public static String parserSpace(String line) {
		char[] ch= line.toCharArray();
		int n=0;
		for (int i=1;i<ch.length;i++){
			if (ch[i]==' ' && ch[n]=='\t') continue;
			n++;
			ch[n]=ch[i];
			if (ch[n]==' ') ch[n]='\t';
		}
		n++;
		return new String(ch, 0, n);
	}

}
