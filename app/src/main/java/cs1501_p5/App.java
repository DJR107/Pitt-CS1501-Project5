/**
 * A driver for CS1501 Project 5
 * @author	Dr. Farnan
 */
package cs1501_p5;

import java.math.BigInteger;

public class App 
{
	public static void main(String[] args) 
	{
		BigInteger biA = new BigInteger("-183483599872");
		BigInteger biB = new BigInteger("584969879856");

		HeftyInteger hiA = new HeftyInteger(biA.toByteArray());
		HeftyInteger hiB = new HeftyInteger(biB.toByteArray());

		hiA.print();
		hiB.print();

		BigInteger biRes = biA.multiply(biB);
		HeftyInteger hiRes = hiA.multiply(hiB);

		byte[] arr = biRes.toByteArray();
		System.out.print("BigInteger: ");
		for (int i=0; i<arr.length; i++)
	 	{
	 		System.out.print(arr[i]+" ");
	 	}
	 	System.out.println();

	 	hiRes.print();

		if (biRes.compareTo(new BigInteger(hiRes.getVal())) == 0)
			System.out.println("YESSSSSSS");
		else
			System.out.println("Fuck");
	}
}