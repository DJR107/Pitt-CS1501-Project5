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
		BigInteger biA = new BigInteger("28752398052908590823590823590827390587209853723");
		BigInteger biB = new BigInteger("982739084652073650274690216490823590847259872985728753075390287352908375");

		HeftyInteger hiA = new HeftyInteger(biA.toByteArray());
		HeftyInteger hiB = new HeftyInteger(biB.toByteArray());

		hiA.print();
		hiB.print();

		HeftyInteger[] hiRes = hiA.XGCD(hiB);
		BigInteger x = new BigInteger(hiRes[1].getVal());
		BigInteger y = new BigInteger(hiRes[2].getVal());

		BigInteger biGCD = biA.gcd(biB);

		BigInteger biCheck = biA.multiply(x).add(biB.multiply(y));

		byte[] arr = biGCD.toByteArray();
		System.out.print("BigInteger: ");
		for (int i=0; i<arr.length; i++)
	 	{
	 		System.out.print(arr[i]+" ");
	 	}
	 	System.out.println();

	 	System.out.print("Mod -> ");
	 	hiRes[0].print();
	 	System.out.print("X -> ");
		hiRes[1].print();
		System.out.print("Y -> ");
		hiRes[2].print();

		if (biGCD.compareTo(new BigInteger(hiRes[0].getVal())) == 0 && biGCD.compareTo(biCheck) == 0)
			System.out.println("YESSSSSSS");
		else
			System.out.println("Fuck");
	}
}