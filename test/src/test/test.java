package test;

import java.util.ArrayList;

public class test
{
	private static void insertElement(ArrayList<Integer> sList)
	{
		sList.add(1);
	}

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		ArrayList<Integer> aList = new ArrayList<Integer>();
		aList.add(0);
		
		for (int i = 0; i < aList.size(); i++)
			System.out.println(aList.get(i));

		
		insertElement(aList);
		for (int i = 0; i < aList.size(); i++)
			System.out.println(aList.get(i));
		
		
	}

}
